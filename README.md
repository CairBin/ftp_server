# ftp_server

## 描述

Java实现的FTP服务端。

## 功能

* 支持主动模式和被动模式：主动模式服务器连接客户端。被动模式返回给客户端固定好的端口号和地址，让客户端去连接服务器。（均为数据连接）

* 使用线程池进行优化

* Guice实现依赖注入

* 封装了Mutex和RwLock的包装类

* 工作目录、控制连接的端口号、被动模式的端口号与地址、用户名和口令等可通过配置文件配置

* 支持ASCII和BINARY两种模式

* 用户口令校验，可通过配置选择性开放匿名账户(anonymous)



* 对FTP客户端命令解析，实现FTP命令如下：

```java

public enum FtpCommand {
    // 接入命令
    USER,   // 用户标识符
    PASS,   // 登陆口令
    QUIT,   // 系统注销
    ABOR,   // 数据连接以及上次命令终止

    // 文件管理命令
    CWD,    // 改变到另一个目录
    CDUP,   // 改变到父目录
    DELE,   // 删除文件
    LIST,   // 列出子目录和文件
    NLST,   // 同NLIST
    NLIST,  // 列出子目录或其它属性文件
    MKD,    // 创建目录
    PWD,    // 显示当前目录
    RMD,    // 删除目录
    RNFR,   // 标志要重新命名的文件，参数为旧文件名
    RNTO,   // 重命名，参数为新文件名
    TYPE,   // 定义文件类型

    // 端口相关
    PORT,   // 客户端选择端口，主动模式
    PASV,   // 服务器选择端口，被动模式

    // 文件传输
    RETR,       // 下载文件
    STOR,       // 上传文件
    STOU,       // 上传文件，但是文件名必须唯一
    ALLO,       // 在服务器为文件分配存储空间
    STAT,       // 返回文件状态
    REST,       // 数据点给文件标记位置
    
    // 其他
    NOOP,             // 检查服务器是否工作
    SYST,              // 获取服务器所用OS
    FEAT,


    XMKD,
    XRMD,
    XPWD,
    LPSV
}
```

## 配置文件

配置文件名必须是`ftp`，扩展名可以是`.conf`或`.json`。

```conf
app{
    name = "ftp_server"
    version = "1.0"
    server = {
        port = 21
        pasvPort = 20
        pasvAddress = "127.0.0.1"
        rootDirectory = "temp",
        allowAnonymous = false
    }

    users = {
        cairbin = {
            password = "123456"
        }
    }
}
```


## 其他

开发测试环境为MacOS，支持文件、目录、主动/被动模式等基本功能测试正常。
Windows下仅支持少量命令，但基本功能可以使用。

引入的依赖库如下（并不一定完全）：

* Guice
* lombok
* log4j
* junit
* typesafe config