package com.emarsys.scala.logging

import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

class LoggingSpec extends WordSpec with Matchers {

	import spray.json._

	"Logging" should {

		"write info in json format" in {

			val logger = new Logger("testService")(classOf[String])

			logger.error(LogParameter("Log")
				.addParameters("firstName" -> "John")
				.addParameters("lastName" -> "Doe")
			)

			val filename = "./test.log"
			val line = Source.fromFile(filename).getLines().take(1).toList.head

			val parsedLine = line.parseJson

			val jsonMap = parsedLine.asJsObject.fields

			jsonMap.get("lastName") should be(Some(JsString("Doe")))
			jsonMap.get("firstName") should be(Some(JsString("John")))

		}
	}
}
