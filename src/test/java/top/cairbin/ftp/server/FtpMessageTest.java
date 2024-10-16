/*
 * @Description: file content
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 
 * @Date: 2024-10-17 03:45:11
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-17 06:05:30
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import static org.junit.Assert.assertEquals;
import top.cairbin.ftp.server.utils.FtpMessage;
import org.junit.Test;

public class FtpMessageTest {
    @Test
    public void fmtTest(){
        assertEquals(
            "220 Ftp server ready\r\n",
            FtpMessage.fmtMessage(220, "Ftp server ready")
        );
    }

    @Test
    public void ftpMsgTest(){
        FtpMessage msg = new FtpMessage(220, "Ftp server ready");
        assertEquals(
            "220 Ftp server ready\r\n",
            msg.toString()
        );
    }
}
