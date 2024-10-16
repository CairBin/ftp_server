/*
 * @Description: 日志接口
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 00:17:56
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 00:30:56
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.logger;

public interface ILogger {
    /**
     * @description: debug级别日志
     * @param {String} message 消息字符串
     * @return {*}
     */    
    void debug(String message, Object...args);

    /**
     * @description: info级别日志
     * @param {String} message 消息字符串
     * @return {*}
     */
    void info(String message, Object...args);

    /**
     * @description: warn级别日志
     * @param {String} message 消息字符串
     * @return {*}
     */
    void warn(String message, Object...args);

    /**
     * @description: error级别日志
     * @param {String} message 消息字符串
     * @return {*}
     */
    void error(String message, Object...args);

    /**
     * @description: fatal级别日志
     * @param {String} message 消息字符串
     * @return {*}
     */
    void fatal(String message, Object...args);
}
