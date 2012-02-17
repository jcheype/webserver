package com.jcheype.webServer;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/3/12
 */
public class WebServer {
    private final int port;
    private ServerBootstrap bootstrap=null;

    public WebServer(int port) {
        this.port = port;
    }

    public void init() {
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new WebServerPipelinefactory(new HttpApiServerHandler(new SimpleRestHandler())));

        bootstrap.bind(new InetSocketAddress(port));
    }


    public void stop(){
        if(bootstrap != null){
        }
    }

}
