/*
 * @Description: 处理PWD
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 21:46:20
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 01:14:51
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

public class PwdHandler implements IHandler{
    @Inject
    private ILogger logger;

    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("Dispatching pwd handler...");
        userMtx.lock();
        try {
            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(257, String.format("\"%s\" is the current directory", userMtx.getData().renderingPwd())));
                    writer.flush();
                } catch (Exception e) {
                    logger.error("writing error "+e);
                }
            });
        } catch (Exception e) {
            throw e;
        }finally{
            userMtx.unlock();
        }
    }
    
}
