package trade.model;

import lombok.Data;

/**
 * Represents a position and its current status. The action and company for the position is immutable,
 * while the status is mutable.
 */
@Data
public class Position {
    private final Action action;
    private final Company company;
    private Status status;

    public Position(Action action, Company company, Status status) {
        this.action = action;
        this.company = company;
        this.status = status;
    }

    public Action getAction() {
        return action;
    }

    public Company getCompany() {
        return company;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        // Guaranteed to be atomic by JVM
        this.status = status;
    }

}
