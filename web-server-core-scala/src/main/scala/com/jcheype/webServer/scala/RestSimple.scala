package com.jcheype.webServer.scala

import com.jcheype.webServer._
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors
import com.jcheype.webServer.Route
import java.net.InetSocketAddress
import javax.ws.rs.HttpMethod
import util.DynamicVariable
import org.jboss.netty.channel.Channel

/**
 * Created with IntelliJ IDEA.
 * User: juliencheype
 * Date: 31/5/13
 * Time: 00:47
 * To change this template use File | Settings | File Templates.
 */

object RestSimple {
  var optChannel:Option[Channel] = None

  def start(port:Int, restSimples:RestSimple*) {
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()))

    val restHandler = new SimpleRestHandler
    restSimples.foreach(rs => restHandler.add(rs.restHandler))

    val defaultChannelHandler = new HttpApiServerHandler(restHandler)
    bootstrap.setPipelineFactory(new WebServerPipelinefactory(defaultChannelHandler))

    optChannel = Some(bootstrap.bind(new InetSocketAddress(port)))
  }

  def stop(){
    optChannel match {
      case Some(channel) if channel.isOpen => channel.close
      case _ =>
    }
  }

}

trait RestSimple {
  val restHandler = new SimpleRestHandler

  protected val _request = new DynamicVariable[Request](null)
  protected val _response = new DynamicVariable[Response](null)

  def request: Request = {
    _request.value
  }

  def response: Response = {
    _response.value
  }

  def get(routeString: String)(action: => Any) {
    addRoute(HttpMethod.GET, routeString, action)
  }

  def post(routeString: String)(action: => Any) {
    addRoute(HttpMethod.POST, routeString, action)
  }

  def put(routeString: String)(action: => Any) {
    addRoute(HttpMethod.PUT, routeString, action)
  }

  def delete(routeString: String)(action: => Any) {
    addRoute(HttpMethod.DELETE, routeString, action)
  }

  def addRoute(method: String, routeString: String, action: => Any) {
    val route = new Route(routeString) {
      override def handle(requestJ: Request, responseJ: Response, _map: java.util.Map[String, String]) {
        _request.withValue(requestJ) {
          _response.withValue(responseJ) {
            action
          }
        }
      }
    }

    method match {
      case HttpMethod.GET     => restHandler.get(route)
      case HttpMethod.POST    => restHandler.post(route)
      case HttpMethod.PUT     => restHandler.put(route)
      case HttpMethod.DELETE  => restHandler.delete(route)
      case _ => throw new UnsupportedOperationException
    }

  }
}
