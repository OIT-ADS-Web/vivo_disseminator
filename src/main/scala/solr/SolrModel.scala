package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList,SolrDocument}
import java.util.ArrayList
import scala.collection.JavaConversions._

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

  def search(queryString: String, solr: SolrServer): VivoSearchResult = {
    val query = new SolrQuery().setQuery(queryString).addFacetField("classgroup").setFacetMinCount(1).setRows(1000)
    val response = solr.query(query)

    val docList = response.getResults().toList
    val items = parseItemList(docList)
    items.size match {
      case 0 => new VivoSearchResult(0,Map(),List())
      case _ => {
        val facetList = response.getLimitingFacets().toList
        new VivoSearchResult(items.size.toLong,parseFacetMap(facetList),items)
      }
    }
  }

  protected

  def parseItemList(docList: List[SolrDocument]): List[VivoSearchResultItem] = {
    docList filter ( _.get("classgroup") != null ) map { doc =>
      new VivoSearchResultItem(doc.get("URI").toString,
                               doc.get("nameraw").toString,
                               doc.get("classgroup") match {
                                 case a: ArrayList[String] => parseClassGroupName(a(0))
                                 case s: String => parseClassGroupName(s)
                                 case _ => ""
                               })
    }
  }

  def parseClassGroupName(classgroup: String): String = {
    classgroup.replace("http://vivoweb.org/ontology#vitroClassGroup","")
  }

  def parseFacetMap(facetList: List[FacetField]): Map[String,Long] = {
    facetList.size match {
      case 0 => Map()
      case _ => facetList(0).getValues().map { f => (parseClassGroupName(f.getName),f.getCount) }.toMap
    }
  }

}

class VivoSearchResult(val numFound: Long,val  groups: Map[String,Long],val  items: List[VivoSearchResultItem]) extends AddToJson

class VivoSearchResultItem(val uri: String,val  name: String,val  group: String)
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
  def \(key:String): String = {
    extraItems match {
      case Some(m) => m.get(key).orNull
      case _ => null
    }
  }

  def get(key:String): String = {
    \(key)
  }

  def getOrElse(key:String,default:String): String = {
    \(key) match {
      case null => default
      case m:String => m
      case _ => null
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
