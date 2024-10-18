/*
 * @Description: 处理STAT
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 23:07:10
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 02:54:23
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;
import top.cairbin.ftp.server.utils.ListLine;

public class StatHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[StatHandler]Dispatching stat handler");
        userMtx.lock();
        try {
            if(params.args == null){

                writerMtx.lockAndSet(writer->{
                    try {
                        String content = "";
                        writer.write("211-Status of the server:\r\n");
                        content += String.format("User: %s\r\n", userMtx.getData().getUsername());
                        content += String.format("Current directory: %s\r\n", userMtx.getData().pwd());
                        content += String.format("TYPE: \r\n", userMtx.getData().getTransType().name());
                        writer.write(content);
                        writer.write(FtpMessage.fmtMessage(211, "End of status."));
                        writer.flush();
                    } catch (IOException e) {
                        logger.error("writing error: "+e);
                    }
                });

                return;
            }


            String path = (String)params.getArgs();
            Path pathT = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), path);
            if(!Files.exists(pathT)){
                writerMtx.lockAndSet(writer->{
                    try {
                        writer.write(FtpMessage.fmtMessage(553, "Not found."));
                        writer.flush();
                    } catch (IOException e) {
                        logger.error("writing error: "+e);
                    }
                });
                return;
            }

            if(!this.server.getRoot().isValid(pathT.toString())){
                writerMtx.lockAndSet(writer->{
                    try {
                        writer.write(FtpMessage.fmtMessage(550, "Permission denied."));
                        writer.flush();
                    } catch (IOException e) {
                        logger.error("writing error: "+e);
                    }
                });
                return;
            }

            String list = ListLine.getListLines(pathT, false);
            writerMtx.lockAndSet(writer->{
                try {
                    writer.write("213-Status of {}:\r\n");
                    writer.write(list);
                    writer.write(FtpMessage.fmtMessage(213, "End of status."));
                    writer.flush();
                } catch (IOException e) {
                    logger.error("writing error: "+e);
                }
            });


        } finally {
            userMtx.unlock();
        }
    }
    
}
