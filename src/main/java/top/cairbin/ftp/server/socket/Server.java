/*
 * @Description: FTP服务器
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:55:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 07:53:17
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import java.util.Map;
import java.util.UUID;

import top.cairbin.ftp.server.InjectorFactory;
import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.handler.IHandler;
import top.cairbin.ftp.server.thread.IThreadPool;
import top.cairbin.ftp.server.utils.FtpCommand;
import top.cairbin.ftp.server.utils.FtpCommandParser;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;
import top.cairbin.ftp.server.utils.PathValidator;


@Singleton
public class Server {

    @Inject
    private ILogger logger;    
    
    @Inject 
    private IThreadPool threads;

    private ServerSocket listener;
    private PathValidator root;
    private Mutex<HashMap<String, Mutex<User>>> users;
    private String pasvIp;
    private ServerSocket pasvListener;
    private int pasvPort;

    public Server() {
        this.users = new Mutex<>(new HashMap<>());
    }

    public ServerSocket getPasvListener() {
        return this.pasvListener;
    }

    public void setPasvListener(ServerSocket listener) {
        this.pasvListener = listener;
    }

    /** 
     * @description: 设置FTP工作的根目录
     * @param {String} root
     */    
    public void setRoot(String root) throws Exception {
        File dir = new File(root);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory: " + root);
        }
        this.root = new PathValidator(root);
        logger.warn("[Server] Set working directory: {}", this.root.getRealPath());
    }

    /** 
     * @description: 获取线程池对象
     * @return {IThreadPool}
     */    
    public IThreadPool getThreadPool() {
        return this.threads;
    }

    /** 
     * @description: 获取TCP Listener
     * @return {ServerSocket}
     */    
    public ServerSocket getSocket() {
        return this.listener;
    }

    /** 
     * @description: 获取根目录
     * @return {PathValidator}
     */    
    public PathValidator getRoot() {
        return this.root;
    }

    /** 
     * @description: 设置被动模式的IP
     * @param {String} ip
     */    
    public void setPasvIp(String ip) {
        this.pasvIp = ip;
    }

    /** 
     * @description: 设置被动模式的端口
     * @param {int} port
     */    
    public void setPasvPort(int port) {
        this.pasvPort = port;
    }

    /** 
     * @description: 获取被动模式的端口
     * @return {int}
     */    
    public int getPasvPort() {
        return this.pasvPort;
    }

    /** 
     * @description: 获取被动模式的IP
     * @return {String}
     */    
    public String getPasvIp() {
        return this.pasvIp;
    }

    /** 
     * @description: 初始化服务器Socket
     * @param {int} port
     */    
    public void createSocket(int port) throws IOException {
        this.listener = new ServerSocket(port);
        logger.warn("[Server] Server is listening on port {}", port);
        logger.warn("[Server] Server address: {}", this.listener.getLocalSocketAddress());
    }

    /** 
     * @description: 开始监听
     */    
    public void listener() throws Exception {
        validateServerState();

        while (true) {
            Socket socket = this.listener.accept();
            logger.info("[Server] Connected from {}", socket.getRemoteSocketAddress());
            threads.submit(() -> handle(socket));
        }
    }

    /** 
     * @description: 多线程处理Socket连接请求
     * @param {Socket} socket
     */    
    public void handle(Socket socket) {
        SocketHelper helper = new SocketHelper(socket);
        handleLogin(helper);

        try {
            var writerMutex = new Mutex<BufferedWriter>(helper.getWriter("UTF-8"));
            var reader = helper.getReader("UTF-8");

            processClientMessages(helper, writerMutex, reader);
        } catch (Exception e) {
            logger.error("[Server] Error: {}", e.getMessage(), e);
        }
    }

    private void validateServerState() throws Exception {
        if (this.root == null) throw new Exception("Working directory is null!");
        if (this.listener == null) throw new Exception("TCP listener is null!");
        if (this.pasvIp == null) throw new Exception("Passive IP is null!");
    }

    private void processClientMessages(SocketHelper helper, Mutex<BufferedWriter> writerMutex, BufferedReader reader) throws IOException {
        while (true) {
            String buf = reader.readLine();
            if (buf == null || buf.isEmpty()) {
                logger.info("[Server] Connection closed: {}", helper.getRemoteAddress());
                removeUser(helper);
                return;
            }

            String message = buf.trim();
            logger.debug("[Server] Received message from {}: {}", helper.getRemoteAddress(), message);
            FtpParams params = FtpCommandParser.parse(message);
            logger.debug("[Server] Parsed message: Command: {}, args: {}", params.getCmd().name(), params.getArgs() == null ? "null" : params.getArgs());

            var userMtx = getUserMutex(helper);

            if (params.cmd == FtpCommand.QUIT) {
                logger.debug("[Server] QUIT message received");
                removeUser(helper);
            }

            threads.submit(() -> {
                try {
                    dispatch(writerMutex, params, userMtx);
                } catch (Exception e) {
                    logger.error("[Server] Dispatcher error: {}", e.getMessage());
                    sendErrorResponse(writerMutex, "Error occurred: " + e.getMessage());
                }
            });
        }
    }

    private void removeUser(SocketHelper helper) {
        users.lock();
        try {
            users.getData().remove(helper.getRemoteAddress());
        } finally {
            users.unlock();
        }
    }

    private Mutex<User> getUserMutex(SocketHelper helper) {
        return users.lockAndGet(mapper -> mapper.get(helper.getRemoteAddress()));
    }

    private void handleLogin(SocketHelper helper) {
        users.lock();
        try {
            String address = helper.getRemoteAddress();
            if (users.getData().containsKey(address)) return;

            var writer = helper.getWriter("UTF-8");
            String readyMsg = FtpMessage.fmtMessage(220, "FTP server ready.");
            writer.write(readyMsg);
            writer.flush();
            logger.debug("[Server] Sent ready message: {}", readyMsg);
            users.getData().put(address, new Mutex<>(new User(address, this.root.getRealPath())));
        } catch (Exception error) {
            logger.error("[Server] Login handler error: {}", error.getMessage());
        } finally {
            users.unlock();
        }
    }

    private void dispatch(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        Map<FtpCommand, IHandler> handlers = InjectorFactory.getInjector()
                .getInstance(Key.get(new TypeLiteral<Map<FtpCommand, IHandler>>() {}));
        IHandler handler = handlers.get(params.getCmd());

        if (params.cmd == FtpCommand.STOU) {
            String args = (String) params.getArgs();
            String uuid = UUID.fromString(args).toString();
            params.setArgs(uuid);
            logger.debug("[Server] Dispatcher: Triggering STOU with uuid = {}", uuid);
        }
        handler.handle(writerMtx, params, userMtx);
    }

    private void sendErrorResponse(Mutex<BufferedWriter> writerMtx, String errorMessage) {
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(550, errorMessage));
                writer.flush();
            } catch (Exception err) {
                logger.error("[Server] Writing message error: {}", err.getMessage());
            }
        });
    }
}