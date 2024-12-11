import java.util.HashMap;
import java.util.Map;

public class Train extends Entity {
  private static Map<Integer, Train> trains = new HashMap<>();

  private Train(String name) { super(name); }

  public static Train make(String name) {
    // create new object
    Train t = new Train(name);
    int hash_code = t.hashCode();
    // look up object in cache and previously created object if in cache
    if (trains.containsKey(hash_code)) {
      return trains.get(hash_code);
    }
    else {
      // add newly created object to cache and return
      trains.put(hash_code, t);
      return t;
    }
  }
}
