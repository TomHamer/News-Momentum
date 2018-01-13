package rss;

import com.rometools.rome.feed.synd.SyndEntry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by Tom on 23/12/2017.
 */
public class RSSFeedPollerHelper {

    private RSSFeedPollerHelper() {

    }

    static URL getFeed() throws MalformedURLException {
        return getFeed("https://www.accc.gov.au/rss/media_releases.xml");
    }

    private static URL getFeed(String url) throws MalformedURLException {
        return new URL(url);
    }

    static boolean itemIsRecent(SyndEntry item) {
        return (new Date().getTime() - item.getPublishedDate().getTime() < 999999999);
    }

}
