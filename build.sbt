organization := "br.ufc.insightlab"
name := "linked-graphast"
version := "1.1.0"

publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
publishConfiguration := publishConfiguration.value.withOverwrite(true)


resolvers += Resolver.mavenLocal
resolvers += "Insight Lab github Maven2 Repository" at "http://www.insightlab.ufc.br/maven-repo/"

libraryDependencies += "br.ufc.insightlab" % "graphast-core" % "1.1.0"
libraryDependencies += "br.ufc.insightlab" % "ror" % "1.0.0"

libraryDependencies += "org.wildfly.swarm" % "teiid-jdbc" % "2018.5.0"
libraryDependencies += "org.teiid" % "teiid-client" % "10.2.0"
libraryDependencies += "org.teiid" % "teiid-common-core" % "10.2.0"
libraryDependencies += "org.teiid" % "teiid-api" % "10.2.0"


libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.6"


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

libraryDependencies += "edu.washington.cs" % "figer_2.10" % "0"


scalaVersion := "2.11.8"

mappings in (Compile, packageBin) ~= { _.filter(!_._1.getName.endsWith(".model.gz")) }
