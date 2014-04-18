name := "visual-gpx"

version := "0.0.1"

organization := "at.droelf"

scalaVersion := "2.10.4"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "http://oss.sonatype.org/content/repositories/releases",
                "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
                )

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.5.1"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftmodules"   %% "lift-jquery-module_2.5" % "2.4",
    "net.liftweb"       %% "lift-mapper"        % liftVersion,
    "org.eclipse.jetty" % "jetty-webapp"        % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "org.specs2"        %% "specs2"             % "1.14"            % "test",
    "com.h2database" % "h2" % "1.3.175",
    "com.droelf.gpxparser" % "gpxparser" % "1.0-SNAPSHOT",
    "net.liftmodules" %% "widgets_2.5" % "1.3",
    "com.github.nscala-time" %% "nscala-time" % "1.0.0"
  )
}

