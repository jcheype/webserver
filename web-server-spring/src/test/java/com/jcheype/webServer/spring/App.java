package com.jcheype.webServer.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 */
public class App {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClassPathXmlApplicationContext ap;

    public App(String[] configs) {
        ap = new ClassPathXmlApplicationContext(configs);
        SpringWebServer springWebServer = ap.getBean(SpringWebServer.class);
        logger.debug("STARTING");
        springWebServer.setPort(9999);
        springWebServer.start();
    }

    public static void main(String[] args) throws InterruptedException {
        new App(new String[]{"/application-context.xml"});
        Thread.sleep(Long.MAX_VALUE);
    }
}
