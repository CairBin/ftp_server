/*
 * @Description: ABOR处理
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 14:19:19
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 05:10:14
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.socket.UserState;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class AbortHandler implements IHandler {
    @Inject 
    ILogger logger;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[AbortHandler]Dispatching abort handler...");
        userMtx.lock();
        try {
            userMtx.getData().setState(UserState.ONLINE);
            var sessionMtx = userMtx.getData().getSession();
            if(sessionMtx != null){
                sessionMtx.lockAndSet(session->{
                    session.setAborted(true);
                });
            }
            

            writerMtx.lockAndSet(writer ->{
                try{
                    String msg = FtpMessage.fmtMessage(426, "Connection closed unexpectedly.");
                    writer.write(msg);
                    writer.flush();
                    logger.debug("[AbortHandler]Writing message: "+msg);
                }catch(Exception e){
                    logger.error("[AbortHandler]Writing error: " + e);
                    logger.error("[AbortHandler]Writing error message: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            throw e;
        }finally{
            userMtx.unlock();
        }
    }
    
}
