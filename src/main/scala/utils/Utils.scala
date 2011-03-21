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
