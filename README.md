# brail2gtfs-rt
# GTFS-realtime generator for the NMBS the Belgian Railway company 

Now you can use real-time data for your website, feed reader, or application. We provide our information in the General Transit Feed Specification (GTFS-realtime) format.

We can’t wait to see what you come up with! Be sure to tell us about your creation.


## How Do I Get Started ?

You will be able to access all the data  temporally on the web on irail.gent.be

To run the application at home you will need to have maven and the java jdk installed. 
You can either use an IDE as netbeans to run the code, 
How to run it from CLI 

##Requirements

You should have an unzipped gtfs feed of the Belgium railway unzipped in the root directory. This can be downloaded from http://gtfs.irail.be/nmbs/nmbs-latest.zip


```
mvn install
```
This will make sure all the dependancies are downloaded.
And then to run the application

```
java -jar target/RDFMapperNMBS-1.0-SNAPSHOT-jar-with-dependencies.jar
```

All the information is scraped from the NMBS and from iRail


Refer to the GTFS-realtime specification at https://developers.google.com/transit/gtfs-realtime/ for more
details on message field type, cardinality, etc. 

## Support or Contact 
If your run into any bugs or need any kind of support i am willing to help you. 
You can reach me on twitter @TimTijssens