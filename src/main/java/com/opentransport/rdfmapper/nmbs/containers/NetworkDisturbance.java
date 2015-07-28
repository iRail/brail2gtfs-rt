/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opentransport.rdfmapper.nmbs.containers;

/**
 *
 * @author timtijssens
 */
public class NetworkDisturbance {
    
    private String title;
    private String description;
    private String link;
    private String language;
    private String pubDate;
    
    public NetworkDisturbance(String title, String description, String link , String language, String pubDate){
    this.title = title;
    this.description = description;
    this.link=link;
    this.language=language;
    this.pubDate=pubDate;      
    }
    
    

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return the pubDate
     */
    public String getPubDate() {
        return pubDate;
    }
    
    
}
