/*
 * @Description: 文件池测试
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:13:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-21 01:44:17
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import org.junit.Test;
import top.cairbin.ftp.server.file.IFileManager;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.thread.IThreadPool;

public class FileManagerTest {
    private ILogger logger;

    
    public FileManagerTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
    }

    @Test
    public void testFileCachePool() throws Exception {
        IFileManager manager = InjectorFactory.getInjector().getInstance(IFileManager.class);
        manager.createFile("temp/hello.txt");
        IThreadPool pool = InjectorFactory.getInjector().getInstance(IThreadPool.class);
        pool.submit(()->{
            try{
                var lock = manager.get("temp/hello.txt");
                lock.write(file->{
                    try{
                        file.getWriter().append("Hello World");
                        file.getWriter().newLine();
                        file.getWriter().flush();
                    }catch(Exception e){
                        logger.error("{}", e);
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        });

    }
}
