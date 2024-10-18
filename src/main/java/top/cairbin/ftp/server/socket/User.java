package top.cairbin.ftp.server.socket;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import top.cairbin.ftp.server.lock.Mutex;

@Getter
@Setter
public class User {
    public String username;
    public UserState state;
    public String address;
    public Mutex<Session> session;
    public TransferType transType;
    public PathGuard path;
    public User(String name, String address, String root) throws IOException{
        this.username = name;
        this.state = UserState.LOGGING;
        this.transType = TransferType.ASCII;
        this.session = null;
        this.path = new PathGuard(root);
    }

    public User( String address, String root) throws IOException{
        this("anonymous", address, root);
    }

    public void cwd(String path) throws IOException{
        this.path.cwd(path);
    }

    public String pwd(){
        return this.path.pwd().replaceFirst("/", "");
    }

    public String renderingPwd(){
        return this.path.pwd();
    }
}
