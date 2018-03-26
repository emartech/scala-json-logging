val scalaV = "2.12.5"

name := "emarsys-scala-logging"
organization := "com.emarsys"
version := "0.1"
scalaVersion := scalaV


scalacOptions ++= Seq(
	"-deprecation",
	"-encoding", "UTF-8",
	"-unchecked",
	"-feature",
	"-Ywarn-dead-code",
	"-Xlint",
	"-Xfatal-warnings"
)


libraryDependencies ++= {
	val scalaTestV = "3.0.5"
	Seq(
		"net.logstash.logback" %  "logstash-logback-encoder" % "4.11",
		"ch.qos.logback"       %  "logback-classic"          % "1.2.3",
		"io.spray"             %% "spray-json"               % "1.3.4",
		"org.scalatest"        %% "scalatest"                % scalaTestV % "test",
    "org.mockito"          %  "mockito-core"             % "2.16.0"   % "test"
	)
}

publishTo := Some(Resolver.file("releases", new File("releases")))