addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")
addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.18.2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.16")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.4.0")

// workaround for java.lang.NoClassDefFoundError: org/apache/commons/lang3/Validate
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % "3.5",
  "org.json4s" %% "json4s-native" % "3.2.10"
)