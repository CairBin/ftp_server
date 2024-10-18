/*
 * @Description: 处理密码请求
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:15:08
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 00:05:11
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

public class PassHandler implements IHandler {
    @Inject
    private ILogger logger;
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("Dispatching pass handler");
        userMtx.lockAndSet(user->{
            user.setState(UserState.ONLINE);
        });

        writerMtx.lockAndSet(writer->{
            try{
                writer.write(FtpMessage.fmtMessage(230, "User logged in, proceed."));
                writer.flush();
            }catch(Exception e){
                logger.error("writing error: " + e);
            }
        });
    }
    
}
