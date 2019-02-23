package utils;

import android.location.Criteria;
import android.location.LocationManager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ai.snips.snipsdemo.DangerZoneObject;

public class ActionsUtil {

    public static String getCriteria(LocationManager m) {
        String p;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        p = m.getBestProvider(criteria, true);
        return p;
    }
    public static ArrayList<DangerZoneObject> read(String file) {
        ArrayList<DangerZoneObject> result = new ArrayList<>();
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line;
            while ((line = buf.readLine()) != null) {
                if (line.equals("name")) {
                    DangerZoneObject object = new DangerZoneObject("", 0.0, 0.0, "");
                    object.setName(buf.readLine());
                    object.setLati(Double.parseDouble(buf.readLine()));
                    object.setLongi(Double.parseDouble(buf.readLine()));
                    result.add(object);
                }
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<DangerZoneObject> read(String file, String delete) {
        ArrayList<DangerZoneObject> result = new ArrayList<>();
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line;
            String temp;
            while ((line = buf.readLine()) != null) {
                if (line.equals("name")) {
                    DangerZoneObject object = new DangerZoneObject("", 0.0, 0.0, "");
                    object.setName(buf.readLine());
                    object.setLati(Double.parseDouble(buf.readLine()));
                    object.setLongi(Double.parseDouble(buf.readLine()));
                    temp = object.getName() + "\n" + "LG: " + object.getLongi() + ", BG: " + object.getLati() + " \nDist: ";
                    if (delete != null) {
                        if (!delete.contains(temp)) {
                            result.add(object);
                        }
                    } else {
                        result.add(object);
                    }
                }
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void write(String file, ArrayList<DangerZoneObject> myArrayList, String delete) {
        FileOutputStream out;
        myArrayList.addAll(read(file, delete));
        try {
            out = new FileOutputStream(file);
            for (int i = myArrayList.size() - 1; i > -1; i--) {
                String content = "name\n" + myArrayList.get(i).getName() + "\n" + myArrayList.get(i).getLati() + "\n" + myArrayList.get(i).getLongi() + "\n";
                out.write(content.getBytes());
            }
            out.close();
        } catch (Exception e) { //fehlende Permission oder sd an pc gemountet
            e.printStackTrace();
        }
    }

    public static void write(String file, ArrayList<DangerZoneObject> myArrayList) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            for (int i = myArrayList.size() - 1; i > -1; i--) {
                String content = "name\n" + myArrayList.get(i).getName() + "\n" + myArrayList.get(i).getLati() + "\n" + myArrayList.get(i).getLongi() + "\n";
                out.write(content.getBytes());
            }
            out.close();
        } catch (Exception e) { //fehlende Permission oder sd an pc gemountet
            e.printStackTrace();
        }
    }
}
