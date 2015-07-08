package com.opentransport.rdfmapper.old;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.opentransport.rdfmapper.StationDatabase;
import com.opentransport.rdfmapper.containers.LiveBoard;
import com.opentransport.rdfmapper.containers.NextStop;
import com.opentransport.rdfmapper.containers.Service;
import com.opentransport.rdfmapper.containers.StationInfo;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Njcola De Clercq
 */
public class OldMapper {
    
    public void map() {
        Model model = ModelFactory.createDefaultModel();
        String transit = "http://vocab.org/transit/terms/";
        model.setNsPrefix("transit",transit);
        String stationPrefix = transit + "station#";
        model.setNsPrefix("station",stationPrefix);
        
        final OldLiveBoardFetcher liveBoardFetcher = new OldLiveBoardFetcher();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        List<Future<LiveBoard>> futures = new ArrayList<>();
        
//        final List<String> stations = new ArrayList<>();
//        stations.add("008894508");  //sint-niklaas
//        stations.add("008894201");  //lokeren
//        stations.add("008894433");  //belsele
//        stations.add("008894425");  //sinaai
//        stations.add("008894714");  //nieuwkerken-waas
//        stations.add("008894748");  //beveren
//        stations.add("008893120");  //gent-dampoort
//        stations.add("008894151");  //beervelde
//        stations.add("008894755");  //melsele
//        stations.add("008894821");  //zwijndrecht
        
        final List<String> stations = StationDatabase.getInstance().getAllStationIds();
        
        for (int i = 0; i < stations.size(); i++) {
            final String station = stations.get(i);
            final int number = i + 1;
            Future<LiveBoard> future = pool.submit(new Callable<LiveBoard>() {
                @Override
                public LiveBoard call() throws Exception {
                    System.out.println("START FETCH " + number);
                    long start = System.currentTimeMillis();
                    LiveBoard liveBoard = liveBoardFetcher.getLiveBoard(station,"","",10);
                    long stop = System.currentTimeMillis();
                    System.out.println("STOP FETCH " + number + "  "
                            + liveBoard.getStationInfo().getName()
                            + " (" + (stop - start) + " ms)");
                    return liveBoard;
                }
            });
            futures.add(future);
        }
        
        for (int i = 0; i < futures.size(); i++) {
            LiveBoard liveBoard = null;
            try {
                liveBoard = futures.get(i).get(3000,TimeUnit.MILLISECONDS);
                System.out.println((i + 1) + "  " + liveBoard.getStationInfo().getName());
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                final String station = stations.get(i);
                stations.add(station);
                final int number = i + 1;
                Future<LiveBoard> future = pool.submit(new Callable<LiveBoard>() {
                    @Override
                    public LiveBoard call() throws Exception {
                        System.out.println("RESTART FETCH " + number);
                        long start = System.currentTimeMillis();
                        LiveBoard liveBoard = liveBoardFetcher.getLiveBoard(station,"","",10);
                        long stop = System.currentTimeMillis();
                        System.out.println("RESTOP FETCH " + number + "  "
                                + liveBoard.getStationInfo().getName()
                                + " (" + (stop - start) + " ms)");
                        return liveBoard;
                    }
                });
                futures.add(future);
                continue;
            }
            StationInfo info = liveBoard.getStationInfo();
            Resource station = model.createResource(transit + "station/" + info.getId());
            Property stationName = model.createProperty(stationPrefix + "name");
            station.addProperty(stationName,info.getName());
            Property stationLongitude = model.createProperty(stationPrefix + "longitude");
            station.addProperty(stationLongitude,info.getLongitude());
            Property stationLatitude = model.createProperty(stationPrefix + "latitude");
            station.addProperty(stationLatitude,info.getLatitude());

            List<Service> services = liveBoard.getServices();

            for (int j = 0; j < services.size(); j++) {
                Service service = services.get(j);
                Property stationService = model.createProperty(transit + "service");
                Resource blank = model.createResource();
                station.addProperty(stationService,blank);
                Property departureTime = model.createProperty(transit + "departureTime");
                blank.addProperty(departureTime,service.getScheduledDepartureTime());
                Property delay = model.createProperty(transit + "delay");
                blank.addProperty(delay,service.getDelay());
                Property destination = model.createProperty(transit + "toStop");
                blank.addProperty(destination,service.getDestination());
                Property trainNumber = model.createProperty(transit + "trainNumber");
                blank.addProperty(trainNumber,service.getTrainNumber());
                Property platform = model.createProperty(transit + "platform");
                blank.addProperty(platform,service.getPlatform());
                NextStop nextStop = service.getNextStop();
                if (nextStop != null) {
                    Property nextStopOfService = model.createProperty(transit + "nextStop");
                    Resource blank2 = model.createResource();
                    blank.addProperty(nextStopOfService,blank2);
                    Property arrivalTime = model.createProperty(transit + "arrivalTime");
                    blank2.addProperty(arrivalTime,nextStop.getScheduledArrivalTime());
                    Property nextStopDelay = model.createProperty(transit + "delay");
                    blank2.addProperty(nextStopDelay,nextStop.getDelay());
                    Property nextStopName = model.createProperty(stationPrefix + "name");
                    blank2.addProperty(nextStopName,nextStop.getName());
                    Property nextStopPlatform = model.createProperty(transit + "platform");
                    blank2.addProperty(nextStopPlatform,nextStop.getPlatform());
                    String id = StationDatabase.getInstance().getStationId(nextStop.getName());
                    Resource nextStopURI = model.createResource(transit + "station/" + id);
                    Property stop = model.createProperty(transit + "stop");
                    blank2.addProperty(stop,nextStopURI);
                }
            }
        }
        
        pool.shutdownNow();
        
        try {
            FileWriter fw = new FileWriter("test.ttl");
            model.write(fw,"TTL");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(OldMapper.class.getName()).log(Level.SEVERE,null,ex);
        }
        
    }

}