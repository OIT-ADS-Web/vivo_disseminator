package edu.duke.oit.vw.solr.test
import org.specs._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrDocumentList

import edu.duke.oit.vw.solr._

object TestServers {
    val currentDirectory = new java.io.File(".").getCanonicalPath
    val solrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
    val solrSrv = Solr.solrServer(solrCfg)

    val vivo = new Vivo("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")
}

class VivoSolrIndexerSpec extends Specification {

  "A Vivo Solr Indexer" should {
    val vivo = TestServers.vivo
    val solrSrv = TestServers.solrSrv

    solrSrv.deleteByQuery("*:*")
    vivo.initializeJenaCache()

    val vsi = new VivoSolrIndexer(vivo, solrSrv)

    "be connected to a vivo server with more than 0 people" in {
      // guard against running tests with bad db
      vivo.numPeople must be_> (0)
    }

    "create a document in the index for each person in vivo with their uri as the id" in {
      vsi.indexPeople()
      val people = vivo.select(vivo.sparqlPrefixes + """
        select ?p where { ?p rdf:type core:FacultyMember }
      """)
      for (p <- people) {
        val query = new SolrQuery().setQuery("id:\"" + p('p) + "\"")
        val personDocs = solrSrv.query(query).getResults()
        personDocs.getNumFound() must_== 1
      }
    }

  }


}
