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

    @Override
    public void run() {

        try {
            this.feedUrl=RSSFeedPollerHelper.getFeed();
        } catch(MalformedURLException e) {
            System.out.println("Could not get feed.");
        }

        while(true) {
            try {

                //add a pause to stop the poller from querying the ACCC too much
                TimeUnit.SECONDS.sleep(1);


                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new InputStreamReader(feedUrl.openStream()));

                if(feed.getEntries() != null) {
                    List<SyndEntryImpl> data = feed.getEntries();
                    data = data.subList(0,min(data.size(),LOOKAHEAD));

                    for(SyndEntryImpl d : data) {

                        if (!seen.contains(d)) {
                            seen.add(d);
                            if (RSSFeedPollerHelper.itemIsRecent(d)) {
                                for (HashMap companyData : DataReader.getCompanyNames(d.getTitle())) {
                                    System.out.println("Extracted company data "+companyData);
                                    if (companyData != null) {
                                        if (DataReader.getRequiredAction(d.getTitle()).equals(Action.BUY)) {
                                            Trader.buy((String) companyData.get("ticker"), (String) companyData.get("marketSym"));
                                        } else if (DataReader.getRequiredAction(d.getTitle()).equals(Action.SELL)) {
                                            Trader.shortSell((String) companyData.get("ticker"), (String) companyData.get("marketSym"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    throw new FeedException("RSS feed was empty");
                }

            } catch (InterruptedException e) {
                System.out.println("Poller was interrupted.");
            }  catch (IOException e) {
                System.out.println("Connection failed.");
            } catch (FeedException e) {
                System.out.println("Failed to open feed.");
                e.printStackTrace();
            }
        }

    }
}
