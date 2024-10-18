/*
 * @Description: 处理RNFR
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 14:38:49
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:53:51
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

public class RnfrHandler implements IHandler{
    @Inject
    ILogger logger;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[RnfrHandler]Dispatching rnfr handler");
        userMtx.lock();
        try{
            String filename = (String)params.getArgs();
            if(filename == null || filename.isEmpty())
                throw new Error("RNFR: error filename");
            var sessionMtx = userMtx.getData().getSession();
            sessionMtx.lockAndSet(session->{
                session.setFilename(filename);
            });

            writerMtx.lockAndSet(writer->{
                try{
                    writer.write(FtpMessage.fmtMessage(350, "File status okay; about to rename."));
                    writer.flush();
                }catch(Exception e){
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
