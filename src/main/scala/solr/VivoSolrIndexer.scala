package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.jena.connection._
import edu.duke.oit.jena.actor.JenaCache

class Vivo(url: String, user: String, password: String, dbType: String, driver: String) {
  def initializeJenaCache() = {
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

class VivoSolrIndexer(vivo: Vivo, solr: SolrServer) {
  def indexPeople() = {
   // TODO: remove next line, and only use vivo.select - hardcoding now for dev purposes 
   // vivo.select should have cached/live argument and vivo instance should know whether to init or not
    vivo.initializeJenaCache()
    val peopleUris = vivo.select(vivo.sparqlPrefixes + """
      select ?person where { ?person rdf:type core:FacultyMember }
      """).map(_('person))
    println("people uris: " + peopleUris)
    for (p <- peopleUris) {
      PersonIndexer.index(p.toString,vivo,solr)
    }
  }

}

object PersonIndexer {

  def index(uri: String,vivo: Vivo,solr: SolrServer) = {
    val personData = vivo.select(vivo.sparqlPrefixes + """
      SELECT distinct ?name ?title
      WHERE{
        """+uri+""" rdf:type core:FacultyMember .
        """+uri+""" rdfs:label ?name .
        OPTIONAL{ """+uri+""" core:preferredTitle ?title } .
    }
    """)
    println("data for " + uri + ":" + personData)

    val publicationData = vivo.select(vivo.sparqlPrefixes + """
      select *
      where {
        """+uri+""" core:authorInAuthorship ?authorship .
        ?publication core:informationResourceInAuthorship ?authorship .
        ?publication rdfs:label ?title .
        OPTIONAL { ?publication bibo:numPages ?numPages . }
        OPTIONAL { ?publication bibo:edition ?edition . }
        OPTIONAL { ?publication bibo:volume ?volume . }
        OPTIONAL { ?publication bibo:year ?year . }
        OPTIONAL { ?publication bibo:issue ?issue . }
        OPTIONAL { ?publication core:hasPublicationVenue ?publicationVenue . ?publicationVenue rdfs:label ?publishedIn . }
        OPTIONAL { ?publication core:publisher ?publisher. ?publisher rdfs:label ?publishedBy . }
        OPTIONAL { ?publication bibo:pageStart ?startPage .}
        OPTIONAL { ?publication bibo:pageEnd ?endPage }
      }
    """)
    println("pubData for " + uri + ": " + publicationData)

   // grab uri's - get author data
   val publicationURIs = publicationData.map(_('publication))
   println("pub URIs: " + publicationURIs)
   for(pubURI <- publicationURIs) {
     val p = vivo.select(vivo.sparqlPrefixes + """
      select ?authorName ?rank
      where {
        """+pubURI+""" core:informationResourceInAuthorship ?authorship .
        ?authorship core:linkedAuthor ?author .
        ?author rdfs:label ?authorName .
        OPTIONAL { ?authorship core:authorRank ?rank }
      }
    """)
    println("authorData for " + pubURI + ": " + p)
   }

  }

}
