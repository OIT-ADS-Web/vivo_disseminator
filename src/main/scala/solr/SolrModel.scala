package edu.duke.oit.vw.solr

class SolrModel(id: String) {


}

/**
 * The <code>extraItems</code> value is a catch all hash that
 * can be used to add attributes from the originating SPARQL
 * that aren't explicitly defined.
 * <code>extraItems</code> must a Map[String, String].
 */
class ExtraItems(extraItems:Option[Map[String, String]]) {
  
  def \(key:String):Option[String] = {
    extraItems match {
      case Some(m) => m.get(key)
      case _ => None
    }
  }
}

case class Publication(uri:String, 
                       vivoType:String, 
                       year:String,
                       authors:List[String],
                       extraItems:Option[Map[String, String]]) 
     extends ExtraItems(extraItems)

case class Person(uri:String, 
                  vivoType:String, 
                  name:String, 
                  publications:List[Publication],
                  extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems) {
  
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
