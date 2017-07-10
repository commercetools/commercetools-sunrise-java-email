import ReleaseTransformations._
import sbt.Keys._

name := "commercetools-sunrise-email"

organization in ThisBuild := "com.commercetools.sunrise.email"

lazy val javaMailVersion = "1.5.5"

/**
 * PROJECT DEFINITIONS
 */

lazy val `commercetools-sunrise-email` = (project in file("."))
  .aggregate(`email-api`, `email-smtp`)
  .settings(javaUnidocSettings ++ commonSettings : _*)

lazy val `email-api` = project
  .configs(IntegrationTest)
  .settings(commonSettings ++ commonTestSettings : _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.google.code.findbugs" % "jsr305" % "3.0.0",
      "javax.mail" % "javax.mail-api" % javaMailVersion withSources()
    )
  )

lazy val `email-smtp` = project
  .configs(IntegrationTest)
  .settings(commonSettings ++ commonTestSettings : _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.sun.mail" % "mailapi" % javaMailVersion withSources(),
      "com.sun.mail" % "smtp" % javaMailVersion withSources(),
      "com.sun.mail" % "dsn" % javaMailVersion withSources()
    )
  )
  .dependsOn(`email-api`)


/**
 * COMMON SETTINGS
 */

lazy val commonSettings = Release.publishSettings ++ Seq (
  autoScalaLibrary := false, //this is a pure Java module, no Scala dependency
  crossPaths := false, //this is a pure Java module, no Scala version suffix on JARs
  javacOptions in (Compile, doc) := Seq("-quiet", "-notimestamp"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

/**
 * TEST SETTINGS
 */
lazy val commonTestSettings = itBaseTestSettings ++ configCommonTestSettings("test,it")

lazy val itBaseTestSettings = Defaults.itSettings ++ configTestDirs(IntegrationTest, "it")

def configTestDirs(config: Configuration, folderName: String) = Seq(
  javaSource in config := baseDirectory.value / folderName,
  scalaSource in config := baseDirectory.value / folderName,
  resourceDirectory in config := baseDirectory.value / s"$folderName/resources"
)

def configCommonTestSettings(scopes: String) = Seq(
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  parallelExecution := false, // Many tests start a Greenmail SMTP server on the same port
  libraryDependencies ++= Seq (
    "com.novocode" % "junit-interface" % "0.11" % scopes,
    "org.assertj" % "assertj-core" % "3.4.1" % scopes,
    "com.icegreen" % "greenmail" % "1.5.0" % scopes
  )
)

/**
  * RELEASE SETTINGS
  */

publishMavenStyle in ThisBuild := true

publishArtifact in Test in ThisBuild := false

publishTo in ThisBuild <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
