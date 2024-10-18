/*
 * @Description: 程序入口
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 00:10:09
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 09:50:48
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import top.cairbin.ftp.server.socket.Server;

public class App 
{
    public static void main( String[] args )
    {
        var injector = InjectorFactory.getInjector();
        Server server = injector.getInstance(Server.class);
        try {
            server.createSocket(21);
            server.setRoot("temp");
            server.setPasvIp("127.0.0.1");
            server.setPasvPort(20);
            server.listener();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
