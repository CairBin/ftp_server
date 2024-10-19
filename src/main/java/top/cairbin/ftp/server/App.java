/*
 * @Description: 程序入口
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 00:10:09
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 11:45:21
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;
import com.google.inject.Injector;

import top.cairbin.ftp.server.socket.Server;

public class App 
{
    public static void main( String[] args )
    {
        Injector injector = InjectorFactory.getInjector();
        Server server = injector.getInstance(Server.class);
        try {
            server.initSocket();
            server.listener();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
