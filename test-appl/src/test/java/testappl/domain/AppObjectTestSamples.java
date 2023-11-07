package testappl.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AppObjectTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AppObject getAppObjectSample1() {
        return new AppObject().id(1L).code("code1").name("name1").seq(1L);
    }

    public static AppObject getAppObjectSample2() {
        return new AppObject().id(2L).code("code2").name("name2").seq(2L);
    }

    public static AppObject getAppObjectRandomSampleGenerator() {
        return new AppObject()
            .id(longCount.incrementAndGet())
            .code(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .seq(longCount.incrementAndGet());
    }
}
