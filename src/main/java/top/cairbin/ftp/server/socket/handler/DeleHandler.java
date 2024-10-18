/*
 * @Description: 处理DELE，删除文件命令
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 22:33:58
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 06:52:13
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class DeleHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[DeleHandler]Dispatching dele handler...");
        userMtx.lock();
        try{
            if(params.getArgs() == null)
                throw new Exception("args must not be null");
            String filename = (String)params.getArgs();
            if(filename.isEmpty())
                throw new Exception("filename must not be empty");

            var path = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), filename);

            if(!Files.exists(path)){
                writerMtx.lockAndSet(writer->{
                    try {
                        String msg = FtpMessage.fmtMessage(553, "Not found.");
                        writer.write(msg);
                        writer.flush();
                        logger.debug("[DeleHandler]Writing message: " + msg);
                    } catch (Exception e) {
                        logger.error("[DeleHandler]Writing error: " + e);
                        logger.error("[DeleHandler]Writing error message: " + e.getMessage());
                    }
                });
                return;
            }

            if(!server.getRoot().isValid(path.toString())){
                logger.warn("[DeleHandler]Trying to access illegal path!");
                writerMtx.lockAndSet(writer->{
                    try{
                        String msg = FtpMessage.fmtMessage(550, "Permission denied.");
                        writer.write(msg);
                        writer.flush();
                        logger.debug("[DeleHandler]Writing message: " + msg);
                    }catch(Exception e){
                        logger.error("[DeleHandler]Writing error: " + e);
                        logger.error("[DeleHandler]Writing error message: " + e.getMessage());
                    }
                });
            }

            boolean flag = Files.deleteIfExists(path);
            if(flag){
                writerMtx.lockAndSet(writer->{
                    try {
                        String msg = FtpMessage.fmtMessage(250, "Requested file action okay, completed.");
                        writer.write(msg);
                        writer.flush();
                        logger.debug("[DeleHandler]Writing message: " + msg);
                    } catch (Exception e) {
                        logger.error("[DeleHandler]Writing error: " + e);
                        logger.error("[DeleHandler]Writing error message:" + e.getMessage());
                    }
                });
            }

        }catch(Exception e){
            throw e;
        }finally{
            userMtx.unlock();
        }
    }
    
}
