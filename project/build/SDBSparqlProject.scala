import sbt._

class SDBSparqlProject(info: ProjectInfo) extends DefaultProject(info) {

  // this restrict the executed classes names to end with either "Spec" or "Unit"
  override def includeTest(s: String) = { s.endsWith("Spec") || s.endsWith("Unit") }

  val scalaToolsSnapshots = ScalaToolsSnapshots

  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

  val scalatools_snapshot = "Scala Tools Snapshot" at
    "http://scala-tools.org/repo-snapshots/"

  val scalatools_release = "Scala Tools Snapshot" at
    "http://scala-tools.org/repo-releases/"


  override def repositories = Set(
    //"Java.Net" at "http://download.java.net/maven/2",
    ScalaToolsSnapshots
  )

  override def libraryDependencies = Set(
    "com.hp.hpl.jena" % "jena" % "2.6.4",
    "com.hp.hpl.jena" % "arq" % "2.8.7",
    "com.hp.hpl.jena" % "tdb" % "0.8.8",
    "com.hp.hpl.jena" % "sdb" % "1.3.3",

    "mysql" % "mysql-connector-java" % "5.1.14",

    // "org.scalatest" % "scalatest" % "1.2" % "test->default",
    // "org.scala-tools.testing" % "specs" % "1.6.1-2.8.0.Beta1-RC6",

    // didn't need this for some reason
     "javax.servlet" % "servlet-api" % "2.5", 

    // scala libraries
    "org.scala-tools.testing" %% "specs" % "1.6.8-SNAPSHOT" % "test->default", 
    "net.liftweb" %% "lift-json-ext" % "2.2-RC5", 

    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "1.6",

    // might want to switch to logback in the future - updated version of log4j
    // "com.googlecode.sli4j" % "sli4j-slf4j" % "2.0",
    // "com.googlecode.sli4j" % "sli4j-slf4j-logback" % "2.0",

    "org.apache.solr" % "solr-core" % "1.4.1",
    "org.apache.solr" % "solr-solrj" % "1.4.1",
    
    "org.apache.activemq" % "activemq-camel" % "5.5.0",
    "org.apache.activemq" % "activemq-core" % "5.5.0",
    "com.h2database" % "h2" % "1.3.154" % "test->default"


  ) ++ super.libraryDependencies

  // override def ivyXML =
  //   <dependencies>
  //     <dependency org="org.apache.solr" name="solr-core" rev="3.1.0">
  //       <exclude module="slf4j-api"/>
  //       <exclude module="slf4j-jdk14"/>
  //     </dependency>
  //     <dependency org="org.apache.solr" name="solr-solrj" rev="3.1.0">
  //       <exclude module="slf4j-api"/>
  //     </dependency>
  //   </dependencies>

}
