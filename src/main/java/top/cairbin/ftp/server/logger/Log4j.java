/*
 * @Description: Log4j 实现ILogger
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 00:20:01
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 00:31:12
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.logger;

import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

import org.apache.logging.log4j.LogManager;

@Singleton
public class Log4j implements ILogger{
    private Logger logger = LogManager.getLogger("Default Logger");

    public Log4j(){

    }

    @Override
    public void debug(String message, Object... args) {
        this.logger.debug(message, args);
    }

    @Override
    public void info(String message, Object... args) {
        this.logger.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        this.logger.warn(message, args);
    }

    @Override
    public void error(String message, Object... args) {
       this.logger.error(message, args);
    }

    @Override
    public void fatal(String message, Object... args) {
        this.logger.fatal(message, args);
    }
    
}
