/*
 * @Description: 处理STOR和STOU
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 17:45:13
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 15:03:19
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        var sessionMtx = userMtx.getData().getSession();
        sessionMtx.lock();
        try {
            if (params.args == null) {
                throw new IllegalArgumentException("args is null");
            }

            String filename = (String) params.getArgs();
            if (filename.isEmpty()) {
                throw new IllegalArgumentException("Invalid filename");
            }

            Path path = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), filename);
            sessionMtx.getData().setFilename(filename);
     

            // 检查文件路径是否合法
            if (!this.server.getRoot().isValid(path.toString())) {
                logger.warn("[StorHandler] Trying to access illegal file: " + filename);
                sendResponse(writerMtx, 550, "Permission denied.");
                return;
            }

            // 判断文件是否存在
            if (!Files.exists(path)) {
                logger.debug("[StorHandler]File does not exist: " + path);
                Files.createFile(path);
            }

            // 检查是否是目录
            if (Files.isDirectory(path)) {
                logger.debug("[StorHandler] That's a directory...");
                sendResponse(writerMtx, 550, "Permission denied. The path is a directory.");
                return;
            }

            long offset = sessionMtx.getData().offset;

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
            if(!sessionMtx.getData().socket.isClosed())
                sessionMtx.getData().getSocket().close();
            logger.debug("尝试解锁");
            sessionMtx.unlock();
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

    private boolean performFileTransfer(Mutex<Session> sessionMtx, Path path, long offset) throws Exception {
        // 此函数外面的session已经上锁
        var reader = (new SocketHelper(sessionMtx.getData().socket).getReader("UTF-8"));
        while(true){
            if(sessionMtx.getData().aborted){
                logger.debug("[StorHandler] Aborted.");
                break;
            }

            char[] buffer = new char[4096];
            int len = -1;
            len = reader.read(buffer);
            logger.debug("[StorHandler] Read data length: " + len);
            if(len == -1){
                logger.debug("[StorHandler] End of file.");
                break;
            }

            final int l = len;
            final long off = offset;
            var fileHandlerRw = fileManager.get(path.toString());
            int[] byteLength = {0};
            fileHandlerRw.write(writer->{
                String contentRead = new String(buffer, 0, l);

                // 使用 RandomAccessFile 进行写入
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "rw")) {
                    randomAccessFile.seek(off);
                    byte[] byteBuffer = contentRead.getBytes("UTF-8");
                    randomAccessFile.write(byteBuffer);
                    byteLength[0] = byteBuffer.length;
                } catch (IOException e) {
                    logger.error("Error writing to file: " + e);
                }
                            
            });

            offset += byteLength[0];
            sessionMtx.getData().offset += byteLength[0];
            sessionMtx.getData().finishedSize += len;
        }
        
        return !sessionMtx.getData().aborted;
    }
}