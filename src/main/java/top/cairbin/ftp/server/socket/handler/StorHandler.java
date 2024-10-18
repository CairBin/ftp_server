/*
 * @Description: 处理STOR和STOU
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 17:45:13
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 07:14:16
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.inject.Inject;

import top.cairbin.ftp.server.file.IFileManager;
import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.Session;
import top.cairbin.ftp.server.socket.SocketHelper;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class StorHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Inject
    private IFileManager fileManager;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[StorHandler] Dispatching STOR handler");

        userMtx.lock();
        try {
            if (params.args == null) {
                throw new IllegalArgumentException("args is null");
            }

            String filename = (String)params.getArgs();
            if (filename.isEmpty()) {
                throw new IllegalArgumentException("Invalid filename");
            }

            Path path = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), filename);
            var sessionMtx = userMtx.getData().getSession();

            sessionMtx.lock();
            try {
                sessionMtx.getData().setFilename(filename);
            } finally {
                sessionMtx.unlock();
            }

            // 检查文件路径是否合法
            if (!this.server.getRoot().isValid(path.toString())) {
                logger.warn("[StorHandler] Trying to access illegal file: " + filename);
                sendResponse(writerMtx, 550, "Permission denied.");
                return;
            }

            // 判断文件是否存在
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            // 检查是否是目录
            if (Files.isDirectory(path)) {
                sendResponse(writerMtx, 550, "Permission denied. The path is a directory.");
                return;
            }

            long offset = sessionMtx.lockAndGet(session -> session.getOffset());

            if (offset > Files.size(path)) {
                offset = Files.size(path);
            }

            // 开始文件传输
            sendResponse(writerMtx, 150, "Open BINARY mode data connection for " + filename);

            boolean transferCompleted = performFileTransfer(sessionMtx, path, offset);

            if (transferCompleted) {
                sendResponse(writerMtx, 226, "Transfer completed.");
            } else {
                sendResponse(writerMtx, 426, "Connection closed; transfer aborted.");
            }

        } catch (Exception e) {
            logger.error("Error handling STOR command: " + e);
            throw e;
        } finally {
            userMtx.unlock();
        }
    }

    private void sendResponse(Mutex<BufferedWriter> writerMtx, int code, String message) throws IOException {
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(code, message));
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error writing response", e);
            }
        });
    }

    private boolean performFileTransfer(Mutex<Session> sessionMtx, Path path, long offset) {
        while (true) {
            boolean transferComplete = sessionMtx.lockAndGet(session -> {
                try {
                    if (session.aborted) {
                        return true;
                    }

                    SocketHelper helper = new SocketHelper(session.getSocket());
                    char[] buffer = new char[2048];
                    int bytesRead = helper.getReader("UTF-8").read(buffer);

                    if (bytesRead == -1) {
                        return true;
                    }

                    var fileHandlerRw = fileManager.get(path.toString());
                    fileHandlerRw.write(writer -> {
                        try {
                            writer.getWriter().write(buffer, 0, bytesRead);
                            writer.getWriter().flush();
                            session.finishedSize += bytesRead;
                        } catch (IOException e) {
                            logger.error("Failed to write: " + e);
                        }
                    });
                    return false;
                } catch (Exception e) {
                    logger.error("Transfer error: " + e);
                    return true;
                }
            });

            if (transferComplete) {
                break;
            }
        }

        // Close socket after transfer
        sessionMtx.lockAndSet(session -> {
            try {
                session.getSocket().close();
            } catch (IOException e) {
                logger.error("Failed to close socket: " + e);
            }
        });

        return !sessionMtx.lockAndGet(Session::isAborted);
    }
}