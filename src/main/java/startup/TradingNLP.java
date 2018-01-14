package startup;

import lombok.extern.slf4j.Slf4j;
import rss.RSSFeedPoller;
import sweeper.PositionSweeper;
import util.UnexpectedResponseException;


/**
 * Created by Tom on 18/12/2017.
 */
@Slf4j
public class TradingNLP {

    private TradingNLP() {

    }

    public static void main(String[] args) throws UnexpectedResponseException {
        // Start RSS poller
        RSSFeedPoller rssFeedPoller = RSSFeedPoller.create();
        Thread poller = new Thread(rssFeedPoller);

        log.info("Starting RSS poller");
        poller.start();

        // Start position sweeper
        PositionSweeper positionSweeper = PositionSweeper.getSweeper();
        positionSweeper.handoverPositions(rssFeedPoller.getPositions());

        Thread sweeper = new Thread(positionSweeper);
        log.info("Starting position sweeper");
        sweeper.start();
    }

}
