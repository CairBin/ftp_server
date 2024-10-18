/*
 * @Description: 处理SYST
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:28:01
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:56:26
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

public class SystHandler implements IHandler {
    @Inject
    ILogger logger;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[SystHandler]Dispatching syst handler");
        writerMtx.lockAndSet(writer->{
            try {
                writer.write(FtpMessage.fmtMessage(215,"UNIX Type: L8"));
                writer.flush();
            } catch (IOException e) {
                logger.error("Failed to write: " + e);
            }
        });
    }
    
}
