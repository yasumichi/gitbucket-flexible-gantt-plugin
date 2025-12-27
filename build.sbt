name := "gitbucket-flexible-gantt-plugin"
organization := "io.github.gitbucket"
version := "0.1.1"
scalaVersion := "2.13.16"
gitbucketVersion := "4.36.2"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

libraryDependencies +=   "com.github.takezoe"  %% "blocking-slick" % "0.0.14"