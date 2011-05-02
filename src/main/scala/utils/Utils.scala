package edu.duke.oit.jena.utils

import org.scardf._
import org.scardf.NodeConverter._

trait ToMethods {
  implicit def toMethods(obj: AnyRef) = new {
    def methods = obj.getClass.getMethods.map(_.getName)
  }
}

trait Timer {
  def timer(label: String = "")(continue: => Unit) = {
    var a = System.currentTimeMillis
    continue
    var b = System.currentTimeMillis
    println(label + " | Total Time(msec): " + (b.toInt - a.toInt))
  }
}

trait SimpleConversion {

  def getString(node: Node): String = {
    node match {
      case n: PlainLiteral => n / asString
      case b: TypedLiteral => {
        b.isLiteral match {
          case true => b / asLexic
          case _ => b.toString
        }
      }
      case _ => node.toString
    }
  }

}

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
