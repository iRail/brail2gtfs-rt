/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author timtijssens
 */
public class NetworkDisturbanceFetcher {
    
    public  NetworkDisturbanceFetcher(){
    ScrapeDisturbances();
    }
    
    private static void ScrapeDisturbances(){
        System.out.println("Start Scraping");
        Document doc;
        try {
            doc = Jsoup.connect("http://www.belgianrail.be/jpm/sncb-nmbs-routeplanner/help.exe/en?tpl=him_map").get();
            Elements disturbances = doc.select("#himmap_table_div table tr td a span a");
            System.out.println(disturbances.toString());
        } catch (IOException ex) {
            Logger.getLogger(NetworkDisturbanceFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
    
    }
    
}
