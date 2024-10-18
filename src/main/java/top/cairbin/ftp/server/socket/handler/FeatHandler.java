/*
 * @Description: 处理FEAT
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 15:12:05
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 06:30:01
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

public class FeatHandler implements IHandler{
    @Inject
    ILogger logger;
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[FeatHandler]Dispatching feat handler...");
        writerMtx.lockAndSet(writer->{
            try{
                writer.write("221-Features:\r\n");
                writer.write(" REST STREAM\r\n");
                writer.write(" MDTM\r\n");
                writer.write(FtpMessage.fmtMessage(221, "End."));
                writer.flush();
            }catch(Exception e){
                logger.error("[FeatHandler]Writing error " + e);
                logger.error("[FeatHandler]Writing error message: " + e.getMessage());
            }
        });
    }
    
}
