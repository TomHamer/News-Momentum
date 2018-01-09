import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

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
public class RSSFeedPoller implements Runnable {

    private final static int LOOKAHEAD = 5;
    private URL feedUrl;
    private HashSet seen = new HashSet<SyndEntryImpl>();


    public RSSFeedPoller() {
        try {
            this.feedUrl = RSSFeedPollerHelper.getFeed();
        } catch (MalformedURLException e) {
            System.out.println("Could not get feed.");
        }

    }

    @Override
    public void run() {

        while (true) {
            try {
                poll();
            } catch (InterruptedException e) {
                System.out.println("Poller was interrupted.");
            } catch (IOException e) {
                System.out.println("Connection failed.");
            } catch (FeedException e) {
                System.out.println("Failed to open feed.");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void poll() throws InterruptedException, IOException, FeedException {
        TimeUnit.SECONDS.sleep(1);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new InputStreamReader(feedUrl.openStream()));

        if (feed.getEntries() != null) {
            List<SyndEntryImpl> data = feed.getEntries();
            data = data.subList(0, min(data.size(), LOOKAHEAD));
            data.stream().filter(x -> !seen.contains(x)).forEach(d -> {
                seen.add(d);
                for (HashMap companyData : DataReader.getCompanyNames(d.getTitle())) {
                    System.out.println("Extracted company data " + companyData);
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
