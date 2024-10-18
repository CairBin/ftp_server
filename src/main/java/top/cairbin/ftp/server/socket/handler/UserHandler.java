/*
 * @Description: 处理 User命令
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:00:53
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 00:02:17
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.socket.UserState;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class UserHandler implements IHandler {
    @Inject
    ILogger logger;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("Dispathing user handler");
        userMtx.lock();
        try{
            String username = (String)params.getArgs();
            if(username == null || username.isEmpty()){
                throw new Error("Invalid username");
            }
            userMtx.getData().setUsername(username);
            userMtx.getData().setState(UserState.LOGGING);
            writerMtx.lockAndSet(writer ->{
                try {
                    writer.write(FtpMessage.fmtMessage(331, "Username okay, need password."));
                    writer.flush();
                } catch (IOException e) {
                    logger.error("Error writing: "+e);
                }
            });
        }catch(Exception e){
            throw e;
        }finally {
            userMtx.unlock();
        }
    }
    
}
