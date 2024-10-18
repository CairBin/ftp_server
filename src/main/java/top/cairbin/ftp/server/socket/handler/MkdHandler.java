/*
 * @Description: 处理MKD
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 21:51:22
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:42:30
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.nio.file.Paths;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class MkdHandler implements IHandler{
    @Inject
    private ILogger logger;

    @Inject
    private Server server;
    
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[MkdHandler]Dispatching mkd handler...");
        userMtx.lock();
        try {
            if(params.getArgs() == null)
                throw new Exception("params must not be null");
            String dir = (String)params.getArgs();
            if(dir.isEmpty())
                throw new Exception("dir must not be empty");;

            var file = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), dir).toFile();
            
            if(!this.server.getRoot().isValid(file.toString())){
                // 非法区域
                logger.warn("[MkdHandler]Accessing illegal area");
                writerMtx.lockAndSet(writer->{
                    try {
                        writer.write(FtpMessage.fmtMessage(550, "Permission denied."));
                        writer.flush();
                    } catch (Exception ex) {
                        logger.error("[MkdHandler]Error writing: " + ex);
                        logger.error("[MkdHandler]Error message: " + ex.getMessage());
                    }
                });
                return;
            }

            boolean flag = file.exists() ? true : file.mkdirs();
            if(flag){
                logger.info(String.format("[MkdHandler]Directory exists or creating successful, path=\"%s\"", file.toString()));
                writerMtx.lockAndSet(writer->{
                    try {
                        writer.write(FtpMessage.fmtMessage(257, "Dir created."));
                        writer.flush();
                    } catch (Exception ex) {
                        logger.error("[MkdHandler]Error writing: " + ex);
                        logger.error("[MkdHandler]Error message: " + ex.getMessage());
                    }
                });
                return;
            }

            writerMtx.lockAndSet(writer->{
                try {
                    logger.warn("MKD handler: Failed to create dir. Permission denied.");
                    writer.write(FtpMessage.fmtMessage(550, "Permission denied."));
                    writer.flush();
                } catch (Exception ex) {
                    logger.error("[MkdHandler]Error writing: " + ex);
                    logger.error("[MkdHandler]Error message: " + ex.getMessage());
                }
            });
                
        } catch (Exception e) {
            logger.error("[MkdHandler]Error: " + e);
            logger.error("[MkdHandler]Error message: " + e.getMessage());
            throw e;
        }finally{
            userMtx.unlock();
        }
    }
    
}
