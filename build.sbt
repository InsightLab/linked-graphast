organization := "br.ufc.insightlab"
name := "linked-graphast"
version := "1.0.0"

publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
publishConfiguration := publishConfiguration.value.withOverwrite(true)


resolvers += Resolver.mavenLocal

libraryDependencies += "org.insightlab" % "graphast-core" % "1.0.1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.11.1"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "info.debatty" % "java-string-similarity" % "1.1.0"

libraryDependencies += "org.apache.jena" % "jena-arq" % "3.8.0"
libraryDependencies += "org.apache.jena" % "jena-core" % "3.8.0"

libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.23.1"

libraryDependencies += "org.neo4j" % "neo4j" % "3.4.7"
libraryDependencies += "org.neo4j" % "neo4j-kernel" % "3.4.7"
libraryDependencies += "org.neo4j" % "neo4j-cypher" % "3.4.7"

libraryDependencies += "com.carrotsearch" % "hppc" % "0.8.1"


scalaVersion := "2.11.8"

assemblyJarName in assembly := "linked-graphast.jar"

