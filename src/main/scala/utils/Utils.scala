package edu.duke.oit.jena.utils

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