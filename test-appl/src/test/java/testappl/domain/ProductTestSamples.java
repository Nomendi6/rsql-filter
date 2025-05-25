package testappl.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProductTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Product getProductSample1() {
        return new Product().withId(1L).withCode("code1").withName("name1").withSeq(1L);
    }

    public static Product getProductSample2() {
        return new Product().withId(2L).withCode("code2").withName("name2").withSeq(2L);
    }

    public static Product getProductRandomSampleGenerator() {
        return new Product()
            .withId(longCount.incrementAndGet())
            .withCode(UUID.randomUUID().toString())
            .withName(UUID.randomUUID().toString())
            .withSeq(longCount.incrementAndGet());
    }
}
