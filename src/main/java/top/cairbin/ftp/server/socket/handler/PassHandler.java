/*
 * @Description: 处理密码请求
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.1
 * @Date: 2024-10-17 14:15:08
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 10:42:55
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.socket.UserState;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class PassHandler implements IHandler {
    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("[PassHandler] Dispatching pass handler...");
        userMtx.lock();
        try{
            String token = params.args == null ? "" : (String) params.args;
            String username = userMtx.getData().username;

            if(!username.toLowerCase().equals("anonymous")){
                if(!server.hasUserConfig(username) || !server.getUserConfig(username).getToken().equals(token)){
                    sendNotLogin(writerMtx);
                    return;
                }
            }
            
            userMtx.getData().setState(UserState.ONLINE);
            writerMtx.lockAndSet(writer->{
                try{
                    writer.write(FtpMessage.fmtMessage(230, "User logged in, proceed."));
                    writer.flush();
                }catch(Exception e){
                    logger.error("writing error: " + e);
                }
            });
            
        }finally{
            userMtx.unlock();
        }

    }

    private void sendNotLogin(Mutex<BufferedWriter> writerMtx){
        writerMtx.lockAndSet(writer->{
            try{
                writer.write(FtpMessage.fmtMessage(530, "Not logged in."));
                writer.flush();
            }catch(Exception e){
                logger.error("[PassHandler]Writing error: " + e);
                logger.error("[PassHandler]Writing error: " + e.getMessage());
            }
        });
    }

}
