/*
 * @Description: FTP消息
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 03:41:24
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 03:49:08
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FtpMessage {
    public int code;
    public String message;

    public FtpMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%d %s\r\n", code, message);
    }

    /**
     * @description: 获取格式化后的消息
     * @param {int} code 状态码
     * @param {String} message 消息
     * @return {*}
     */    
    public static String fmtMessage(int code, String message){
        return String.format("%d %s\r\n", code, message);
    }
}
