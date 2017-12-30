import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Tom on 23/12/2017.
 */
public class DataReader {

    private static boolean initialised = false;
    private static HashSet<String> goodPhrases;
    private static HashSet<String> badPhrases;

    private static boolean isProperNoun(String s) {
        return !(s.toCharArray()[0]==s.toLowerCase().toCharArray()[0]);
    }

    public static HashMap<String, String> getCompanyData(String s) throws IOException {
        HashMap<String, String> toReturn = new HashMap<>();
        if (isProperNoun(s)) {
            //need to lemmalise s, but for now
            s=s.replace("’s","");

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
        return (List<HashMap<String, String>>) Arrays.asList(headline.split(" ")).stream().map((Function) o -> {
            try {
                return getCompanyData((String) o);
            } catch (IOException e) {
                return null;
            }
        }).filter(o -> !(o==null)).collect(Collectors.toList());
    }


    public static Action getRequiredAction(String title) {

        if (!initialised) initialise();

        for (String phrase : goodPhrases) {
            if (title.contains(phrase)) {
                return (Action.BUY);
            }
        }

        for (String phrase : badPhrases) {
            if (title.contains(phrase)) {
                return (Action.SELL);
            }
        }

        return Action.HOLD;


    }

    private static void initialise() {

        goodPhrases = new HashSet<>();
        badPhrases = new HashSet<>();

        try(BufferedReader br = new BufferedReader(new FileReader("resources/good-phrases.txt"))) {
            String line = "";

            while (line != null) {
                line = br.readLine();
                if (line != null) goodPhrases.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try(BufferedReader br = new BufferedReader(new FileReader("resources/bad-phrases.txt"))) {
            String line = "";

            while (line != null) {
                line = br.readLine();
                if (line != null) badPhrases.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        initialised = true;
    }


}
