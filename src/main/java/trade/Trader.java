package trade;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by Tom on 26/12/2017.
 */

@Slf4j
public class Trader implements Runnable {

    private Action toDo;
    private String ticker;
    private String marketSymbol;
    private int closeDelay;
    private IGWrapper igWrapper;


    public Trader(Action action, String ticker, String marketSymbol, int closeDelay) throws IOException {
        this.toDo = action;
        this.ticker = ticker;
        this.marketSymbol = marketSymbol;
        this.closeDelay = closeDelay;
        this.igWrapper = new IGWrapper();
    }


    private void buy(String ticker, String marketSymbol, int closeDelay) {
        //place a buy order, then make an async call that sells it after a period of time eg 30 mins
        try {
            String referenceNo = igWrapper.getCFD(Action.BUY, ticker, marketSymbol, 100);
            log.info("Reference number: " + referenceNo);
        } catch (IOException e) {
            log.error("Could not get CFD", e);
        } catch (UnexpectedResponseException e) {
            log.error("Unexpected response", e);
        }
    }

    private void shortSell(String ticker, String marketSymbol, int closeDelay) {
        //short sell
        try {
            String referenceNo = igWrapper.getCFD(Action.SELL, ticker, marketSymbol, 100);
            log.info(referenceNo);
        } catch (IOException e) {
            log.error("Could not get CFD", e);
        } catch (UnexpectedResponseException e) {
            log.error("Unexpected response", e);
        }
    }

    @Override
    public void run() {
        switch (toDo) {
            case HOLD:
                break;
            case BUY:
                buy(ticker, marketSymbol, closeDelay);
                break;
            case SELL:
                shortSell(ticker, marketSymbol, closeDelay);
                break;
            default:
                throw new RuntimeException("Unsupported operation " + toDo);
        }
    }
}
