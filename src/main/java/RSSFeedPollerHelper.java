import com.rometools.rome.feed.synd.SyndEntry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by Tom on 23/12/2017.
 */
public class RSSFeedPollerHelper {
    static URL getFeed() throws MalformedURLException {
        return new URL("https://www.accc.gov.au/rss/media_releases.xml");
    }
    static boolean itemIsRecent(SyndEntry item) {
        return (new Date().getTime()-item.getPublishedDate().getTime() <999999999);
    }
}
