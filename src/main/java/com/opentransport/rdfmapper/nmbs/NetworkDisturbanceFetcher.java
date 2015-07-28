/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import com.opentransport.rdfmapper.nmbs.containers.NetworkDisturbance;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
   private ErrorLogWriter  errorWriter = new  ErrorLogWriter();
   private ArrayList<NetworkDisturbance> NetWorkDisturbances = new ArrayList<NetworkDisturbance>();
    
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
   private boolean isPresent(String word, String sentence)
    {
        if(sentence.indexOf(word) >= 0){            
            return true;
        }else{return false;} 
            
    }
   private  GtfsRealtime.Alert.Cause setCause (String title){
       
        if ( isPresent("Strike", title)) {
                    return GtfsRealtime.Alert.Cause.STRIKE;                   
        }
        if ( isPresent("failure", title)) {
                    return GtfsRealtime.Alert.Cause.TECHNICAL_PROBLEM;                   
        }
       
       
       return null;
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
        //English Version
        try {
            doc = Jsoup.connect(webSiteURL).timeout(10*1000).get(); 
           //Get all elements with img tag ,
           Elements disturbances = doc.getElementsByTag("item");
            for (Element el : disturbances) {
                GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
                GtfsRealtime.Alert.Builder alert = GtfsRealtime.Alert.newBuilder();
                
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
                //System.out.println(reformTitle(el.child(0).html()));
                
                
               // alert.setCause(setCause(reformTitle(el.child(0).html())));
               

                
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
                             
                feedMessage.addEntity(i, feedEntity);
                i++;
                
               // timeRange.setStart();
                
               // alert.setActivePeriod(index, timeRange)
                
             
           }  
            
            
           
        } catch (IOException ex) {
            Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
             errorWriter.writeError(ex.toString());
        } 
        return feedMessage;
    
    }
    private void multipleLanguage(){
    String webSiteURLEN = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/help.exe/en?tpl=rss_feed";
    String webSiteURLNL = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/help.exe/nn?tpl=rss_feed";
    String webSiteURLFR = "http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/help.exe/fr?tpl=rss_feed";
    
        downloadFile("en", webSiteURLEN);
        downloadFile("nl", webSiteURLNL);
        downloadFile("fr", webSiteURLFR);
     
    }
    private void downloadFile(String language,String fileUrl){

      
        Document doc;
        int i =0;
        //English Version
        try {
            doc = Jsoup.connect(webSiteURL).timeout(10*1000).get(); 
           //Get all elements with img tag ,
           Elements disturbances = doc.getElementsByTag("item");
            for (Element el : disturbances) {
                
                NetworkDisturbance disturbance = new NetworkDisturbance(reformTitle(el.child(0).html()), el.child(1).html(), el.child(2).html(), language, el.child(3).html());      
                NetWorkDisturbances.add(disturbance);  
                i++;                
               // timeRange.setStart();                
               // alert.setActivePeriod(index, timeRange)            
             
           }         
            
           
        } catch (IOException ex) {
            Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
             errorWriter.writeError(ex.toString());
        }  
        
    
    
    }
    private GtfsRealtime.FeedMessage.Builder createAlerts(){
        
        
        GtfsRealtime.FeedMessage.Builder feedMessage =  GtfsRealtime.FeedMessage.newBuilder();        
        GtfsRealtime.FeedHeader.Builder feedHeader = GtfsRealtime.FeedHeader.newBuilder(); 
        feedHeader.setGtfsRealtimeVersion("1.0");
        feedHeader.setIncrementality(GtfsRealtime.FeedHeader.Incrementality.FULL_DATASET);
         //Unix Style
        feedHeader.setTimestamp(System.currentTimeMillis() / 1000L);
        feedMessage.setHeader(feedHeader);
        
        for (int i = 0; i < NetWorkDisturbances.size(); i++) {
            NetworkDisturbance disturbance = NetWorkDisturbances.get(i);
                String lang = disturbance.getLanguage();
            
                GtfsRealtime.FeedEntity.Builder feedEntity = GtfsRealtime.FeedEntity.newBuilder();
                GtfsRealtime.Alert.Builder alert = GtfsRealtime.Alert.newBuilder();
                
                //Setting the Description 
                GtfsRealtime.TranslatedString.Builder translatedDescriptionString =GtfsRealtime.TranslatedString.newBuilder();
                GtfsRealtime.TranslatedString.Translation.Builder translationsDescription = GtfsRealtime.TranslatedString.Translation.newBuilder();
                translationsDescription.setText( disturbance.getDescription());
                translationsDescription.setLanguage(lang);                
                translatedDescriptionString.addTranslation(0, translationsDescription);
                alert.setDescriptionText(translatedDescriptionString);
                //----------
                //Setting the Header text also known as Title
                GtfsRealtime.TranslatedString.Builder translatedHeaderString =GtfsRealtime.TranslatedString.newBuilder();              
                GtfsRealtime.TranslatedString.Translation.Builder translationsHeader = GtfsRealtime.TranslatedString.Translation.newBuilder();                
                translationsHeader.setText(disturbance.getTitle());
                //System.out.println(reformTitle(el.child(0).html()));
                
                
               // alert.setCause(setCause(reformTitle(el.child(0).html())));
               

                
                translationsHeader.setLanguage(lang);
                translatedHeaderString.addTranslation(0, translationsHeader);
                alert.setHeaderText(translatedHeaderString);
                //-----------
                //setting the url 
                GtfsRealtime.TranslatedString.Builder translatedUrlString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationUrl = GtfsRealtime.TranslatedString.Translation.newBuilder();
                
                translationUrl.setText(disturbance.getLink());
                translationUrl.setLanguage(lang);
                translatedUrlString.addTranslation(0, translationsHeader);
                alert.setUrl(translatedUrlString);
                //-----------              
                
 
                feedEntity.setAlert(alert);
                feedEntity.setId( disturbance.getPubDate() );
                             
                feedMessage.addEntity(i, feedEntity);
            
            
            
            
            
        }
        
        
       return feedMessage;
      
        
        
        
    }
    private GtfsRealtime.Alert.Builder returnAlert (String webSiteURLLang, String language){
       try {
           Document doc;        
           int i =0;
           doc = Jsoup.connect(webSiteURLLang).timeout(10*1000).get();
           
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
                translationsDescription.setLanguage(language);
                
                translatedDescriptionString.addTranslation(0, translationsDescription);
                alert.setDescriptionText(translatedDescriptionString);
                //----------
                //Setting the Header text also known as Title
                GtfsRealtime.TranslatedString.Builder translatedHeaderString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationsHeader = GtfsRealtime.TranslatedString.Translation.newBuilder();
                
                translationsHeader.setText(reformTitle(el.child(0).html()));
                System.out.println(reformTitle(el.child(0).html()));


                
                translationsHeader.setLanguage(language);
                translatedHeaderString.addTranslation(0, translationsHeader);
                alert.setHeaderText(translatedHeaderString);
                //-----------
                //setting the url 
                GtfsRealtime.TranslatedString.Builder translatedUrlString =GtfsRealtime.TranslatedString.newBuilder();
              
                GtfsRealtime.TranslatedString.Translation.Builder translationUrl = GtfsRealtime.TranslatedString.Translation.newBuilder();
                
                translationUrl.setText(el.child(2).html());
                translationUrl.setLanguage(language);
                translatedUrlString.addTranslation(0, translationsHeader);
                alert.setUrl(translatedUrlString);
                //-----------              
                
                String description =  el.child(1).html();   
                String pubDate = el.child(3).html(); 
               
                feedEntity.setAlert(alert);
                feedEntity.setId( pubDate +i );
                             
               // feedMessage.addEntity(i, feedEntity);
                i++;
                
               // timeRange.setStart();
                
               // alert.setActivePeriod(index, timeRange)
                
             
           }     
           return null;
       } catch (IOException ex) {
           Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
       }
       return null;
    }
    private void writeDisturbanceFile(){ 
        multipleLanguage();
        GtfsRealtime.FeedMessage.Builder feedMessage =  createAlerts();

       //Write the new Disturbance back to disk
        try {
            
                 FileOutputStream output = new FileOutputStream("service_alerts");
      
                  feedMessage.build().writeTo(output);
                  output.close();
                  System.out.println("Network Disturbance file writen successful");
            
        } catch (IOException e) {
            System.err.println("Error failed to write file");
             errorWriter.writeError(e.toString());
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
