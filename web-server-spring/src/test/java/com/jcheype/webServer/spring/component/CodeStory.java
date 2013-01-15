package com.jcheype.webServer.spring.component;

import com.jcheype.webServer.Request;
import com.jcheype.webServer.Response;
import com.jcheype.webServer.ResponseBuilder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created with IntelliJ IDEA.
 * User: jcheype
 * Date: 04/01/13
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */

@Component
public class CodeStory {
    Logger logger = LoggerFactory.getLogger(CodeStory.class);
    @Path("/")
    @GET
    public String getEmail(Request request, Response response){
        logger.debug("request: " + request.getParam("q"));
        if("Quelle est ton adresse email".equals(request.getParam("q")))
            return "cheype@gmail.com";


        HttpResponse build = new ResponseBuilder()
                .setStatus(HttpResponseStatus.NO_CONTENT)
                .build();


        response.write(build);
        return null;
    }
}
