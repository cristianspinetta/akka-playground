import sbt._
import Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

object Settings {

  val JavaVersion = "1.8"
  val ScalaVersion = "2.11.8"

  lazy val basicSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq(
    scalaVersion := ScalaVersion,
    resolvers ++= Dependencies.resolutionRepos,
    version <<= version in ThisBuild,
    javacOptions   := Seq(
      "-Xlint:-options",
      "-source", JavaVersion, "-target", JavaVersion),
    scalacOptions  := Seq(
      "-encoding",
      "utf8",
      "-g:vars",
      "-feature",
      "-unchecked",
      "-optimise",
      "-deprecation",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Yinline-warnings",
      "-Xlog-reflective-calls"
    )
  )

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test := formattingPreferences
  )

  def formattingPreferences =
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AlignParameters, false)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
}
