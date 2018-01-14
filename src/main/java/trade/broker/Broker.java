package trade.broker;

import trade.model.Company;

public interface Broker {

    String buy(Company company, int size);

    String sell(Company company, int size);

}
