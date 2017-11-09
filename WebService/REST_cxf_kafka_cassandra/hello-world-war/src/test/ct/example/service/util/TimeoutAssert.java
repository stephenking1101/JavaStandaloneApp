package example.service.util;

import static org.junit.Assert.fail;

public class TimeoutAssert {

    public interface TimeOutFunction {
        Object getResult();
    }

    public static void timeoutEquals(int second, Object expect, TimeOutFunction actualFunction) throws InterruptedException {
        Object actual = actualFunction.getResult();
        Long s = 0L;
        while (s <= second * 1000L) {
            if (expect.equals(actual)) {
                return ;
            }
            Thread.sleep(100L);
            actual = actualFunction.getResult();
            s = s + 100L;
        }
        fail("timeout equals in " + second + "s expected:<" + expect + "> actual:<" + actual + ">");
    }
}
