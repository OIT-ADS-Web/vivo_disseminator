package edu.duke.oit.test.helpers

import edu.duke.oit.vw.solr._

object TestServers {
  val currentDirectory = new java.io.File(".").getCanonicalPath
  val widgetSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
  val widgetSolr = Solr.solrServer(widgetSolrCfg)
  val vivoSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivocore")
  val vivoSolr = Solr.solrServer(vivoSolrCfg)

  // no longer used
  // val vivo = new Vivo("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")

  // in memory database - need the DB_CLOSE_DELAY in order for the db to perist across connections
  val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  val user = "sa"
  val password = ""
  val dbType = "H2"
  
  val vivo = new Vivo(url,user,password,dbType,"org.h2.Driver")

  def loadSampleData = {
    import edu.duke.oit.jena.connection._
    import com.hp.hpl.jena.rdf.model._
    import com.hp.hpl.jena.util.FileManager
    import java.io.InputStream
    
    print("loading sample data...")
    val sampleFile = currentDirectory+"/src/test/resources/vivo_base.rdf"

    Class.forName("org.h2.Driver")
    val cInfo = new JenaConnectionInfo(url,user,password,dbType)

    Jena.truncateAndCreateStore(cInfo)
    Jena.sdbModel(cInfo, "http://vitro.mannlib.cornell.edu/default/vitro-kb-2") { dbModel =>
      // use the FileManager to find the input file
      val in = FileManager.get.open(sampleFile)

      // read the RDF/XML file
      dbModel.read(in, null);
    }
    println("[DONE]")

  }
}
