package com.opentransport.rdfmapper.nmbs;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.opentransport.rdfmapper.nmbs.containers.LiveBoard;
import com.opentransport.rdfmapper.nmbs.containers.NextStop;
import com.opentransport.rdfmapper.nmbs.containers.Service;
import com.opentransport.rdfmapper.nmbs.containers.StationInfo;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicola De Clercq
 */
public class JenaMapper {
    
    public static void map() {
        Set<String> missingStations = new HashSet<>();
        
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(JenaMapper.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        Model model = ModelFactory.createDefaultModel();
        String rplod = "http://semweb.mmlab.be/ns/rplod/";
        model.setNsPrefix("rplod",rplod);
        String transit = "http://vocab.org/transit/terms/";
        model.setNsPrefix("transit",transit);
        String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
        model.setNsPrefix("geo",geo);
        String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
        model.setNsPrefix("rdfs",rdfs);
        String iRail = "http://irail.be/";
        String stationsNMBS = iRail + "stations/NMBS/";
        
        Property label = model.createProperty(rdfs + "label");
        Property longitude = model.createProperty(geo + "long");
        Property latitude = model.createProperty(geo + "lat");
        Property departureStop = model.createProperty(rplod + "stop");
        Property scheduledDepartureTime = model.createProperty(rplod + "scheduledDepartureTime");
        Property delay = model.createProperty(rplod + "delay");
        Property actualDepartureTime = model.createProperty(rplod + "actualDepartureTime");
        Property headsign = model.createProperty(transit + "headsign");
        Property routeLabel = model.createProperty(rplod + "routeLabel");
        Property platform = model.createProperty(rplod + "platform");
        Property nextStopScheduledArrivalTime = model.createProperty(rplod + "nextStopScheduledArrivalTime");
        Property nextStopDelay = model.createProperty(rplod + "nextStopDelay");
        Property nextStopActualArrivalTime = model.createProperty(rplod + "nextStopActualArrivalTime");
        Property nextStopPlatform = model.createProperty(rplod + "nextStopPlatform");
        Property stop = model.createProperty(rplod + "nextStop");
        
        List<String> stations = StationDatabase.getInstance().getAllStationIds();
        
        LiveBoardFetcher liveBoardFetcher = new LiveBoardFetcher();
        List<LiveBoard> liveBoards = liveBoardFetcher.getLiveBoards(stations,"","",10000);
        
        SimpleDateFormat isoSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        SimpleDateFormat uriSDF = new SimpleDateFormat("yyyyMMddHHmm");
        
        long start1 = System.currentTimeMillis();
        
        for (int i = 0; i < liveBoards.size(); i++) {
            LiveBoard liveBoard = liveBoards.get(i);
            StationInfo info = liveBoard.getStationInfo();
            String stationId = stationsNMBS + info.getId();
            Resource station = model.createResource(stationId);
            station.addProperty(label,info.getName());
            station.addProperty(longitude,info.getLongitude());
            station.addProperty(latitude,info.getLatitude());
            
            List<Service> services = liveBoard.getServices();

            for (int j = 0; j < services.size(); j++) {
                Service service = services.get(j);
                String schedDepT = service.getScheduledDepartureTime();
                String uriDepT = "";
                try {
                    uriDepT = uriSDF.format(isoSDF.parse(schedDepT));
                } catch (ParseException ex) {
                    Logger.getLogger(JenaMapper.class.getName()).log(Level.SEVERE,null,ex);
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
                NextStop nextStop = service.getNextStop();
                if (nextStop != null) {
                    Resource stationDeparture = model.createResource(stationId + "/departures/" + uriDepT + hashedString);
                    stationDeparture.addProperty(departureStop,station);
                    stationDeparture.addProperty(scheduledDepartureTime,schedDepT);
                    stationDeparture.addProperty(delay,service.getDelay());
                    stationDeparture.addProperty(actualDepartureTime,service.getActualDepartureTime());
                    stationDeparture.addProperty(headsign,dest);
                    stationDeparture.addProperty(routeLabel,trainId);
                    stationDeparture.addProperty(platform,service.getPlatform());
                    stationDeparture.addProperty(nextStopScheduledArrivalTime,nextStop.getScheduledArrivalTime());
                    stationDeparture.addProperty(nextStopDelay,nextStop.getDelay());
                    stationDeparture.addProperty(nextStopActualArrivalTime,nextStop.getActualArrivalTime());
                    stationDeparture.addProperty(nextStopPlatform,nextStop.getPlatform());
                    String id = StationDatabase.getInstance().getStationId(nextStop.getName());
                    if (id == null) {
                        missingStations.add(nextStop.getName());
                    }
                    Resource nextStopURI = model.createResource(stationsNMBS + id);
                    stationDeparture.addProperty(stop,nextStopURI);
                }
            }
        }
        
        System.out.println("Stations not in database: " + missingStations + " (" + missingStations.size() + ")");
        
        long end1 = System.currentTimeMillis();
        System.out.println("RDF MAPPING (" + (end1 - start1) + " ms)");
        long start2 = System.currentTimeMillis();
        
        //trage implementatie
//        VirtGraph graph = new VirtGraph("test3","jdbc:virtuoso://localhost:1111","dba","dba");
//        graph.clear();
//        GraphUtil.addInto(graph,model.getGraph());
        
        try {
            FileWriter fw = new FileWriter("NMBS.ttl");
            model.write(fw,"TTL");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(JenaMapper.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        long end2 = System.currentTimeMillis();
        System.out.println("WRITING TO FILE (" + (end2 - start2) + " ms)");
        
    }
    
}