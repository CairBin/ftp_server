/*
 * @Description: file content
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 02:02:34
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 02:03:27
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.thread;

public interface IThreadPool {
    /**
     * @description: 提交任务到任务队列
     * @param {Task} task 任务
     * @return {*}
     */    
    public void submit(ITask task);



    /**
     * @description: 关闭线程池，所有任务执行完成后关闭所有线程
     * @return {*}
     */
    public void shutdown();
}
