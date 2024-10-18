/*
 * @Description: 处理RNTO
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:45:25
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 07:55:43
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;

import com.google.inject.Inject;

import top.cairbin.ftp.server.file.IFileManager;
import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.Session;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class RntoHandler implements IHandler {
    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Inject
    private IFileManager manager;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[RntoHandler] Dispatching RNTO handler");
        
        userMtx.lock();
        try {
            validateParams(params);
            
            String newFilename = (String) params.getArgs();
            String pwd = userMtx.getData().pwd();
            var sessionMtx = userMtx.getData().getSession();
            String filename = getCurrentFilename(sessionMtx);

            String oldPath = buildPath(pwd, filename);
            String newPath = buildPath(pwd, newFilename);

            // 验证合法性
            if (!isValidPath(oldPath) || !isValidPath(newPath)) {
                sendError(writerMtx, "Permission denied.");
                return;
            }

            if (!manager.move(oldPath, newPath)) {
                throw new Exception("Could not move");
            }

            logger.info("[RntoHandler] Moved successfully to " + newPath);
            sendSuccess(writerMtx, "RNTO ok.");
        } catch (Exception e) {
            logger.error("[RntoHandler] Error: {}", e.getMessage(), e);
            throw e; // Re-throwing the exception for the caller to handle
        } finally {
            userMtx.unlock();
        }
    }

    private void validateParams(FtpParams params) throws Exception {
        if (params.getArgs() == null) {
            throw new Exception("args must not be null");
        }
        String newFilename = (String) params.getArgs();
        if (newFilename.isEmpty()) {
            throw new Exception("Invalid filename");
        }
    }

    private String getCurrentFilename(Mutex<Session> sessionMtx) {
        return sessionMtx.lockAndGet(session -> session.getFilename());
    }

    private String buildPath(String pwd, String filename) {
        return Paths.get(server.getRoot().getRealPath(), pwd, filename).toString();
    }

    private boolean isValidPath(String path) throws IOException {
        return server.getRoot().isValid(path);
    }

    private void sendError(Mutex<BufferedWriter> writerMtx, String message) {
        logger.warn("[RntoHandler] {}", message);
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(550, message));
                writer.flush();
            } catch (Exception e) {
                logger.error("[RntoHandler] Writing error: {}", e.getMessage(), e);
            }
        });
    }

    private void sendSuccess(Mutex<BufferedWriter> writerMtx, String message) {
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(250, message));
                writer.flush();
            } catch (Exception e) {
                logger.error("[RntoHandler] Writing error: {}", e.getMessage(), e);
            }
        });
    }
}