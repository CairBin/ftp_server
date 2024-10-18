/*
 * @Description: 处理REST
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:49:00
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 14:54:51
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

public class RestHandler implements IHandler {
    @Inject
    ILogger logger;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        userMtx.lock();
        try{
            int offset = (Integer)(params.getArgs());
            var sessionMtx = userMtx.getData().getSession();
            sessionMtx.lockAndSet(session->{
                session.setOffset(offset);
            });

            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(350, "Rest position set."));
                } catch (Exception e) {
                    logger.error("Error writing: "+e);
                }
            });
        }catch(Exception e){
            throw e;
        }finally{
            userMtx.unlock();
        }
    }
    
}
