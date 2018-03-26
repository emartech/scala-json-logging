package com.emarsys.scala.logging

import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers._
import org.slf4j.{LoggerFactory, Marker}
import spray.json._

case class LogParameter(message: String, loggingData: Map[String, Any] = Map.empty) {
	def addParameters(additionalParameters: (String, Any)*) = LogParameter(message, loggingData ++ additionalParameters)
}

object LogParameter {
	private val messageKey = "message"
	private val exceptionKey = "exception"
	private val classKey = "class"
	private val stackTraceKey = "stacktrace"

	def apply(message: String, t: Throwable) = new LogParameter(message, Map(
		exceptionKey -> Map(
			classKey -> t.getClass,
			messageKey -> t.getMessage,
			stackTraceKey -> t.getStackTrace.toSeq.map(_.toString)
		)
	))
}

trait LogJsonProtocol extends DefaultJsonProtocol {
	val PARAMETER_MAX_LENGTH_BYTES = 2000

	implicit object anyJsonWriter extends JsonWriter[Any] {
		override def write(obj: Any): JsValue = obj match {
			case null => JsNull
			case i: Int => JsNumber(i)
			case l: Long => JsNumber(l)
			case f: Float => JsNumber(f)
			case d: Double => JsNumber(d)
			case s: String => JsString(truncateText(s))
			case s: Seq[Any] => JsArray(s.map(_.toJson).toVector)
			case true => JsTrue
			case false => JsFalse
			case m: Map[_, Any] => JsObject(m map { case (key, value) => key.toString -> value.toJson })

			case o => JsString(truncateText(o.toString))
		}

		private def truncateText(text: String) = text.take(PARAMETER_MAX_LENGTH_BYTES)
	}

}

class Logger[T](serviceName: String)(clazz: Class[T]) extends LogJsonProtocol {

	private val logger = LoggerFactory.getLogger(clazz)

	def debug(logParameter: => LogParameter): Unit =
		if (logger.isDebugEnabled) logger.debug(includeLogData(logParameter), logParameter.message)

	def info(logParameter: => LogParameter): Unit =
		if (logger.isInfoEnabled) logger.info(includeLogData(logParameter), logParameter.message)

	def warn(logParameter: => LogParameter): Unit =
		if (logger.isWarnEnabled) logger.warn(includeLogData(logParameter), logParameter.message)

	def error(logParameter: => LogParameter): Unit =
		if (logger.isErrorEnabled) logger.error(includeLogData(logParameter), logParameter.message)

	private def includeLogData(logParameter: => LogParameter): Marker =
		logParameter.loggingData.foldLeft(append("serviceName", serviceName)) { (marker: LogstashMarker, current) =>
			val (field, data) = current
			marker.and[LogstashMarker](appendRaw(field, data.toJson.toString))
		}
}

object Logger {
	def apply[T](serviceName: String)(clazz: Class[T]): Logger[T] = new Logger(serviceName)(clazz)
}
