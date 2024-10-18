/*
 * @Description: RMD处理，删除目录
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 22:03:04
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 07:57:01
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.google.inject.Inject;
import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpMessage;
import top.cairbin.ftp.server.utils.FtpParams;

// public class RmdHandler implements IHandler {
//     @Inject
//     private ILogger logger;

//     @Inject
//     private Server server;


//     @Override
//     public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
//         logger.debug("[RmdHandler]Dispatching rmd handler");
//         userMtx.lock();
//         try{
//             if(params == null)
//             throw new Exception("params must not be null");
//         String dir = (String)params.getArgs();
//         if(dir.isEmpty()){
//             throw new Exception("Invalid dir");
//         }

//         dir = Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), dir).toString();

//         if(!this.server.getRoot().isValid(dir)){
//             logger.warn("[RmdHandler]Trying to access illegal directory: " + dir);
//             writerMtx.lockAndSet(writer->{
//                 try {
//                     writer.write(FtpMessage.fmtMessage(550, "Permission denied."));
//                     writer.flush();
//                 } catch (Exception e) {
//                     this.logger.error("[Rmdhandler]Writing error: " + e);
//                     this.logger.error("[Rmdhandler]Writing error message: " + e.getMessage());
//                 }
//             });
//             return;
//         }

//         File file = new File(dir);
//         if(!file.exists()){
//             writerMtx.lockAndSet(writer->{
//                 try {
//                     writer.write(FtpMessage.fmtMessage(553, "Directory does not exist."));
//                     writer.flush();
//                 } catch (Exception e) {
//                     this.logger.error("[Rmdhandler]Writing error: " + e);
//                     this.logger.error("[Rmdhandler]Writing error message: " + e.getMessage());
//                 }
//             });
//             return;
//         }

//         boolean flag = file.delete();
//         if(!flag){
//             logger.info("[RmdHandler]Failed to delete directory.");
//             writerMtx.lockAndSet(writer->{
//                 try {
//                     writer.write(FtpMessage.fmtMessage(550, "Failed to delete directory."));
//                     writer.flush();
//                 } catch (Exception e) {
//                     this.logger.error("[Rmdhandler]Writing error: " + e);
//                     this.logger.error("[Rmdhandler]Writing error message: " + e.getMessage());
//                 }
//             });
//             return;
//         }

//         logger.warn("[RmdHandler]Success to delete directory.");
//         writerMtx.lockAndSet(writer->{
//             try {
//                 writer.write(FtpMessage.fmtMessage(200, "Directory deleted successfully."));
//                 writer.flush();
//             } catch (Exception e) {
//                 this.logger.error("[Rmdhandler]Writing error: " + e);
//                 this.logger.error("[Rmdhandler]Writing error message: " + e.getMessage());
//             }
//         });

//         }catch(Exception e){
//             throw e;
//         }finally{
//             userMtx.unlock();
//         }

//     } 
    
// }


public class RmdHandler implements IHandler {
    @Inject
    private ILogger logger;

    @Inject
    private Server server;

    @Override
    public void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception {
        logger.debug("[RmdHandler] Dispatching RMD handler");
        
        userMtx.lock();
        try {
            validateParams(params);
            String dir = buildDirectoryPath(params, userMtx);

            // 验证路径是否合法
            if (!isValidDirectory(dir)) {
                sendError(writerMtx, "Permission denied.");
                return;
            }

            File file = new File(dir);
            if (!file.exists()) {
                sendError(writerMtx, "Directory does not exist.");
                return;
            }

            if (!file.delete()) {
                sendError(writerMtx, "Failed to delete directory.");
                return;
            }

            logger.warn("[RmdHandler] Successfully deleted directory: " + dir);
            sendSuccess(writerMtx, "Directory deleted successfully.");
        } catch (Exception e) {
            logger.error("[RmdHandler] Error: {}", e.getMessage(), e);
            throw e; // Re-throwing the exception for the caller to handle
        } finally {
            userMtx.unlock();
        }
    }

    private void validateParams(FtpParams params) throws Exception {
        if (params == null) {
            throw new Exception("params must not be null");
        }
        String dir = (String) params.getArgs();
        if (dir == null || dir.isEmpty()) {
            throw new Exception("Invalid directory");
        }
    }

    private String buildDirectoryPath(FtpParams params, Mutex<User> userMtx) {
        String dir = (String) params.getArgs();
        return Paths.get(server.getRoot().getRealPath(), userMtx.getData().pwd(), dir).toString();
    }

    private boolean isValidDirectory(String dir) throws IOException {
        return server.getRoot().isValid(dir);
    }

    private void sendError(Mutex<BufferedWriter> writerMtx, String message) {
        logger.warn("[RmdHandler] {}", message);
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(550, message));
                writer.flush();
            } catch (Exception e) {
                logger.error("[RmdHandler] Writing error: {}", e.getMessage(), e);
            }
        });
    }

    private void sendSuccess(Mutex<BufferedWriter> writerMtx, String message) {
        writerMtx.lockAndSet(writer -> {
            try {
                writer.write(FtpMessage.fmtMessage(200, message));
                writer.flush();
            } catch (Exception e) {
                logger.error("[RmdHandler] Writing error: {}", e.getMessage(), e);
            }
        });
    }
}