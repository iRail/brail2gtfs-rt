/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import java.io.FileOutputStream;
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
           

    
    public  NetworkDisturbanceFetcher(){
        
    //    demoDisturbance();
    writeDisturbanceFile();
    
    
    }

   private static String reformTitle(String title) {
         String characterDelimiter ="CDATA";
        int startPosition = 0, endPosition = 0;
        startPosition = title.indexOf(characterDelimiter)+6;
        characterDelimiter = "]]&gt";
        endPosition = title.indexOf(characterDelimiter); 
       return title.substring(startPosition, endPosition);
    }
    
    private  GtfsRealtime.FeedMessage.Builder scrapeDisturbances(){
        GtfsRealtime.FeedMessage.Builder feedMessage =  GtfsRealtime.FeedMessage.newBuilder();        
        GtfsRealtime.FeedHeader.Builder feedHeader = GtfsRealtime.FeedHeader.newBuilder(); 
        feedHeader.setGtfsRealtimeVersion("1.0");
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
         //Unix Style
        feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
        feedMessage.setHeader(feedHeader);
        
        System.out.println("Start Scraping");
        Document doc;
        int i =0;
        try {
            doc = Jsoup.connect(webSiteURL).timeout(10*1000).get(); 
           //Get all elements with img tag ,
           Elements disturbances = doc.getElementsByTag("item");
            for (Element el : disturbances) {
                GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
                GtfsRealtime.Alert.Builder alert = GtfsRealtime.Alert.newBuilder();
                //Entity -> Alert $
                //Alert -> Time Range
                //GtfsRealtime.TimeRange.Builder timeRange = GtfsRealtime.TimeRange.newBuilder();             
          
                //alert.setCause(GtfsRealtime.Alert.Cause.STRIKE);
                
                //Setting the Description 
                GtfsRealtime.TranslatedString.Builder translatedDescriptionString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationsDescription = GtfsRealtime.TranslatedString.Translation.newBuilder();
                            
                translationsDescription.setText( el.child(1).html());
                translationsDescription.setLanguage("en");
                
                translatedDescriptionString.addTranslation(0, translationsDescription);
                alert.setDescriptionText(translatedDescriptionString);
                //----------
                //Setting the Header text also known as Title
                GtfsRealtime.TranslatedString.Builder translatedHeaderString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationsHeader = GtfsRealtime.TranslatedString.Translation.newBuilder();
                
                translationsHeader.setText(reformTitle(el.child(0).html()));
                translationsHeader.setLanguage("en");
                translatedHeaderString.addTranslation(0, translationsHeader);
                alert.setHeaderText(translatedHeaderString);
                //-----------
                //setting the url 
                GtfsRealtime.TranslatedString.Builder translatedUrlString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationUrl = GtfsRealtime.TranslatedString.Translation.newBuilder();
                
                translationUrl.setText(el.child(2).html());
                translationUrl.setLanguage("en");
                translatedUrlString.addTranslation(0, translationsHeader);
                alert.setUrl(translatedUrlString);
                //-----------              
                
            
                
                String description =  el.child(1).html();   
                String pubDate = el.child(3).html(); 
               
                feedEntity.setAlert(alert);
                feedEntity.setId( pubDate +i );
                
                System.out.println(i);
                feedMessage.addEntity(i, feedEntity);
                i++;
                
               // timeRange.setStart();
                
               // alert.setActivePeriod(index, timeRange)
                
             
           }     
            
           
        } catch (IOException ex) {
            Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
        return feedMessage;
    
    }
    private void writeDisturbanceFile(){ 
        GtfsRealtime.FeedMessage.Builder feedMessage =  scrapeDisturbances();
        
      
       //Write the new Disturbance back to disk
        try {
            
                 FileOutputStream output = new FileOutputStream("gtfs-rt-disturbance");
      
                  feedMessage.build().writeTo(output);
                  output.close();
                  System.out.println("File writen successful");
            
        } catch (IOException e) {
            System.err.println("Error failed to write file");
        }    
    
    }
    private void demoDisturbance(){
    GtfsRealtime.FeedMessage.Builder fm = GtfsRealtime.FeedMessage.newBuilder();
    GtfsRealtime.FeedHeader.Builder fh = GtfsRealtime.FeedHeader.newBuilder();
    
    
    fh.setGtfsRealtimeVersion("1.0");
    fh.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
         //Unix Style
    fh.setTimestamp(System.currentTimeMillis() / 1000L);
    fm.setHeader(fh);
    
    GtfsRealtime.FeedEntity.Builder fe = GtfsRealtime.FeedEntity.newBuilder();
    GtfsRealtime.Alert.Builder fa = GtfsRealtime.Alert.newBuilder();
    
    fa.setCause(GtfsRealtime.Alert.Cause.STRIKE);
    fe.setAlert(fa);
    fe.setId("test");
    fm.addEntity(0, fe);
    
    
    }


    
}
