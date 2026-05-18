package com.javaosc.benchmark.benchmark.tasks;

import com.javaosc.benchmark.benchmark.BenchmarkExecutionContext;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.BenchmarkTaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public final class ConcurrencyBenchmark implements BenchmarkTask {
    @Override
    public String name() {
        return "Concurrent Task Handling";
    }

    @Override
    public String category() {
        return "Concurrency";
    }

    @Override
    public BenchmarkTaskResult execute(BenchmarkExecutionContext context) throws Exception {
        long deadline = System.nanoTime() + context.targetDuration().toNanos();
        long completed = 0;
        while (System.nanoTime() < deadline) {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
                int offset = i;
                tasks.add(() -> countPrimes(4_000 + offset));
            }
            List<Future<Long>> futures = context.executorService().invokeAll(tasks);
            for (Future<Long> future : futures) {
                future.get();
                completed++;
            }
        }
        double seconds = context.targetDuration().toMillis() / 1000.0;
        return new BenchmarkTaskResult(completed / seconds, "tasks/sec", completed + " futures completed");
    }

    private long countPrimes(int limit) {
        long count = 0;
        for (int n = 2; n < limit; n++) {
            boolean prime = true;
            for (int divisor = 2; divisor * divisor <= n; divisor++) {
                if (n % divisor == 0) {
                    prime = false;
                    break;
                }
            }
            if (prime) {
                count++;
            }
        }
        return count;
    }
}
