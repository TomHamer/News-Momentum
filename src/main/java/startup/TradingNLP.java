package startup;

import lombok.extern.slf4j.Slf4j;
import rss.RSSFeedPoller;


/**
 * Created by Tom on 18/12/2017.
 */
@Slf4j
public class TradingNLP {

    private TradingNLP() {

    }

    public static void main(String[] args) {
        RSSFeedPoller poller = RSSFeedPoller.create();

        log.info("Starting poller");
        poller.run();
    }

}
