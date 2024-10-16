package top.cairbin.ftp.server.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FtpParams {
    public FtpCommand cmd;
    public Object args;

    public FtpParams(FtpCommand cmd, Object args) {
        this.cmd = cmd;
        this.args = args;
    }
}
