/*
 * @Description: 下载文件
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-18 08:03:34
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-21 02:31:44
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;
import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.Session;
import top.cairbin.ftp.server.socket.SocketHelper;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class RetrHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;


    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[RetrHandler] Dispatching RETR handler");

        userMtx.lock();
        try {
            if (params.args == null) {
                throw new IllegalArgumentException("args is null");
            }

            String filename = (String) params.getArgs();
            if (filename.isEmpty()) {
                throw new IllegalArgumentException("Invalid filename");
            }

            Path path = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), filename);

            if(!this.server.getRoot().isValid(path.toString())){
                sendResponse(writerMtx, 550, "Permission denied.");
                return;
            }
            if (!Files.exists(path)) {
                sendResponse(writerMtx, 550, "File not found.");
                return;
            }
            if (Files.isDirectory(path)) {
                sendResponse(writerMtx, 550, "Permission denied. The path is a directory.");
                return;
            }

        
            // long fileSize = Files.size(path);
            sendResponse(writerMtx, 150, "Opening BINARY mode data connection for " + filename);

            boolean transferCompleted = performFileTransfer(writerMtx, path, userMtx.getData().getSession());

            if (transferCompleted) {
                sendResponse(writerMtx, 226, "Transfer completed.");
            } else {
                sendResponse(writerMtx, 426, "Connection closed; transfer aborted.");
            }

        } catch (Exception e) {
            logger.error("Error handling RETR command: " + e);
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

    private boolean performFileTransfer(Mutex<BufferedWriter> writerMtx, Path path, Mutex<Session> sessionMtx) {
        sessionMtx.lock();
        try (InputStream inputStream = Files.newInputStream(path)){
            var writer = sessionMtx.getData().getSocket().getOutputStream();
            byte[] buffer = new byte[2048];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                writer.write(buffer, 0, bytesRead);
                writer.flush();            
            }
            writer.close();
            return true;

        } catch (IOException e) {
            logger.error("[RetrHandler]Transfer error: " + e);
            logger.error("[RetrHandler]Transfer error message: " + e.getMessage());
            return false;
        }finally{
            sessionMtx.unlock();
        }
    }
}