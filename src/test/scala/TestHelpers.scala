package edu.duke.oit.test.helpers

import edu.duke.oit.vw.solr._

object TestServers {
    val currentDirectory = new java.io.File(".").getCanonicalPath
    val widgetSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
    val widgetSolr = Solr.solrServer(widgetSolrCfg)
    val vivoSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivocore")
    val vivoSolr = Solr.solrServer(vivoSolrCfg)

    val vivo = new Vivo("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")
}
