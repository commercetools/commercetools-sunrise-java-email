import ReleaseTransformations._
import sbt.Keys._

name := "commercetools-sunrise-email"

organization := "com.commercetools.sunrise.email"

lazy val javaMailVersion = "1.5.5"

/**
 * PROJECT DEFINITIONS
 */

lazy val `commercetools-sunrise-email` = (project in file("."))
  .aggregate(`email-common`, `email-smtp`)
  .settings(javaUnidocSettings ++ commonSettings : _*)

lazy val `email-common` = project
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
  .dependsOn(`email-common`)


/**
 * COMMON SETTINGS
 */

lazy val commonSettings = releaseSettings ++ Seq (
  scalaVersion := "2.11.8",
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

lazy val releaseSettings = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)
