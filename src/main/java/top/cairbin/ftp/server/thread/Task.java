/*
 * @Description: 任务接口
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 01:53:36
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 01:55:52
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.thread;

@FunctionalInterface
public interface Task {
    /**
     * @description: 执行任务
     * @return {*}
     */
    void execute();
}