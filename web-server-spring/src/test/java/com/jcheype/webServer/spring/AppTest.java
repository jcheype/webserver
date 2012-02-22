package com.jcheype.webServer.spring;

import com.google.common.io.ByteStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/application-context.xml"})
public class AppTest {
    private DefaultHttpClient httpClient = new DefaultHttpClient();


    @Autowired
    SpringWebServer springWebServer;

    @Before
    public void setUp() throws Exception {
        springWebServer.setPort(9999);
        springWebServer.start();
    }

    @After
    public void tearDown() throws Exception {
        springWebServer.stop();
    }

    @Test
    public void pojo_get() throws Exception {
        HttpGet httpGet = new HttpGet("http://localhost:9999/toto/foo/bar");
        HttpResponse response = httpClient.execute(httpGet);
        String s = new String(ByteStreams.toByteArray(response.getEntity().getContent()));
        assertEquals(200, response.getStatusLine().getStatusCode());
    }
}
