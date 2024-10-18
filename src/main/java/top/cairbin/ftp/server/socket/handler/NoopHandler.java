/*
 * @Description: 处理NOOP
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 15:04:39
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 15:07:23
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class NoopHandler implements IHandler {
    
    @Inject
    ILogger logger;
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        writerMtx.lockAndSet(writer->{
            try {
                writer.write(FtpMessage.fmtMessage(200, "NOOP ok."));
                writer.flush();
            } catch (Exception e) {
                logger.error("writing error "+e);
            }
        });
    }
    
}
