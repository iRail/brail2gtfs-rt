package com.opentransport.rdfmapper.nmbs;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;

/**
 *
 * @author Nicola De Clercq
 */
public class StationFetcher {
    
    public void writeAllStationsToJson() {
        List<String> fetchedStationNames = fetchAllStationNamesFromFile();
        Set<String> ids = new HashSet<>();
        JSONObject json = new JSONObject();
        JSONArray stations = new JSONArray();
        for (int i = 0; i < fetchedStationNames.size(); i++) {
            String stationName = fetchedStationNames.get(i);
            StationInfo info = getStationInfo(stationName + " nmbs");
            String id = info.getId();
            if (!ids.contains(id)) {
                ids.add(id);
                if (!info.getName().contains("NMBS")) {
                    System.out.println("Geen NMBS in naam: " + stationName);
                }
                if (id.length() != 9) {
                    System.out.println("Verkeerde lengte voor ID: " + stationName);
                }
                JSONObject station = new JSONObject();
                station.put("name",info.getName());
                station.put("id",info.getId());
                station.put("longitude",info.getLongitude());
                station.put("latitude",info.getLatitude());
                stations.add(station);
            }
            else {
                System.out.println("Dubbel: " + stationName + " (" + id + ")");
            }
        }
        
        json.put("stations",stations);
        
        System.out.println("Aantal stations: " + stations.size());
        
        try {
            FileWriter fw = new FileWriter("NMBS_stations.json");
            fw.write(json.toString());
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    
    public String getStationId(String stationName) {
        return getStationInfo(stationName + " nmbs").getId();
    }
    
    private StationInfo getStationInfo(String stationName) {
        StationInfo info = new StationInfo();
        String data = fetchStationData(stationName,1);
        try {
            Document doc = new SAXBuilder().build(new StringReader(data));
            Element root = doc.getRootElement();
            List<Element> res = root.getChildren();
            List<Element> stations = res.get(0).getChildren();
            Element station = stations.get(0);
            info.setName(station.getAttributeValue("name"));
            info.setId(station.getAttributeValue("externalStationNr"));
            String x = station.getAttributeValue("x");
            int xl = x.length() - 6;
            String longitude = x.substring(0,xl) + "." + x.substring(xl);
            info.setLongitude(longitude);
            String y = station.getAttributeValue("y");
            int yl = y.length() - 6;
            String latitude = y.substring(0,yl) + "." + y.substring(yl);
            info.setLatitude(latitude);
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        return info;
    }
    
    private List<String> fetchAllStationNamesFromFile() {
        List<String> stations = new ArrayList<>();
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader("NMBS_stations.txt"));
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    stations.add(line);
                }
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        } catch (IOException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        System.out.println("Aantal stationsnamen: " + stations.size());
        
        return stations;
    }
    
    private List<String> fetchAllStationNamesFromWeb() {
        List<String> stations = new ArrayList<>();
        try {   
            org.jsoup.nodes.Document doc = Jsoup.connect("http://www.b-rail.be/main/stationsinfo/station_list.php").get();
            String html = doc.body().toString();
            String[] htmlList = html.toLowerCase().split("<br[^>]*>");
            for (int i = 0; i < htmlList.length; i++) {
                String station = htmlList[i];
                if (station.matches("-[a-z].*")) {
                    if (station.matches("-[a-z].*\\(.*\\)")) {
                        station = station.substring(0,station.indexOf("("));
                    }
                    stations.add(station.substring(1));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        for (int i = 0; i < stations.size(); i++) {
            System.out.println(stations.get(i));
        }
        
        System.out.println("Aantal stationsnamen: " + stations.size());
        
        return stations;
    }
    
    private String fetchStationData(String stationName, int numberOfResults) {
        URL url = null;
        try {
            url = new URL("http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/extxml.exe");
        } catch (MalformedURLException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        conn.setDoOutput(true);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        String postData = "<?xml version=\"1.0 encoding=\"iso-8859-1\"?>"
                + "<ReqC ver=\"1.1\" prod=\"iRail API v1.0\" lang=\"nl\">"
                + "<LocValReq id=\"stat1\" maxNr=\"" + numberOfResults + "\">"
                + "<ReqLoc match=\"" + stationName + "\" type=\"ST\"/>"
                + "</LocValReq>"
                + "</ReqC>";
        conn.setRequestProperty("Content-Length",String.valueOf(postData.length()));
        
        DataOutputStream os;
        BufferedReader br;
        StringBuilder sb = new StringBuilder();
        String line;
        
        try {
            os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(postData);
            os.close();
            
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(StationFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        return sb.toString();
    }

}