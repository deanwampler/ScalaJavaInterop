package interop

object JListMapper {
  def map[T1,T2](list: java.util.List[T1])(f: T1 => T2): java.util.ArrayList[T2] = {
    val newList = new java.util.ArrayList[T2]
    val iter = list.iterator
    while (iter.hasNext) 
      newList.add(f(iter.next))
    newList
  }
  
  // See README
  class StringToStringFunction1 extends Function1[String,String] {
    def apply(s: String) = s // default implementation; override in actual use.
  }
}