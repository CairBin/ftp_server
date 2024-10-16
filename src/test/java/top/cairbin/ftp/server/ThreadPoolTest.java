/*
 * @Description: 线程池测试
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 02:05:25
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 02:07:39
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import org.junit.Test;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.thread.IThreadPool;

public class ThreadPoolTest {
    private ILogger logger;

    public ThreadPoolTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
    }

    @Test
    public void testThreadPool() {
        IThreadPool threadPool = InjectorFactory.getInjector().getInstance(IThreadPool.class);
        for(int i = 0; i < 11; i++) {
            threadPool.submit(() -> {
                logger.info("Thread {} is running", Thread.currentThread().getId());
            });
        }
    }
}
