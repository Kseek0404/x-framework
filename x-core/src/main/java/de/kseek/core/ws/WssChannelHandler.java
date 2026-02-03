package de.kseek.core.ws;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class WssChannelHandler extends ChannelInitializer<SocketChannel> {

    private String sslKeyPath;
    private String sslKeyPwd;

    public WssChannelHandler() {
    }

    public WssChannelHandler(String sslKeyPath, String sslKeyPwd) {
        this.sslKeyPath = sslKeyPath;
        this.sslKeyPwd = sslKeyPwd;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // TODO Auto-generated method stub
//        SSLContext sslContext = createSSLContext("JKS", sslKeyPath, sslKeyPwd);
//        //SSLEngine 此类允许使用ssl安全套接层协议进行安全通信            
//        SSLEngine engine = sslContext.createSSLEngine();
//        engine.setUseClientMode(false);
//        ch.pipeline().addLast(new SslHandler(engine));
        ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0,30, TimeUnit.SECONDS));
        ch.pipeline().addLast("http-codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast("handler", new WebSocketServerHandler());
    }

    public static SSLContext createSSLContext(String type, String path, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(type); /// "JKS"
        InputStream ksInputStream = new FileInputStream(path); /// 证书存放地址
        ks.load(ksInputStream, password.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
