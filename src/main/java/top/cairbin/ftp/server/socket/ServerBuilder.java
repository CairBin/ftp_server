/*
 * @Description: Server builder
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-19 09:03:22
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 11:00:12
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.io.IOException;
import java.util.HashMap;

import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.thread.IThreadPool;

public class ServerBuilder {
    private int port;
    private String rootDirectory;
    private String pasvAddress;
    private int pasvPort;
    private HashMap<String, UserConfig> userConfigs;
    private boolean allowAnonymous = false;
    private ILogger logger;
    private IThreadPool threadPool;

    public ServerBuilder(){
        this.userConfigs = new HashMap<>();
    }

    public ServerBuilder setPort(int port){
        this.port = port;
        return this;
    }

    public ServerBuilder setRootDirectory(String dir){
        this.rootDirectory = dir;
        return this;
    }

    public ServerBuilder setAllowAnonymous(boolean allow){
        this.allowAnonymous = allow;
        return this;
    }

    public ServerBuilder setPasvAddress(String address){
        this.pasvAddress = address;
        return this;
    }

    public ServerBuilder setPasvPort(int port){
        this.pasvPort = port;
        return this;
    }

    public ServerBuilder setUser(UserConfig user){
        this.userConfigs.put(user.getUsername(), user);
        return this;
    }

    public ServerBuilder setLogger(ILogger logger){
        this.logger = logger;
        return this;
    }

    public ServerBuilder setThreadPool(IThreadPool pool){
        this.threadPool = pool;
        return this;
    }

    public Server build() throws IOException{
        ServerConfig config = new ServerConfig();
        config.port = this.port;
        config.rootDirectory = this.rootDirectory;
        config.pasvAddress = this.pasvAddress;
        config.pasvPort = this.pasvPort;
        config.userConfigs = this.userConfigs;
        config.allowAnonymous = this.allowAnonymous;
        if(logger == null && threadPool == null)
            return new Server(config);
        else
            return new Server(config, logger, threadPool);
    }

}
