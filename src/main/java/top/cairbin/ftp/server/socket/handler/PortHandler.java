/*
 * @Description: file content
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 19:37:31
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 00:35:41
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.net.Socket;

import com.google.inject.Inject;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Mode;
import top.cairbin.ftp.server.socket.Session;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

public class PortHandler implements IHandler {
    @Inject
    ILogger logger;
    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.info("Dispatching port handler...");
        userMtx.lock();
        try{
            if(params.args == null)
                throw new Exception("params must not be null");
            String address = (String)params.getArgs();
            String[] parts = address.split(":");
            if(parts.length!= 2)
                throw new Exception("Invalid PORT address");
            int port = Integer.parseInt(parts[1]);
            logger.info("PORT: Address: " + parts[0] + " Port: " + port);
            Socket socket = new Socket(parts[0], port);
            userMtx.getData().setSession(new Mutex<Session>(new Session(Mode.PORT, socket)));
        
            writerMtx.lockAndSet(writer->{
                try {
                    writer.write(FtpMessage.fmtMessage(200, "PORT command successful."));
                    writer.flush();
                } catch (Exception e) {
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
