import org.junit.Test;

import java.io.IOException;

public class IGWrapperTest {

    @Test
    public void testGetCFD() throws IOException {
        IGWrapper igWrapper = new IGWrapper();
        System.out.println(igWrapper.getCFD(Action.BUY,"TLS","ASX",100));
    }

}
