package com.jcheype.webServer.scala

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.http.client.methods.HttpGet
import org.apache.http.HttpResponse
import org.apache.http.impl.client.DefaultHttpClient
import org.scalatest.matchers.ShouldMatchers

/**
 * Created with IntelliJ IDEA.
 * User: juliencheype
 * Date: 31/5/13
 * Time: 22:34
 */
class RestSimpleTest extends FunSuite with BeforeAndAfter with ShouldMatchers with RestSimple{
  val httpclient:DefaultHttpClient = new DefaultHttpClient


  before {
    RestSimple.start(9999, this)
  }

  test("webserver test"){
    val httpGet:HttpGet = new HttpGet("http://localhost:9999/test")
    val response:HttpResponse = httpclient.execute(httpGet)

    val status = response.getStatusLine.getStatusCode

    status should be === (200)

  }

  get("/test"){
    response.write("hello")
  }
}
