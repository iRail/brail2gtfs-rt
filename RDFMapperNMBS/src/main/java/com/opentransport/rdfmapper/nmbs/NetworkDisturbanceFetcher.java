/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author timtijssens
 */
public class NetworkDisturbanceFetcher {
    
    
    //The url of the website
   private static final String webSiteURL = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/help.exe/en?tpl=rss_feed";    
           
   GtfsRealtime.FeedMessage.Builder feedMessage =  GtfsRealtime.FeedMessage.newBuilder();        
   GtfsRealtime.FeedHeader.Builder feedHeader =GtfsRealtime.FeedHeader.newBuilder();  
 
    
    public  NetworkDisturbanceFetcher(){
        
    feedHeader.setGtfsRealtimeVersion("1.0");
    feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
         //Unix Style
    feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
    
    scrapeDisturbances();
    }

   private static String reformTitle(String title) {
         String characterDelimiter ="CDATA";
        int startPosition = 0, endPosition = 0;
        startPosition = title.indexOf(characterDelimiter)+6;
        characterDelimiter = "]]&gt";
        endPosition = title.indexOf(characterDelimiter); 
       return title.substring(startPosition, endPosition);
    }
    
    private static void scrapeDisturbances(){
        System.out.println("Start Scraping");
        Document doc;
        try {
            doc = Jsoup.connect(webSiteURL).timeout(10*1000).get(); 
           //Get all elements with img tag ,
           Elements disturbances = doc.getElementsByTag("item");
            for (Element el : disturbances) {
                GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
                GtfsRealtime.Alert.Builder alert = GtfsRealtime.Alert.newBuilder();
                //Entity -> Alert $
                //Alert -> Time Range
                GtfsRealtime.TimeRange.Builder timeRange = GtfsRealtime.TimeRange.newBuilder();
                //setting the title 
             
                //Setting the Description 
                GtfsRealtime.TranslatedString.Builder translatedDecriptionString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationsDescription = GtfsRealtime.TranslatedString.Translation.newBuilder();
                            
                translationsDescription.setText( el.child(1).html());
                translationsDescription.setLanguage("en");
                
                translatedDecriptionString.setTranslation(1, translationsDescription);
                //----------
                
                
                String title = el.child(0).html();
                title = reformTitle(title);
                
                String description =  el.child(1).html();
                
                
                String link =el.child(2).html(); 
                String pubDate = el.child(3).html(); 
                
                
                
               // timeRange.setStart();
                
               // alert.setActivePeriod(index, timeRange)
                
                
                
                
             
           }     
           
        } catch (IOException ex) {
            Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
    
    }
    
}
