package io.kyligence.kedryrunbenchmark;

import io.kyligence.benchmark.utils.ProgressBarUtil;

//@SpringBootTest
class KeDryrunBenchmarkApplicationTests {

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            System.out.print("\r" + "message" + "  Progress: [" + i);
            Thread.sleep(1000);
        }
    }
}
