/*
 * @Description: 会话
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:59:29
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 06:09:42
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.net.Socket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Session {
    public Mode mode;
    public long totalSize;
    public long finishedSize;
    public String filename;
    public boolean finished;
    public boolean aborted;
    public long offset;
    public Socket socket;
    
    public Session(Mode mode, Socket socket){
        this.mode = mode;
        this.finished = false;
        this.aborted = false;
        this.offset = 0;
        this.totalSize = 0;
        this.finishedSize = 0;
        this.filename = null;
        this.socket = socket;
    }
}
