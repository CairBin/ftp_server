/*
 * @Description: 文件操作
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 05:44:51
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 06:04:39
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import lombok.Getter;

@Getter
public class FileHandler {
    private String path;
    private BufferedReader reader;
    private BufferedWriter writer;

    public FileHandler(String path){
        this.path = path;
        try{
            this.reader = new BufferedReader(new FileReader(path));
            this.writer = new BufferedWriter(new FileWriter(path));
        }catch(Exception e){
            
        }
        
    }
}
