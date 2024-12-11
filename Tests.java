import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;;

public class Tests {
  @Test public void testPass() {
    assertTrue("true should be true", true);
  }

  @Test public void loadConfigTest() {
    MBTA mbta = new MBTA();
    mbta.loadConfig("sample.json");
    // mbta.printAll();
  }


  @Test public void verifyTest() {
    MBTA mbta = new MBTA();
    mbta.addLine("DC", new LinkedList<>(Arrays.asList("Tenley", "Dupont", "Georgetown")));
    mbta.addJourney("Tourist", new LinkedList<>(Arrays.asList("Tenley", "Georgetown")));
    Train train = Train.make("DC");
    Station station1 = Station.make("Tenley");
    Station station2 = Station.make("Dupont");
    Station station3 = Station.make("Georgetown");
    Passenger passenger = Passenger.make("Tourist");
    Log log = new Log(List.of(new BoardEvent(passenger, train, station1),
                              new MoveEvent(train, station1, station2),
                              new MoveEvent(train, station2, station3),
                              new DeboardEvent(passenger, train, station3)));
    Verify.verify(mbta, log);

  }

  @Test public void simpleVerifyTest(){
    MBTA m = new MBTA();
    
    m.addLine("l1", List.of("l1_s1", "l1_s2", "l1_s3", "shared", "l1_s5"));
    m.addLine("l2", List.of("l2_s1", "shared", "l2_s3", "l2_s4"));
    
    Train one = Train.make("l1");
    Train two = Train.make("l2");
    Passenger p1 = Passenger.make("p1");
     
    Station l1_s1 = Station.make("l1_s1");
    Station l1_s2 = Station.make("l1_s2");
    Station l1_s3 = Station.make("l1_s3");
    Station shared = Station.make("shared");
    Station l1_s5 = Station.make("l1_s5");
    
    Station l2_s1 = Station.make("l2_s1");
    Station l2_s3 = Station.make("l2_s3");
    Station l2_s4 = Station.make("l2_s4");
    
    m.addJourney("p1", List.of("l1_s1",  "shared",  "l2_s4"));
    
    Log log = new Log(List.of(
              new BoardEvent(p1, one, l1_s1),
              new MoveEvent(one, l1_s1, l1_s2),
              new MoveEvent(one, l1_s2, l1_s3),
              new MoveEvent(one, l1_s3, shared),
              new DeboardEvent(p1, one, shared),
              new MoveEvent(one, shared, l1_s5),
              new MoveEvent(two, l2_s1, shared),
              new BoardEvent(p1, two, shared),
              new MoveEvent(two, shared, l2_s3),
              new MoveEvent(two, l2_s3, l2_s4),
              new DeboardEvent(p1, two, l2_s4)));
    
    Verify.verify(m,log);
  }

  @Test public void test2() {
    MBTA new_mbta = new MBTA();
    Log log = new Log();
  
    Station s1 = Station.make("s1");
    Station s2 = Station.make("s2");
    Station s3 = Station.make("s3");
    Station s4 = Station.make("s4");
    Passenger p1 = Passenger.make("p1");
    Passenger p2 = Passenger.make("p2");
    Train t1 = Train.make("t1");
  
    new_mbta.addLine("t1", List.of("s1", "s2", "s3", "s4"));
    new_mbta.addJourney("p1", List.of("s2", "s3"));
    new_mbta.addJourney("p2", List.of("s1", "s3", "s2"));
  
    Log events = new Log(List.of(
            new BoardEvent(p2, t1, s1),
            new MoveEvent(t1, s1, s2),
            new BoardEvent(p1, t1, s2),
            new MoveEvent(t1, s2, s3),
            new DeboardEvent(p2, t1, s3),
            new DeboardEvent(p1, t1, s3),
            new MoveEvent(t1, s3, s4),
            new MoveEvent(t1, s4, s3),
            new BoardEvent(p2, t1, s3)
    ));
  
    try {
      Verify.verify(new_mbta, events);
      throw new RuntimeException();
    } catch (Exception e) {}
  }

  @Test 
    public void testVerificationTrip1() {
      MBTA mbta = new MBTA();
      Passenger jeff = Passenger.make("Jeff");
      Station newyorkPenn = Station.make("New York");
      Station eastOrange = Station.make("East Orange");
      Station brickChurch = Station.make("Brick Church");
      Station southOrange = Station.make("South Orange");
      Station maplewood = Station.make("Maplewood");
      Train morrisEssex = Train.make("Morris Essex");

      List<String> morrisessexline = new ArrayList<>();
      morrisessexline.add("New York");
      morrisessexline.add("East Orange");
      morrisessexline.add("Brick Church");
      morrisessexline.add("South Orange");
      morrisessexline.add("Maplewood");

      List<String> jeffJourney = new ArrayList<>();
      jeffJourney.add("East Orange");
      jeffJourney.add("Maplewood");

      mbta.addLine("Morris Essex", morrisessexline);
      mbta.addJourney("Jeff", jeffJourney);

      List<Event> events = new ArrayList<>();
      events.add(new MoveEvent(morrisEssex, newyorkPenn, eastOrange));
      events.add(new BoardEvent(jeff, morrisEssex, eastOrange));
      events.add(new MoveEvent(morrisEssex, eastOrange, brickChurch));
      events.add(new MoveEvent(morrisEssex, brickChurch, southOrange));
      events.add(new MoveEvent(morrisEssex, southOrange, maplewood));
      events.add(new DeboardEvent(jeff, morrisEssex, maplewood));

      Log e = new Log(events);

      Verify.verify(mbta, e);
    }

    @Test public void verifyTransfer() {
      // Set up //
      MBTA mbta = new MBTA();
      
      List<String> lineA = Arrays.asList("1", "2", "3");
      List<String> lineB = Arrays.asList("0", "1", "3", "5");
      List<String> lineC = Arrays.asList("5", "4");

      List<String> riderA = Arrays.asList("1", "5", "4");
      List<String> riderB = Arrays.asList("2", "1", "5", "4");
      List<String> riderC = Arrays.asList("5", "3", "1", "2");

      Station s0 = Station.make("0");
      Station s1 = Station.make("1");
      Station s2 = Station.make("2");
      Station s3 = Station.make("3");
      Station s4 = Station.make("4");
      Station s5 = Station.make("5");

      Train trainA = Train.make("LineA");
      Train trainB = Train.make("LineB");
      Train trainC = Train.make("LineC");

      mbta.addLine("LineA", lineA);
      mbta.addLine("LineB", lineB);
      mbta.addLine("LineC", lineC);

      Passenger a = Passenger.make("A");
      Passenger b = Passenger.make("B");
      Passenger c = Passenger.make("C");

      mbta.addJourney("A", riderA);
      mbta.addJourney("B", riderB);
      mbta.addJourney("C", riderC);

      

      // Journey //
      Log logger = new Log();
      logger.train_moves(trainA, s1, s2);       // The trainA goes from "1" to "2"
      logger.train_moves(trainB, s0, s1);       // The trainB goes from "0" to "1"
      logger.passenger_boards(a, trainB, s1);   // "a" gets on the trainB at "1"
      logger.train_moves(trainB, s1, s3);       // The trainB goes from "1" to "3"
      logger.train_moves(trainC, s5, s4);       // The trainC goes from "5" to "4"
      logger.train_moves(trainB, s3, s5);       // The trainB goes from "3" to "5"
      logger.train_moves(trainA, s2, s3);       // The trainA goes from "2" to "3"
      logger.train_moves(trainA, s3, s2);       // The trainA goes from "3" to "2"
      logger.passenger_deboards(a, trainB, s5); // "a" gets off of the trainB at "5"
      logger.passenger_boards(c, trainB, s5);   // "c" gets on the trainB at "5"
      logger.passenger_boards(b, trainA, s2);   // "b" gets on the trainA at "2"
      logger.train_moves(trainA, s2, s1);       // The trainA goes from "2" to "1"
      //failing after prev
      logger.passenger_deboards(b, trainA, s1); // "b" gets oof of the trainA at "1"
      logger.train_moves(trainB, s5, s3);       // The trainB goes from "5" to "3"
      logger.train_moves(trainC, s4, s5);       // The trainC goes from "4" to "5"
      logger.passenger_boards(a, trainC, s5);   // "a" gets on the trainC at "5"
      logger.passenger_deboards(c, trainB, s3); // "c" gets off of the trainB at "3"
      logger.passenger_boards(c, trainB, s3);   // "c" gets on the trainB at "3"
      logger.train_moves(trainA, s1, s2);       // The trainA goes from "1" to "2"
      logger.train_moves(trainB, s3, s1);       // The trainB goes from "3" to "1"
      logger.train_moves(trainA, s2, s3);       // The trainA goes from "2" to "3"
      logger.train_moves(trainA, s3, s2);       // The trainA goes from "3" to "2"
      logger.passenger_deboards(c, trainB, s1); // "c" gets off of the trainB at "1"
      logger.passenger_boards(b, trainB, s1);   // "b" gets on the the trainB at "1"
      logger.train_moves(trainB, s1, s0);       // The trainB goes from "1" to "0"
      logger.train_moves(trainB, s0, s1);       // The trainB goes from "0" to "1"
      logger.train_moves(trainB, s1, s3);       // The trainB goes from "1" to "3"
      logger.train_moves(trainA, s2, s1);       // The trainA goes from "2" to "1"
      logger.passenger_boards(c, trainA, s1);   // "c" gets on the trainA at "1"
      logger.train_moves(trainA, s1, s2);       // The trainA goes from "1" to "2"
      logger.train_moves(trainC, s5, s4);       // The trainC goes from "5" to "4"
      logger.passenger_deboards(a, trainC, s4);
      logger.train_moves(trainB, s3, s5);       // The trainB goes from "3" to "5"
      logger.passenger_deboards(b, trainB, s5); // "b" gets off of the trainB at "5"
      logger.train_moves(trainB, s5, s3);       // The trainB goes from "5" to "3"
      logger.passenger_deboards(c, trainA, s2); // "c" gets off of the trainA at "2" (DONE)
      logger.train_moves(trainC, s4, s5);       // The trainC goes from "4" to "5"
      logger.passenger_boards(b, trainC, s5);   // "b" gets on the trainC at "5"
      logger.train_moves(trainC, s5, s4);       // The trainC goes from "5" to "4"
      logger.passenger_deboards(b, trainC, s4); // "b" gets off of the trainC at "4" (DONE)
  
      Verify.verify(mbta, logger);
  }

  @Test public void AutoTest() {
    MBTA mbta = new MBTA();
    mbta.addLine("d", new LinkedList<>(Arrays.asList("J", "K", "L")));

    Train d = Train.make("d");

    Station J = Station.make("J");
    Station K = Station.make("K");
    Station L = Station.make("L");

    Passenger Bob = Passenger.make("Bob");

    mbta.addJourney("Bob", Arrays.asList("J", "L"));   

    Log log = new Log();
    log.passenger_boards(Bob, d, J);
    log.train_moves(d, J, K);
    log.train_moves(d, K, L);
    log.passenger_deboards(Bob, d, L);

    Verify.verify(mbta, log);
  }

  
}