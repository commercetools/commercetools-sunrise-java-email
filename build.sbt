import ReleaseTransformations._

name := "commercetools-sunrise-email"

organization := "io.commercetools.sunrise"

lazy val `commercetools-sunrise-email` = (project in file("."))
  .configs(IntegrationTest)
  .settings(commonSettings ++ commonTestSettings : _*)

lazy val commonSettings = releaseSettings ++ Seq (
  scalaVersion := "2.11.8",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

javaUnidocSettings

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