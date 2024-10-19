/*
 * @Description: 被动模式
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.1
 * @Date: 2024-10-17 18:50:24
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 14:52:18
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */

package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class PasvHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[PasvHandler] Dispatching PASV handler...");

        try {
            String addr = server.getPasvIp();
            final String ip = addr.split(":")[0].replace(".", ",");
            int port = server.getPasvPort();

            logger.debug("[PasvHandler] Address: " + ip + ", Port: " + port);

            // 初始化 PASV ServerSocket（仅创建一次）
            if (server.getPasvListener() == null) {
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(port));
                server.setPasvListener(socket);
            }

            // 向客户端发送被动模式的地址和端口信息
            sendPasvResponse(writerMtx, ip, port);

            // 提交任务到线程池，等待 PASV 连接
            server.getThreadPool().submit(() -> acceptPasvConnection(userMtx));

        } catch (Exception e) {
            logger.error("[PasvHandler] Error during PASV handling: {}", e);
            throw e;
        }
    }

    /**
     * 发送被动模式响应给客户端
     */
    private void sendPasvResponse(Mutex<BufferedWriter> writerMtx, String ip, int port) throws Exception {
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(227,
                        String.format("Entering Passive Mode (%s,%d,%d)", ip, port / 256, port % 256)));
                writer.flush();
                logger.debug("[PasvHandler] Sent PASV response to client.");
            } catch (Exception e) {
                logger.error("[PasvHandler] Error writing PASV response: {}", e);
            }
        });
    }

    /**
     * 接受被动模式的客户端连接
     */
    private void acceptPasvConnection(Mutex<User> userMtx) {
        try {
            // logger.debug("[PasvHandler] Waiting for PASV connection...");
            // Socket socket = server.getPasvListener().accept(); // 等待连接
            // logger.debug("[PasvHandler] Accepted PASV connection.");

            // // 设置用户的会话
            // userMtx.lockAndSet(user -> {
            // user.setSession(new Mutex<>(new Session(Mode.PASV, socket)));
            // });
            server.pasvAcceptSession(userMtx);

        } catch (Exception e) {
            logger.error("[PasvHandler] Error accepting PASV connection: {}", e);
        }
    }
}