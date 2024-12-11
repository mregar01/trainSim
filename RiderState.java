import java.util.*;

public class RiderState extends Thread {
        public MBTA mbta;
        public Log log;
        public Passenger passenger;
        public Station finalStation;
        public Station currDestination;
        public int index = 2;

        public RiderState(Passenger p, MBTA Mbta, Log currLog) {
                mbta = Mbta;
                log = currLog;
                passenger = p;
                finalStation = Mbta.trips.get(p.toString()).getLast();                
                currDestination = Mbta.trips.get(p.toString()).get(1);
        }

        public void run() {
                while (true) {
                        int currPosition = mbta.currentPosition.get(passenger);
                        Station currStation = mbta.riders.get(passenger);
                        Station nextStation = mbta.trips.get(passenger.toString()).get(currPosition + 1);
                        Train curTrain = mbta.passengerOnTrain.get(passenger);
                        if (curTrain != null) {
                                mbta.passLock.lock();
                                while (mbta.trains.get(curTrain) != nextStation) {
                                        try {
                                                mbta.passCondition.await();
                                        } catch (Exception e) {
                                                System.err.println(e);
                                        }
                                }

                                log.passenger_deboards(passenger, mbta.passengerOnTrain.get(passenger) , nextStation);
                                if (currPosition == mbta.trips.get(passenger.toString()).size() - 2) {
                                        mbta.DeboardEvent(passenger, mbta.passengerOnTrain.get(passenger), nextStation);
                                        mbta.passCondition.signalAll();
                                        mbta.passLock.unlock();
                                        return;
                                }
                                
                                mbta.DeboardEvent(passenger, mbta.passengerOnTrain.get(passenger), nextStation);
                                mbta.passCondition.signalAll();
                                mbta.passLock.unlock();
                        } else {
                                mbta.passLock.lock();
                                List<Train> incompleteTrip = new LinkedList<>();
                                for (Train t : mbta.trains.keySet()) {
                                        List<Station> currLine = mbta.lines.get(t.toString());
                                        if (currLine.contains(currStation) && currLine.contains(nextStation)) {
                                                incompleteTrip.add(t);
                                        }
                                }
                                
                                while (! incompleteTrip.contains(mbta.stationStatus.get(currStation))) {
                                        try {
                                                mbta.passCondition.await();                                                
                                        } catch (Exception e) {
                                                System.err.println(e);
                                        }
                                }

                                mbta.BoardEvent(passenger, (mbta.stationStatus.get(currStation)), currStation);
                                log.passenger_boards(passenger, (mbta.stationStatus.get(currStation)), currStation);
                                mbta.passCondition.signalAll();
                                mbta.passLock.unlock();
                        }
                }
        }
}