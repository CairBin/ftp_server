/*
 * @Description: FTP服务器
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:55:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 14:50:15
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    private ServerSocket pasvListener;

    private final ServerConfig serverConfig;

    private final Mutex<HashMap<String, Object>> data; // 存放自定义数据

    /**
     * @description: 此构造函数用于给依赖注入容器使用
     * @return {*}
     */    
    public Server(ServerConfig serverConfig) throws IOException {
        this.users  = new Mutex<>(new HashMap<>());
        this.data   =  new Mutex<>(new HashMap<>());
        this.root = new PathValidator(serverConfig.rootDirectory);
        this.serverConfig = serverConfig;
    }

    /**
     * @description: 普通构造函数
     */    
    public Server(ServerConfig serverConfig, ILogger logger, IThreadPool threads) throws IOException{
        this(serverConfig);
        this.logger = logger;
        this.threads = threads;
        this.root = new PathValidator(serverConfig.rootDirectory);
    }

    /** 
     * @description: 获取被动模式的Listener
     */
    public ServerSocket getPasvListener() {
        return this.pasvListener;
    }

    /**
     * @description: 设置被动模式的监听器
     * @param {ServerSocket} serverSocket
     * @return {*}
     */    
    public void setPasvListener(ServerSocket serverSocket){
        this.pasvListener = serverSocket;
    }

    /**
     * @description: 处理被动模式下会话设置
     * @param {Mutex<User>} userMtx
     * @return {*}
     */    
    public void pasvAcceptSession(Mutex<User> userMtx){
        this.threads.submit(()->{
            userMtx.lock();
            try {
                var socket = this.pasvListener.accept();
                logger.debug("[Server-PASV]Accepting pasv connection");
                userMtx.getData().setSession(new Mutex<Session>(new Session(Mode.PASV, socket)));
            }catch(Exception e) {
                logger.error("[Server-PASV]Accept error: {}", e);
                logger.error("[Server-PASV]Accept error message: {}", e);
            }finally{
                userMtx.unlock();
            }
        });
    }

    /**
     * @description: 获取自定义数据的锁对象
     * @return {*}
     */    
    public Mutex<HashMap<String, Object>> getDataMutex(){
        return this.data;
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
     * @description: 获取根目录的PathValidator
     * @return {PathValidator}
     */    
    public PathValidator getRoot() {
        return this.root;
    }

    /** 
     * @description: 获取被动模式的端口
     * @return {int}
     */    
    public int getPasvPort() {
        return this.serverConfig.pasvPort;
    }

    /** 
     * @description: 获取被动模式的IP
     * @return {String}
     */    
    public String getPasvIp() {
        return this.serverConfig.pasvAddress;
    }

    /** 
     * @description: 获取用户信息
     * @return {FtpCommandParser}
     */
    public final UserConfig getUserConfig(String username){
        return this.serverConfig.userConfigs.get(username);
    }

    /**
     * @description: 是否允许匿名登陆
     * @return {boolean}
     */    
    public boolean isAllowAnonymous(){
        return this.serverConfig.allowAnonymous;
    }

    /**
     * @description: 是否存在此用户配置
     * @return {boolean}
     */    
    public boolean hasUserConfig(String username){
        return this.serverConfig.userConfigs.containsKey(username);
    }

    /** 
     * @description: 初始化服务器Socket
     * @param {int} port
     */    
    public void initSocket() throws IOException {
        int port = this.serverConfig.port;
        this.listener = new ServerSocket(port);
        logger.warn("[Server] Server is listening on port {}", port);
        logger.warn("[Server] Server address: {}", this.listener.getLocalSocketAddress());
    }


    /** 
     * @description: 开始监听
     */    
    public void listener() throws Exception {
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
        handleHello(helper);

        try {
            var writerMutex = new Mutex<BufferedWriter>(helper.getWriter("UTF-8"));
            var reader = helper.getReader("UTF-8");

            processClientMessages(helper, writerMutex, reader);
        } catch (Exception e) {
            logger.error("[Server] Error: {}", e.getMessage(), e);
        }
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

            if(params.cmd != FtpCommand.USER && params.cmd != FtpCommand.PASS){
                userMtx.lock();
                boolean flag = userMtx.getData().state == UserState.LOGGING;
                userMtx.unlock();
                if(flag){
                    logger.warn("[Server] Illegal handling");
                    sendIllegalResponse(writerMutex, "Not logged in");
                    helper.close();
                    removeUser(helper);
                    return;
                }
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

    private void handleHello(SocketHelper helper) {
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

    private void sendIllegalResponse(Mutex<BufferedWriter> writerMtx, String message){
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(530, message));
                writer.flush();
            } catch (Exception err) {
                logger.error("[Server] Writing message error: {}", err.getMessage());
            }
        });
    }
}