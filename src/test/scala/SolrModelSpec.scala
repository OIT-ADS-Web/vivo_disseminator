package edu.duke.oit.solr.test

import org.specs._
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.client.solrj.SolrQuery

import edu.duke.oit.vw.solr._

class SolrModelSpec extends Specification {

  val solrCfg = new SolrConfig("/Users/jimwood/dev/sdb_sparql/solr/solr.xml","/Users/jimwood/dev/sdb_sparql/solr","vivowidgetcore")
  val solrSrv = Solr.solrServer(solrCfg)
  val doc1 = new SolrInputDocument()
  doc1.addField("id",1)
  doc1.addField("test_field","a string for testing")
  solrSrv.add(doc1)
  solrSrv.commit()

  "A Solr Model" should {
    "retrieve a document by id from the supplied solr server" in {
      //val sm = new SolrModel(1)
      val sq = new SolrQuery().setQuery("id:1")
      val sr = solrSrv.query(sq)
      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+sr)

    } tag("focus")

  } tag("focus")

}

