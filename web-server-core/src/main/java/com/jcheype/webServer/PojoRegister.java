package com.jcheype.webServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 17/3/12
 */
public class PojoRegister {
    private static final Logger logger = LoggerFactory.getLogger(PojoRegister.class);

    public void register(Object pojo, SimpleRestHandler restHandler) {
        Path objectPath = pojo.getClass().getAnnotation(Path.class);
        String rootPath = "";
        if (objectPath != null && objectPath.value() != null) {
            rootPath = objectPath.value();
        }

        for (Method m : pojo.getClass().getMethods()) {
            Path methodPath = m.getAnnotation(Path.class);
            if (methodPath != null || objectPath != null) {
                String currentPath = methodPath != null ? rootPath + methodPath.value() : rootPath;

                Route route = createRoute(currentPath, pojo, m);


                GET getAnnotation = m.getAnnotation(GET.class);
                POST postAnnotation = m.getAnnotation(POST.class);
                PUT putAnnotation = m.getAnnotation(PUT.class);
                DELETE deleteAnnotation = m.getAnnotation(DELETE.class);

                if (getAnnotation != null)
                    restHandler.get(route);

                if (postAnnotation != null)
                    restHandler.post(route);

                if (putAnnotation != null)
                    restHandler.put(route);

                if (deleteAnnotation != null)
                    restHandler.delete(route);

                //DEFAULT to GET
                if(methodPath != null &&
                        getAnnotation == null &&
                        postAnnotation == null &&
                        putAnnotation == null &&
                        deleteAnnotation == null
                        ){
                    restHandler.get(route);
                }
            }
        }
    }

    private Route createRoute(String path, final Object pojo, final Method method) {

        logger.debug("new ROUTE: {}", path);
        return new TemplatePojoRoute(path, pojo, method);
    }
}
