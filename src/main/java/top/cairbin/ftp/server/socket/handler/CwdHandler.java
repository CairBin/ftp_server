/*
 * @Description: 处理CWD
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 21:41:07
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:23:06
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

public class CwdHandler implements IHandler{
    @Inject
    private ILogger logger;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[CdupHandler]Dispatching cwd handler...");
        userMtx.lock();
        try{
            String dir = (String)params.getArgs();
            if(dir == null || dir.isEmpty())
                throw new Exception("CWD: error directory");
            userMtx.getData().cwd(dir);
            logger.info("CWD: successfully");
            writerMtx.lockAndSet(writer ->{
                try{
                    writer.write(FtpMessage.fmtMessage(250, "CWD okay."));
                    writer.flush();
                }catch(Exception e){
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
