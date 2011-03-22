package edu.duke.oit.test.helpers

import edu.duke.oit.vw.solr._

object TestServers {
    val currentDirectory = new java.io.File(".").getCanonicalPath
    val solrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
    val solrSrv = Solr.solrServer(solrCfg)

    val vivo = new Vivo("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")
}
