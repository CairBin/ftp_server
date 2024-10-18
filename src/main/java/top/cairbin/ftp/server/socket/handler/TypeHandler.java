/*
 * @Description: 处理TYPE
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 20:32:42
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 03:11:38
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.TransferType;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class TypeHandler implements IHandler {
    @Inject
    private ILogger logger;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        if(params.args == null)
            throw new Exception("params.args is null");
        String type = (String) params.getArgs();
        if(type.equals("I")){
            userMtx.lockAndSet(user->{
                user.setTransType(TransferType.BINARY);
            });
            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(200, "Type set to Binary."));
                    writer.flush();
                } catch (IOException e) {
                    logger.error("writing error: "+e);
                }
            });
        }else if(type.equals("A")){
            userMtx.lockAndSet(user->{
                user.setTransType(TransferType.ASCII);
            });
            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(200, "Type set to ASCII."));
                    writer.flush();
                } catch (IOException e) {
                    logger.error("writing error: "+e);
                }
            });
        }else{
            logger.warn("Unsupported transfer type");
            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(504, "Command not implemented for that parameter."));
                    writer.flush();
                } catch (IOException e) {
                    logger.error("writing error: "+e);
                }
            });
        }
    
    }
    
}
