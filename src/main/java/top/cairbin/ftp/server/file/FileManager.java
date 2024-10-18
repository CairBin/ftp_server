/*
 * @Description: 文件缓存池
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 02:33:34
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 21:33:52
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.file;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.Singleton;

import top.cairbin.ftp.server.lock.RwLock;

@Singleton
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
            File file = new File(path);
            boolean isExist = file.exists();
            if(isExist && this.files.containsKey(path))
                return this.files.get(path);

            if(isExist && !this.files.containsKey(path)){
                this.files.put(path, new RwLock<FileHandler>(new FileHandler(path)));
                return this.files.get(path);
            }

            if(!isExist && this.files.containsKey(path)){
                this.files.remove(path);
            }

            boolean flag = file.createNewFile();
            if(!flag) throw new Exception("Could not create file");
            return this.files.get(path);

        }finally{
            lock.unlock();
        }
    }

    @Override
    public boolean isExist(String path) {
        lock.lock();
        try{
            File file = new File(path);
            boolean isExist = file.exists();
            if(isExist && this.files.containsKey(path))
                return true;

            if(isExist && !this.files.containsKey(path)){
                this.files.put(path, new RwLock<FileHandler>(new FileHandler(path)));
                return true;
            }

            if(!isExist && this.files.containsKey(path)){
                this.files.remove(path);
            }

            return false;
        }finally{
            lock.unlock();
        }
    }

    @Override
    public RwLock<FileHandler> get(String path) throws Exception {
        lock.lock();
        try{
            return createFile(path);
        }finally{
            lock.unlock();
        }
    }

    @Override
    public boolean move(String oldPath, String newPath) throws Exception {
        lock.lock();
        try{
            if(this.files.containsKey(oldPath)){
                var rw = this.files.get(oldPath);
                rw.write(writer->{
                    this.files.remove(oldPath);
                });
            }
            
            if(this.files.containsKey(newPath)){
                var rw = this.files.get(newPath);
                rw.write(writer->{
                    this.files.remove(newPath);
                });
            }

            File file = new File(oldPath);
            if(!file.exists()){
                throw new Error("File not found: " + oldPath);
            }

            if(!file.renameTo(new File(newPath)))
                return false;
            this.files.put(newPath, new RwLock(new FileHandler(newPath)));
            return true;
            
        }finally{
            lock.unlock();
        }
    }

    @Override
    public boolean deleteFile(String path) throws Exception {
        lock.lock();
        try{
            if(this.files.containsKey(path)){
                var rw = this.files.get(path);
                rw.write(writer->{
                    this.files.remove(path);
                });
            }

            File file = new File(path);
            return file.delete();

        }finally{
            lock.unlock();
        }
    }

    
}
