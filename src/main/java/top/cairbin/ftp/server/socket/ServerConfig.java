/*
 * @Description: 服务器参数配置
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-19 09:23:10
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-19 09:25:55
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfig {
    public int port;
    public String rootDirectory;
    public boolean allowAnonymous;
    public int pasvPort;
    public String pasvAddress;
    public HashMap<String, UserConfig> userConfigs;
}
