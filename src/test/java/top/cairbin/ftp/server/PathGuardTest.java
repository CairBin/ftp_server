/*
 * @Description: PathGuard测试
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 16:06:52
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 16:10:29
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;
import top.cairbin.ftp.server.socket.PathGuard;

import java.io.IOException;

import org.junit.Test;

public class PathGuardTest {
    @Test
    public void testPathGuard() throws IOException{
        PathGuard pg = new PathGuard("guard_test/");

        pg.cwd("test");
        System.out.println("Current directory: " + pg.pwd());

        pg.cwd("..");
        System.out.println("Current directory: " + pg.pwd());
    }
}
