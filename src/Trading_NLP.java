import com.sun.syndication.io.FeedException;

import java.io.IOException;



/**
 * Created by Tom on 18/12/2017.
 */
public class Trading_NLP {

    public static void main(String[] args) throws IOException, FeedException {
        RSSFeedPoller poller = new RSSFeedPoller();
        poller.run();
    }

}
