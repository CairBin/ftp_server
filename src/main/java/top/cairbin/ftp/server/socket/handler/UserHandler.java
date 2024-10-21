/*
 * @Description: 处理 User命令
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:00:53
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-22 02:51:45
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;

import com.google.inject.Inject;
import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.socket.UserState;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class UserHandler implements IHandler {
    @Inject
    ILogger logger;

    @Inject
    Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("Dispathing user handler");
        try{
            String username = (String)params.getArgs();
            if(username == null || username.isEmpty()){
                throw new Error("Invalid username");
            }

            if(!username.toLowerCase().equals("anonymous") && !this.server.hasUserConfig(username)){
                logger.info("[UserHandler]Authentication failed. Username: {}",username);
                writerMtx.lockAndSet(writer ->{
                    try {
                        writer.write(FtpMessage.fmtMessage(530, "Not logged in."));
                        writer.flush();
                    } catch (IOException e) {
                        logger.error("Error writing: "+e);
                    }
                });
                return;
            }

            // 验证匿名用户
            if(username.toLowerCase().equals("anonymous") && !server.isAllowAnonymous()){
                logger.info("[UserHandler]Anyonmous account not allowed.");
                writerMtx.lockAndSet(writer->{
                    try {
                        writer.write(FtpMessage.fmtMessage(530, "Not logged in."));
                        writer.flush();
                    } catch (IOException ex) {
                        logger.error("Error writing: " + ex);
                    }
                });
                return;
            }

            userMtx.lock();
            userMtx.getData().setUsername(username);
            userMtx.getData().setState(UserState.LOGGING);
            userMtx.unlock();
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
        }
    }
    
}
