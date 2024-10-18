/*
 * @Description: 处理QUIT
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 14:57:00
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 07:52:21
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class QuitHandler implements IHandler {

    @Inject
    ILogger logger;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("[QuitHandler] Dispatching quit handler");

        userMtx.lock();
        try {
            clearSessionData(userMtx);
            sendGoodbyeMessage(writerMtx);
        } catch (Exception e) {
            logger.error("[QuitHandler] Error handling quit: {}", e.getMessage(), e);
            throw e; // Re-throwing the exception to let caller handle it
        } finally {
            userMtx.unlock();
        }
    }

    private void clearSessionData(Mutex<User> userMtx) {
        var sessionMtx = userMtx.getData().getSession();
        if (sessionMtx != null) {
            sessionMtx.lock();
            try {
                sessionMtx.setData(null);
            } finally {
                sessionMtx.unlock();
            }
        }
    }

    private void sendGoodbyeMessage(Mutex<BufferedWriter> writerMtx) {
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(221, "Byebye."));
                writer.flush();
                // writer.close();
            } catch (IOException e) {
                logger.error("[QuitHandler] Writing error: {}", e.getMessage(), e);
            }
        });
    }
}