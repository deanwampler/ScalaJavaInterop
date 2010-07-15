# Scala-Java Interoperability
## Dean Wampler

Lightning talk at the July 15th, 2010 Chicago-Area Scala Enthusiasts meeting.

# Introduction

This talk explores how Scala writes valid byte code and what that means for interoperation with Java. I compiled the code with `sbt` and then used `javap` to examine the byte code signatures.

## References

See the following for more details:

* **Programming Scala**, Chapter 14:
 * [Java Interoperability](http://programming-scala.labs.oreilly.com/ch14.html#JavaInterop)
 * [Java Interoperability](http://programming-scala.labs.oreilly.com/ch14.html#JavaLibraryInterop)
* Daniel Spiewak's post on [Interop Between Java and Scala](http://www.codecommit.com/blog/java/interop-between-java-and-scala)

# Character Encoding

Scala allows a wider variety of characters for type and method names than Java's `[_a-zA-Z][_a-zA-Z0-9]*`. So, Scala encodes the extra characters thusly:

    trait AllOpChars { 
      def == : Unit   // $eq$eq 
      def >  : Unit   // $greater 
      def <  : Unit   // $less 
      def +  : Unit   // $plus 
      def -  : Unit   // $minus 
      def *  : Unit   // $times 
      def /  : Unit   // $div 
      def \  : Unit   // $bslash 
      def |  : Unit   // $bar 
      def !  : Unit   // $bang 
      def ?  : Unit   // $qmark 
      def :: : Unit   // $colon$colon 
      def %  : Unit   // $percent 
      def ^  : Unit   // $up 
      def &  : Unit   // $amp 
      def @@ : Unit   // $at$at 
      def ## : Unit   // $hash$hash 
      def ~  : Unit   // $tilde 
    }
  
There appear to be some additional coding conventions for "specialized" type instantiations, e.g., `List[Double]` for `List`. I couldn't find any documentation on these. Feedback welcome!

# Classes

Classes declared in Scala vs. Java have almost identical byte code. The Scala generated code will implement a `ScalaObject` interface.

Note that the references above refer to an internal `$tag` method required by the `ScalaObject` interface. This method was removed in Scala 2.8.

# Traits vs. Interfaces

Traits with no defined methods or fields are identical to interfaces and can be used interchangeably between Java and Scala.

Traits with methods lead to the generation of a companion `Foo$class` class, which holds the method implementations.

# Generics

Scala type parameters are a superset of Java generics, _e.g.,_ covariant and contravariant subtyping.

    trait Function2[-A1, -A2, +R] {
      def apply(a1: A1, a2: A2): R
    }
    
Java only lets you specify covariant and contravariant behavior at the _call_ site, not the _definition_ site. Scala gets away from this because of type erasure! That maligned feature lets Scala "sneak in" the improved behavior.

# "Operators" Are Methods

When you write

    val list = 1 :: 2 :: 3 :: Nil
    
You are actually just calling methods on `List`.

    abstract class List[+A] {
      def ::[B >: A](e: B) = ...
      ...
    }

# Higher-Order Functions in Java

Functions in Scala are objects, e.g., a one-argument function is the following.

    trait Function1[-T, +R] {
      def apply (t: T): R
      
      def toString = ...
      def compose[A](g: A => T): A => R = ...
      def andThen[A](g: R => A): T => A = ...
    }
    
You can use these in Java! However there's a catch. In 2.7.7, you had to define `apply` and the internal method `$tag`. In 2.8.0, it appears that the new `@specialized` annotations cause problems on the Java side; you get errors for undefined `andThen` methods, e.g., for type `R` = `Double`.

To work around this, I created a concrete subclass of `Function1[String,String]` on the Scala side with a default implementation of the `apply` method. Then, on the Java side, I created a subclass that overrides `apply` to do the actual work desired.

# Interoperation with Java Libraries.

The most important issue you'll encounter, beyond what we've discussed so far, is the issue where some Java libraries, _e.g.,_ the Spring Framework, expect objects to follow JavaBeans conventions:

    public class Person {
      public Person(...) {...}
      public String getName() {...};
      public void   setName(String s) {...};
      // etc.
    }
    
Whereas the corresponding Scala class:

    case class Person(name: String, ...)
    
Uses `name` for the getter and setter method names. In this case, use the `@Jscala.reflect.BeanProperty`. See the `PersonBean` example. As written, `getName` and `setName` methods are generated. Try this experiment, remove the assignment for name, so that it is pure abstract. Compile again and look at the class file with `javap`. You'll see that just the getter is defined, not the setter, even though we declared it as a `var`.

# Notes on the examples.

The code is built using `sbt`. (The `# foo` are comments and `$` and `>` are shell and `sbt` prompts, respectively.

    $ ./sbt       # invoke the supplied sbt script
    > update      # Upate jars
    > compile     # build the code.
    
(There are no tests provided) The class files are written to the `target/scala_2.8.0.RC7/classes/`.

Use `javap`, part of the JDK distribution to see how the names, method signatures, etc. are encoded at the byte code level. (Note the space before the package name `interop`.)

    javap -classpath target/scala_2.8.0.RC7/classes/ interop.JListMapper
    javap -classpath target/scala_2.8.0.RC7/classes/ 'interop.JListMapper$' 

Note that you need to escape the '$' in the latter example. I just put the object name in single quotes.

Use `scalap`, part of the Scala distribution to see how the names, method signatures, etc. are encoded reinterpreted as Scala.

  scalap -classpath target/scala_2.8.0.RC7/classes/ interop.JListMapper
  scalap -classpath target/scala_2.8.0.RC7/classes/ 'interop.JListMapper$' 

Use the open-source tool [jad](http://varaneckas.com/jad) to attempt to reverse engineer the byte code back to working Java. It doesn't always work completely. I'm not sure why, but it might make assumptions that known `javac` compilers wrote the code and hence not properly understand idiosyncrasies of `scalac` output.

There is one executable in the code.

    scala -cp target/scala_2.8.0.RC7/classes/ interop.JFunctionMain foo bar baz

    
