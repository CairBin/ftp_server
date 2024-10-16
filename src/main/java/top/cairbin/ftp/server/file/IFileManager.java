/*
 * @Description: 文件缓存池
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 03:09:16
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 05:50:50
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.file;

import java.io.File;

import top.cairbin.ftp.server.lock.RwLock;

public interface IFileManager {
    public RwLock<FileHandler> createFile(String path) throws Exception;
    public boolean isExist(String path);
    public RwLock<FileHandler> get(String path) throws Exception;
}
