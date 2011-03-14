package edu.duke.oit.vw.solr.test
import org.specs._

import edu.duke.oit.vw.solr._

class VivoSolrIndexerSpec extends Specification {

  "A Vivo Solr Indexer" should {

    "do something (hack fest goes here)" in {
      val currentDirectory = new java.io.File(".").getCanonicalPath
      val solrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
      val solrSrv = Solr.solrServer(solrCfg)

      val vivo = new Vivo("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")

      val vsi = new VivoSolrIndexer(vivo, solrSrv)
      vsi.indexPeople()
    }


  }


}
