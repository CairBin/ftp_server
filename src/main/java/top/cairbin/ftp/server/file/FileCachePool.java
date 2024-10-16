/*
 * @Description: 文件缓存池
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 02:33:34
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 03:37:38
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.file;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import top.cairbin.ftp.server.lock.RwLock;

public class FileCachePool implements IFileCachePool{
    // 最大打开文件数
    private final int maxSize;

    // 已经打开的文件，键是目录
    private HashMap<String, RwLock<File>> files;
    
    // HashMap的互斥锁
    private final ReentrantLock lock = new ReentrantLock();

    public FileCachePool(int maxSize){
        this.maxSize = maxSize;
        this.files = new HashMap<>();
    }

    public FileCachePool(){
        this(10);
    }

    public boolean createFile(String path) throws Exception{
        if(this.files.containsKey(path)) return false;

        lock.lock();
        File file = new File(path);
        if(file.exists()){
            lock.unlock();
            return false;
        }

        try{
            if(file.createNewFile()){
                this.files.put(path, new RwLock<>(file));
                lock.unlock();
                return true;
            }
        }catch(Exception e){
            lock.unlock();
            throw e;
        }

        lock.unlock();
        return false;
    }

    public boolean isExist(String path){
        if(this.files.containsKey(path))
            return true;
        File file = new File(path);
        return file.exists();
    }

    public boolean has(String path){
        return this.files.containsKey(path);
    }

    public RwLock<File> get(String path) throws Exception{
        lock.lock();
        if(this.has(path)){
            RwLock<File> file = this.files.get(path);
            lock.unlock();
            return file;
        }

        if(!isExist(path)){
            lock.unlock();
            throw new Exception("File not found: " + path);
        }

        RwLock<File> file = new RwLock(new File(path));
        if(files.size() == this.maxSize){
            this.files.clear();
        }
        this.files.put(path, file);
        lock.unlock();
        return file;
    }

    public void remove(String path){
        lock.lock();
        if(this.has(path)){
            this.files.remove(path);
        }
        lock.unlock();
    }

}
