import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class Requests {

    private static String baseURL = "https://apiwww.easylogcloud.com";
    private static String apiToken;
    private static String GUID;
    private static HashMap<String, String> devices = new HashMap<>();

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
            JSONObject response = (JSONObject) new JSONParser().parse(new InputStreamReader(stream, "UTF-8"));
            GUID = response.get("GUID").toString();
            System.out.printf("%s\n", GUID);
        }
    }

    protected static void getLocationGUID() throws IOException, ParseException {
        String query = baseURL + "/Locations.svc/UserLocations?";
        query += "APIToken=" + apiToken;
        query += "&userGUID=" + GUID;
        query += "&selectedUserGUID=" + GUID;
        InputStream stream = getData(query);
        if (stream != null) {
            JSONObject response = (JSONObject) new JSONParser().parse(new InputStreamReader(stream, "UTF-8"));
            GUID = response.get("GUID").toString();
            System.out.printf("%s\n", GUID);
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
            JSONArray response = (JSONArray) new JSONParser().parse(new InputStreamReader(stream, "UTF-8"));
            for (int i = 0; i < response.size(); i++) {
                JSONObject obj = (JSONObject) response.get(i);
                devices.put(obj.get("name").toString(), obj.get("GUID").toString());
            }
        }
    }

    protected static void getTempsTimes(String device, Date startDate, Date endDate) throws IOException, ParseException {
        HashMap<Date, Double> tempstimes = new HashMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyy%20KK:mm:ss");

        LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneOffset.systemDefault());
        start = start.minusHours(start.getHour()).minusMinutes(start.getMinute()).minusSeconds(start.getSecond()).minusNanos(start.getNano());
        start = LocalDateTime.ofInstant(start.toInstant(ZoneOffset.systemDefault().getRules().getStandardOffset(startDate.toInstant())), ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneOffset.systemDefault());
        end = end.plusHours(23-end.getHour()).plusMinutes(59-end.getMinute()).plusSeconds(59-end.getSecond()).minusNanos(end.getNano());
        end = LocalDateTime.ofInstant(end.toInstant(ZoneOffset.systemDefault().getRules().getStandardOffset(endDate.toInstant())), ZoneOffset.UTC);

        if (ZoneOffset.systemDefault().getRules().isDaylightSavings(startDate.toInstant())) {
            start = start.minusHours(1);
        }
        if (ZoneOffset.systemDefault().getRules().isDaylightSavings(endDate.toInstant())) {
            end = end.minusHours(1);
        }

        end = end.minusDays(1);

        String startStr = start.format(DateTimeFormatter.ofPattern("MM/dd/yyyy%20HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("MM/dd/yyyy%20HH:mm:ss"));
        String deviceID = devices.get(device);
        String query = baseURL + "/Devices.svc/Readings?";
        query += "APIToken=" + apiToken;
        query += "&userGUID=" + GUID;
        query += "&sensorGUID=" + deviceID;
        query += "&startDate=" + startStr;
        query += "&endDate=" + endStr;
        query += "&localTime=false&descendingDateOrder=true&samplesOnly=false";
        InputStream stream = getData(query);
        if (stream != null) {
            JSONArray response = (JSONArray) new JSONParser().parse(new InputStreamReader(stream, "UTF-8"));
            for (int i = 0; i < response.size(); i++) {
                System.out.printf("%d", i);
                JSONObject obj = (JSONObject) response.get(i);
                String d = (String) obj.get("datetime");
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(Long.parseLong(d.substring(6, d.length() - 7)));
                double temp = Double.parseDouble(((JSONArray) obj.get("channels")).get(0).toString());
                tempstimes.put(c.getTime(), temp * (double)9/5 + 32);
            }
        }

        System.out.printf("%s, %f", tempstimes.keySet().toArray()[0], fmt.format(tempstimes.get(tempstimes.keySet().toArray()[0])));
        System.out.printf("%s, %f", tempstimes.keySet().toArray()[tempstimes.size()-1], fmt.format(tempstimes.get(tempstimes.keySet().toArray()[tempstimes.size()-1])));

        /*for (int i = 0; i < tempstimes.size(); i++) {
            System.out.printf("%s, %s\n", tempstimes.keySet().toArray()[i], tempstimes.get(tempstimes.keySet().toArray()[i]));
        }*/
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

    protected static String[] getDeviceNames() {
        return devices.keySet().toArray(new String[0]);
    }

    protected static boolean isValidGUID() {
        if (GUID != null) {
            if (!GUID.equals("")) {
                return true;
            }
        }
        return false;
    }
}
