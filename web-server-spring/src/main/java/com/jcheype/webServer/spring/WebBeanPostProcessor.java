package com.jcheype.webServer.spring;

import com.jcheype.webServer.PojoRegister;
import com.jcheype.webServer.SimpleRestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/6/12
 */
@Component
public class WebBeanPostProcessor implements BeanPostProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SimpleRestHandler simpleRestHandler;

    @Autowired
    private PojoRegister pojoRegister;

    @Override
    public Object postProcessBeforeInitialization(Object pojo, String s) throws BeansException {
        return pojo;
    }

    @Override
    public Object postProcessAfterInitialization(Object pojo, String s) throws BeansException {
        pojoRegister.register(pojo, simpleRestHandler);
        return pojo;
    }

    
}
