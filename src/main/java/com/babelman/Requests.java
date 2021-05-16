package com.babelman;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.time.temporal.ChronoUnit.DAYS;

public abstract class Requests {

    private final static String baseURL = "https://apiwww.easylogcloud.com";
    private static String apiToken;
    private static String GUID;
    private final static ArrayList<Device> devices = new ArrayList<>();

    protected static void setApiToken(String token) {
        apiToken = token;
    }

    protected static void getGUID(String email, String pass) throws IOException, ParseException {
        String query = baseURL + "/Users.svc/Login?";
        query += "APIToken=" + apiToken;
        query += "&email=" + email;
        query += "&password=" + pass;
        InputStream stream = getData(query);
        if (stream != null) {
            JSONObject response = (JSONObject) new JSONParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            GUID = response.get("GUID").toString();
        }
    }

    protected static void getDeviceInfo() throws IOException, ParseException {
        String query = baseURL + "/Devices.svc/AllDevicesSummary?";
        query += "APIToken=" + apiToken;
        query += "&userGUID=" + GUID;
        query += "&selectedUserGUID=" + GUID;
        query += "&includeArchived=false";
        InputStream stream = getData(query);
        if (stream != null) {
            JSONArray response = (JSONArray) new JSONParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            for (Object o : response) {
                JSONObject obj = (JSONObject) o;
                devices.add(new Device(obj.get("name").toString(), obj.get("GUID").toString(), obj.get("MACAddress").toString()));
            }
        }
    }

    protected static void getTempsTimes(String deviceName, Date startDate, Date endDate, String shareUser) throws IOException, ParseException {
        ArrayList<DataElement> tempData = new ArrayList<>();

        LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneOffset.systemDefault());
        start = start.minusHours(start.getHour()).minusMinutes(start.getMinute()).minusSeconds(start.getSecond()).minusNanos(start.getNano());
        start = LocalDateTime.ofInstant(start.toInstant(ZoneOffset.systemDefault().getRules().getStandardOffset(startDate.toInstant())), ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneOffset.systemDefault());
        end = end.minusDays(1).plusHours(23-end.getHour()).plusMinutes(59-end.getMinute()).plusSeconds(59-end.getSecond()).minusNanos(end.getNano());
        end = LocalDateTime.ofInstant(end.toInstant(ZoneOffset.systemDefault().getRules().getStandardOffset(endDate.toInstant())), ZoneOffset.UTC);

        if (ZoneOffset.systemDefault().getRules().isDaylightSavings(startDate.toInstant())) {
            start = start.minusHours(1);
        }
        if (ZoneOffset.systemDefault().getRules().isDaylightSavings(endDate.toInstant())) {
            end = end.minusHours(1);
        }

        Device device = devices.get(0);
        for (Device d : devices) {
            if (d.getName().equals(deviceName)) {
                device = d;
                break;
            }
        }
        ApplicationGUI.setProgress(5);

        String startStr = start.format(DateTimeFormatter.ofPattern("MM/dd/yyyy%20HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("MM/dd/yyyy%20HH:mm:ss"));
        String deviceID = device.getId();
        String query = baseURL + "/Devices.svc/Readings?";
        query += "APIToken=" + apiToken;
        query += "&userGUID=" + GUID;
        query += "&sensorGUID=" + deviceID;
        query += "&startDate=" + startStr;
        query += "&endDate=" + endStr;
        query += "&localTime=false&descendingDateOrder=false&samplesOnly=false";
        InputStream stream = getData(query);

        long daySpan = DAYS.between(start, end) + 1;
        int count = 0;

        LocalDateTime date = null;
        boolean alarm = false;
        double openTemp = Double.POSITIVE_INFINITY, closeTemp = Double.POSITIVE_INFINITY,
                highTemp = -Double.MAX_VALUE, lowTemp = Double.MAX_VALUE;
        double temp = 0;
        if (stream != null) {
            JSONArray response = (JSONArray) new JSONParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
            ApplicationGUI.setProgress(10);
            for (Object o : response) {
                JSONObject obj = (JSONObject) o;
                String d = (String) obj.get("datetime");
                LocalDateTime day = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(d.substring(6, d.length() - 7))), ZoneOffset.systemDefault());

                if (date == null) {
                    date = day;
                } else if (day.toLocalDate().isAfter(date.toLocalDate())) {
                    count++;
                    ApplicationGUI.setProgress((int)(10+(40)*(double)count/daySpan));
                    tempData.add(new DataElement(date.toLocalDate(), openTemp, closeTemp, highTemp, lowTemp, alarm));
                    date = day;
                    openTemp = Double.POSITIVE_INFINITY;
                    closeTemp = Double.POSITIVE_INFINITY;
                    highTemp = -Double.MAX_VALUE;
                    lowTemp = Double.MAX_VALUE;
                    alarm = false;
                }

                if (openTemp == Double.POSITIVE_INFINITY && day.getHour() > 7) openTemp = temp;
                if (closeTemp == Double.POSITIVE_INFINITY && day.getHour() > 18) closeTemp = temp;

                temp = Double.parseDouble(((JSONArray) obj.get("channels")).get(0).toString());
                temp = temp * (double)9/5 + 32;
                temp = (double)Math.round(temp*10)/10;

                highTemp = Double.max(highTemp, temp);
                lowTemp = Double.min(lowTemp, temp);

                if (!alarm) {
                    alarm = ((long)obj.get("readingType") == -1);
                }
            }

            if (date != null) tempData.add(new DataElement(date.toLocalDate(), openTemp, closeTemp, highTemp, lowTemp, alarm));
        }

        try {
            GoogleQuickstart.createNewSpreadsheet(startDate, endDate, device, tempData, shareUser);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private static InputStream getData(String query) throws IOException {
        URL obj = new URL(query);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        if (con.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            return con.getInputStream();
        } else {
            return null;
        }
    }

    public static String[] getDeviceNames() {
        String[] names = new String[devices.size()];
        for(int i = 0; i < devices.size(); i++) {
            names[i] = devices.get(i).getName();
        }
        return names;
    }

    public static boolean isValidGUID() {
        if (GUID != null) {
            return !GUID.equals("");
        }
        return false;
    }
}
