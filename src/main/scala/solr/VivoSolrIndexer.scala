package edu.duke.oit.vw.solr

import edu.duke.oit.jena.connection._
import edu.duke.oit.jena.actor.JenaCache

object Vivo {
  def initializeJenaCache(url: String, user: String, password: String, dbType: String, driver: String) = {
    Class.forName(driver)
    JenaCache.setFromDatabase(new JenaConnectionInfo(url,user,password,dbType),
                              "http://vitro.mannlib.cornell.edu/default/vitro-kb-2")
  }

  def queryJenaCache(sparql: String) = {
    JenaCache.queryModel(sparql)
  }

  // TODO: make select choose between cached/non-cached based on configuration
  def select(sparql: String) = {
    queryJenaCache(sparql)
  }

  def sparqlPrefixes: String = """
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
    PREFIX swrl: <http://www.w3.org/2003/11/swrl#>
    PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    PREFIX bibo: <http://purl.org/ontology/bibo/>
    PREFIX dcelem: <http://purl.org/dc/elements/1.1/>
    PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>
    PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>
    PREFIX ero: <http://purl.obolibrary.org/obo/>
    PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX core: <http://vivoweb.org/ontology/core#>
  """
}

object VivoSolrIndexer {
  // get people uris - pass each to PersonIndexer
  //TODO : pass in solr server
  def indexPeople() = {
   // TODO: remove next line, and only use Vivo.select - hardcoding now for dev purposes 
    Vivo.initializeJenaCache("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")
    val peopleUris = Vivo.select(Vivo.sparqlPrefixes + """
      select ?person where { ?person rdf:type core:FacultyMember }
      """).map(_('person))
    println("people uris: " + peopleUris)
    for (p <- peopleUris) {
      PersonIndexer.index(p.toString)
    }
  }

}

object PersonIndexer {
// get base person data - create or update index document
// get publication uris - pass each to PublicationIndexer

  def index(uri: String) = {
    // TODO: once again Vivo object used directly - pass in?
    val personData = Vivo.select(Vivo.sparqlPrefixes + """
      SELECT distinct ?name ?title
      WHERE{
        """+uri+""" rdf:type core:FacultyMember .
        """+uri+""" rdfs:label ?name .
        OPTIONAL{ """+uri+""" core:preferredTitle ?title } .
    }
    """)
    println("data for " + uri + ":" + personData)


  }

}


object PublicationIndexer {
//take person uri + publication uri - get pub data then create or update person index document with json array of pubs
}
