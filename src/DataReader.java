import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Tom on 23/12/2017.
 */
public class DataReader {

    private static boolean isProperNoun(String s) {
        return !(s.toCharArray()[0]==s.toLowerCase().toCharArray()[0]);
    }

    public static HashMap<String, String> getCompanyData(String s) throws IOException {
        HashMap<String, String> toReturn = new HashMap<>();
        if (isProperNoun(s)) {
            String url = "https://www.google.com.au/search?q=" + s;
            String html = Jsoup.connect(url).get().html();
            Document doc = Jsoup.parse(html);

            if (html.contains(">Stock price<")) {
                Element stockTicker = doc.select("span[class$=kno-fv]").select("a[class$=fl]").get(0);
                Element marketSymbol = doc.select("span[class$=kno-fv]").select("span[class$=_RWc]").get(0);

                toReturn.put("ticker",stockTicker.text());
                toReturn.put("marketSym",marketSymbol.text().replace("(","").replace(")",""));
                return toReturn;

            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    public static List<HashMap<String,String>> getCompanyNames(String headline)  {
        /*
        * Using list-map-filter-collect allows for implicit concurrency.
         */

        System.out.println("Processing \""+headline+"\"");
        List<HashMap<String, String>> toReturn = (List<HashMap<String, String>>) Arrays.asList(headline.split(" ")).stream().map((Function) o -> {
            try {
                return getCompanyData((String) o);
            } catch (IOException e) {
                return null;
            }
        }).filter(o -> !(o==null)).collect(Collectors.toList());
        return toReturn;
    }


    public static Action getRequiredAction(String title) {

        if(title.contains("won't oppose")) {
            return(Action.BUY);
        }

        if(title.contains("to oppose")) {
            return(Action.SELL);
        }

        return Action.HOLD;


    }


}
