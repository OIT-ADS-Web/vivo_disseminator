package edu.duke.oit.vw.solr

class SolrModel(id: String) {


}

import edu.duke.oit.jena.utils._

/**
 * The <code>extraItems</code> value is a catch all hash that
 * can be used to add attributes from the originating SPARQL
 * that aren't explicitly defined.
 * <code>extraItems</code> must a Map[String, String].
 */
class ExtraItems(extraItems:Option[Map[String, String]]) extends ToMethods {
  
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

  def json = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val j = JField("extraItems", Extraction.decompose(extraItems))
    JObject(j :: Nil)
  }

  def jsonString = Printer.compact(JsonAST.render(json))

}

case class Publication(uri:String, 
                       vivoType:String, 
                       year:String,
                       authors:List[String],
                       extraItems:Option[Map[String, String]]) 
     extends ExtraItems(extraItems) 
{
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
  import net.liftweb.json.JsonAST.{JObject,JValue}

  override def json = {
    val j = ("uri" -> uri) ~
    ("vivoType"-> vivoType) ~
    ("year" -> year) ~
    ("authors" -> authors)
    j ~ super.json
  }

}

object Publication {
  def json(pub:Publication) = {
    import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
    implicit val formats = net.liftweb.json.DefaultFormats
    Printer.compact(JsonAST.render(Extraction.decompose(pub)))
  }
}

case class Person(uri:String, 
                  vivoType:String, 
                  name:String, 
                  publications:List[Publication],
                  extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems)

object Person {
  def json(person:Person) = {
    import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
    implicit val formats = net.liftweb.json.DefaultFormats
    Printer.compact(JsonAST.render(Extraction.decompose(person)))
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
