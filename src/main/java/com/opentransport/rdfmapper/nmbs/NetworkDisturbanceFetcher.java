/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import com.opentransport.rdfmapper.nmbs.containers.NetworkDisturbance;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author timtijssens
 */
public class NetworkDisturbanceFetcher {

    private ErrorLogWriter errorWriter;
    private ArrayList<NetworkDisturbance> networkDisturbances = new ArrayList<NetworkDisturbance>();

    // URLs to network disturbances on Belgianrail website
    private String websiteURLEN =  "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/query.exe/eny?performLocating=512&tpl=himmatch2oldjson&look_nv=type|himmatch|maxnumber|300||no_match|yes";
    private String webSiteURLNL = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/query.exe/nny?performLocating=512&tpl=himmatch2oldjson&look_nv=type|himmatch|maxnumber|300||no_match|yes";
    private String webSiteURLFR =  "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/query.exe/fry?performLocating=512&tpl=himmatch2oldjson&look_nv=type|himmatch|maxnumber|300||no_match|yes";
    
    private void scrapeNetworkDisturbanceWebsite(String language, String _url) {
        Document doc;
        int i = 0;
        //English Version
        try {            
            URL url = new URL(_url);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonArray disturbances = root.getAsJsonObject().getAsJsonArray("him");
                        
            for (JsonElement el : disturbances) {
                JsonObject obj = (JsonObject) el;
                
                String header = obj.get("header").getAsString();
                String description = obj.get("text").getAsString();
                String urls = ""; // todo check JsonNull
                
                String begin = obj.get("begin").getAsString(); // dd.MM.yyyy HH:mm
                String end = obj.get("end").getAsString(); // dd.MM.yyyy HH:mm
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
                df.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
                
                Date date;
                long startEpoch, endEpoch;
                try {
                    date = df.parse(begin);
                    startEpoch = date.getTime() / 1000;
                    date = df.parse(end);
                    endEpoch = date.getTime() / 1000;
                } catch (ParseException ex) {
                    // When time range is missing, take 12 hours before and after
                    startEpoch = new Date().getTime()-43200;
                    endEpoch = new Date().getTime()+43200;
                    
                    Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                int startStationId, endStationId, impactStationId;
                if (obj.has("startstation_extId")) {
                    startStationId = obj.get("startstation_extId").getAsInt();
                } else {
                    startStationId = -1;
                }
                if (obj.has("endstation_extId")) {
                    endStationId = obj.get("endstation_extId").getAsInt();
                } else {
                    endStationId = -1;
                }
                if (obj.has("impactstation_extId")) {
                    impactStationId = obj.get("impactstation_extId").getAsInt();
                } else {
                    impactStationId = -1;
                }
                
                String id = obj.get("id").getAsString();
                
                NetworkDisturbance disturbance = new NetworkDisturbance(header, description, urls, language, startEpoch, endEpoch, startStationId, endStationId, impactStationId, id);

                networkDisturbances.add(disturbance);
            }

        } catch (IOException ex) {
            Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
            errorWriter.writeError(ex.toString());
        }

    }

    private GtfsRealtime.FeedMessage.Builder createAlerts() {

        GtfsRealtime.FeedMessage.Builder feedMessage = GtfsRealtime.FeedMessage.newBuilder();
        GtfsRealtime.FeedHeader.Builder feedHeader = GtfsRealtime.FeedHeader.newBuilder();
        feedHeader.setGtfsRealtimeVersion("1.0");
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
        //Unix Style
        feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
        feedMessage.setHeader(feedHeader);

        for (int i = 0; i < networkDisturbances.size(); i++) {
            NetworkDisturbance disturbance = networkDisturbances.get(i);
            String lang = disturbance.getLanguage();

            GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
            GtfsRealtime.Alert.Builder alert = GtfsRealtime.Alert.newBuilder();

            //Setting the Description 
            GtfsRealtime.TranslatedString.Builder translatedDescriptionString = GtfsRealtime.TranslatedString.newBuilder();
            GtfsRealtime.TranslatedString.Translation.Builder translationsDescription = GtfsRealtime.TranslatedString.Translation.newBuilder();
            translationsDescription.setText(disturbance.getDescriptionText());
            translationsDescription.setLanguage(lang);
            translatedDescriptionString.addTranslation(0, translationsDescription);
            alert.setDescriptionText(translatedDescriptionString);

            //Setting the Header text also known as Title
            GtfsRealtime.TranslatedString.Builder translatedHeaderString = GtfsRealtime.TranslatedString.newBuilder();
            GtfsRealtime.TranslatedString.Translation.Builder translationsHeader = GtfsRealtime.TranslatedString.Translation.newBuilder();
            translationsHeader.setText(disturbance.getHeaderText());
                //System.out.println(reformTitle(el.child(0).html()));

            translationsHeader.setLanguage(lang);
            translatedHeaderString.addTranslation(0, translationsHeader);
            alert.setHeaderText(translatedHeaderString);

            // Setting the url 
            GtfsRealtime.TranslatedString.Builder translatedUrlString = GtfsRealtime.TranslatedString.newBuilder();

            GtfsRealtime.TranslatedString.Translation.Builder translationUrl = GtfsRealtime.TranslatedString.Translation.newBuilder();

            translationUrl.setText(disturbance.getUrl());
            translationUrl.setLanguage(lang);
            translatedUrlString.addTranslation(0, translationUrl);
            // alert.setUrl(translatedUrlString);

            // Setting time interval            
            GtfsRealtime.TimeRange.Builder range = GtfsRealtime.TimeRange.newBuilder();
            range.setStart(disturbance.getStart());
            range.setEnd(disturbance.getEnd());
            alert.addActivePeriod(range);
            
            // Set EntitySelector for stops
            String start = "";
            String end = "";
            String impact = ""; // from NBMS API
            
            // Start Station
            GtfsRealtime.EntitySelector.Builder startStopSelector = GtfsRealtime.EntitySelector.newBuilder();
            if (disturbance.getStartStationId() != -1) {
                start = String.valueOf(disturbance.getStartStationId()) + ":0";
                startStopSelector.setStopId(start);
            }
            
            GtfsRealtime.EntitySelector.Builder endStopSelector = GtfsRealtime.EntitySelector.newBuilder();
            // End Station
            if (disturbance.getEndStationId()!= -1) {
                end = String.valueOf(disturbance.getEndStationId())+ ":0";
                endStopSelector.setStopId(end);
            }
                        
            // Impact Station
            GtfsRealtime.EntitySelector.Builder impactStationSelector = GtfsRealtime.EntitySelector.newBuilder();
            if (disturbance.getImpactStationId()!= -1) {
                impact = String.valueOf(disturbance.getImpactStationId()) + ":0";
                impactStationSelector.setStopId(impact);
                alert.addInformedEntity(impactStationSelector);
            }
            
            // Only add unique stop_ids
            if (!start.equals(impact) && start != "") {
                alert.addInformedEntity(startStopSelector);
            }
            if (!end.equals(impact) && end != "" && !end.equals(start)) {
                alert.addInformedEntity(endStopSelector);
            }            
            
            feedEntity.setAlert(alert);
            feedEntity.setId(disturbance.getId());

            feedMessage.addEntity(i, feedEntity);
        }

        return feedMessage;
    }

    public void writeDisturbanceFile() {
        // Scrapes networkdisturbances and saves the data in networkDisturbances arrayList        
        scrapeNetworkDisturbanceWebsite("nl", webSiteURLNL);
        scrapeNetworkDisturbanceWebsite("en", websiteURLEN);
        scrapeNetworkDisturbanceWebsite("fr", webSiteURLFR);
        
        GtfsRealtime.FeedMessage.Builder feedMessage = createAlerts();

        //Write the new Disturbance back to disk
        try {

            FileOutputStream output = new FileOutputStream("service_alerts.pb");

            feedMessage.build().writeTo(output);
            output.close();
            System.out.println("Network Disturbance file written successful");

        } catch (IOException e) {
            System.err.println("Error failed to write network disturbance file");
            errorWriter.writeError(e.toString());
        }
    }
}
