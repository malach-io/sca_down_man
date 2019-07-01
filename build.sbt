organization := "sca_down_man"

scalaVersion := "2.12.4"

version := "1.0-SNAPSHOT"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.1.1"

mainClass in (Compile, run) := Some("scala.ui.DownloadSwing")