package interop;

// import static interop.JListMapper$;
import scala.Function1;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class JFunctionMain {
  public static void main(String[] args) {
    java.util.List<String> argList = new ArrayList<String>();
    for (int i=0; i<args.length; i++)
      argList.add(args[i]);

    // Can't do this in 2.8.0, because of @specialized. See the README.
    // Function1<String,String> toUpper = 
    //    new Function1<String,String>() {
    //  public String apply(String s) {
    //    return s.toUpperCase();
    //  }
    // };

    Function1<String,String> toUpper = new JListMapper$StringToStringFunction1() {
      public String apply(String s) {
        return s.toUpperCase();
      }
    };
    
    // Note that we pass one, 2-argument list to "map", rather
    // than two, 1-argument lists, as declared in the Scala code.
    List<String> uppers = JListMapper$.MODULE$.map(argList, toUpper); 
    Iterator<String> iter = uppers.iterator(); 
    while (iter.hasNext())
      System.out.println(iter.next());    
  }
}