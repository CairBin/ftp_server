/*
 * @Description: ALLO处理
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 23:01:50
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:20:30
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

public class AlloHandler implements IHandler {
    @Inject
    private ILogger logger;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[AlloHandler]Dispatching allo handler...");
        writerMtx.lockAndSet(writer->{
            try{
                String msg = FtpMessage.fmtMessage(200, "ALLO command okay.");
                writer.write(msg);
                writer.flush();
                logger.debug("[AlloHandler]Writing message: " + msg);
            }catch(Exception e){
                logger.error("[AlloHandler]Writing error: " + e);
                logger.error("[AlloHandler]Writing error: " + e.getMessage());
            };
        });
    }
    
}
