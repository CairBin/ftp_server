/*
 * @Description: 处理NLIST
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 18:42:25
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 07:28:40
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.SocketHelper;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;
import top.cairbin.ftp.server.utils.ListLine;

public class NlistHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[NlistHandler] Dispatching NLST (nlist) handler...");
        
        Path path;
        try {
            // 获取路径，参数为空则获取用户当前工作目录
            path = userMtx.lockAndGet(user -> {
                String realPath = server.getRoot().getRealPath();
                String userPwd = user.pwd();
                String args = (String) params.getArgs();
                return args == null ? Paths.get(realPath, userPwd) : Paths.get(realPath, userPwd, args);
            });

            // 路径不存在，返回550错误
            if (!Files.exists(path)) {
                sendErrorResponse(writerMtx, 550, "No such file.");
                return;
            }

            // 路径合法性检查
            if (!server.getRoot().isValid(path.toString())) {
                logger.warn("[NlistHandler] Attempt to access illegal path: " + path);
                sendErrorResponse(writerMtx, 550, "Permission denied.");
                return;
            }

            logger.debug("[NlistHandler] Accessing path: " + path);

            // 获取文件列表
            String list = ListLine.getListLines(path, true);

            // 通知客户端数据连接已建立
            boolean responseSent = sendResponse(writerMtx, 150, "Opening ASCII mode data connection for file list");
            if (!responseSent) throw new IOException("Failed to send data connection message.");

            // 通过数据连接发送文件列表
            boolean transferSuccess = transferList(userMtx, list);
            if (!transferSuccess) throw new IOException("Failed to transfer file list.");

            // 传输完成通知
            sendResponse(writerMtx, 226, "Transfer complete.");

        } catch (Exception e) {
            logger.error("[NlistHandler] Error handling NLST: " + e);
            throw e;
        }
    }

    // 发送错误消息
    private void sendErrorResponse(Mutex<BufferedWriter> writerMtx, int code, String message) throws IOException {
        writerMtx.lockAndSet(writer -> {
            try{
                writer.write(FtpMessage.fmtMessage(code, message));
                writer.flush();
            }catch(Exception e){
                logger.error("[NlistHandler] Error sending error message: " + e);
                return;
            }
            logger.debug("[NlistHandler] Sent error message: " + message);
        });
    }

    // 发送正常响应
    private boolean sendResponse(Mutex<BufferedWriter> writerMtx, int code, String message) {
        return writerMtx.lockAndGet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(code, message));
                writer.flush();
                logger.debug("[NlistHandler] Sent response: " + message);
                return true;
            } catch (IOException e) {
                logger.error("[NlistHandler] Error sending response: " + e);
                return false;
            }
        });
    }

    // 处理文件列表的传输
    private boolean transferList(Mutex<User> userMtx, String list) {
        return userMtx.lockAndGet(user -> {
            var sessionMtx = user.getSession();
            return sessionMtx.lockAndGet(session -> {
                try {
                    SocketHelper helper = new SocketHelper(session.getSocket());
                    var writer = helper.getWriter("UTF-8");
                    writer.write(list);
                    writer.flush();
                    session.setFinished(true);
                    writer.close();
                    logger.debug("[NlistHandler] File list transfer completed.");
                    return true;
                } catch (IOException e) {
                    logger.error("[NlistHandler] Error transferring file list: " + e);
                    return false;
                }
            });
        });
    }
}