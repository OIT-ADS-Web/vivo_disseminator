package edu.duke.oit.jena.actor

import scala.actors.Actor
import scala.actors.Actor._

import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}
import edu.duke.oit.jena.connection._
import org.scardf.jena.JenaGraph
import com.hp.hpl.jena.tdb.TDBFactory

case object Graph

case class SetModel(model: JModel)

object JenaActor extends Actor {

  def act = {
    var model: Option[JModel] = None
    loop {
      react {
        case SetModel(m: JModel) => model = Some(m)
        case Graph => {
          reply(model)
        }
        case _ => println("JenaActor message not found.")
      }
    }
  }

}

object JenaCache {

  val jenaActor = JenaActor.start

  def setFromDatabase(cInfo: JenaConnectionInfo, modelUri: String) {
    Jena.sdbModel(cInfo, modelUri) {
      dbModel =>
        var model = TDBFactory.createModel // ModelFactory.createDefaultModel
        model.add(dbModel)
        setModel(model)
    }

  }

  def setModel(m: JModel) = {
    jenaActor ! new SetModel(m)
  }

  def queryModel(query: String) = {
    val model = jenaActor !? Graph
    model match {
      case Some(m: JModel) => {
        var jg = new JenaGraph(m)
        jg.select(query)
      }
      case _ => List()
    }
  }

}
