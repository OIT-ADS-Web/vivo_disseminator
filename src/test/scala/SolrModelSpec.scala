package edu.duke.oit.solr.test

import org.specs._
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrDocumentList

import edu.duke.oit.vw.solr._

// use scala collections with java iterators
import scala.collection.JavaConversions._

class SolrModelSpec extends Specification {
  val currentDirectory = new java.io.File(".").getCanonicalPath
  val solrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
  val solrSrv = Solr.solrServer(solrCfg)
  val doc1 = new SolrInputDocument()
  doc1.addField("id","http://faculty.duke.edu/test/1")
  doc1.addField("test_field","a string for testing")
  solrSrv.add(doc1)
  solrSrv.commit()
  val doc2 = new SolrInputDocument
  doc2.addField("id","http://faculty.duke.edu/test/2")
  doc2.addField("test_field", "this should be found ing1")
  solrSrv.add(doc2)
  solrSrv.commit()

  val a = Class.forName("org.apache.solr.common.SolrInputDocument").newInstance
  println(a)

  "A Solr Model" should {
    "retrieve a document by id from the supplied solr server" in {
      //val sm = new SolrModel(1)
      val sq = new SolrQuery().setQuery("id:\"http://faculty.duke.edu/test/2\"")
      val sr = solrSrv.query(sq)
      val response = sr.getResponse.get("response").asInstanceOf[SolrDocumentList]
      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>") 
      // println(response)
      for (solrDoc <- response) println(solrDoc.get("test_field").asInstanceOf[String].endsWith("ing1"))
      for (solrDoc <- response) println(solrDoc.get("test_field"))

      println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>") 
    } // tag("focus")

  } // tag("focus")

}

import edu.duke.oit.vw.solr._

class SolrExtractionSpec extends Specification {

  "Extract person name" in {
    val person = PersonExtraction(testPersonJson)
    person.name must_== "Smith J"
  }

  "Extract publications" in {
    val person = PersonExtraction(testPersonJson)
    person.publications mustEqual List(Publication( "http://vivo.duke.edu/test1",
                                              "http://purl.org/ontology/bibo/Article",
                                              "2005",
                                              List("Lawrence GL", "Smith J"),
                                              Some(Map("issue" -> "13"))) )
  }

  "Extract value from publications " in {
    val person = PersonExtraction(testPersonJson)
    val issue = person.publications.head \ "issue"
    issue mustEqual Some("13")
  }

  val testPersonJson = """
    {
      "uri": "http://vivo.duke.edu/person1",
      "name" : "Smith J",
      "vivoType" : "http://xmlns.com/foaf/0.1/Person"
      "publications": [
        {
          "uri": "http://vivo.duke.edu/test1",
          "vivoType": "http://purl.org/ontology/bibo/Article",
          "title": "Programming Tips",
          "year": "2005",
          "authors": ["Lawrence GL","Smith J"],
          "extraItems": {
            "issue": "13"
          }
        }
      ]
    }
  """
}
