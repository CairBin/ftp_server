/*
 * @Description: 互斥锁
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 07:33:28
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 07:55:21
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.lock;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class Mutex<T> {
    private T data;
    private final ReentrantLock lock;

    // 构造函数，接受要保护的对象
    public Mutex(T data) {
        this.data = data;
        this.lock = new ReentrantLock();
    }

    /**
     * @description: 锁定并返回结果
     * @param {Function<T,R>} function
     * @return {*}
     */    
    public <R> R lockAndGet(Function<T, R> function) {
        lock.lock();
        try {
            return function.apply(data);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @description: 锁定并更新数据
     * @param {Consumer<T>} consumer
     * @return {*}
     */    
    public void lockAndSet(Consumer<T> consumer) {
        lock.lock();
        try {
            consumer.accept(data);
        } finally {
            lock.unlock();
        }
    }

    // 手动加锁与解锁
    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    // 手动获取数据
    public T getData() {
        lock.lock();
        try {
            return data;
        } finally {
            lock.unlock();
        }
    }

    // 手动设置数据
    public void setData(T data) {
        lock.lock();
        try {
            this.data = data;
        } finally {
            lock.unlock();
        }
    }
}