/*
 * @Description: 文件缓存池
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 03:09:16
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 03:10:36
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.file;

import java.io.File;

import top.cairbin.ftp.server.lock.RwLock;

public interface IFileCachePool {
    public boolean createFile(String path);

    public boolean isExist(String path);

    public boolean has(String path);

    public RwLock<File> get(String path) throws Exception;

    public void remove(String path);
}
