package com.opentransport.rdfmapper.old;

import com.opentransport.rdfmapper.StationDatabase;
import com.opentransport.rdfmapper.containers.StationInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Nicola De Clercq
 */
public class HariLiveBoardFetcher {
    
    @Deprecated
    public void getLiveBoard(String stationId, String date, String time, int numberOfResults) {
        StationInfo info = StationDatabase.getInstance().getStationInfo(stationId);
        System.out.println(info.getName() + " (" + info.getLongitude() + ", " + info.getLatitude() + ")");
        String link = "http://hari.b-rail.be/Hafas/bin/stboard.exe/nn?start=yes"
                + "&time=" + time + "&date=" + date
                + "&input=" + stationId + "&maxJourneys=" + numberOfResults
                + "&boardType=dep&L=vs_java3&productsFilter=0111111000000000";
        URL url = null;
        try {
            url = new URL(link);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HariLiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException ex) {
            Logger.getLogger(HariLiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
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
            Logger.getLogger(HariLiveBoardFetcher.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        String data = "<data>" + sb.toString() + "</data>";
        System.out.println(data);
        try {
            Document doc = new SAXBuilder().build(new StringReader(data));
            Element root = doc.getRootElement();
            List<Element> trains = root.getChildren();
            for (int i = 0; i < trains.size(); i++) {
                Element train = trains.get(i);
                String delay = train.getAttributeValue("delay");
                if (delay.equals("-")) {
                    delay = "0";
                }
                else {
                    delay = train.getAttributeValue("e_delay");
                }
                String trainNumber = train.getAttributeValue("prod");
                trainNumber = trainNumber.replace("#"," (") + ")";
                trainNumber = trainNumber.replaceAll("\\s+"," ");
                String platform = train.getAttributeValue("platform");
                if (platform == null) {
                    platform = "";
                }
                System.out.println(train.getAttributeValue("fpTime") + " +"
                        + delay + "\t" + train.getAttributeValue("targetLoc")
                        + "\t" + trainNumber + "\t" + platform);
            }
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(HariLiveBoardFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}