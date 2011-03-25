package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList,SolrDocument}

trait SolrModel {

  def getDocumentById(id: String,solr: SolrServer): Option[SolrDocument] = {
    val query = new SolrQuery().setQuery("id:\"" + id + "\"")
    val docList = solr.query(query).getResults()
    if (docList.getNumFound() > 0) {
      Option(docList.iterator.next())
    } else {
      None
    }
  }

}


/**
 * Json helper methods
 */
object Json {
  
  /**
   * Covert <code>item</item> to a json string representation format.
   *
   * @param item convert the item of type T to a json string.
   */
  def toJson[T](item:T) = {
    import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
    implicit val formats = net.liftweb.json.DefaultFormats
    Printer.compact(JsonAST.render(Extraction.decompose(item)))
  }

}

trait AddToJson {

  /**
   * Convert the current object to a json String.
   * @return String representation of json.
   */
  def toJson = {
    Json.toJson(this)
  }

}

import edu.duke.oit.jena.utils._

/**
 * The <code>extraItems</code> value is a catch all hash that
 * can be used to add attributes from the originating SPARQL
 * that aren't explicitly defined.
 * <code>extraItems</code> must a Map[String, String].
 */
class ExtraItems(extraItems:Option[Map[String, String]]) extends ToMethods with AddToJson {
  
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.{JsonAST,Printer,Extraction}
  import net.liftweb.json.JsonAST.{JField,JObject}

  /**
   * 
   * @param key the key string to look for in the extraItems Map
   * @return Some(String) with the string value from the extraItems Map;
   *         otherwise None
   */
  def \(key:String):Option[String] = {
    extraItems match {
      case Some(m) => m.get(key)
      case _ => None
    }
  }

}


case class Publication(uri:String,
                       vivoType:String,
                       title:String,
                       authors:List[String],
                       extraItems:Option[Map[String, String]]) 
     extends ExtraItems(extraItems) with AddToJson

case class Person(uri:String,
                  vivoType:String,
                  name:String,
                  title:String,
                  publications:List[Publication],
                  extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems) with AddToJson

object Person extends SolrModel {
  // def json(person:Person) = {
  //   import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
  //   implicit val formats = net.liftweb.json.DefaultFormats
  //   Printer.compact(JsonAST.render(Extraction.decompose(person)))
  // }

  def find(uri: String, solr: SolrServer): Option[Person] = {
    getDocumentById(uri,solr) match {
      case Some(sd) => Option(PersonExtraction(sd.get("json").toString))
      case _ => None
    }
  }
}


/**
 * Wraps the lift-json parsing and extraction of a person.
 */
object PersonExtraction {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats 

    val j = JsonParser.parse(json)
    j.extract[Person]
  }
}
