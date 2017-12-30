/**
 * Created by Tom on 26/12/2017.
 */

public class Trader {

    public static void buy(String ticker, String marketSymbol) {
        //place a buy order, then make an async call that sells it after a period of time eg 30 mins

        System.out.println("Buying "+ticker+" from "+marketSymbol);

    }

    public static void shortSell(String ticker, String marketSymbol) {
        //short sell
        System.out.println("Shorting "+ticker+" from "+marketSymbol);
    }


}
