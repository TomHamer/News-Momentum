package trade;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import trade.broker.Broker;
import trade.broker.IGWrapper;
import trade.model.Position;
import trade.model.Status;
import util.UnexpectedResponseException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles all buy and sell operations. Failed execution of positions are retried, until the retry count has been
 * exceeded and they fail over.
 */
@Slf4j
public class TraderWithRetry implements ITrader {

    private static final int RETRY_TIME = 5;
    private static final int RETRY_COUNT = 3;

    private static final int CHECK_FUTURE_DELAY = 10;

    /**
     * Represents the execution status of a position that is being retried.
     */
    @Value
    private static final class Task implements Comparable<Task> {
        Position position;
        int count;
        DateTime expectedCompletionTime;

        @Override
        public int compareTo(Task other) {
            return expectedCompletionTime.compareTo(other.getExpectedCompletionTime());
        }
    }

    private Broker broker;
    private ScheduledExecutorService scheduledExecutor;

    // Priority queue ordered by expected completion time
    private PriorityBlockingQueue<ScheduledFuture<Task>> scheduledTasks;

    /**
     * Create a new TraderWithRetry. Throws UnexpectedResponseException if broker could not be created
     */
    public TraderWithRetry() throws UnexpectedResponseException {
        try {
            this.broker = new IGWrapper();
        } catch (IOException e) {
            throw new UnexpectedResponseException("Could not create IGWrapper");
        }
        this.scheduledExecutor = Executors.newScheduledThreadPool(4);
        this.scheduledTasks = new PriorityBlockingQueue<>();

        // Start new thread for checking the scheduled tasks
        new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(CHECK_FUTURE_DELAY);
                    checkScheduledTasks();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Scheduled Task checking thread stopped");
                }
            }
        }).start();
    }

    /**
     * Execute a position specified by the user
     */
    public void executePosition(@NonNull Position position) {
        // Ignore positions that have already succeeded
        if (position.getStatus() == Status.SUCCEEDED) {
            return;
        }

        // Execute position action
        executeAction(position);

        // Schedule position to be run again in the future if failed
        if (position.getStatus() == Status.FAILED) {
            retryPosition(position, 1);
        }

        log.info("Successfully executed position {}", position);
    }

    /**
     * Execute a position based on the action
     */
    private void executeAction(Position position) {
        // Execute the action
        switch (position.getAction()) {
            case BUY:
                buy(position, 10);
                break;
            case SELL:
                sell(position);
                break;
            case HOLD:
                hold(position);
                break;
            default:
                throw new RuntimeException("Unsupported action " + position.getAction());
        }
    }

    /**
     * Retry a position asynchronously until it has failed enough times
     */
    private void retryPosition(Position position, int count) {
        if (count > RETRY_COUNT) {
            throw new RuntimeException("Maximum retry count for position exceeded: " + position);
        }

        log.warn("Retrying position that failed to execute: {}", position);

        scheduledTasks.add(scheduledExecutor.schedule(() -> {
            executePosition(position);
            return new Task(position, count, DateTime.now().plusSeconds(RETRY_TIME));
        }, RETRY_TIME, TimeUnit.SECONDS));
    }

    /**
     * Check scheduled tasks, and retry them if required
     */
    private void checkScheduledTasks() throws ExecutionException, InterruptedException {
        // Consume all scheduled tasks that have been cancelled or are 'done'
        while (!scheduledTasks.isEmpty()) {

            ScheduledFuture<Task> future = scheduledTasks.peek();

            // Must check future is cancelled before isDone to ensure our queue checking operation isn't blocked
            if (future == null) {
                return;
            } else if (future.isCancelled()) {
                log.error("Future task erroneously cancelled", future);
                scheduledTasks.poll();
            } else if (!future.isDone()) {
                return;
            } else if (future.isDone()) {
                // Exception will be thrown if this future failed
                Task task = future.get();

                // Check if position succeeded, if not retry given count isn't exceeded
                Status positionStatus = task.getPosition().getStatus();
                switch (positionStatus) {
                    case SUCCEEDED:
                        log.info("Successfully executed position {} after {} retries", task.getPosition(), task.getCount());
                        break;
                    case FAILED:
                        // Retry task if it hasn't already been retried 'x' times
                        if (task.getCount() > RETRY_COUNT) {
                            log.error("Could not successfully execute position {} even after {} retries",
                                    task.getPosition(), RETRY_COUNT);
                        } else {
                            retryPosition(task.getPosition(), task.getCount() + 1);
                        }
                        break;
                    case IN_PROGRESS:
                    case PENDING:
                    default:
                        throw new RuntimeException("Unexpected status of position " + task.getPosition());
                }
                // Remove original task from queue
                scheduledTasks.poll();
            }
        }
    }

    /**
     * Execute a buy order
     */
    private void buy(Position position, int closeDelay) {
        final int positionSize = 100;
        String referenceNo = broker.buy(position.getCompany(), positionSize);

        if (referenceNo == null) {
            position.setStatus(Status.FAILED);
        } else {
            // Asynchronously sell the CFD after certain period
            scheduledTasks.add(scheduledExecutor.schedule(() -> {
                sell(position);
                return new Task(position, 1, DateTime.now().plusSeconds(closeDelay));
            }, closeDelay, TimeUnit.SECONDS));
            // Set position of buy task to be in progress (i.e. waiting for sell)
            position.setStatus(Status.IN_PROGRESS);
        }
    }

    /**
     * Execute a sell order
     */
    private void sell(Position position) {
        final int positionSize = 100;
        String referenceNo = broker.sell(position.getCompany(), positionSize);

        if (referenceNo == null) {
            position.setStatus(Status.FAILED);
        } else {
            position.setStatus(Status.SUCCEEDED);
        }
    }

    /**
     * Execute a hold order (does nothing)
     */
    private void hold(Position position) {
        position.setStatus(Status.SUCCEEDED);
    }

}
