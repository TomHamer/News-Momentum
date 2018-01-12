
import java.io.IOException;


/**
 * Created by Tom on 18/12/2017.
 */
public class TradingNLP {

    public static void main(String[] args) throws IOException {
        RSSFeedPoller poller = new RSSFeedPoller();
        poller.run();
    }

}
