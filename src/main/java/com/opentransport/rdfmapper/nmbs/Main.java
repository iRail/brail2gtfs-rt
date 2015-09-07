package com.opentransport.rdfmapper.nmbs;

import static com.opentransport.rdfmapper.nmbs.AddTripDemoUpdate.PromptForUpdate;
import com.opentransport.rdfmapper.nmbs.containers.GtfsRealtime;
import com.opentransport.rdfmapper.nmbs.containers.LiveBoard;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
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
    
    public static int EXIT_AUTOMATICALLY = 4; // minutes
    
    protected static RoutesReader rr;
    protected static TripReader tr;
    protected static CalendarDateReader cdr;
    
    protected static ScrapeTrip scrapeTrip;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Thread thread1 = new Thread() {
            public void run() {
                
                // Create trip_updates.pb
                generateTripUpdates();

                // Test
                // testData("trip_updates.pb");
                
                System.exit(0);
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                // Creating this class generates a new service_alerts.pb
                NetworkDisturbanceFetcher ndf = new NetworkDisturbanceFetcher();
                ndf.writeDisturbanceFile();
                
                // Test
                // testData("service_alerts.pb");
            }
        };
        Thread thread3 = new Thread() {
            public void run() {
                System.out.println("Safety Thread");

                try {
                    Thread.sleep(EXIT_AUTOMATICALLY * 400000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Safety thread aborted the program");
                System.exit(0);

            }
        };
        thread1.start();
        thread2.start();
        //Safety Thread to make sure that the program exits
        thread3.start();
    }

    public static void generateTripUpdates() {
        scrapeTrip = new ScrapeTrip();
        
//        Thread loadRoutes = new Thread() {
//            @Override
//            public void run() {
//                rr = new RoutesReader();
//                scrapeTrip.setRoutesReader(rr);
//            }
//        };
//        loadRoutes.start();
        
        Thread loadCalendarDates = new Thread() {
            public void run() {
                cdr =  new CalendarDateReader();
                scrapeTrip.setCalendarDateReader(cdr);
            }
        };                
        loadCalendarDates.start();
                        
        Thread loadTrips = new Thread() {
            public void run() {
                tr = new TripReader();
                scrapeTrip.setTripReader(tr);
            }
        };        
        loadTrips.start();
        
        
        String stationsNMBS = "http://irail.be/stations/NMBS/";
        
        List<String> stationIds = StationDatabase.getInstance().getAllStationIdsFromGTFSFeed();
        
        LiveBoardFetcher liveBoardFetcher = new LiveBoardFetcher();
        
        System.out.println("START OF LIVEBOARD FETCH");
        liveBoardFetcher.getLiveBoards(stationIds,"","",10000);
        
        scrapeTrip.startScrape(liveBoardFetcher.getTrainDelays());
    }
    
    public static void testData(String fileName) {
        try {
            GtfsRealtimeExample testenData = new GtfsRealtimeExample(fileName);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
