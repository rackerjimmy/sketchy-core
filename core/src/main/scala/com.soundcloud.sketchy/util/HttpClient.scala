package com.soundcloud.sketchy.util

import java.io.{ IOException, ByteArrayInputStream }
import java.net.URL
import uk.co.bigbeeconsultants.http.{ HttpClient => BeeHttpClient, Config }
import uk.co.bigbeeconsultants.http.header._
import uk.co.bigbeeconsultants.http.request.RequestBody

import com.soundcloud.sketchy.monitoring.Instrumented

/**
 * HttpClient wrapper to reduce complexity and add monitoring
 */
class HttpClient(
  name: String,
  followRedirect: Boolean = false)
  extends BeeHttpClient(Config(
    followRedirects = followRedirect,
    keepAlive = true,
    connectTimeout = 3000,
    readTimeout = 5000)) with Instrumented with Logging {

  def metricsTypeName: String = name
  def metricsSubtypeName: Option[String] = Some("http")

  /*
   * POST request for given URL and body; if json is true, the body is
   * considered to be in JSON format, otherwise plain text. The HTTP status
   * code and the response body are returned.
   */
  def post(url: String, body: String, json: Boolean): (Int, String) = {
    val (mediaType, header) =
      if (json) (MediaType.APPLICATION_JSON, "application/json")
      else (MediaType.TEXT_PLAIN, "text/plain")

    try {
      val resp = super.post(
        url = new URL(url),
        body = Some(RequestBody(body, mediaType)),
        requestHeaders = Headers(HeaderName.ACCEPT -> header))
      meter("post", resp.status.code)
      (resp.status.code, resp.body.asString)
    } catch {
      case e: IOException =>
        log.error(e, "could not execute post request to <%s>, body: %s".format(url, body))
        meter("post", 0)
        (0, "")
    }
  }

  /*
   * PUT request for given URL and body; if json is true, the body is
   * considered to be in JSON format, otherwise plain text. The HTTP status
   * code and the response body are returned.
   */
  def put(url: String, body: String, json: Boolean): (Int, Array[Byte]) = {
    val (mediaType, header) =
      if (json) (MediaType.APPLICATION_JSON, "application/json")
      else (MediaType.TEXT_PLAIN, "text/plain")

    try {
      val resp = super.put(
        url = new URL(url),
        body = RequestBody(body, mediaType),
        requestHeaders = Headers(HeaderName.ACCEPT -> header))
      meter("put", resp.status.code)
      (resp.status.code, resp.body.asBytes)
    } catch {
      case e: IOException =>
        log.error(e, "could not execute put request")
        meter("put", 0)
        (0, Array[Byte]())
    }
  }

  /*
   * GET request for given URL. The HTTP status code and the response body
   * are returned.
   */
  def get(url: String): (Int, Array[Byte]) = {
    try {
      val resp = super.get(new URL(url))
      meter("get", resp.status.code)
      (resp.status.code, resp.body.asBytes)
    } catch {
      case e: IOException =>
        log.error(e, "could not execute get request")
        meter("get", 0)
        (0, Array[Byte]())
    }
  }

  // metrics setup
  private val counter = prometheusCounter("client", "request", "status")

  private def meter(request: String, status: Int) {
    counter.newPartial()
      .labelPair("client", metricsTypeName)
      .labelPair("request", request)
      .labelPair("status", status.toString)
      .apply().increment()
  }
}
