/*
 * @Description: 工作线程
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 01:54:08
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 02:03:56
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.thread;

public class Worker extends Thread {
    private final TaskQueue taskQueue;
    private volatile boolean isRunning = true;

    public Worker(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // 获取任务并执行
                ITask task = taskQueue.getTask();
                if (task != null) {
                    task.execute();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        isRunning = false;
        this.interrupt(); // 如果线程在等待任务，打断它
    }
}