/*
 * @Description: 文件缓存池
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 02:33:34
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 06:04:11
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.file;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import top.cairbin.ftp.server.lock.RwLock;

public class FileManager implements IFileManager{
    // 文件，键是目录
    private HashMap<String, RwLock<FileHandler>> files;
    
    // HashMap的互斥锁
    private final ReentrantLock lock = new ReentrantLock();

    public FileManager(){
        this.files = new HashMap<>();
    }

    @Override
    public RwLock<FileHandler> createFile(String path) throws Exception {
        lock.lock();
        try{
            if(this.files.containsKey(path))
                return this.files.get(path);
            
            File file = new File(path);
            if(file.exists()){
                RwLock<FileHandler> temp = new RwLock(new FileHandler(path));
                this.files.put(path, temp);
                return temp;
            }

            if(!file.createNewFile()){
                throw new Error("Failed to create file: " + path);
            }else{
                RwLock<FileHandler> temp = new RwLock(new FileHandler(path));
                this.files.put(path, temp);
                return temp;
            }

        }finally{
            lock.unlock();
        }
    }

    @Override
    public boolean isExist(String path) {
        lock.lock();
        try{
            if(this.files.containsKey(path))
                return true;
            File file = new File(path);
            if(file.exists()){
                this.files.put(path, new RwLock(new FileHandler(path)));
                return true;
            }else{
                return false;
            }
        }finally{
            lock.unlock();
        }
    }

    @Override
    public RwLock<FileHandler> get(String path) throws Exception {
        lock.lock();
        try{
            if(this.files.containsKey(path))
                return this.files.get(path);
            
            File file = new File(path);
            if(file.exists()){
                RwLock<FileHandler> temp = new RwLock(new FileHandler(path));
                this.files.put(path, temp);
                return temp;
            }

            throw new Error("File not found: " + path);
        }finally{
            lock.unlock();
        }
    }

    
}
