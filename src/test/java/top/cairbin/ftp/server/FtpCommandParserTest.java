/*
 * @Description: FTP命令解析测试
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 01:38:06
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 01:48:55
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;
import static org.junit.Assert.*;

import org.junit.Test;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.utils.*;

public class FtpCommandParserTest {
    private ILogger logger;

    public FtpCommandParserTest() {
        this.logger = InjectorFactory.getInjector().getInstance(ILogger.class);
    }

    @Test
    public void testParser(){
        String str = "PORT 192,168,1,10,12,34";
        var params = FtpCommandParser.parse(str);
        if(params.cmd != FtpCommand.PORT || params.args == null)
            throw new Error("Error parsing");
        
        assertEquals((String)params.args, "192.168.1.10:3106");
    }
}
