import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Tom on 26/12/2017.
 */

public class Trader implements Runnable {

    private Action toDo;
    private String ticker;
    private String marketSymbol;
    private int closeDelay;
    private IGWrapper igWrapper;


    public Trader(Action action, String ticker, String marketSymbol, int closeDelay) throws IOException {
        this.toDo=action;
        this.ticker = ticker;
        this.marketSymbol = marketSymbol;
        this.closeDelay = closeDelay;
        igWrapper = new IGWrapper();
    }


    public void buy(String ticker, String marketSymbol, int closeDelay) throws IOException, JSONException {
        //place a buy order, then make an async call that sells it after a period of time eg 30 mins
        String referenceNo = igWrapper.getCFD(Action.BUY, ticker, marketSymbol, 100);
        System.out.println(referenceNo);
    }

    public void shortSell(String ticker, String marketSymbol, int closeDelay) throws IOException, JSONException {
        //short sell
        String referenceNo = igWrapper.getCFD(Action.SELL, ticker, marketSymbol, 100);



    }

    @Override
    public void run() {
        switch (toDo) {
            case BUY:
                try {
                    buy(ticker, marketSymbol, closeDelay);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            case SELL:
                try {
                    shortSell(ticker, marketSymbol, closeDelay);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }
}
