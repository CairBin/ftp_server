/*
 * @Description: 目录操作
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 15:55:50
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 00:51:42
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.socket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class PathGuard {
    private String root;  // 根目录
    private String pwd;   // 当前工作目录

    // 构造函数，初始化根目录并标准化路径
    public PathGuard(String root) throws IOException {
        Path rootPath = Paths.get(root).toRealPath();  // 获取绝对路径
        this.root = rootPath.toString();
        this.pwd = "";  // 初始工作目录为空
    }

    /**
     * @description: 更改当前工作目录
     * @param {String} path
     * @return {*}
     */    
    public void cwd(String path) throws IOException {
        // 如果是当前目录，则不做任何操作
        if (path.equals(".")) {
            return;
        }

        Path newPath;

        // 根据传入路径构造完整路径，支持相对路径和绝对路径
        if (path.startsWith("/")) {
            newPath = Paths.get(this.root).resolve(path.substring(1));
        } else {
            newPath = Paths.get(this.root).resolve(this.pwd).resolve(path);
        }

        // 检查路径是否存在
        if (!Files.exists(newPath)) {
            throw new IOException("Path not found: " + path);
        }

        // 标准化路径
        newPath = newPath.toRealPath();

        // 检查路径是否在根目录之下
        if (!newPath.startsWith(this.root)) {
            throw new IOException("Path not allowed: " + path);
        }

        // 更新当前工作目录，相对于根目录的相对路径
        this.pwd = rootRelativePath(newPath);
    }

    /**
     * @description: 获取当前工作目录
     * @return {*}
     */    
    public String pwd() {
        return this.pwd.isEmpty() ? "/" : this.pwd;
    }

    /**
     * @description: 将路径转化为相对于根目录的相对路径
     * @param {Path} path
     * @return {*}
     */    
    private String rootRelativePath(Path path) {
        Path relativePath = Paths.get(this.root).relativize(path);
        return relativePath.toString().replace("\\", "/");  // 处理Windows路径
    }

}