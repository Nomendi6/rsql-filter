package testappl.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AppObjectTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AppObject getAppObjectSample1() {
        return new AppObject().withId(1L).withCode("code1").withName("name1").withSeq(1L);
    }

    public static AppObject getAppObjectSample2() {
        return new AppObject().withId(2L).withCode("code2").withName("name2").withSeq(2L);
    }

    public static AppObject getAppObjectRandomSampleGenerator() {
        return new AppObject()
            .withId(longCount.incrementAndGet())
            .withCode(UUID.randomUUID().toString())
            .withName(UUID.randomUUID().toString())
            .withSeq(longCount.incrementAndGet());
    }
}
