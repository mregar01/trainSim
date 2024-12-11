import java.io.*;
import java.io.ObjectInputFilter.Config;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.*;

import javax.lang.model.UnknownEntityException;
import javax.lang.model.element.UnknownElementException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.gson.*;

import com.google.gson.Gson;

import java.io.IOException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MBTA {

  // Creates an initially empty simulation
  public MBTA() { }

  public HashMap<String, List<Station>> lines = new HashMap<>();
  public HashMap<String, List<Station>> trips = new HashMap<>();
  public HashMap<Train, Station> trains = new HashMap<>();
  public HashMap<Train, List<Passenger>> onTrains = new HashMap<>();
  public HashMap<Passenger, Station> riders = new HashMap<>();
  public HashMap<Station, Train> stationStatus = new HashMap<>();
  public HashMap<Passenger, Train> passengerOnTrain = new HashMap<>();
  public HashMap<Train, Boolean> direction = new HashMap<>();
  public HashMap<Passenger, Integer> currentPosition = new HashMap<>();

  public Lock trainLock = new ReentrantLock();
  public Condition trainCondition = trainLock.newCondition();

  public Lock passLock = new ReentrantLock();
  public Condition passCondition = passLock.newCondition();


  // Adds a new transit line with given name and stations
  public void addLine(String name, List<String> stations) {
    List<Station> all_stat = new LinkedList<>();
    Train t = Train.make(name);
    for (String s : stations) {
      Station curr = Station.make(s);
      all_stat.add(curr);
    }
    lines.put(name, all_stat);
    trains.put(t, all_stat.get(0));
    onTrains.put(t, new LinkedList<>());

    for (Station s : all_stat) {
      stationStatus.put(s, null);
    }

    stationStatus.put(all_stat.get(0), t);
    direction.put(t, true);
    
  }

  // Adds a new planned journey to the simulation
  public void addJourney(String name, List<String> stations) {
    List<Station> all_stat = new LinkedList<>();
    Passenger p = Passenger.make(name);
    for (String s : stations) {
      all_stat.add(Station.make(s));
    }

    trips.put(name, all_stat);
    riders.put(p, all_stat.get(0));
    passengerOnTrain.put(p, null);
    currentPosition.put(p, 0);

  }

  // Return normally if initial simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkStart() {

    for (String l : lines.keySet()) {
      Train curr = Train.make(l);
      if (!trains.get(curr).equals(lines.get(l).get(0)) || !trains.containsKey(curr)) {
        throw new RuntimeException();
      }
    }

    for (String p : trips.keySet()) {
      Passenger curr = Passenger.make(p);
      if (!riders.containsKey(curr) || !riders.get(curr).equals(trips.get(p).get(0))) {
        throw new RuntimeException();
      }
    }
    
  }

  // Return normally if final simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkEnd() {
    if (!trips.isEmpty()) {
      throw new RuntimeException();
    }

    if (!riders.isEmpty()) {
      throw new RuntimeException();
    }

    for (Train curr : onTrains.keySet()) {
      if (!onTrains.get(curr).isEmpty()) {
        throw new RuntimeException();
      }
    }
  }

  // reset to an empty simulation
  public void reset() {
    lines.clear();
    trips.clear();
    trains.clear();
    onTrains.clear();
    riders.clear();
    stationStatus.clear();
    passengerOnTrain.clear();
  }

  // adds simulation configuration from a file
  public void loadConfig(String filename) {
    Gson gson = new Gson();
    try {
      String json_content = new String(Files.readAllBytes(Paths.get(filename)));
      Format f = gson.fromJson(json_content, Format.class);
      for (String line: f.lines.keySet()) {
        addLine(line, f.lines.get(line));
      }
      for (String trip: f.trips.keySet()) {
        addJourney(trip, f.trips.get(trip));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void moveEvent(Train t, Station s1, Station s2){
    if (!lines.containsKey(t.toString())){
      throw new RuntimeException();
    }

    if (!lines.get(t.toString()).contains(s1)){
      throw new RuntimeException();
    }

    if (!lines.get(t.toString()).contains(s2)){
      throw new RuntimeException();
    }

    //check curr station accuracy
    if (trains.get(t) != s1) {
      throw new RuntimeException();
    }

    //get index of curr station
    int index = 0;
    for (int i = 0; i < lines.get(t.toString()).size(); i++) {
      if (lines.get(t.toString()).get(i) == s1) {
        index = i;
      }
    }

    //increment index if at end or beginning
    if (index == lines.get(t.toString()).size() - 1) {
      direction.put(t, false);
      index --;
    } else if (index == 0){
      direction.put(t, true);
      index++;
    } else if (direction.get(t) == true) {
      index++;
    } else {
      index--;
    }

    //check station two accuracy
    if (lines.get(t.toString()).get(index) != s2) {
      throw new RuntimeException();
    }

    //check if there is a train at the next station
    if (stationStatus.get(s2) != null) {
      throw new RuntimeException();
    }

    trains.put(t, s2);
    stationStatus.put(s1, null);
    stationStatus.put(s2, t);

    for (Passenger p : onTrains.get(t)) {
      riders.put(p, s2);
    }
  }

  public void BoardEvent(Passenger p, Train t, Station s){
    if (!lines.containsKey(t.toString())){
      System.err.println("fail 1");
      throw new RuntimeException();
    }

    if (!lines.get(t.toString()).contains(s)){
      System.err.println("fail 2");
      throw new RuntimeException();
    }
   
    if (!trips.containsKey(p.toString())){
      System.err.println("fail 3");
      throw new RuntimeException();
    }

    //make sure it can board here
    if (!trips.get(p.toString()).contains(s)) {
      throw new RuntimeException();
    }

    //make sure train at stop
    if (trains.get(t) != s) {
      throw new RuntimeException();
    }

    //make sure passenger isn't on a train
    if (passengerOnTrain.get(p) != null) {
      throw new RuntimeException();
    }

    onTrains.get(t).add(p);
    passengerOnTrain.put(p, t);

  }

  public void DeboardEvent(Passenger p, Train t, Station s){
    if (!lines.containsKey(t.toString())){
      System.err.println("fail 1");
      throw new RuntimeException();
    }

    if (!lines.get(t.toString()).contains(s)){
      System.err.println("fail 2");
      throw new RuntimeException();
    }
   
    if (!trips.containsKey(p.toString())){
      System.err.println("fail 3");
      throw new RuntimeException();
    }

    //make sure it can deboard here
    if (!trips.get(p.toString()).contains(s)) {
      throw new RuntimeException();
    }

    //make sure train at stop
    if (trains.get(t) != s) {
      throw new RuntimeException();
    }

    //make sure passenger is on correct train
    if (passengerOnTrain.get(p) != t) {
      throw new RuntimeException();
    }

    //take off train
    onTrains.get(t).remove(p);
    passengerOnTrain.put(p, null);

    //remove them if final stop
    if (s == trips.get(p.toString()).getLast()) {
      trips.remove(p.toString());
      riders.remove(p);
    }

    int index = currentPosition.get(p);
    currentPosition.put(p, index+1);
  }
}
