/*
 * @Description: 读写锁封装
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 02:18:07
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 02:24:34
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Consumer;

public class RwLock<T> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private T data;

    /**
     * @description: 构造函数，初始化受保护的资源
     * @param {T} 受保护的数据
     */    
    public RwLock(T data) {
        this.data = data;
    }

    /**
     * @description: 读操作，允许多个线程同时访问
     * @param {Function<T,R>} readFunction 读操作的函数
     * @return {*}
     */
    public <R> R read(Function<T, R> readFunction) {
        lock.readLock().lock();
        try {
            // 调用传入的读取函数并返回结果
            return readFunction.apply(data);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @description: 写操作，只有一个线程可以执行
     * @param {Consumer<T>} writeConsumer 写操作
     * @return {*}
     */    
    public void write(Consumer<T> writeConsumer) {
        lock.writeLock().lock();
        try {
            // 调用传入的写函数修改资源
            writeConsumer.accept(data);
        } finally {
            lock.writeLock().unlock();
        }
    }
}