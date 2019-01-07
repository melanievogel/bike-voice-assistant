package ai.snips.snipsdemo;

import android.location.Location;
import android.text.Editable;

public class DangerZoneObject {

    String name;
    Double longi;
    Double lati;
    String distance;

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

    public DangerZoneObject(String name, Double longi, Double lati, String distance){

        this.name = name;
        this.longi = longi;
        this.lati = lati;
        this.distance = distance;

    }




}
