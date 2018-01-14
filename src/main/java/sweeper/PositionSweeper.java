package sweeper;

import lombok.extern.slf4j.Slf4j;
import trade.ITrader;
import trade.TraderWithRetry;
import trade.model.Position;
import util.UnexpectedResponseException;

import java.util.concurrent.BlockingQueue;

/**
 * Clears the queue of positions taken up by the RSSFeedPoller
 */
@Slf4j
public class PositionSweeper implements Runnable {

    private static PositionSweeper singletonSweeper;

    private BlockingQueue<Position> positions;
    private ITrader trader;

    private PositionSweeper() throws UnexpectedResponseException {
        this.trader = new TraderWithRetry();
    }

    public static synchronized PositionSweeper getSweeper() throws UnexpectedResponseException {
        // Only allow one sweeper to exist (singleton)
        if (singletonSweeper == null) {
            singletonSweeper = new PositionSweeper();
        }
        return singletonSweeper;
    }

    public void handoverPositions(BlockingQueue<Position> positions) {
        this.positions = positions;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Position position = positions.take();
                trader.executePosition(position);
            } catch (InterruptedException e) {
                log.error("Could not take position from queue", e);
            }
        }
    }
}
