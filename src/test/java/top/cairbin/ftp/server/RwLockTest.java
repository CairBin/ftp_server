/*
 * @Description: 测试读写锁
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 02:25:26
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 02:31:29
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import org.junit.Test;

import top.cairbin.ftp.server.lock.RwLock;
import top.cairbin.ftp.server.logger.ILogger;

public class RwLockTest {
    private ILogger logger;
    
    public RwLockTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
    }

    @Test
    public void testRwLock() {
        // 创建一个封装了 Integer 类型的读写锁
        RwLock<Integer> rwLock = new RwLock<>(0);

        // 创建一个线程来执行写操作
        Thread writer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                String str = String.format("%s writing: %d",Thread.currentThread().getName(), i);
                int d = i;
                rwLock.write(data -> {
                    logger.info(str);
                    data = d; // 修改数据
                });
                try {
                    Thread.sleep(1000); // 模拟写操作的延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // 创建多个线程来执行读操作
        Thread reader1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                Integer result = rwLock.read(data -> {
                    logger.info(Thread.currentThread().getName() + " reading: " + data);
                    return data;
                });
                try {
                    Thread.sleep(500); // 模拟读操作的延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread reader2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                Integer result = rwLock.read(data -> {
                    System.out.println(Thread.currentThread().getName() + " reading: " + data);
                    return data;
                });
                try {
                    Thread.sleep(500); // 模拟读操作的延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // 启动读写线程
        writer.start();
        reader1.start();
        reader2.start();

        // 等待线程完成
        try {
            writer.join();
            reader1.join();
            reader2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
      
    }
}
