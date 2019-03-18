name := "Game"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.joml" % "joml" % "1.9.12"

libraryDependencies ++= {
  val version = "3.1.6"
  val os = sys.props("os.name").toLowerCase.split(' ')(0)

  Seq(
    "lwjgl",
    "lwjgl-glfw",
    "lwjgl-opengl",
    "lwjgl-stb"
  ).flatMap {
    module => {
      Seq(
        "org.lwjgl" % module % version,
        "org.lwjgl" % module % version classifier s"natives-$os"
      )
    }
  }
}

libraryDependencies += "tech.sparse" %% "toml-scala" % "0.2.0"

scalacOptions += "-language:postfixOps"
