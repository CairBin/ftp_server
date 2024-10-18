/*
 * @Description: 测试路径合法验证器
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-18 01:34:54
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 01:44:47
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import top.cairbin.ftp.server.utils.PathValidator;

public class PathValidatorTest {
    @Test
    public void testIsValid() throws IOException{
        PathValidator validator = new PathValidator("temp/valid_test");
        String validPath = "test/../";
        String invalidPath = "test/../..";
        assertTrue(validator.isValid(validPath));
        assertTrue(!validator.isValid(invalidPath));
    }
}
