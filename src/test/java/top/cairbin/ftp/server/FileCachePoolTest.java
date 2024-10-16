/*
 * @Description: 文件池测试
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:13:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 03:35:34
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;


import top.cairbin.ftp.server.file.IFileCachePool;
import top.cairbin.ftp.server.lock.RwLock;
import top.cairbin.ftp.server.logger.ILogger;

public class FileCachePoolTest {
    private ILogger logger;

    private IFileCachePool pool;
    
    public FileCachePoolTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
        this.pool = InjectorFactory.getInjector().getInstance(IFileCachePool.class);
    }

    @Test
    public void testFileCachePool() throws Exception {
        // 创建一个 FileCachePool 实例
        if(!pool.createFile("hello.txt")){
            logger.error("Failed to create file");
        }

        logger.info("File created successfully");
        RwLock<File> lock = pool.get("hello.txt");
        lock.write(file->{
            logger.info("writing file...");
            try{
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write("Hello, World!".getBytes());
                }
            }catch(Exception e){
                logger.error("Error writing file", e);
            }
        });
        
        logger.info("Trying to delete file");
        lock.write(file->{
            if(file.delete()){
                logger.info("File deleted successfully");
            }
        });
    }
}
