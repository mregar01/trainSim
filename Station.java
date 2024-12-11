import java.util.*;


public class Station extends Entity {
  private Station(String name) { super(name); }

  private static Map<Integer, Station> stations = new HashMap<>(); 

  public static Station make(String name) {
    // Change this method!
    Station s = new Station(name);
    int hash_code = s.hashCode();
    // look up object in cache and previously created object if in cache
    if (stations.containsKey(hash_code)) {
      return stations.get(hash_code);
    }
    else {
      // add newly created object to cache and return
      stations.put(hash_code, s);
      return s;
    }
  }
}
