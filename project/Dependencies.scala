import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    Classpaths.typesafeSnapshots,
    "Typesafe Maven Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/releases/"
  )

  val akkaVersion          = "2.4.8"
  val kamonVersion         = "0.6.2"
  val aspectjVersion       = "1.8.9"
  val sprayVersion         = "1.3.3"

  val slf4jApi                      = "org.slf4j"                  %  "slf4j-api"                         % "1.7.13"
  val logbackCore                   = "ch.qos.logback"             %  "logback-core"                      % "1.1.3"
  val logbackClassic                = "ch.qos.logback"             %  "logback-classic"                   % "1.1.3"
  val typesafeConfig                = "com.typesafe"               %  "config"                            % "1.3.0"
  val scalatest                     = "org.scalatest"             %%  "scalatest"                         % "2.2.6"
  val mockito                       = "org.mockito"                %  "mockito-core"                      % "2.0.42-beta"
  val akkaActor                     = "com.typesafe.akka"         %%  "akka-actor"                        % akkaVersion
  val akkaStream                    = "com.typesafe.akka"         %%  "akka-stream"                       % akkaVersion
  val akkaHttpExperimental          = "com.typesafe.akka"         %%  "akka-http-experimental"            % akkaVersion
  val akkaHttpSprayJsonExperimental = "com.typesafe.akka"         %%  "akka-http-spray-json-experimental" % akkaVersion
  val akkaHttpTestKit               = "com.typesafe.akka"         %%  "akka-http-testkit"                 % akkaVersion
  val akkaSlf4j                     = "com.typesafe.akka"         %%  "akka-slf4j"                        % akkaVersion
  val akkaTestKit                   = "com.typesafe.akka"         %%  "akka-testkit"                      % akkaVersion
  val akkaHttpCore                  = "com.typesafe.akka"         %%  "akka-http-core"                    % akkaVersion

  val kamonAkka                     = "io.kamon"                  %%  "kamon-akka"                        % kamonVersion
  val kamonAutoweave                = "io.kamon"                  %%  "kamon-autoweave"                   % kamonVersion
  val kamonSpray                    = "io.kamon"                  %%  "kamon-spray"                       % kamonVersion
  val kamonAkkaHttpExperimental     = "io.kamon"                  %%  "kamon-akka-http-experimental"      % kamonVersion
  val kamonLogReporter              = "io.kamon"                  %%  "kamon-log-reporter"                % kamonVersion

  val sprayCan                      = "io.spray"                  %%  "spray-can"                         % sprayVersion
  val sprayRouting                  = "io.spray"                  %%  "spray-routing"                     % sprayVersion
  val sprayTestkit                  = "io.spray"                  %%  "spray-testkit"                     % sprayVersion
  val sprayClient                   = "io.spray"                  %%  "spray-client"                      % sprayVersion

  val scalaLogging                  = "com.typesafe.scala-logging" %% "scala-logging"                     % "3.4.0"


  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")
  def optional  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile,optional")
}
