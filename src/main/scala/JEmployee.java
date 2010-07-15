package interop;

public class JEmployee implements Person {
  
  private String _name = "";
  
  public String name() {
    return _name;
  }
  public void name_$eq(String s) {
    _name = s;
  }

  public boolean valid(String s) {
    return Person$class.valid(this, s);
  }
}