package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.LiveBoard;
import com.opentransport.rdfmapper.nmbs.containers.NextStop;
import com.opentransport.rdfmapper.nmbs.containers.Service;
import com.opentransport.rdfmapper.nmbs.containers.Stop;
import com.opentransport.rdfmapper.nmbs.containers.TrainId;
import com.opentransport.rdfmapper.nmbs.containers.TrainInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Nicola De Clercq
 */
public class LiveBoardFetcher {
    
    private static final int NUMBER_OF_CONNECTIONS = 50;
    private static final int CONNECTION_TIMEOUT_IN_MILLISECONDS = 3000;
    private static final long POOL_TIMEOUT = 60;
    
    private int trainsDelayed =1;
    private Map<String,String> trainDelays = new HashMap();
    
    private Map<TrainId,TrainInfo> trainCache;
    private Map<String,String> sources;
    private Map<String,String> retries;
    
    public LiveBoardFetcher() {
        trainCache = new ConcurrentHashMap<>();
        sources = new ConcurrentHashMap<>();
        retries = new ConcurrentHashMap<>();
    }
    
    public List<LiveBoard> getLiveBoards(List<String> stationIds, String date, String time, int numberOfResults) {
        sources.clear();
        trainCache.clear();
        retries.clear();
        ExecutorService sourcesPool = Executors.newFixedThreadPool(NUMBER_OF_CONNECTIONS);
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < stationIds.size(); i++) {
            final int number = i + 1;
            final String stationId = stationIds.get(i);
            final String link = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/stboard.exe/nox"
                + "?input=" + stationId + "&date=" + date + "&time=" + time
                + "&maxJourneys=" + numberOfResults + "&boardType=dep"
                + "&productsFilter=0111111000&start=yes";
            sourcesPool.execute(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    String source = getUrlSource(link);
                    if (source != null) {
                        sources.put(stationId,source);
                    }
                    else {
                        retries.put(stationId,link);
                    }
                    long end = System.currentTimeMillis();
//                    System.out.println("LIVEBOARD FETCH " + number + " (" + (end - start) + " ms)");
                }
            });
        }
        
        sourcesPool.shutdown();
        try {
            sourcesPool.awaitTermination(POOL_TIMEOUT,TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        ExecutorService retriesPool = Executors.newFixedThreadPool(2);
        for (Entry e : retries.entrySet()) {
            final String stationId = (String) e.getKey();
            final String link = (String) e.getValue();
            retriesPool.execute(new Runnable() {
                @Override
                public void run() {
                    String source = getUrlSource(link); 
                    if (source != null) {
                        sources.put(stationId,source);
//                        System.out.println("RETRIED " + stationId);
                    }
                    else {
                        String newLink = link.replaceAll("&maxJourneys=[0-9]*","&maxJourneys=10");
                        String newSource = getUrlSource(newLink);
                        if (newSource != null) {
                            sources.put(stationId,newSource);
                            System.out.println("ONLY 10 SERVICES " 
                                    + StationDatabase.getInstance().getStationInfo(stationId).getName()
                                    + " [" + stationId + "]");
                        }
                        else {
                            sources.put(stationId,"");
                            System.out.println("FAILED " 
                                    + StationDatabase.getInstance().getStationInfo(stationId).getName()
                                    + " [" + stationId + "]");
                        }
                    }
                }
            });
        }
        
        retriesPool.shutdown();
        try {
            retriesPool.awaitTermination(POOL_TIMEOUT / 2,TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        long end1 = System.currentTimeMillis();
        System.out.println("LIVEBOARD FETCHING (" + (end1 - start1) + " ms)");
        System.out.println("NUMBER OF LIVEBOARDS: " + sources.size());
        long start2 = System.currentTimeMillis();
        
        ExecutorService trainCachePool = Executors.newFixedThreadPool(NUMBER_OF_CONNECTIONS);
        for (int i = 0; i < stationIds.size(); i++) {
            final int numberI = i + 1;
            String stationId = stationIds.get(i);
            Document doc = Jsoup.parse(sources.get(stationId));
            Elements trains = doc.select(".journey");
            
            for (int j = 0; j < trains.size(); j++) {
                final int numberJ = j + 1;
                Element train = trains.get(j);
                Elements trainA = train.select("a");
                final String trainLink = trainA.attr("href");
                final String trainNumber = trainA.text();
                //Train Number is Trip ID
               ;
                String stationInfo = train.ownText().replaceAll(">","").trim();
                String trainTarget = stationInfo;
                int split = stationInfo.indexOf(" perron ");
                if (split != -1) {
                    trainTarget = stationInfo.substring(0,split);
                }
                final String destination = trainTarget;
                String trainDelay = train.select(".delay").text().replaceAll("\\+","");
              
                if (trainDelay.length() > 0) {
                    System.out.println(trainNumber);
                    System.out.println(trainDelay);
                    trainDelays.put(trainNumber, trainDelay);
                    trainsDelayed ++;
                    System.out.println(trainsDelayed);
                    System.out.println(trainDelays.size());
                            
                    
                    
                }
                
                if (!trainDelay.equals("Afgeschaft")) {
                    trainCachePool.execute(new Runnable() {
                        @Override
                        public void run() {
                            long start = System.currentTimeMillis();
                            if (!trainCache.containsKey(new TrainId(trainNumber,destination))) {
                                insertTrain(trainNumber,destination,trainLink);
                            }
                            long end = System.currentTimeMillis();
//                            System.out.println("TRAIN FETCH " + numberI + " - " + numberJ + " (" + (end - start) + " ms)");
                        }
                    });
                }
            }
           
        }
         System.out.println("Finished Reading Trains");
         ScrapeTrip scrapeDelayedTrains  = new ScrapeTrip();
         scrapeDelayedTrains.startScrape(trainDelays);
         
         
        trainCachePool.shutdown();
        try {
            trainCachePool.awaitTermination(POOL_TIMEOUT,TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        long end2 = System.currentTimeMillis();
        System.out.println("TRAIN FETCHING (" + (end2 - start2) + " ms)");
        System.out.println("NUMBER OF TRAINS: " + trainCache.size());
        long start3 = System.currentTimeMillis();
        
        List<LiveBoard> liveBoards = new ArrayList<>();
        for (int i = 0; i < stationIds.size(); i++) {
            LiveBoard liveBoard = parseLiveBoard(stationIds.get(i));
            liveBoards.add(liveBoard);
        }
        
        long end3 = System.currentTimeMillis();
        System.out.println("LIVEBOARD PARSING (" + (end3 - start3) + " ms)");
        
        return liveBoards;
    }
    
    public LiveBoard getLiveBoard(String stationId, String date, String time, int numberOfResults) {
        ArrayList<String> stations = new ArrayList<>();
        stations.add(stationId);
        List<LiveBoard> liveBoards = getLiveBoards(stations,date,time,numberOfResults);
        return liveBoards.get(0);
    }
    
    private LiveBoard parseLiveBoard(String stationId) {
        LiveBoard liveBoard = new LiveBoard();
        List<Service> services = new ArrayList<>();
        liveBoard.setStationInfo(StationDatabase.getInstance().getStationInfo(stationId));
        Document doc = Jsoup.parse(sources.get(stationId));
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm,dd/MM/yy");
        Calendar time = Calendar.getInstance();
        String[] timeA = null;
        if (doc.select(".qs").isEmpty()) {
            System.out.println("ERROR IN LIVEBOARD "
                    + StationDatabase.getInstance().getStationInfo(stationId).getName()
                    + " [" + stationId + "]");
        }
        else {
            Element timeE = doc.select(".qs").get(0);
            String timeS = timeE.ownText().replaceAll("Vertrek ","");
            timeA = timeS.split(",");
            try {
                Date t = sdf.parse(timeS);
                time.setTime(t);
            } catch (ParseException ex) {
                Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
            }
        }
        
        Elements trains = doc.select(".journey");
        for (int i = 0; i < trains.size(); i++) {
            Service service = new Service();
            Element train = trains.get(i);
            Elements trainA = train.select("a");
            service.setTrainNumber(trainA.text());
            String stationInfo = train.ownText().replaceAll(">","").trim();
            String trainTarget = stationInfo;
            String trainPlatform = "";
            int split = stationInfo.indexOf(" perron ");
            if (split != -1) {
                trainTarget = stationInfo.substring(0,split);
                trainPlatform = stationInfo.substring(split + 8);
            }
            else {
                String platf = train.select(".platformChange").text().replaceAll("perron ","");
                if (!platf.isEmpty()) {
                    trainPlatform = platf;
                }
            }
            service.setDestination(trainTarget);
            service.setPlatform(trainPlatform);
            service.setScheduledDepartureTime(train.select("strong").get(1).text());
            String trainDelay = train.select(".delay").text().replaceAll("\\+","");
            service.setDelay(trainDelay);
            service.setNextStop(getNextStop(service.getTrainNumber(),
                        service.getDestination(),liveBoard.getStationInfo().getName()));
            if (trainDelay.equals("Afgeschaft")) {
                service.setDelay("CANCELLED");
                if (service.getNextStop() != null) {
                    service.getNextStop().setDelay("CANCELLED");
                }
            }
            
            //ISO 8601
            SimpleDateFormat isoSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            Calendar depC = Calendar.getInstance();
            try {
                Date d = sdf.parse(service.getScheduledDepartureTime() + "," + timeA[1]);
                depC.setTime(d);
            } catch (ParseException ex) {
                Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
            }
            Calendar actualDepC = (Calendar) depC.clone();
            int departureDelay = 0;
            try {
                departureDelay = Integer.parseInt(service.getDelay());
            } catch (NumberFormatException ex) {
            }
            actualDepC.add(Calendar.MINUTE,departureDelay);
            if (actualDepC.before(time)) {
                depC.add(Calendar.DATE,1);
                actualDepC.add(Calendar.DATE,1);
            }
            else {
                Calendar temp = (Calendar) time.clone();
                temp.add(Calendar.DATE,1);
                if (!temp.after(actualDepC)) {
                    depC.add(Calendar.DATE,-1);
                    actualDepC.add(Calendar.DATE,-1);
                }
            }
            service.setScheduledDepartureTime(isoSDF.format(depC.getTime()));
            service.setActualDepartureTime(isoSDF.format(actualDepC.getTime()));
            if (service.getNextStop() != null) {
                Calendar arrC = (Calendar) depC.clone();
                String[] arrTime = service.getNextStop().getScheduledArrivalTime().split(":");
                arrC.set(Calendar.HOUR_OF_DAY,Integer.parseInt(arrTime[0]));
                arrC.set(Calendar.MINUTE,Integer.parseInt(arrTime[1]));
                Calendar actualArrC = (Calendar) arrC.clone();
                int arrivalDelay = 0;
                try {
                    arrivalDelay = Integer.parseInt(service.getNextStop().getDelay());
                } catch (NumberFormatException ex) {
                }
                actualArrC.add(Calendar.MINUTE,arrivalDelay);
                while (arrC.before(depC)) {
                    arrC.add(Calendar.DATE,1);
                }
                while (actualArrC.before(actualDepC)) {
                    actualArrC.add(Calendar.DATE,1);
                }
                service.getNextStop().setScheduledArrivalTime(isoSDF.format(arrC.getTime()));
                service.getNextStop().setActualArrivalTime(isoSDF.format(actualArrC.getTime()));
            }
            
            services.add(service);
        }
        
        liveBoard.setServices(services);
        
        return liveBoard;
    }
    
    private NextStop getNextStop(String trainNumber, String destination, String stationName) {
        TrainInfo info = trainCache.get(new TrainId(trainNumber,destination));
        if (info != null) {
            List<Stop> stops = info.getStops();
            for (int i = 0; i < stops.size(); i++) {
                if (stops.get(i).getName().equals(stationName)) {
                    Stop stop = stops.get(i + 1);
                    NextStop nextStop = new NextStop();
                    nextStop.setName(stop.getName());
                    nextStop.setScheduledArrivalTime(stop.getArrivalTime());
                    nextStop.setDelay(stop.getArrivalDelay());
                    nextStop.setPlatform(stop.getPlatform());
                    return nextStop;
                }
            }
        }
        
        return null;
    }
    
    private void insertTrain(String trainNumber, String destination, String trainLink) {
        List<Stop> stopsList = new ArrayList<>();
        Document doc = Jsoup.parse(getUrlSource(trainLink.replaceAll("sqIdx=[0-9]*","sqIdx=0")));
        Elements route = doc.select(".trainRoute");
        if (route.isEmpty()) {
            return;
        }
        Elements stops = route.get(0).select("tr");
        for (int i = 0; i < stops.size(); i++) {
            Elements tds = stops.get(i).select("td");
            if (tds.size() == 5) {
                Element arrivalTimeTd = tds.get(2);
                String arrivalTime = arrivalTimeTd.ownText().replaceAll("\u00A0","");
                Element departureTimeTd = tds.get(3);
                String departureTime = departureTimeTd.ownText().replaceAll("\u00A0","");
                if (arrivalTime.matches("[0-9][0-9]:[0-9][0-9]") || departureTime.matches("[0-9][0-9]:[0-9][0-9]")) {
                    Stop stop = new Stop();
                    stop.setName(tds.get(1).text());
                    stop.setArrivalTime(arrivalTime);
                    stop.setArrivalDelay(arrivalTimeTd.select(".delay").text().replaceAll("\\+",""));
                    stop.setDepartureTime(departureTime);
                    stop.setDepartureDelay(departureTimeTd.select(".delay").text().replaceAll("\\+",""));
                    stop.setPlatform(tds.get(4).text().replaceAll("\u00A0",""));
                    stopsList.add(stop);
                }
            }
        }
        
        trainCache.put(new TrainId(trainNumber,destination),new TrainInfo(trainNumber,destination,stopsList));
    }
    
    private String getUrlSource(String link) {
        URL url = null;
        try {
            url = new URL(link);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(CONNECTION_TIMEOUT_IN_MILLISECONDS);
            conn.setConnectTimeout(CONNECTION_TIMEOUT_IN_MILLISECONDS);
        } catch (IOException ex) {
            Logger.getLogger(LiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException ex) {
            // Probeert het ophalen van de pagina opnieuw
//            System.out.println("########## RETRY ##########");
            return getUrlSource(link);
        }
        
        String source = sb.toString();
        
        if (source.contains("<title>Fout</title>")) {
            return null;
        }
        
        return source;
    }

}