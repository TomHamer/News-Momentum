package rss;

import com.google.common.base.Charsets;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import lombok.extern.slf4j.Slf4j;
import rss.util.DataReader;
import trade.model.Action;
import trade.model.Company;
import trade.model.Position;
import trade.model.Status;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.min;

/**
 * Created by Tom on 23/12/2017.
 */
@Slf4j
public class RSSFeedPoller implements Runnable {

    private static final int LOOKAHEAD = 5;
    private static final int POLL_DELAY = 30;

    private URL feedUrl;
    private HashSet<SyndEntry> processed;
    private BlockingQueue<Position> positions;

    private RSSFeedPoller() {
        try {
            this.feedUrl = RSSFeedPollerHelper.getFeed();
            this.processed = new HashSet<>();
            this.positions = new LinkedBlockingQueue<>();
        } catch (MalformedURLException e) {
            log.error("Could not getSweeper feed", e);
        }
    }

    public static RSSFeedPoller create() {
        return new RSSFeedPoller();
    }

    public BlockingQueue<Position> getPositions() {
        return positions;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                log.info("Polling...");
                poll();
            } catch (InterruptedException e) {
                log.error("Poller was interrupted", e);
            } catch (IOException e) {
                log.error("Connection failed", e);
            } catch (FeedException e) {
                log.error("Failed to open feed", e);
            } catch (Exception e) {
                log.error("Unexpected exception", e);
            }
        }
    }

    private void poll() throws InterruptedException, IOException, FeedException {
        TimeUnit.SECONDS.sleep(POLL_DELAY);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new InputStreamReader(feedUrl.openStream(), Charsets.UTF_8));

        if (feed.getEntries() != null) {
            List<SyndEntry> data = feed.getEntries();
            data = data.subList(0, min(data.size(), LOOKAHEAD));
            data.stream().filter(x -> !processed.contains(x)).forEach(d -> {
                processed.add(d);
                for (Company company : DataReader.getCompanies(d.getTitle())) {
                    Action position = DataReader.getRequiredAction(d.getTitle());
                    log.info("Taking " + position.name() + " position on " + company);
                    positions.add(new Position(position, company, Status.PENDING));
                }
            });
        } else {
            throw new FeedException("RSS feed was empty");
        }
    }

}
