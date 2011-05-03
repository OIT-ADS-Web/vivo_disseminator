package edu.duke.oit.vw.queue

import org.apache.activemq.broker.{BrokerService,TransportConnector}
import java.net.URI

import edu.duke.oit.vw.solr.VivoSolrIndexer

import akka.actor.Actor
import akka.camel.{Message, Consumer}

object IndexUpdater {

  var broker:Option[BrokerService] = None
  val brokerName = "VivoWidgets"
  val queueName = "VivoChanges"

  // uri = "tcp://localhost:61616"

  def start(uri:String) = {
    broker match {
      case Some(brokerService) => brokerService
      case _ => createAndStart(uri)
    }
  }

  def createAndStart(uri:String) = {
    var bService = new BrokerService
    // val connector = new TransportConnector
    // connector.setUri(new URI(uri))
    // bService.addConnector(connector)
    // bService.setBrokerName(IndexUpdater.brokerName)
    // bService.start()
    // broker = Some(bService)

    // configure the broker
    bService.addConnector(uri)

    bService
  }

  def startConsumer(vsi:Option[VivoSolrIndexer]) = {
    // import akka.actor.Actor._
    import akka.camel.CamelServiceManager._
 
    println(">> sarting camel service")
    startCamelService
    println(">> done starting camel service")
    val indexUpdater = Actor.actorOf(new IndexUpdater(vsi)).start
    println(">> finished actor")

  }

  def checkSize = {
    broker match {
      case Some(b) => {
        println(">> checking the size")
        b.checkQueueSize(IndexUpdater.queueName)
      }
      case _ => println(">> broker not found!")
    }
  }

}

import edu.duke.oit.jena.utils._

case class UpdateMessage(uri:String, from:Option[String])


/**
 * Wraps the lift-json parsing and extraction of a person.
 */
object UpdateMessage {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats 

    val j = JsonParser.parse(json)
    j.extract[UpdateMessage]
  }
}


class IndexUpdater(vsi: Option[VivoSolrIndexer]=None) extends Actor with Consumer {

  def this() = {
    this(None)
  }

  def endpointUri = "activemq:queue:" + IndexUpdater.queueName
 
  def receive = {
    case msg:Message => { 
      println(">> received message")
      val msgString = msg.bodyAs[String]
      println(">> msgString: " +msgString)
      val updateMessage = UpdateMessage(msgString)
      vsi match {
        case Some(vsi) => {
          println(">> reindex: " + updateMessage.uri)
          vsi.reindexUri(updateMessage.uri)
        }
        case _ => println(">> No VivoSolrIndexer available for: " + msgString)
      }
    }
    case _ => { 
      println(">> no message!!")
    }
  }

}
