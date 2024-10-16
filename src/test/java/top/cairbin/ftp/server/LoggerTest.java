/*
 * @Description: 测试日志记录器
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 00:39:24
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 00:48:02
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;
import org.junit.Test;


import top.cairbin.ftp.server.logger.ILogger;
public class LoggerTest {

    private ILogger logger;
    
    public LoggerTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
    }

    @Test
    public void testLogger() {
        this.logger.debug("This is a debug message");
    }

    @Test
    public void testLoggerInfo() {
        logger.info("This is an info message");
    }

    @Test
    public void testLoggerWarn() {
        logger.warn("This is a warn message");
    }

    @Test
    public void testLoggerError() {
        logger.error("This is an error message");
    }

    @Test
    public void testLoggerFatal() {
        logger.fatal("This is a fatal message");
    }

}
