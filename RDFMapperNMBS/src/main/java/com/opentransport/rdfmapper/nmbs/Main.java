package com.opentransport.rdfmapper.nmbs;

import static com.opentransport.rdfmapper.nmbs.AddTripDemoUpdate.PromptForUpdate;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 *
 * @author Tim Tijssens
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
       runDemo();
       runScraper();
       scrapeLiveBoards();
       // testData();
        

        
        

         }
    public static void runDemo(){        
        GtfsRealtime.FeedMessage.Builder feedMessage = null;  
        
        try {
            feedMessage = PromptForUpdate();
        } catch (IOException ex) {
            Logger.getLogger(AddTripDemoUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }        
       //Write the new TripUpdate back to disk
        try {
            
                 FileOutputStream output = new FileOutputStream("gtfs-rt");
      
                  feedMessage.build().writeTo(output);
                  output.close();
                  System.out.println("File writen successful");
            
        } catch (IOException e) {
            System.err.println("Error failed to write file");
        }    
    }
    public static void runScraper(){
     ScrapeTrip scraper = new ScrapeTrip();
    
    }
    public static void testData(){
        try {
            GtfsRealtimeExample testenData =  new GtfsRealtimeExample();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    public static void scrapeLiveBoards(){
            
        Properties prop = new Properties();
        File f = new File("config.properties");
        if (f.exists() && f.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                prop.load(fis);
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
                System.out.println("Error reading config file: \"" + f.getName() + "\"");
                System.exit(-1);
            }
        }
        else {
            System.out.println("Could not find config file: \"" + f.getName() + "\"");
            System.exit(-2);
        }
        
        final int port = Integer.parseInt(prop.getProperty("port"));
        final int updateInterval = Integer.parseInt(prop.getProperty("updateInterval"));
        final boolean stoppable = Boolean.parseBoolean(prop.getProperty("stoppable"));
        
        System.out.println("START OF LIVEBOARD FETCH");
        SortedMapper mapper = new SortedMapper();
        
        final ServerContainer container = new ServerContainer(mapper,"NMBS");
        SocketAddress address = new InetSocketAddress(port);
        try {
            ContainerSocketProcessor csp = new ContainerSocketProcessor(container);
            Connection connection = new SocketConnection(csp);
            connection.connect(address);
            System.out.println(Calendar.getInstance().getTime() + ": SERVER STARTED AT PORT " + port);
            
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Calendar.getInstance().getTime() + ": START OF UPDATE");
                    long start = System.currentTimeMillis();
                    container.setMapper(new SortedMapper());
                    long end = System.currentTimeMillis();
                    System.out.println(Calendar.getInstance().getTime() + ": END OF UPDATE (" + (end - start) + " ms)");
                }
            },updateInterval,updateInterval,TimeUnit.SECONDS);
            
            if (stoppable) {
                boolean shutdown = false;
                while(!shutdown) {
                    System.out.println("Type STOP to shut down the server");
                    Scanner s = new Scanner(new InputStreamReader(System.in));
                    if (s.nextLine().toLowerCase().equals("stop")) {
                        scheduler.shutdown();
                        connection.close();
                        s.close();
                        System.out.println(Calendar.getInstance().getTime() + ": SERVER STOPPED!");
                        shutdown = true;
                    }
                    else {
                        System.out.print("ERROR! ");
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
        }
    
    }
}