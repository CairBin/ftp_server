/*
 * @Description: 被动模式
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 18:50:24
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 06:27:26
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.net.ServerSocket;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Mode;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.Session;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class PasvHandler implements IHandler {

    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[PasvHandler]Dispatching pasv handler...");
        try{
            String addr = server.getPasvIp();
            logger.debug("[PasvHandler]Socket address: " + addr);
            final String ip = addr.split(":")[0].replace(".",",");
            int port = server.getPasvPort();

            logger.debug("[PasvHandler]Address: " + ip);
            logger.debug("[PasvHandler]Port: " + port);

            if(server.getPasvListener() == null){
                server.setPasvListener(new ServerSocket(port));
            }

            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(227,
                        
                        String.format("Entering Passive Mode (%s,%d,%d)", 
                            ip,
                            port/256,
                            port%256)
                    ));
                    writer.flush();
                } catch (Exception e) {
                    logger.error("Error writing: " + e);
                }

            });

            this.server.getThreadPool().submit(()->{
                userMtx.lock();
                try {
                    var socket = server.getPasvListener().accept();
                    logger.debug("[PasvHandler]Accepting pasv connection");
                    userMtx.getData().setSession(new Mutex<Session>(new Session(Mode.PASV, socket)));
                }catch(Exception e) {
                    logger.error("[PasvHandler]Accept error: {}", e);
                }finally{
                    userMtx.unlock();
                }
            });

        }catch(Exception e){
            throw e;
        }finally{

        }
        
    }
    
}
