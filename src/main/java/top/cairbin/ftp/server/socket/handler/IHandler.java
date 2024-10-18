/*
 * @Description: 消息处理接口
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 13:58:56
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 13:59:57
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket.handler;

import java.io.BufferedWriter;

import top.cairbin.ftp.server.lock.Mutex;
import top.cairbin.ftp.server.socket.User;
import top.cairbin.ftp.server.utils.FtpParams;

public interface IHandler {
    void handle(Mutex<BufferedWriter> writerMtx, FtpParams params, Mutex<User> userMtx) throws Exception;
}
