package com.opentransport.rdfmapper.old;

import com.opentransport.rdfmapper.StationDatabase;
import com.opentransport.rdfmapper.containers.LiveBoard;
import com.opentransport.rdfmapper.containers.NextStop;
import com.opentransport.rdfmapper.containers.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Nicola De Clercq
 */
public class OldLiveBoardFetcher {
    
    public LiveBoard getLiveBoard(String stationId, String date, String time, int numberOfResults) {
        LiveBoard liveBoard = new LiveBoard();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        List<Future<Service>> futures = new ArrayList<>();
        liveBoard.setStationInfo(StationDatabase.getInstance().getStationInfo(stationId));
        String link = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/stboard.exe/nox"
                + "?input=" + stationId + "&date=" + date + "&time=" + time
                + "&maxJourneys=" + numberOfResults + "&boardType=dep"
                + "&productsFilter=0010001000&start=yes";
        
        Document doc = Jsoup.parse(getUrlSource(link));
        Elements trains = doc.select(".journey");
        for (int i = 0; i < trains.size(); i++) {
            final Service service = new Service();
            Element train = trains.get(i);
            Elements trainA = train.select("a");
            final String trainLink = trainA.attr("href");
            service.setTrainNumber(trainA.text());
            String stationInfo = train.ownText().replaceAll(">","").trim();
            String trainTarget = stationInfo;
            String trainPlatform = "";
            int split = stationInfo.indexOf(" perron ");
            if (split != -1) {
                trainTarget = stationInfo.substring(0,split);
                trainPlatform = stationInfo.substring(split + 8);
            }
            service.setDestination(trainTarget);
            service.setPlatform(trainPlatform);
            service.setScheduledDepartureTime(train.select("strong").get(1).text());
            final String trainDelay = train.select(".delay").text().replaceAll("\\+","");
            service.setDelay(trainDelay);
            Future<Service> future = pool.submit(new Callable<Service>() {
                @Override
                public Service call() throws Exception {
                    if (!trainDelay.equals("Afgeschaft")) {
                        service.setNextStop(getNextStop(trainLink));
                    }
                    return service;
                }
            });
            futures.add(future);
        }
        
        List<Service> services = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                services.add(futures.get(i).get(300,TimeUnit.MILLISECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                System.out.println("Fail in nextStop " + (i + 1) + " for " + liveBoard.getStationInfo().getName());
            }
        }
        liveBoard.setServices(services);
        
        pool.shutdownNow();
        
        return liveBoard;
    }
    
    private NextStop getNextStop(String trainLink) {
        NextStop nextStop = new NextStop();
        Document doc = Jsoup.parse(getUrlSource(trainLink));
        Elements stops = doc.select(".trainRoute").get(0).select("tr");
        boolean departureStationPassed = false;
        for (int i = 0; i < stops.size(); i++) {
            Elements tds = stops.get(i).select("td");
            if (tds.size() == 5) {
                Element arrivalTimeTd = tds.get(2);
                String arrivalTime = arrivalTimeTd.ownText();
                if (!departureStationPassed) {
                    departureStationPassed = true;
                }
                else if (arrivalTime.matches("[0-9][0-9]:[0-9][0-9]")) {
                    nextStop.setName(tds.get(1).text());
                    nextStop.setScheduledArrivalTime(arrivalTime);
                    nextStop.setDelay(arrivalTimeTd.select(".delay").text().replaceAll("\\+",""));
                    nextStop.setPlatform(tds.get(4).text());
                    return nextStop;
                }
            }
        }
        
        return null;
    }
    
    private String getUrlSource(String link) {
        URL url = null;
        try {
            url = new URL(link);
        } catch (MalformedURLException ex) {
            Logger.getLogger(OldLiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(OldLiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        return sb.toString();
    }

}