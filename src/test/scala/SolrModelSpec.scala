package edu.duke.oit.solr.test

import org.specs._
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrDocumentList

import edu.duke.oit.vw.solr._
import edu.duke.oit.test.helpers.TestServers

// use scala collections with java iterators
import scala.collection.JavaConversions._

class SolrModelSpec extends Specification {
  val solrSrv = TestServers.widgetSolr


  "A Solr Model" should {
    val doc1 = new SolrInputDocument()
    doc1.addField("id","http://faculty.duke.edu/test/1")
    doc1.addField("json","a string for testing")
    solrSrv.add(doc1)
    solrSrv.commit()
    val doc2 = new SolrInputDocument
    doc2.addField("id","http://faculty.duke.edu/test/2")
    doc2.addField("json", "this should be found ing1")
    solrSrv.add(doc2)
    solrSrv.commit()

    object TestModel extends SolrModel

    "retrieve a document by id from the supplied solr server" in {
      TestModel.getDocumentById("http://faculty.duke.edu/test/2",solrSrv).get("json").toString must_== "this should be found ing1"
    }

  }

  "The Person Object" should {

    "find an indexed person by uri and return some person instance" in {
      val testPersonJson = """
        {
          "uri": "http://vivo.duke.edu/person1",
          "name" : "Smith J",
          "vivoType" : "http://xmlns.com/foaf/0.1/Person",
          "title" : "Professor of Testology",
          "publications": [
            {
              "uri": "http://vivo.duke.edu/test1",
              "vivoType": "http://purl.org/ontology/bibo/Article",
              "title": "Programming Tips",
              "authors": ["Lawrence GL","Smith J"],
              "extraItems": {
                "issue": "13",
                "year": "2005"
              }
            }
          ]
        }
      """

      val doc1 = new SolrInputDocument()
      doc1.addField("id","http://vivo.duke.edu/person1")
      doc1.addField("json",testPersonJson)
      solrSrv.add(doc1)
      solrSrv.commit()

      val p = Person.find("http://vivo.duke.edu/person1",solrSrv).get
      p.uri  must_== "http://vivo.duke.edu/person1"
      p.name must_== "Smith J"
      p.publications(0).title must_== "Programming Tips"
    }

    "not find a non-indexed person and return None" in {
      Person.find("no_chance_this_has_been_indexed",solrSrv) must_== None
    }
  }
}

class SolrExtractionSpec extends Specification {

  "Extract person name" in {
    val person = PersonExtraction(testPersonJson)
    person.name must_== "Smith J"
  }

  "Extract publications" in {
    val person = PersonExtraction(testPersonJson)
    person.publications mustEqual List(Publication( "http://vivo.duke.edu/test1",
                                              "http://purl.org/ontology/bibo/Article",
                                              "Programming Tips",
                                              List("Lawrence GL", "Smith J"),
                                              Some(Map("issue" -> "13","year" -> "2005"))) )
  }

  "Extract value from publications " in {
    val person = PersonExtraction(testPersonJson)
    val issue = person.publications.head \ "issue"
    issue mustEqual "13"
  }

  val testPersonJson = """
    {
      "uri": "http://vivo.duke.edu/person1",
      "name" : "Smith J",
      "vivoType" : "http://xmlns.com/foaf/0.1/Person",
      "title" : "Professor of Testology",
      "publications": [
        {
          "uri": "http://vivo.duke.edu/test1",
          "vivoType": "http://purl.org/ontology/bibo/Article",
          "title": "Programming Tips",
          "authors": ["Lawrence GL","Smith J"],
          "extraItems": {
            "issue": "13",
            "year": "2005"
          }
        }
      ]
    }
  """
}
 
// "
class SolrJsonProducingSpec extends Specification {
  import edu.duke.oit.vw.solr._

  "Produce json for extraItems" in {
    val ei = new ExtraItems(Option(Map("a" -> "b", "c" -> "d")))
    ei.toJson must_== """{"extraItems":{"a":"b","c":"d"}}"""
  }

  "Produce json from the Json object" in {
    val ei = new ExtraItems(Option(Map("a" -> "b", "c" -> "d")))
    Json.toJson(ei) must_== """{"extraItems":{"a":"b","c":"d"}}"""
  }

  "Produce publication json" in {
    val pub = Publication("http://vivo.duke.edu/test1",
                          "http://purl.org/ontology/bibo/Article",
                          "Programming Tips",
                          List("Smith J"),
                          Option(Map("issue" -> "13","year"->"2005")))
    pub.toJson must_== """{"uri":"http://vivo.duke.edu/test1","vivoType":"http://purl.org/ontology/bibo/Article","title":"Programming Tips","authors":["Smith J"],"extraItems":{"issue":"13","year":"2005"}}"""
  }
}
