package io.kyligence.kedryrunbenchmark;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;
import io.kyligence.benchmark.utils.ProgressBarUtil;
import lombok.val;

import java.util.Random;

//@SpringBootTest
class KeDryrunBenchmarkApplicationTests {

    public static void main(String[] args) throws InterruptedException {
        val metricRegistry = new MetricRegistry();
        Histogram histogram = metricRegistry.histogram("test");
//        Histogram histogram = new Histogram(new SlidingWindowReservoir(1000000));
        double total = 0;
        int totali = 1000000;

        for (int i = 0; i < totali; i++) {
            int value = new Random().nextInt(5000);
            total += value;
            histogram.update(value);
        }

        System.out.println(histogram.getSnapshot().getMean()+"     "+ total/totali);
    }
}
