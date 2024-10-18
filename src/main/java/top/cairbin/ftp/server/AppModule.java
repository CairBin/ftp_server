/*
 * @Description: 配置依赖注入
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 00:32:01
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-18 08:06:11
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import top.cairbin.ftp.server.file.FileManager;
import top.cairbin.ftp.server.file.IFileManager;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.logger.Log4j;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.handler.AbortHandler;
import top.cairbin.ftp.server.socket.handler.AlloHandler;
import top.cairbin.ftp.server.socket.handler.CdupHandler;
import top.cairbin.ftp.server.socket.handler.CwdHandler;
import top.cairbin.ftp.server.socket.handler.DeleHandler;
import top.cairbin.ftp.server.socket.handler.FeatHandler;
import top.cairbin.ftp.server.socket.handler.IHandler;
import top.cairbin.ftp.server.socket.handler.ListHandler;
import top.cairbin.ftp.server.socket.handler.MkdHandler;
import top.cairbin.ftp.server.socket.handler.NlistHandler;
import top.cairbin.ftp.server.socket.handler.NoopHandler;
import top.cairbin.ftp.server.socket.handler.PassHandler;
import top.cairbin.ftp.server.socket.handler.PasvHandler;
import top.cairbin.ftp.server.socket.handler.PortHandler;
import top.cairbin.ftp.server.socket.handler.PwdHandler;
import top.cairbin.ftp.server.socket.handler.QuitHandler;
import top.cairbin.ftp.server.socket.handler.RestHandler;
import top.cairbin.ftp.server.socket.handler.RetrHandler;
import top.cairbin.ftp.server.socket.handler.RmdHandler;
import top.cairbin.ftp.server.socket.handler.RnfrHandler;
import top.cairbin.ftp.server.socket.handler.RntoHandler;
import top.cairbin.ftp.server.socket.handler.StatHandler;
import top.cairbin.ftp.server.socket.handler.StorHandler;
import top.cairbin.ftp.server.socket.handler.SystHandler;
import top.cairbin.ftp.server.socket.handler.TypeHandler;
import top.cairbin.ftp.server.socket.handler.UserHandler;
import top.cairbin.ftp.server.thread.IThreadPool;
import top.cairbin.ftp.server.thread.ThreadPool;
import top.cairbin.ftp.server.utils.FtpCommand;

public class AppModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(IThreadPool.class)
            .to(ThreadPool.class);
        
        MapBinder<FtpCommand, IHandler> handlerMapBinder = 
            MapBinder.newMapBinder(binder(), FtpCommand.class, IHandler.class);
        handlerMapBinder.addBinding(FtpCommand.USER).to(UserHandler.class);
        handlerMapBinder.addBinding(FtpCommand.PASS).to(PassHandler.class);
        handlerMapBinder.addBinding(FtpCommand.ABOR).to(AbortHandler.class);
        handlerMapBinder.addBinding(FtpCommand.NOOP).to(NoopHandler.class);
        handlerMapBinder.addBinding(FtpCommand.QUIT).to(QuitHandler.class);
        handlerMapBinder.addBinding(FtpCommand.REST).to(RestHandler.class);
        handlerMapBinder.addBinding(FtpCommand.RNFR).to(RnfrHandler.class);
        handlerMapBinder.addBinding(FtpCommand.RNTO).to(RntoHandler.class);
        handlerMapBinder.addBinding(FtpCommand.SYST).to(SystHandler.class);
        handlerMapBinder.addBinding(FtpCommand.FEAT).to(FeatHandler.class);
        handlerMapBinder.addBinding(FtpCommand.LIST).to(ListHandler.class);
        handlerMapBinder.addBinding(FtpCommand.STOR).to(StorHandler.class);
        handlerMapBinder.addBinding(FtpCommand.NLIST).to(NlistHandler.class);
        handlerMapBinder.addBinding(FtpCommand.PASV).to(PasvHandler.class);
        handlerMapBinder.addBinding(FtpCommand.PORT).to(PortHandler.class);
        handlerMapBinder.addBinding(FtpCommand.TYPE).to(TypeHandler.class);
        handlerMapBinder.addBinding(FtpCommand.CWD).to(CwdHandler.class);
        handlerMapBinder.addBinding(FtpCommand.PWD).to(PwdHandler.class);
        handlerMapBinder.addBinding(FtpCommand.MKD).to(MkdHandler.class);
        handlerMapBinder.addBinding(FtpCommand.RMD).to(RmdHandler.class);
        handlerMapBinder.addBinding(FtpCommand.CDUP).to(CdupHandler.class);
        handlerMapBinder.addBinding(FtpCommand.DELE).to(DeleHandler.class);
        // 注意STOU由StorHandler处理， 在传入参数的时候修改UUID
        handlerMapBinder.addBinding(FtpCommand.STOU).to(StorHandler.class);
        handlerMapBinder.addBinding(FtpCommand.ALLO).to(AlloHandler.class);
        handlerMapBinder.addBinding(FtpCommand.STAT).to(StatHandler.class);
        // NLST 同 NLIST
        handlerMapBinder.addBinding(FtpCommand.NLST).to(NlistHandler.class);
        handlerMapBinder.addBinding(FtpCommand.RETR).to(RetrHandler.class);
        // 同MKD
        handlerMapBinder.addBinding(FtpCommand.XMKD).to(MkdHandler.class);
        // 同RMD
        handlerMapBinder.addBinding(FtpCommand.XRMD).to(RmdHandler.class);
        // 同PWD
        handlerMapBinder.addBinding(FtpCommand.XPWD).to(PwdHandler.class);
        bind(Server.class).in(Singleton.class);
    }   

    @Provides
    @Singleton
    private ILogger getLogger(){
        return new Log4j();
    }

    @Provides
    @Singleton
    private IFileManager getFileManager(){
        return new FileManager();
    }

}
