/*
 * @Description: FTP Command
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 00:58:11
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 01:05:36
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.utils;

public enum FtpCommand {
    // 接入命令
    USER,   // 用户标识符
    PASS,   // 登陆口令
    QUIT,   // 系统注销
    ABOR,   // 数据连接以及上次命令终止

    // 文件管理命令
    CWD,    // 改变到另一个目录
    CDUP,   // 改变到父目录
    DELE,   // 删除文件
    LIST,   // 列出子目录和文件
    NLIST,  // 列出子目录或其它属性文件
    MKD,    // 创建目录
    PWD,    // 显示当前目录
    RMD,    // 删除目录
    RNFR,   // 标志要重新命名的文件，参数为旧文件名
    RNTO,   // 重命名，参数为新文件名
    TYPE,   // 定义文件类型

    // 端口相关
    PORT,   // 客户端选择端口，主动模式
    PASV,   // 服务器选择端口，被动模式

    // 文件传输
    RETR,       // 下载文件
    STOR,       // 上传文件
    STOU,       // 上传文件，但是文件名必须唯一
    ALLO,       // 在服务器为文件分配存储空间
    STAT,       // 返回文件状态
    REST,       // 数据点给文件标记位置
    
    // 其他
    NOOP,             // 检查服务器是否工作
    SYST              // 获取服务器所用OS
}