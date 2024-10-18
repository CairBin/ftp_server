/*
 * @Description: 校验路径合法性
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-18 01:30:51
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 03:27:17
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathValidator {
    private final Path rootDir;

    public PathValidator(String rootDir) throws IOException {
        this.rootDir = Paths.get(rootDir).toRealPath();
    }

    /**
     * @description: 获取真实路径
     * @return {*}
     */    
    public String getRealPath(){
        return rootDir.toString();
    }

    /**
     * @description: 获取Path对象
     * @return {*}
     */
    public Path getPathObject(){
        return this.rootDir;
    }

    /**
     * @description: 比较targer是否在rootDir内部
     * @param {String} targetPath
     * @return {*}
     * @throws IOException 
     */
    public boolean isValid(String targetPath) throws IOException{

        // 获取目标路径的真实、规范化路径
        Path target = Paths.get(targetPath).normalize();

        // 将根目录和目标路径结合成一个绝对路径
        Path fullPath = rootDir.resolve(target).normalize();

        // 检查目标路径是否以根目录开头
        return fullPath.startsWith(rootDir);

    }
    
}
