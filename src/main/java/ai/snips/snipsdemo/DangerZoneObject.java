package ai.snips.snipsdemo;

import android.location.Location;

public class DangerZoneObject {

    int id;
    String name;
    Double longi;
    Double lati;
    String distance;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }

    public void setLati(Double lati) {
        this.lati = lati;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLongi() {
        return longi;
    }

    public Double getLati() {
        return lati;
    }

    public String getDistance() {
        return distance;
    }

    public DangerZoneObject(int id, String name, Double longi, Double lati, String distance){

        this.id = id;
        this.name = name;
        this.longi = longi;
        this.lati = lati;
        this.distance = distance;

    }




}
