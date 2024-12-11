public class TrainState extends Thread {
    public Train train;
    public MBTA mbta;
    public Log log;

    public TrainState(Train t, MBTA Mbta, Log currLog) {
        train = t;
        mbta = Mbta;
        log = currLog;
    }

    public void run() {
        mbta.passLock.lock();

        while (! mbta.trips.isEmpty()) {
            try {
                mbta.passCondition.signalAll();
                mbta.passLock.unlock();
                sleep(10);
            } catch (Exception e) {
                System.err.println(e);
            }

            mbta.trainLock.lock();
            Station currStation = mbta.trains.get(train);

            int currIndex = 0;
            for (int i = 0; i < mbta.lines.get(train.toString()).size(); i++) {
                if (mbta.lines.get(train.toString()).get(i) == currStation) {
                    currIndex = i;
                }
            }

            if (currIndex == mbta.lines.get(train.toString()).size() - 1) {                
                currIndex --;
            } else if (currIndex == 0){
                // direction.put(t, true);
                currIndex++;
            } else if (mbta.direction.get(train) == true) {
                currIndex++;
            } else {
                currIndex--;
            }
        

            Station nextStation = mbta.lines.get(train.toString()).get(currIndex);

            while (mbta.stationStatus.get(nextStation) != null) {
                if (mbta.trips.isEmpty()) {
                    mbta.trainCondition.signalAll();
                    mbta.trainLock.unlock();
                    return;
                }
                try {
                    mbta.trainCondition.await();
                } catch (Exception e) {
                    System.err.println(e);
                }
            }

            mbta.passLock.lock();
            mbta.moveEvent(train, currStation, nextStation);
            log.train_moves(train, currStation, nextStation);
            mbta.trainCondition.signalAll();
            mbta.trainLock.unlock();

        }

        mbta.passCondition.signalAll();
        mbta.passLock.unlock();
        return;
    }
}