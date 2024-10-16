/*
 * @Description: 配置依赖注入
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 00:32:01
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 02:04:51
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import top.cairbin.ftp.server.logger.Log4j;
import top.cairbin.ftp.server.thread.IThreadPool;
import top.cairbin.ftp.server.thread.ThreadPool;
import top.cairbin.ftp.server.logger.ILogger;

public class AppModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(IThreadPool.class)
            .to(ThreadPool.class);
    }

    @Provides
    @Singleton
    private ILogger getLogger(){
        return new Log4j();
    }
}
