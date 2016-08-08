import sbt._
import sbt.Keys._

object Projects extends Build {
  import AspectJ._
  import Dependencies._
  import Settings._

  lazy val root = Project("akka-playground", file("."))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(noPublishing: _*)
    .aggregate(binaryTree, akkaHttpMicroservice, kamonSprayPlayground, kamonAkkaHttpPlayground)

  lazy val binaryTree = Project("binary-tree",file("binary-tree"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(libraryDependencies ++=
      compile(typesafeConfig, slf4jApi ,logbackCore, logbackClassic, akkaActor, akkaSlf4j, akkaTestKit) ++
        test(scalatest, mockito))
    .settings(noPublishing: _*)
    .settings(mainClass in (Compile, run) := Some("cs.Run"))

  lazy val akkaHttpMicroservice = Project("akka-http-microservice",file("akka-http-microservice"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(libraryDependencies ++=
      compile(typesafeConfig, slf4jApi ,logbackCore, logbackClassic, akkaActor, akkaStream, akkaHttpExperimental,
        akkaHttpSprayJsonExperimental, akkaHttpTestKit) ++
        test(scalatest, mockito))
    .settings(noPublishing: _*)

  lazy val kamonSprayPlayground  = Project("kamon-spray-playground",file("kamon-spray-playground"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(libraryDependencies ++=
      compile(typesafeConfig, slf4jApi ,logbackCore, logbackClassic, akkaActor, sprayCan, sprayClient, sprayRouting,
        kamonSpray, kamonLogReporter) ++
        test(scalatest, mockito))
    .settings(noPublishing: _*)

  lazy val kamonAkkaHttpPlayground  = Project("kamon-akka-http-playground",file("kamon-akka-http-playground"))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(libraryDependencies ++=
      compile(typesafeConfig, slf4jApi ,logbackCore, logbackClassic, akkaActor, akkaHttpExperimental, akkaHttpSprayJsonExperimental,
        kamonAkkaHttpExperimental, kamonLogReporter) ++
        test(scalatest, mockito))
    .settings(noPublishing: _*)

  val noPublishing = Seq(publish := (), publishLocal := (), publishArtifact := false)
}
