package com.opentransport.rdfmapper.nmbs;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        
        System.out.println("AMOUNT OF CONNECTIONS NMBS: " + liveBoardFetcher.countConnections);
        
        scrapeTrip.startScrape(liveBoardFetcher.getTrainDelays());
        
        System.out.println("AMOUNT OF CONNECTIONS IRAIL: " + scrapeTrip.countConnections);
    }
    
    public static void testData(String fileName) {
        try {
            GtfsRealtimeExample testenData = new GtfsRealtimeExample(fileName);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
