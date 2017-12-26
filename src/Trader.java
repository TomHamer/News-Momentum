/**
 * Created by Tom on 26/12/2017.
 */
public class Trader {

    public static void buy(String ticker, String marketSymbol) {
        //place a buy order
        System.out.println("Buying "+ticker+" from "+marketSymbol);
    }

    public static void shortSell(String ticker, String marketSymbol) {
        //short sell
        System.out.println("Shorting "+ticker+" from "+marketSymbol);
    }


}
