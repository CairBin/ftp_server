/*
 * @Description: Socket工具
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 07:20:04
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 07:40:05
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class SocketHelper {
    private Socket socket;

    public SocketHelper(Socket socket){
        this.socket = socket;
    }

    public void close() throws IOException{
        this.socket.close();
    }

    public boolean isClosed(){
        return this.socket.isClosed();
    }

    public BufferedReader getReader(String encode) throws UnsupportedEncodingException, IOException{
        return new BufferedReader(new InputStreamReader(
            this.socket.getInputStream(),
            encode
        ));
    }

    public BufferedWriter getWriter(String encode) throws UnsupportedEncodingException, IOException{
        return new BufferedWriter(new OutputStreamWriter(
            this.socket.getOutputStream(),
            encode
        ));
    }

    public String getRemoteAddress(){
        return this.socket.getRemoteSocketAddress().toString();
    }

    
}
