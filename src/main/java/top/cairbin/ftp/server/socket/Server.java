/*
 * @Description: FTP服务器
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:55:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 06:05:03
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import com.google.inject.Inject;

import top.cairbin.ftp.server.file.IFileManager;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.thread.IThreadPool;

public class Server {
    @Inject
    private ILogger logger;    
    
    @Inject
    private IFileManager files;

    @Inject 
    private IThreadPool threads;

    
}
