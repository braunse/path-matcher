organization in ThisBuild := "de.sebbraun.helpers"

name in ThisBuild := "path-matcher"

scalaVersion in ThisBuild := "2.12.2"

crossScalaVersions in ThisBuild := Seq("2.11.11", "2.12.2")

licenses in ThisBuild := Seq(
  "MIT" -> url("http://opensource.org/licenses/MIT")
)

developers in ThisBuild := List(
  Developer("braunse",
    "Sébastien Braun",
    "sebastien@sebbraun.de",
    url("https://github.com/braunse"))
)

scmInfo in ThisBuild := Some(ScmInfo(
  url("https://github.com/braunse/simple-lifecycle/tree/master"),
  connection = "scm:git:https://github.com/braunse/simple-lifecycle",
  devConnection = Some("scm:git:ssh://ssh@github.com/braunse/simple-lifecycle.git")
))

homepage in ThisBuild := Some(url("https://github.com/braunse/simple-lifecycle"))

pomIncludeRepository in ThisBuild := { _ => false }

lazy val publishSetting = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  }
)

lazy val scaladocSetting = Seq(
  scalacOptions in(Compile, doc) ++= Seq("-groups", "-implicits")
)

publishMavenStyle in ThisBuild := true

publishTo := None

autoAPIMappings in ThisBuild := true


// project definitions:

sourcesInBase := false

aggregateProjects(js, jvm)

lazy val commonSettings = Seq(
  unmanagedSourceDirectories in Compile += (baseDirectory in ThisBuild).value / "shared/src/main/scala",
  unmanagedSourceDirectories in Test += (baseDirectory in ThisBuild).value / "shared/src/test/scala",
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.0.3" % "test",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
) ++ overridePublishSignedBothSettings ++ publishSetting ++ scaladocSetting

lazy val js = project.in(file("js"))
  .settings(
    commonSettings
  )
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(SignedAetherPlugin)
  .disablePlugins(AetherPlugin)

lazy val jvm = project.in(file("jvm"))
  .settings(
    commonSettings
  )
  .enablePlugins(SignedAetherPlugin)
  .disablePlugins(AetherPlugin)
