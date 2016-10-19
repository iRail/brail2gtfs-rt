# brail2gtfs-rt
# GTFS-RealTime generator for the Belgian Railway company SNCB/NMBS

This GTFS-RT generator provides realtime updates (service alerts and trip updates) for the following NMBS [GTFS](https://github.com/iRail/brail2gtfs) dataset:
http://gtfs.irail.be/nmbs/nmbs-latest.zip

## Requirements

You should have an unzipped gtfs feed of the Belgium railway unzipped in the root directory. This can be downloaded from http://gtfs.irail.be/nmbs/nmbs-latest.zip

Calendar_dates.txt must be ordered by date. You can use following command to do this:

```bash
mv calendar_dates.txt calendar_dates_total.txt
sort --field-separator=',' -k2 -k1 -k3 calendar_dates_total.txt > calendar_dates_sorted.txt
```

It is recommended to take a subset of calendar dates from calendar_dates.txt. 
This subset should start from now to +/- 3 months in the future. 
You can do this with following command:

`sed -n 'START_LINENUMBER,END_LINENUMBER' calendar_dates_sorted.txt > calendar_dates.txt`


## How Do I Get Started ?

To run the application at home you will need to have Maven and the Java JDK installed. 
You can either use an IDE (e.g. Netbeans) or CLI:
```
mvn install
```
This will make sure all the dependencies are downloaded.
Run the application with:

```
java -jar target/RDFMapperNMBS-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Note
Information is scraped from the NMBS [website](belgianrail.be) and iRail [API](api.irail.be).

Refer to the GTFS-realtime specification at https://developers.google.com/transit/gtfs-realtime/ for more
details on message field type, cardinality, etc. 

=================
This project is made possible by a collaboration between [Be-Mobile](http://www.be-mobile-international.com/) and [iRail](irail.be) during open Summer of code 2015.
