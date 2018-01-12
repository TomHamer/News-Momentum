import org.junit.Test;
import trade.Action;
import trade.IGWrapper;

import java.io.IOException;

public class IGWrapperTest {

    @Test
    public void testGetCFD() throws IOException {
        IGWrapper igWrapper = new IGWrapper();
        System.out.println(igWrapper.getCFD(Action.BUY,"TLS","ASX",100));
    }

}
