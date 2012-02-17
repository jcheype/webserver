/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.jcheype.webServer;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple handler that serves incoming HTTP requests to send their respective
 * HTTP responses.  It also implements {@code 'If-Modified-Since'} header to
 * take advantage of browser cache, as described in
 * <a href="http://tools.ietf.org/html/rfc2616#section-14.25">RFC 2616</a>.
 * <p/>
 * <h3>How Browser Caching Works</h3>
 * <p/>
 * Web browser caching works with HTTP headers as illustrated by the following
 * sample:
 * <ol>
 * <li>Request #1 returns the content of <code>/file1.txt</code>.</li>
 * <li>Contents of <code>/file1.txt</code> is cached by the browser.</li>
 * <li>Request #2 for <code>/file1.txt</code> does return the contents of the
 * file again. Rather, a 304 Not Modified is returned. This tells the
 * browser to use the contents stored in its cache.</li>
 * <li>The server knows the file has not been modified because the
 * <code>If-Modified-Since</code> date is the same as the file's last
 * modified date.</li>
 * </ol>
 * <p/>
 * <pre>
 * Request #1 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 *
 * Response #1 Headers
 * ===================
 * HTTP/1.1 200 OK
 * Date:               Tue, 01 Mar 2011 22:44:26 GMT
 * Last-Modified:      Wed, 30 Jun 2010 21:36:48 GMT
 * Expires:            Tue, 01 Mar 2012 22:44:26 GMT
 * Cache-Control:      private, max-age=31536000
 *
 * Request #2 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
 *
 * Response #2 Headers
 * ===================
 * HTTP/1.1 304 Not Modified
 * Date:               Tue, 01 Mar 2011 22:44:28 GMT
 *
 * </pre>
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://www.veebsbraindump.com/">Veebs</a>
 */
public class HttpStaticFileServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(HttpStaticFileServerHandler.class);

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    private final String PATH_FILES;
    private final String PATH_URL;

    public HttpStaticFileServerHandler(String pathFiles, String pathUrl) {
        PATH_FILES = pathFiles;
        PATH_URL = pathUrl;
        logger.info("serving static path: " + PATH_FILES);
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (!(e instanceof MessageEvent)) {
            ctx.sendUpstream(e);
            return;
        }
        Object message = ((MessageEvent) e).getMessage();
        if (!(message instanceof HttpRequest) || !((HttpRequest) message).getUri().startsWith(PATH_URL)) {
            ctx.sendUpstream(e);
            return;
        }

        HttpRequest request = (HttpRequest) message;

        QueryStringDecoder qs = new QueryStringDecoder(request.getUri());

        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        final String path = sanitizeUri(qs.getPath());
        if (path == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        boolean toBeCached = false;
        File file = new File(path);

        if (file.isDirectory())
            file = new File(file, "index.html");

        if (!file.exists()) {
            String newPath = path.replaceAll("_[0-9]+(\\.\\w+)$", "$1");
            logger.debug("newPath: {}", newPath);
            file = new File(newPath);
            toBeCached = true;
        }

        logger.debug("search file: {}", file.getAbsolutePath());
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.equals("")) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client does not have milliseconds 
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }

        RandomAccessFile raf;
        boolean isGz = false;
        try {
            File fileGz = new File(file.getAbsolutePath() + ".gz");
            logger.debug("searching gzip: {}", fileGz.getAbsolutePath());
            String acceptHeader = request.getHeader(Names.ACCEPT_ENCODING);
            if (fileGz.isFile() && acceptHeader != null && acceptHeader.contains("gzip")) {
                isGz = true;
                raf = new RandomAccessFile(fileGz, "r");
            } else
                raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file, toBeCached);
        if (isGz)
            response.setHeader(Names.CONTENT_ENCODING, "gzip");

        Channel ch = ctx.getChannel();//e.getChannel();

        // Write the initial line and the header.
        ch.write(response);

        // Write the content.
        ChannelFuture writeFuture;
        if (ch.getPipeline().get(SslHandler.class) != null) {
            // Cannot use zero-copy with HTTPS.
            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
        } else {
            // No encryption - use zero-copy.
            final FileRegion region =
                    new DefaultFileRegion(raf.getChannel(), 0, fileLength);
            writeFuture = ch.write(region);
            writeFuture.addListener(new ChannelFutureProgressListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    region.releaseExternalResources();
                }

                @Override
                public void operationProgressed(
                        ChannelFuture future, long amount, long current, long total) {
                    System.out.printf("%s: %d / %d (+%d)%n", path, current, total, amount);
                }
            });
        }

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            try{
            sendError(ctx, BAD_REQUEST);
            return;
            }catch (Throwable ex){
                logger.error("cannot send error: ",ex);
            }
        }

        cause.printStackTrace();
//        if (ch.isConnected()) {
//            sendError(ctx, INTERNAL_SERVER_ERROR);
//        }
    }

    private String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") ||
                uri.contains("." + File.separator) ||
                uri.startsWith(".") || uri.endsWith(".")) {
            return null;
        }

        // Convert to absolute path.
        return PATH_FILES + File.separator + uri;
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        try {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.setContent(ChannelBuffers.copiedBuffer(
                    "Failure: " + status.toString() + "\r\n",
                    CharsetUtil.UTF_8));

            // Close the connection as soon as the error message is sent.
            ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            logger.error("cannot send Error", e);
        }
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx Context
     */
    private void sendNotModified(ChannelHandlerContext ctx) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }


    private void setDateHeader(HttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }

    private void setDateAndCacheHeaders(HttpResponse response, File fileToCache, boolean isCached) {
        int httpCacheSeconds = HTTP_CACHE_SECONDS;
        if (isCached)
            httpCacheSeconds = HTTP_CACHE_SECONDS * 60 * 24 * 365;

        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, httpCacheSeconds);
        response.setHeader(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
        response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + httpCacheSeconds);
        response.setHeader(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }


    private void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String name = file.getName();
        String mime = mimeTypesMap.getContentType(name);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, mime);
    }

}