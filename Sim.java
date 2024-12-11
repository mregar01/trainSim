import java.io.*;
import java.util.HashMap;

public class Sim {

  public static HashMap<Train, TrainState> trainThreads = new HashMap<>();
  public static HashMap<Passenger, RiderState> riderThreads = new HashMap<>();

  public static void run_sim(MBTA mbta, Log log) {
    //add trains
    for (Train t : mbta.trains.keySet()) {
      TrainState curr = new TrainState(t, mbta, log);
      trainThreads.put(t, curr);
    }

    //add passengers
    for (Passenger p : mbta.riders.keySet()) {
      RiderState curr = new RiderState(p, mbta, log);
      riderThreads.put(p, curr);
    }

    //start trains
    for (TrainState t : trainThreads.values()) {
      t.start();
    }

    //start riders
    for (RiderState r : riderThreads.values()) {
      r.start();
    }

    try {
      //join trains
      for (TrainState t : trainThreads.values()) {
        t.join();
      }

      //join riders
      for (RiderState r : riderThreads.values()) {
        r.join();
      }
    } catch (InterruptedException e) {
      System.err.println(e);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: ./sim <config file>");
      System.exit(1);
    }

    MBTA mbta = new MBTA();
    mbta.loadConfig(args[0]);

    Log log = new Log();

    run_sim(mbta, log);

    String s = new LogJson(log).toJson();
    PrintWriter out = new PrintWriter("log.json");
    out.print(s);
    out.close();

    mbta.reset();
    mbta.loadConfig(args[0]);
    Verify.verify(mbta, log);
  }
}
