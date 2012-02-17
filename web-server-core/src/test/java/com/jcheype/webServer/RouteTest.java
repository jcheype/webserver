package com.jcheype.webServer;

import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/3/12
 */
public class RouteTest {
    
    @Test
    public void parse_route_and_check_params() {
        Route route = new Route("/test/:param1/:param2");
        Map<String,String> paramsMap = route.parse("/test/foo/bar", null);

        assertEquals("foo", paramsMap.get("param1"));
        assertEquals("bar", paramsMap.get("param2"));
    }

    @Test
    public void parse_bad_route(){
        Route route = new Route("/test/:param1/:param2");
        Map<String,String> paramsMap = route.parse("/foo/test/bar", null);

        assertNull(paramsMap);
    }

    @Test
    public void parse_with_content_type(){
        Route route = new Route("/test/:param1/:param2", "text/plain");
        Map<String,String> paramsMap = route.parse("/test/foo/bar", "text/plain");

        assertEquals("foo", paramsMap.get("param1"));
        assertEquals("bar", paramsMap.get("param2"));
    }

    @Test
    public void parse_no_content_type(){
        Route route = new Route("/test/:param1/:param2", null);
        Map<String,String> paramsMap = route.parse("/test/foo/bar", "text/json");

        assertEquals("foo", paramsMap.get("param1"));
        assertEquals("bar", paramsMap.get("param2"));
    }

    @Test
    public void parse_bad_content_type(){
        Route route = new Route("/test/:param1/:param2", "text/plain");
        Map<String,String> paramsMap = route.parse("/test/foo/bar", "text/json");
        assertNull(paramsMap);
    }
}
