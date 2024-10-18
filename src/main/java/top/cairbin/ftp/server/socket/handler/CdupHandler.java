/*
 * @Description: 处理CDUP
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 22:19:26
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:22:23
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

public class CdupHandler implements IHandler{
    @Inject
    private ILogger logger;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[CdupHandler]Dispatching cdup handler...");
        userMtx.lock();
        try {
            userMtx.getData().cwd("..");
            writerMtx.lockAndSet(writer->{
                try {
                    String msg = FtpMessage.fmtMessage(250, "CDUP command successful.");
                    writer.write(msg);
                    writer.flush();
                    logger.debug("[CdupHandler]Writing message: " + msg);
                } catch (Exception e) {
                    logger.error("[CdupHandler]Writing error: " + e);
                    logger.error("[CdupHandler]Writing error message: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            throw e;
        }finally{
            userMtx.unlock();
        }
    }
    
}
