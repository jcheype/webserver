package com.jcheype.webServer;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/3/12
 */
public class WebServerPipelinefactory implements ChannelPipelineFactory {
    private final Timer timer = new HashedWheelTimer();

    private final ChannelHandler delfaultChannelHandler;
    private final HttpStaticFileServerHandler httpStaticFileServerHandler = new HttpStaticFileServerHandler(".", "/static/");

    public WebServerPipelinefactory(ChannelHandler delfaultChannelHandler) {
        this.delfaultChannelHandler = delfaultChannelHandler;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //pipeline.addLast("ssl", new SslHandler(engine));
        pipeline.addLast("timeout", new IdleStateHandler(timer, 0, 0, 20));

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));

        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("deflater", new HttpContentCompressor());
        pipeline.addLast("static", httpStaticFileServerHandler);

        pipeline.addLast("default", delfaultChannelHandler);
        return pipeline;
    }
}
