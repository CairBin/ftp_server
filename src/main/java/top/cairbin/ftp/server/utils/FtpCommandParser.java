/*
 * @Description: 解析来自客户端的命令及参数
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 01:06:34
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 09:45:57
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.utils;

public class FtpCommandParser {
    public static FtpParams parse(String message){
        String[] msg = message.split(" ");
        if(msg.length == 0){
            return new FtpParams(FtpCommand.NOOP, null);
        }

        try{
            // System.out.println("Msg command--->"+msg[0]);
            FtpCommand cmd = FtpCommand.valueOf(msg[0].toUpperCase());
            // System.out.println("CMD--->" + cmd.name());
            Object args =  null;
            switch(cmd){
                case USER:
                    args = msg[1];
                    break;
                case PASS:
                    if(msg.length >= 2) args = msg[1];
                    break;
                case CWD:
                    args = msg[1];
                    break;
                case DELE:
                    args = msg[1];
                    break;
                case LIST:
                    if(msg.length >= 2) args = msg[1];
                    break;
                case NLIST:
                    if(msg.length >= 2) args = msg[1];
                    break;
                case NLST:
                    if(msg.length >= 2) args = msg[1];
                    break;
                case MKD:
                    args = msg[1];
                    break;
                case XMKD:
                    args = msg[1];
                    break;
                case RMD:
                    args = msg[1];
                    break;
                case XRMD:
                    args = msg[1];
                    break;
                case RNFR:
                    args = msg[1];
                    break;
                case RNTO:
                    args = msg[1];
                    break;
                case TYPE:
                    args = msg[1];
                    break;
                case PORT:
                    String[] parts = msg[1].split(",");
                    if(parts.length != 6) throw new Error("Invalid address for PORT");
                    // 前四个部分为IP地址
                    String ipAddress = String.join(".", parts[0], parts[1], parts[2], parts[3]);
                    // 最后两个部分为端口号
                    int p1 = Integer.parseInt(parts[4].trim().replaceAll("\r|\n", ""));
                    int p2 = Integer.valueOf(parts[5].trim().replaceAll("\r|\n", "")).intValue();
                    // 计算端口号
                    int port = p1 * 256 + p2;
                    args = String.format("%s:%d", ipAddress, port);
                    break;
                case RETR:
                    args = msg[1];
                    break;
                case STOR:
                    args = msg[1];
                    break;
                case STOU:
                    args = msg[1];
                    break;
                case ALLO:
                    args = Integer.parseInt(msg[1]);
                    break;
                case STAT:
                    if(msg.length == 2) args = msg[1];
                    break;
                case REST:
                    args = Integer.parseInt(msg[1]);
                    break;
                default:
                    break;
            }
            
            return new FtpParams(cmd, args);
        }catch(Exception e){
            System.out.println("Error occurred: " + e.getMessage());
            return new FtpParams(FtpCommand.NOOP, null);
        }

    }
}
