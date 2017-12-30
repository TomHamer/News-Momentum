import org.junit.Test;

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

    public IGWrapper() throws IOException {
        String url = "https://demo-api.ig.com/gateway/deal/session";

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("POST");
        con.setRequestProperty("X-IG-API-KEY", "245d44fad406987d8a6ce78e971870fe3bae1087");
        con.setRequestProperty("VERSION", "2");
        con.setRequestProperty("Content-Type", "application/json; charset=utf8");

        //add request header
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

    public void getCFD(Action action, String ticker, String market) throws Exception {
        String url = "https://demo-api.ig.com/gateway/deal/session";

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("X-SECURITY-TOKEN", authToken);
        con.setRequestProperty("VERSION", "2");
        con.setRequestProperty("Content-Type", "application/json; charset=utf8");

        con.setRequestProperty("User-Agent", USER_AGENT);

        //here add the required body and request properties for the trade.
    }

    @Test
    public void IGWrapperTest() throws Exception {
        this.getCFD(Action.BUY,"TLS","ASX");
    }



}
