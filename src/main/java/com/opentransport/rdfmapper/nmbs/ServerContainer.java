package com.opentransport.rdfmapper.nmbs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

/**
 *
 * @author Nicola De Clercq
 */
public class ServerContainer implements Container {
    
    private final String turtle = "text/turtle";
    private final String csv = "text/csv";
    private final String plain = "text/plain";
    private final String html = "text/html";
    private final String charset = "; charset=UTF-8";
    private final byte[] page = "<h1>Not a valid URL!</h1>".getBytes(StandardCharsets.UTF_8);
    private SortedMapper mapper;
    private final String agency;

    public ServerContainer(SortedMapper mapper, String agency) {
        this.mapper = mapper;
        this.agency = agency;
    }
    
    @Override
    public void handle(Request request, Response response) {
        String path = request.getPath().getPath();
        System.out.println(Calendar.getInstance().getTime() + ": GET " + path
                + " (" + request.getClientAddress().toString().replaceAll("^/","") + ")");
        String accept = request.getValue("Accept");
        if (accept == null) {
            accept = "";
        }
        String acceptEncoding = request.getValue("Accept-Encoding");
        if (acceptEncoding == null) {
            acceptEncoding = "";
        }
        response.setValue("Server","RPLOD Server " + agency);
        response.setDate("Date",System.currentTimeMillis());
        response.setDate("Last-Modified",mapper.getLastModified());
        try {
            OutputStream body = response.getOutputStream();
            if ((request.getDate("If-Modified-Since") - mapper.getLastModified()) > -1000) {
                response.setStatus(Status.NOT_MODIFIED);
                body.close();
            }
            else if (path.equalsIgnoreCase("/stations")) {
                if (acceptEncoding.contains("gzip")) {
                    response.setValue("Content-Encoding","gzip");
                    if (accept.contains(turtle)) {
                        response.setContentType(turtle + charset);
                        GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                        for (byte[] b : mapper.getTurtleStations()) {
                            gzipBody.write(b);
                        }
                        gzipBody.close();
                    }
                    else if (accept.contains(csv)) {
                        response.setContentType(csv + charset);
                        GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                        for (byte[] b : mapper.getLinkedcsvStations()) {
                            gzipBody.write(b);
                        }
                        gzipBody.close();
                    }
                    else {
                        response.setContentType(plain + charset);
                        GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                        for (byte[] b : mapper.getTurtleStations()) {
                            gzipBody.write(b);
                        }
                        gzipBody.close();
                    }
                }
                else {
                    if (accept.contains(turtle)) {
                        response.setContentType(turtle + charset);
                        for (byte[] b : mapper.getTurtleStations()) {
                            body.write(b);
                        }
                        body.close();
                    }
                    else if (accept.contains(csv)) {
                        response.setContentType(csv + charset);
                        for (byte[] b : mapper.getLinkedcsvStations()) {
                            body.write(b);
                        }
                        body.close();
                    }
                    else {
                        response.setContentType(plain + charset);
                        for (byte[] b : mapper.getTurtleStations()) {
                            body.write(b);
                        }
                        body.close();
                    }
                }
            }
            else if (path.equalsIgnoreCase("/departures")) {
                if (acceptEncoding.contains("gzip")) {
                    response.setValue("Content-Encoding","gzip");
                    if (accept.contains(turtle)) {
                        response.setContentType(turtle + charset);
                        GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                        for (byte[] b : mapper.getTurtleDepartures()) {
                            gzipBody.write(b);
                        }
                        gzipBody.close();
                    }
                    else if (accept.contains(csv)) {
                        response.setContentType(csv + charset);
                        GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                        for (byte[] b : mapper.getLinkedcsvDepartures()) {
                            gzipBody.write(b);
                        }
                        gzipBody.close();
                    }
                    else {
                        response.setContentType(turtle + charset);
                        GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                        for (byte[] b : mapper.getTurtleDepartures()) {
                            gzipBody.write(b);
                        }
                        gzipBody.close();
                    }
                }
                else {
                    if (accept.contains(turtle)) {
                        response.setContentType(turtle + charset);
                        for (byte[] b : mapper.getTurtleDepartures()) {
                            body.write(b);
                        }
                        body.close();
                    }
                    else if (accept.contains(csv)) {
                        response.setContentType(csv + charset);
                        for (byte[] b : mapper.getLinkedcsvDepartures()) {
                            body.write(b);
                        }
                        body.close();
                    }
                    else {
                        response.setContentType(turtle + charset);
                        for (byte[] b : mapper.getTurtleDepartures()) {
                            body.write(b);
                        }
                        body.close();
                    }
                }
            }
            else {
                response.setContentType(html + charset);
                response.setStatus(Status.NOT_FOUND);
                if (acceptEncoding.contains("gzip")) {
                    response.setValue("Content-Encoding","gzip");
                    GZIPOutputStream gzipBody = new GZIPOutputStream(body);
                    gzipBody.write(page);
                    gzipBody.close();
                }
                else {
                    body.write(page);
                    body.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerContainer.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
    
    public SortedMapper getMapper() {
        return mapper;
    }

    public void setMapper(SortedMapper mapper) {
        this.mapper = mapper;
    }

}