val scalaV = "2.12.5"

name := "emarsys-scala-logging"
organization := "com.emarsys"
version      := "0.1"
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



libraryDependencies ++= Seq(
	"net.logstash.logback"  % "logstash-logback-encoder" % "4.11",
	"io.spray" 							%% "spray-json"              % "1.3.4",
	"org.slf4j"             %  "slf4j-nop"               % "1.7.21"
)

publishTo := Some(Resolver.file("releases", new File("releases")))