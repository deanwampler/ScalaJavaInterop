package interop

trait AbstractPerson {
  def name: String
  def name(s: String): Unit
  def valid(s: String): Boolean
}

trait Person {
  var name: String
  def valid(s: String): Boolean = s.length > 0
}

trait PersonBean {
  @scala.reflect.BeanProperty
  var name: String = "" 
}