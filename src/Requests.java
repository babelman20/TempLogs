import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class Requests {

    private static String GUID;
    private static String baseURL = "https://apiwww.easylogcloud.com";
    private static String apiToken;

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

    protected static boolean isValidGUID() {
        if (GUID != null) {
            if (!GUID.equals("")) {
                return true;
            }
        }
        return false;
    }
}
