package com.emarsys.scala.logging

import net.logstash.logback.marker.LogstashMarker
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.Marker

import scala.collection.JavaConverters._

class LoggingSpec extends WordSpec with Matchers with MockitoSugar {

  class LoggingTestScope {
    val loggerMock = mock[org.slf4j.Logger]
    val textMaxLength = Logger.TEXT_PARAMETER_MAX_LENGTH
    lazy val logger = new Logger("testService")(classOf[LoggingSpec]) {
      override protected lazy val logger = loggerMock
      override val textParameterMaxLength = textMaxLength
    }
  }

  "check is enabled - DEBUG" in new LoggingTestScope {
    when(loggerMock.isDebugEnabled()).thenReturn(false)
    logger.debug(LogParameter("Log"))

    verify(loggerMock, never).debug(any[Marker], any[String])
  }
  "check is enabled - INFO" in new LoggingTestScope {
    when(loggerMock.isInfoEnabled()).thenReturn(false)
    logger.info(LogParameter("Log"))

    verify(loggerMock, never).info(any[Marker], any[String])
  }
  "check is enabled - WARN" in new LoggingTestScope {
    when(loggerMock.isWarnEnabled()).thenReturn(false)
    logger.warn(LogParameter("Log"))

    verify(loggerMock, never).warn(any[Marker], any[String])
  }
  "check is enabled - ERROR" in new LoggingTestScope {
    when(loggerMock.isErrorEnabled()).thenReturn(false)
    logger.error(LogParameter("Log"))

    verify(loggerMock, never).error(any[Marker], any[String])
  }

  "message must be equal to logparam - DEBUG" in new LoggingTestScope {
    when(loggerMock.isDebugEnabled()).thenReturn(true)
    logger.debug(LogParameter("Log"))

    verify(loggerMock).debug(any[Marker], eqTo("Log"))
  }
  "message must be equal to logparam - INFO" in new LoggingTestScope {
    when(loggerMock.isInfoEnabled()).thenReturn(true)
    logger.info(LogParameter("Log"))

    verify(loggerMock).info(any[Marker], eqTo("Log"))
  }
  "message must be equal to logparam - WARN" in new LoggingTestScope {
    when(loggerMock.isWarnEnabled()).thenReturn(true)
    logger.warn(LogParameter("Log"))

    verify(loggerMock).warn(any[Marker], eqTo("Log"))
  }
  "message must be equal to logparam - ERROR" in new LoggingTestScope {
    when(loggerMock.isErrorEnabled()).thenReturn(true)
    logger.error(LogParameter("Log"))

    verify(loggerMock).error(any[Marker], eqTo("Log"))
  }

  "first marker should be service name - DEBUG" in new LoggingTestScope {
    when(loggerMock.isDebugEnabled()).thenReturn(true)
    logger.debug(LogParameter("Log"))

    val captor: ArgumentCaptor[Marker] = ArgumentCaptor.forClass(classOf[Marker])
    verify(loggerMock).debug(captor.capture(), any[String])
    captor.getValue.toString should be("serviceName=testService")
  }

  "first marker should be service name - INFO" in new LoggingTestScope {
    when(loggerMock.isInfoEnabled()).thenReturn(true)
    logger.info(LogParameter("Log"))

    val captor: ArgumentCaptor[Marker] = ArgumentCaptor.forClass(classOf[Marker])
    verify(loggerMock).info(captor.capture(), any[String])
    captor.getValue.toString should be("serviceName=testService")
  }

  "first marker should be service name - WARN" in new LoggingTestScope {
    when(loggerMock.isWarnEnabled()).thenReturn(true)
    logger.warn(LogParameter("Log"))

    val captor: ArgumentCaptor[Marker] = ArgumentCaptor.forClass(classOf[Marker])
    verify(loggerMock).warn(captor.capture(), any[String])
    captor.getValue.toString should be("serviceName=testService")
  }

  "first marker should be service name - ERROR" in new LoggingTestScope {
    when(loggerMock.isErrorEnabled()).thenReturn(true)
    logger.error(LogParameter("Log"))

    val captor: ArgumentCaptor[Marker] = ArgumentCaptor.forClass(classOf[Marker])
    verify(loggerMock).error(captor.capture(), any[String])
    captor.getValue.toString should be("serviceName=testService")
  }


  Seq[(Any, String)](
    "string" -> "\"string\"",
    1 -> "1",
    2L -> "2",
    22f -> "22.0",
    33.3d -> "33.3",
    true -> "true",
    false -> "false",
    (null, "null"),
    Map("hello" -> "world", "number" -> 9) -> """{"hello":"world","number":9}""",
    Seq("s", 1, 2L, 22f, 33.3d, true, false, null, Map("m" -> 777)) -> """["s",1,2,22.0,33.3,true,false,null,{"m":777}]"""
  ).foreach { case (input, output) =>
    s"write error in json format - $output" in new LoggingTestScope {

      when(loggerMock.isErrorEnabled()).thenReturn(true)

      logger.error(LogParameter("Log")
        .addParameters("arg1" -> input)
      )

      val captor: ArgumentCaptor[LogstashMarker] = ArgumentCaptor.forClass(classOf[LogstashMarker])
      verify(loggerMock).error(captor.capture(), any[String])

      captor.getValue.iterator().asScala.toList.map(_.toString) should be(Seq(
        s"""arg1=$output"""
      ))
    }
  }

  "must add logparams" in new LoggingTestScope {
    when(loggerMock.isErrorEnabled()).thenReturn(true)

    logger.error(LogParameter("Log")
      .addParameters("arg1" -> "value1")
      .addParameters("arg2" -> "value2")
    )

    val captor: ArgumentCaptor[LogstashMarker] = ArgumentCaptor.forClass(classOf[LogstashMarker])
    verify(loggerMock).error(captor.capture(), any[String])

    captor.getValue.iterator().asScala.toList.map(_.toString) should be(Seq(
      """arg1="value1"""",
      """arg2="value2""""
    ))
  }

  "truncate long text parameter" in new LoggingTestScope {
    override val textMaxLength: Int = 5

    when(loggerMock.isErrorEnabled()).thenReturn(true)

    logger.error(LogParameter("Log")
      .addParameters("arg1" -> "x" * 10)
    )

    val captor: ArgumentCaptor[LogstashMarker] = ArgumentCaptor.forClass(classOf[LogstashMarker])
    verify(loggerMock).error(captor.capture(), any[String])

    captor.getValue.iterator().asScala.toList.map(_.toString) should be(Seq(
      """arg1="xxxxx""""
    ))
  }


}
