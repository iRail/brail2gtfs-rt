package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.Departure;
import com.opentransport.rdfmapper.nmbs.containers.LiveBoard;
import com.opentransport.rdfmapper.nmbs.containers.NextStop;
import com.opentransport.rdfmapper.nmbs.containers.Service;
import com.opentransport.rdfmapper.nmbs.containers.Station;
import com.opentransport.rdfmapper.nmbs.containers.StationInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicola De Clercq
 */
public class SortedMapper {
    
    private final List<Station> stations;
    private final List<Departure> departures;
    private List<byte[]> turtleStations;
    private List<byte[]> turtleDepartures;
    private List<byte[]> linkedcsvStations;
    private List<byte[]> linkedcsvDepartures;
    private final long lastModified;
    
    public SortedMapper() {
        stations = new ArrayList<>();
        departures = new ArrayList<>();
        map();
        
        mapTurtle();
        mapLinkedcsv();
        
        stations.clear();
        departures.clear();
        
        lastModified = System.currentTimeMillis();
    }
    
    private void map() {
        SortedMap<Calendar,List<Departure>> departuresMap = new TreeMap<>();
        
        Set<String> missingStations = new HashSet<>();
        
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SortedMapper.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        String stationsNMBS = "http://irail.be/stations/NMBS/";
        
        List<String> stationIds = StationDatabase.getInstance().getAllStationIds();
        
        LiveBoardFetcher liveBoardFetcher = new LiveBoardFetcher();
        List<LiveBoard> liveBoards = liveBoardFetcher.getLiveBoards(stationIds,"","",10000);
        
        SimpleDateFormat isoSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        SimpleDateFormat uriSDF = new SimpleDateFormat("yyyyMMddHHmm");
        
        long start = System.currentTimeMillis();
        long sortingTime = 0;
        
        for (int i = 0; i < liveBoards.size(); i++) {
            LiveBoard liveBoard = liveBoards.get(i);
            StationInfo info = liveBoard.getStationInfo();
            String stationId = stationsNMBS + info.getId();
            stations.add(new Station(stationId,info.getName(),info.getLatitude(),
                    info.getLongitude()));
            
            List<Service> services = liveBoard.getServices();

            for (int j = 0; j < services.size(); j++) {
                Service service = services.get(j);
                Calendar actDepT = Calendar.getInstance();
                String schedDepT = service.getScheduledDepartureTime();
                String uriDepT = "";
                try {
                    uriDepT = uriSDF.format(isoSDF.parse(schedDepT));
                    actDepT.setTime(isoSDF.parse(service.getActualDepartureTime()));
                } catch (ParseException ex) {
                    Logger.getLogger(SortedMapper.class.getName()).log(Level.SEVERE,null,ex);
                }
                String trainId = service.getTrainNumber();
                String dest = service.getDestination();
                String hashString = trainId + dest;
                md.reset();
                md.update(hashString.getBytes());
                byte[] digest = md.digest();
                String hashedString = "";
                for (int k = 0; k < digest.length; k++) {
                    hashedString += Integer.toString((digest[k] & 0xff) + 0x100,16).substring(1);
                }
                String departureId = stationId + "/departures/" + uriDepT + hashedString;
                NextStop nextStop = service.getNextStop();
                if (nextStop != null) {
                    String id = StationDatabase.getInstance().getStationId(nextStop.getName());
                    if (id == null) {
                        missingStations.add(nextStop.getName());
                    }
                    String nextStopURI = stationsNMBS + id;
                    long sort = System.currentTimeMillis();
                    if (!departuresMap.containsKey(actDepT)) {
                        departuresMap.put(actDepT,new ArrayList<Departure>());
                    }
                    departuresMap.get(actDepT).add(new Departure(departureId,stationId,
                            schedDepT,service.getDelay(),service.getActualDepartureTime(),
                            service.getPlatform(),nextStopURI,nextStop.getScheduledArrivalTime(),
                            nextStop.getDelay(),nextStop.getActualArrivalTime(),
                            nextStop.getPlatform(),trainId,dest));
                    sortingTime += System.currentTimeMillis() - sort;
                }
            }
        }
        
        for (List<Departure> l : departuresMap.values()) {
            departures.addAll(l);
        }
        
        System.out.println("Stations not in database: " + missingStations + " (" + missingStations.size() + ")");
        
        long end = System.currentTimeMillis();
        System.out.println(Calendar.getInstance().getTime() + ": INTERNAL MAPPING (" + (end - start) + " ms)");
        System.out.println("NUMBER OF DEPARTURES: " + departures.size());
        System.out.println("NUMBER OF DEPARTURE TRIPLES: " + departures.size()*12);
        System.out.println("SORTING TIME: " + sortingTime + " ms");
    }
    
    private void mapTurtle() {
        long start = System.currentTimeMillis();
        
        turtleStations = new ArrayList<>();
        StringBuilder stationsBuilder = new StringBuilder();
        stationsBuilder.append("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
                + "@prefix geo:   <http://www.w3.org/2003/01/geo/wgs84_pos#> .");
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            stationsBuilder.append("\n\n<").append(station.getId())
                    .append(">\n" + "        rdfs:label   \"").append(station.getName())
                    .append("\" ;\n" + "        geo:lat      \"")
                    .append(station.getLatitude())
                    .append("\" ;\n" + "        geo:long     \"")
                    .append(station.getLongitude()).append("\" .");
            turtleStations.add(stationsBuilder.toString().getBytes(StandardCharsets.UTF_8));
            stationsBuilder.setLength(0);
        }
        
        turtleDepartures = new ArrayList<>();
        StringBuilder departuresBuilder = new StringBuilder();
        departuresBuilder.append("@prefix rplod: <http://semweb.mmlab.be/ns/rplod/> .\n"
                + "@prefix transit: <http://vocab.org/transit/terms/> .");
        for (int i = 0; i < departures.size(); i++) {
            Departure departure = departures.get(i);
            departuresBuilder.append("\n\n<").append(departure.getDepartureId())
                    .append(">\n" + "        rplod:stop                           <")
                    .append(departure.getDepartureStationId())
                    .append("> ;\n" + "        rplod:scheduledDepartureTime         \"")
                    .append(departure.getScheduledDepartureTime())
                    .append("\" ;\n" + "        rplod:delay                          \"")
                    .append(departure.getDepartureDelay())
                    .append("\" ;\n" + "        rplod:actualDepartureTime            \"")
                    .append(departure.getActualDepartureTime())
                    .append("\" ;\n" + "        rplod:platform                       \"")
                    .append(departure.getDeparturePlatform())
                    .append("\" ;\n" + "        rplod:nextStop                       <")
                    .append(departure.getArrivalStationId())
                    .append("> ;\n" + "        rplod:nextStopScheduledArrivalTime   \"")
                    .append(departure.getScheduledArrivalTime())
                    .append("\" ;\n" + "        rplod:nextStopDelay                  \"")
                    .append(departure.getArrivalDelay())
                    .append("\" ;\n" + "        rplod:nextStopActualArrivalTime      \"")
                    .append(departure.getActualArrivalTime())
                    .append("\" ;\n" + "        rplod:nextStopPlatform               \"")
                    .append(departure.getArrivalPlatform())
                    .append("\" ;\n" + "        rplod:routeLabel                     \"")
                    .append(departure.getRoute())
                    .append("\" ;\n" + "        transit:headsign                     \"")
                    .append(departure.getHeadsign()).append("\" .");
            turtleDepartures.add(departuresBuilder.toString().getBytes(StandardCharsets.UTF_8));
            departuresBuilder.setLength(0);
        }
        
        long end = System.currentTimeMillis();
        System.out.println(Calendar.getInstance().getTime() + ": TURTLE MAPPING (" + (end - start) + " ms)");
    }
    
    private void mapLinkedcsv() {
        long start = System.currentTimeMillis();
        
        linkedcsvStations = new ArrayList<>();
        StringBuilder stationsBuilder = new StringBuilder();
        stationsBuilder.append("\"$id\",\"http://www.w3.org/2000/01/rdf-schema#label\","
                + "\"http://www.w3.org/2003/01/geo/wgs84_pos#lat\","
                + "\"http://www.w3.org/2003/01/geo/wgs84_pos#long\"");
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            stationsBuilder.append("\n\"").append(station.getId())
                    .append("\",\"").append(station.getName()).append("\",\"")
                    .append(station.getLatitude()).append("\",\"")
                    .append(station.getLongitude()).append("\"");
            linkedcsvStations.add(stationsBuilder.toString().getBytes(StandardCharsets.UTF_8));
            stationsBuilder.setLength(0);
        }
        
        linkedcsvDepartures = new ArrayList<>();
        StringBuilder departuresBuilder = new StringBuilder();
        departuresBuilder.append("\"$id\",\"http://semweb.mmlab.be/ns/rplod/stop\","
                + "\"http://semweb.mmlab.be/ns/rplod/scheduledDepartureTime\","
                + "\"http://semweb.mmlab.be/ns/rplod/delay\","
                + "\"http://semweb.mmlab.be/ns/rplod/actualDepartureTime\","
                + "\"http://semweb.mmlab.be/ns/rplod/platform\","
                + "\"http://semweb.mmlab.be/ns/rplod/nextStop\","
                + "\"http://semweb.mmlab.be/ns/rplod/nextStopScheduledArrivalTime\","
                + "\"http://semweb.mmlab.be/ns/rplod/nextStopDelay\","
                + "\"http://semweb.mmlab.be/ns/rplod/nextStopActualArrivalTime\","
                + "\"http://semweb.mmlab.be/ns/rplod/nextStopPlatform\","
                + "\"http://semweb.mmlab.be/ns/rplod/routeLabel\","
                + "\"http://vocab.org/transit/terms/headsign\"");
        for (int i = 0; i < departures.size(); i++) {
            Departure departure = departures.get(i);
            departuresBuilder.append("\n\"").append(departure.getDepartureId())
                    .append("\",\"").append(departure.getDepartureStationId())
                    .append("\",\"").append(departure.getScheduledDepartureTime())
                    .append("\",\"").append(departure.getDepartureDelay())
                    .append("\",\"").append(departure.getActualDepartureTime())
                    .append("\",\"").append(departure.getDeparturePlatform())
                    .append("\",\"").append(departure.getArrivalStationId())
                    .append("\",\"").append(departure.getScheduledArrivalTime())
                    .append("\",\"").append(departure.getArrivalDelay())
                    .append("\",\"").append(departure.getActualArrivalTime())
                    .append("\",\"").append(departure.getArrivalPlatform())
                    .append("\",\"").append(departure.getRoute())
                    .append("\",\"").append(departure.getHeadsign()).append("\"");
            linkedcsvDepartures.add(departuresBuilder.toString().getBytes(StandardCharsets.UTF_8));
            departuresBuilder.setLength(0);
        }
        
        long end = System.currentTimeMillis();
        System.out.println(Calendar.getInstance().getTime() + ": LINKED CSV MAPPING (" + (end - start) + " ms)");
    }

    public List<byte[]> getTurtleStations() {
        return turtleStations;
    }

    public List<byte[]> getTurtleDepartures() {
        return turtleDepartures;
    }

    public List<byte[]> getLinkedcsvStations() {
        return linkedcsvStations;
    }

    public List<byte[]> getLinkedcsvDepartures() {
        return linkedcsvDepartures;
    }

    public long getLastModified() {
        return lastModified;
    }
    
}