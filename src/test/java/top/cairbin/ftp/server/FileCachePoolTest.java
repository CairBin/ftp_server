/*
 * @Description: 文件池测试
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:13:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 03:14:23
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import org.junit.Test;
import top.cairbin.ftp.server.logger.ILogger;

public class FileCachePoolTest {
    private ILogger logger;
    
    public FileCachePoolTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
    }
}
