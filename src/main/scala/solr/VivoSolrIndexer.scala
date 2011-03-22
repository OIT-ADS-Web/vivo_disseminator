package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument
import org.scardf._
import org.scardf.Node
import org.scardf.NodeConverter._

import edu.duke.oit.jena.connection._
import edu.duke.oit.jena.actor.JenaCache
import edu.duke.oit.jena.utils._

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

  def numPeople = asInt(select(sparqlPrefixes + """
    select (count(?p) as ?numPeople) where { ?p rdf:type core:FacultyMember }
  """)(0)('numPeople).asInstanceOf[NodeFromGraph])

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
    for (p <- peopleUris) {
      PersonIndexer.index(p.toString,vivo,solr)
    }
    solr.commit()
  }

}

object PersonIndexer extends SimpleConversion {

  def index(uri: String,vivo: Vivo,solr: SolrServer) = {
    val personData = vivo.select(vivo.sparqlPrefixes + """
      SELECT *
      WHERE{
        """+uri+""" vitro:moniker ?title .
        """+uri+""" rdf:type ?type .
        """+uri+""" rdfs:label ?name .
        FILTER(?type = foaf:Person) .
    }
    """)

     val publicationData = vivo.select(vivo.sparqlPrefixes + """
       select *
       where {
         """+uri+""" core:authorInAuthorship ?authorship .
         ?publication core:informationResourceInAuthorship ?authorship .
         ?publication rdfs:label ?title .
         ?publication rdf:type ?type .
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

   val pubs: List[Publication] = publicationData.map( pub => new Publication(uri      = getString(pub('publication)),
                                                                             vivoType = getString(pub('type)),
                                                                             title    = getString(pub('title)),
                                                                             authors  = getAuthors(getString(pub('publication)),vivo),
                                                                             extraItems = parseExtraItems(pub,List('publication,'type,'title)))).asInstanceOf[List[Publication]]

    val p = new Person(uri,
                       vivoType = getString(personData(0)('type)),
                       name     = getString(personData(0)('name)),
                       title    = getString(personData(0)('title)),
                       publications = pubs,
                       extraItems = parseExtraItems(personData(0),List('type,'name,'title)))
    val solrDoc = new SolrInputDocument()
    solrDoc.addField("id",p.uri)
    solrDoc.addField("json",Person.json(p))
    solr.add(solrDoc)
  }

  def parseExtraItems(resultMap: Map[QVar,Node], requiredKeys: List[QVar]): Option[Map[String,String]] = {
    val extraItems = resultMap -- requiredKeys
    Option(extraItems.map(kvp => (kvp._1.name -> getString(kvp._2))))
  }

  def getAuthors(pubURI: String, vivo: Vivo): List[String] = {
    val authorData = vivo.select(vivo.sparqlPrefixes + """
     select ?authorName ?rank
     where {
       """+pubURI+""" core:informationResourceInAuthorship ?authorship .
       ?authorship core:linkedAuthor ?author .
       ?author rdfs:label ?authorName .
       OPTIONAL { ?authorship core:authorRank ?rank }
     }
    """)
    val authorsWithRank = authorData.map(a => (getString(a('authorName)),getString(a.getOrElse('rank, Node.from("0")))))
    authorsWithRank.sortWith((a1,a2) => (a1._2.toInt < a2._2.toInt)).map(_._1)
  }
}
