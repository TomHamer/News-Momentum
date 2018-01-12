package rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import lombok.extern.slf4j.Slf4j;
import rss.util.DataReader;
import trade.Action;
import trade.Trader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.min;

/**
 * Created by Tom on 23/12/2017.
 */
@Slf4j
public class RSSFeedPoller implements Runnable {

    private final static int LOOKAHEAD = 5;
    private URL feedUrl;
    private HashSet seen = new HashSet<SyndEntryImpl>();

    private RSSFeedPoller() {
        try {
            this.feedUrl = RSSFeedPollerHelper.getFeed();
        } catch (MalformedURLException e) {
            log.error("Could not get feed", e);
        }
    }

    public static RSSFeedPoller create() {
        return new RSSFeedPoller();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
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
        TimeUnit.SECONDS.sleep(1);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new InputStreamReader(feedUrl.openStream()));

        if (feed.getEntries() != null) {
            List<SyndEntry> data = feed.getEntries();
            data = data.subList(0, min(data.size(), LOOKAHEAD));
            data.stream().filter(x -> !seen.contains(x)).forEach(d -> {
                seen.add(d);
                for (HashMap companyData : DataReader.getCompanyNames(d.getTitle())) {
                    log.info("Extracted company data " + companyData);
                    if (companyData != null) {
                        try {
                            if (DataReader.getRequiredAction(d.getTitle()).equals(Action.BUY)) {
                                Trader t = new Trader(Action.BUY, (String) companyData.get("ticker"), (String) companyData.get("marketSym"), 1000);
                                t.run();
                            } else if (DataReader.getRequiredAction(d.getTitle()).equals(Action.SELL)) {
                                Trader t = new Trader(Action.SELL, (String) companyData.get("ticker"), (String) companyData.get("marketSym"), 1000);
                                t.run();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });
        } else {
            throw new FeedException("RSS feed was empty");
        }
    }

}
