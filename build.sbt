name := "Save2DB"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.jooq" % "jooq" % "3.8.2"
libraryDependencies += "com.h2database" % "h2" % "1.4.192"

unmanagedJars in Compile += file("lib/EugFile.jar")
unmanagedJars in Compile += file("lib/EugFile_specific.jar")
