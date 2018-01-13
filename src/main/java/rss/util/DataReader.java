package rss.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import trade.Action;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Tom on 23/12/2017.
 */
@Slf4j
public class DataReader {

    private static final String GOOD_PHRASES_FILE_NAME = "good-phrases.txt";
    private static final String BAD_PHRASES_FILE_NAME = "bad-phrases.txt";
    private static final URL GOOD_PHRASES_URL = Resources.getResource(GOOD_PHRASES_FILE_NAME);
    private static final URL BAD_PHRASES_URL = Resources.getResource(BAD_PHRASES_FILE_NAME);

    private static boolean initialised = false;
    private static HashSet<String> goodPhrases;
    private static HashSet<String> badPhrases;

    private DataReader() {

    }

    static {
        initialisePhrasesIfRequired();
    }

    private static void initialisePhrasesIfRequired() {
        if (initialised) {
            return;
        }
        // Try load the files into sets
        try {
            goodPhrases = Sets.newHashSet(Resources.readLines(GOOD_PHRASES_URL, Charsets.UTF_8));
            log.info("Loaded good phrases");

            badPhrases = Sets.newHashSet(Resources.readLines(BAD_PHRASES_URL, Charsets.UTF_8));
            log.info("Loaded bad phrases");
            initialised = true;
        } catch (IOException e) {
            log.error("Could not fully load phrases", e);
        }
    }

    private static boolean isProperNoun(String str) {
        return !(str.toCharArray()[0] == str.toLowerCase().toCharArray()[0]);
    }

    private static String removeBrackets(String str) {
        // Should use Regex if we need more brackets
        return str.replace("(", "").replace(")", "");
    }

    @Value
    public static class Company {
        String ticker;
        String marketSym;
    }

    public static Company getCompanyData(@NonNull String s) throws IOException {
        if (isProperNoun(s)) {
            // need to lemmalise s, but for now
            s = s.replace("’s", "");

            String url = "https://www.google.com.au/search?q=" + s;
            String html = Jsoup.connect(url).get().html();
            Document doc = Jsoup.parse(html);

            if (html.contains(">Stock price<")) {
                Element stockTicker = doc.select("span[class$=kno-fv]").select("a[class$=fl]").get(0);
                Element marketSymbol = doc.select("span[class$=kno-fv]").select("span[class$=_RWc]").get(0);
                return new Company(stockTicker.text(), removeBrackets(marketSymbol.text()));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static List<Company> getCompanyNames(@NonNull String headline) {
        // Using list-map-filter-collect allows for implicit concurrency.
        log.info("Processing \"" + headline + "\"");

        List<String> words = Arrays.asList(headline.split(" "));
        return words.stream().map(word -> {
            try {
                return getCompanyData(word);
            } catch (IOException e) {
                log.warn("Couldn't get company data", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Action getRequiredAction(@NonNull String title) {
        initialisePhrasesIfRequired();

        if (goodPhrases.stream().anyMatch(title::contains)) {
            return Action.BUY;
        } else if (badPhrases.stream().anyMatch(title::contains)) {
            return Action.SELL;
        } else {
            return Action.HOLD;
        }
    }

}
