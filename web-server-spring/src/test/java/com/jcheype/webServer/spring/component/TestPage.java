package com.jcheype.webServer.spring.component;

import com.jcheype.webServer.Request;
import com.jcheype.webServer.Response;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/6/12
 */
@Component
public class TestPage {
    Random rand = new Random();
    @Path("/toto/:titi/:tutu")
    @GET
    @Produces("text/html")
    public Map methodToto(Request request, Response response, String titi, String tutu){
        Map<String, String> map = new HashMap<String, String>();
        map.put("titi",Long.toString(rand.nextLong()));
        map.put("tutu","lol");
        map.put("layout", "test.haml");
        return map;
    }
}
