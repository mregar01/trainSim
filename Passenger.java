import java.util.HashMap;

public class Passenger extends Entity {
  private Passenger(String name) { super(name); }

  private static HashMap<Integer, Passenger> passengers = new HashMap<>(); 

  public static Passenger make(String name) {
    // Change this method!
    Passenger p = new Passenger(name);
    int hash_code = p.hashCode();

    if (passengers.containsKey(hash_code)) {
      return passengers.get(hash_code);
    }
    else {
      passengers.put(hash_code, p);
      return p;
    }
  }
}