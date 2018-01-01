import org.junit.Test;
import org.json.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tom on 28/12/2017.
 */
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
            System.out.println("Constructor output:" + con.getHeaderField("content-type"));
        } else {
            throw new IOException("Failed to connect to trading API");
        }

    }

    public void getCFD(Action action, String ticker, String market, int size) throws Exception {
        String url = "https://demo-api.ig.com/gateway/deal/positions/otc";

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("CST",csToken);
        con.setRequestProperty("X-SECURITY-TOKEN", authToken);
        con.setRequestProperty("VERSION", "2");
        con.setRequestProperty("Content-Type", "application/json; charset=utf8");
        con.setRequestProperty("X-IG-API-KEY", API_KEY);

        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setDoOutput(true);

        JSONObject jsonObject = new JSONObject();
        /*I have hard coded the epic for now. Since it is not a constant, it needs to be searched for everytime.
         * This is the epic for ASX Ltd at the time of writing. To search for the epic, GET /marketnavigation needs
         * to be implemented*/
        jsonObject.put("epic", "AA.D.ASX.CASH.IP");
        jsonObject.put("expiry", "DFB"); // Daily funded bet. See https://www.ig.com/uk/glossary-trading-terms/dfb-definition
        jsonObject.put("direction", action.toString());
        jsonObject.put("size", size);
        jsonObject.put("orderType", "MARKET"); // See https://www.ig.com/au/orders-articles
        jsonObject.put("timeInForce", "FILL_OR_KILL"); // See https://labs.ig.com/apiorders
        jsonObject.put("guaranteedStop", false);
        jsonObject.put("trailingStop", false);
        jsonObject.put("forceOpen", false);
        jsonObject.put("currencyCode", "AUD");

//        String str =  "{ \n" +
//                /*I have hard coded the epic for now. Since it is not a constant, it needs to be searched for everytime.
//                * This is the epic for ASX Ltd at the time of writing. To search for the epic, GET /marketnavigation needs
//                * to be implemented*/
//                "\"epic\": \"AA.D.ASX.CASH.IP\", \n" +
//                "\"expiry\": \"DFB\" \n" + // Daily funded bet. See https://www.ig.com/uk/glossary-trading-terms/dfb-definition
//                "\"direction\":" + action + "\n" +
//                "\"size\":" + 1 + "\n" +
//                "\"orderType\": \"MARKET\" \n" +  // See https://www.ig.com/au/orders-articles
//                "\"timeInForce\": \"FILL_OR_KILL\" \n" + // See https://labs.ig.com/apiorders
//                "\"guaranteedStop\":" + false + "\n" +
//                "\"trailingStop\":" + false + "\n" +
//                "\"forceOpen\":" + false + "\n" +
//                "\"currencyCode\": \"AUD\" \n" +
//                "} ";

        /*
        * Also relevant
        * https://labs.ig.com/rest-trading-api-reference/service-detail?id=542
        * https://labs.ig.com/node/37
        * https://www.ig.com/sg/risk-management
        * */



        byte[] outputInBytes = jsonObject.toString().getBytes("UTF-8");
        OutputStream os = con.getOutputStream();
        os.write( outputInBytes );
        os.close();
        String reference;

        if(con.getResponseCode() == 200) {
            reference = con.getHeaderField("dealReference");
            System.out.println(reference);
        } else {
            System.out.println("Method output: " + con.getHeaderField("content-type"));
            throw new IOException("Failed to connect to trading API. Got " + con.getResponseCode() + "(" + con.getHeaderField("errorCode") + ").");
        }

        //here add the required body and request properties for the trade.
    }

    @Test
    public void IGWrapperTest() throws Exception {
        this.getCFD(Action.BUY,"TLS","ASX",100);
    }



}
