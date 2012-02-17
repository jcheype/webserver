package com.jcheype.webServer;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 10/20/11
 */
public class SimpleRestHandler implements RestHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRestHandler.class);

    private final ConcurrentHashMap<HttpMethod, List<Route>> routeMap = new ConcurrentHashMap<HttpMethod, List<Route>>();

    public SimpleRestHandler before(Route route) {
        getRouteList(beforeFilter).add(route);
        return this;
    }

    public SimpleRestHandler after(Route route) {
        getRouteList(afterFilter).add(route);
        return this;
    }

    public SimpleRestHandler get(Route route) {
        getRouteList(HttpMethod.GET).add(route);
        return this;
    }

    public SimpleRestHandler post(Route route) {
        getRouteList(HttpMethod.POST).add(route);
        return this;
    }

    public SimpleRestHandler delete(Route route) {
        getRouteList(HttpMethod.DELETE).add(route);
        return this;
    }

    public SimpleRestHandler put(Route route) {
        getRouteList(HttpMethod.PUT).add(route);
        return this;
    }

    @Override
    public List<Route> getRouteList(HttpMethod method) {
        List<Route> routeList = routeMap.get(method);
        if (routeList == null) {
            routeMap.putIfAbsent(method, new ArrayList<Route>());
            routeList = routeMap.get(method);
        }
        return routeList;
    }
}
