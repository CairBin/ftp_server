/*
 * @Description: Injector的静态工厂和依赖注入配置
 * @License: MIT License
 * @Author: Xinyi Liu(CairBin)
 * @version: 1.0.0
 * @Date: 2024-10-17 00:36:55
 * @LastEditors: Xinyi Liu(CairBin)
 * @LastEditTime: 2024-10-21 02:14:17
 * @Copyright: Copyright (c) 2024 Xinyi Liu(CairBin)
 */
package top.cairbin.ftp.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import top.cairbin.ftp.server.file.FileManager;
import top.cairbin.ftp.server.file.IFileManager;
import top.cairbin.ftp.server.logger.ILogger;
import top.cairbin.ftp.server.logger.Log4j;
import top.cairbin.ftp.server.thread.IThreadPool;
import top.cairbin.ftp.server.thread.ThreadPool;
import top.cairbin.ftp.server.socket.Server;
import top.cairbin.ftp.server.socket.ServerBuilder;
import top.cairbin.ftp.server.socket.UserConfig;
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
import top.cairbin.ftp.server.utils.FtpCommand;

class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IThreadPool.class)
                .to(ThreadPool.class);
                MapBinder<FtpCommand, IHandler> handlerMapBinder = MapBinder.newMapBinder(binder(), FtpCommand.class,
                IHandler.class);
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
        // 同PASV
        handlerMapBinder.addBinding(FtpCommand.LPSV).to(PasvHandler.class);
    }

    @Provides
    @Singleton
    private ILogger getLogger() {
        return new Log4j();
    }

    @Provides
    @Singleton
    private IFileManager getFileManager() {
        return new FileManager();
    }

    @Provides
    @Singleton
    private Config getConfig(ILogger logger){
        String ftpConfigPath = System.getenv("FTP_SERVER_CONFIG");
        if(ftpConfigPath == null || ftpConfigPath.isEmpty()){
            logger.info("Not found FTP_SERVER_CONFIG.");
            logger.info("Loading default configuration...");
            return ConfigFactory.load("ftp");
        }else{
            logger.info("Loding configuration: " + ftpConfigPath);
            Path path = Paths.get(ftpConfigPath);
            return ConfigFactory.parseFile(path.toFile());
        }
    }

    @Provides
    @Singleton
    private Server getFtpServer(IThreadPool pool, ILogger logger, Config config) throws IOException {
        ServerBuilder builder = new ServerBuilder();
        builder.setAllowAnonymous(config.getBoolean("app.server.allowAnonymous"));
        builder.setPort(config.getInt("app.server.port"));
        builder.setPasvPort(config.getInt("app.server.pasvPort"));
        builder.setPasvAddress(config.getString("app.server.pasvAddress"));
        builder.setRootDirectory(config.getString("app.server.rootDirectory"));
        builder.setLogger(logger);
        builder.setThreadPool(pool);
        var userSet = config.getConfig("app.users").root().keySet();
        for (String username : userSet) {
            UserConfig user = new UserConfig();
            user.setUsername(username);
            user.setToken(config.getString("app.users." + username + ".password"));
            builder.setUser(user);
        }

        return builder.build();
    }

}


public class InjectorFactory {
    private static Injector injector = Guice.createInjector(new AppModule());

    public static Injector getInjector() {
        return injector;
    }

}
