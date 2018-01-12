package trade;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tom on 28/12/2017.
 */
@Slf4j
public class IGWrapper {

    private final String USER_AGENT = "Mozilla/5.0";
    private final String authToken;
    private final String csToken;
    private final String API_KEY = "245d44fad406987d8a6ce78e971870fe3bae1087";

    public IGWrapper() throws IOException {
        String url = "https://demo-api.ig.com/gateway/deal/session";

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("X-IG-API-KEY", API_KEY);
        con.setRequestProperty("VERSION", "2");
        con.setRequestProperty("Content-Type", "application/json; charset=utf8");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setDoOutput(true);
        String str =  "{ \n" +
                "\"identifier\": \"newsmomentum\", \n" +
                "\"password\": \"ManalMomentum1\" \n" +
                "} ";
        byte[] outputInBytes = str.getBytes("UTF-8");
        OutputStream os = con.getOutputStream();
        os.write( outputInBytes );
        os.close();

        if(con.getResponseCode() == 200) {
            this.authToken = con.getHeaderField("X-SECURITY-TOKEN");
            this.csToken = con.getHeaderField("CST");
        } else {
            throw new IOException("Failed to connect to trading API");
        }

    }

    private String findEpic(String ticker, String market) throws IOException, UnexpectedResponseException {
        //if(!market.equals("ASX")) {
        //    System.out.println("Only supporting ASX");
        //    throw new NotImplementedException();
        //}
        BufferedReader br = null;
        try {
            final String urlBase = "https://demo-api.ig.com/gateway/deal/markets?searchTerm=";
            String url = urlBase + ticker;
            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("CST", csToken);
            con.setRequestProperty("X-SECURITY-TOKEN", authToken);
            con.setRequestProperty("VERSION", "1");
            con.setRequestProperty("Content-Type", "application/json; charset=utf8");
            con.setRequestProperty("X-IG-API-KEY", API_KEY);

            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setDoOutput(true);
            if (200 <= con.getResponseCode() && con.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JSONObject jsonObject = new JSONObject(br.readLine());
                return jsonObject.getJSONArray("markets").getJSONObject(0).getString("epic");
            }
            throw new UnexpectedResponseException("Expected 200, got "+con.getResponseMessage());
        } catch (Exception e) {
            throw e;
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public String getCFD(Action action, String ticker, String market, int size) throws IOException, UnexpectedResponseException {
        OutputStream os = null;
        BufferedReader br = null;
        try {
            String url = "https://demo-api.ig.com/gateway/deal/positions/otc";

            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("CST", csToken);
            con.setRequestProperty("X-SECURITY-TOKEN", authToken);
            con.setRequestProperty("VERSION", "2");
            con.setRequestProperty("Content-Type", "application/json; charset=utf8");
            con.setRequestProperty("X-IG-API-KEY", API_KEY);

            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("epic", findEpic(ticker, market));
            jsonObject.put("expiry", "DFB"); // Daily funded bet. See https://www.ig.com/uk/glossary-trading-terms/dfb-definition
            jsonObject.put("direction", action.toString());
            jsonObject.put("size", size);
            jsonObject.put("orderLevel", "6695.5m");
            jsonObject.put("orderType", "LIMIT"); // See https://www.ig.com/au/orders-articles
            jsonObject.put("timeInForce", "FILL_OR_KILL"); // See https://labs.ig.com/apiorders
            jsonObject.put("guaranteedStop", false);
            jsonObject.put("trailingStop", false);
            jsonObject.put("forceOpen", false);
            jsonObject.put("currencyCode", "AUD");

            byte[] outputInBytes = jsonObject.toString().getBytes("UTF-8");
            os = con.getOutputStream();
            os.write(outputInBytes);
            os.close();

            if (con.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JSONObject response = new JSONObject(br.readLine());
                return response.getString("dealReference");
            } else {
                log.warn("Method output: " + con.getHeaderField("content-type"));
                throw new IOException("Failed to connect to trading API. Got " + con.getResponseCode() + "(" + con.getHeaderField("errorCode") + ").");
            }
            //here add the required body and request properties for the trade.
        } finally {
            if (os != null) {
                os.close();
            }

            if (br != null) {
                br.close();
            }
        }

    }

    public void exitPosition(String dealReference) {

    }

}
