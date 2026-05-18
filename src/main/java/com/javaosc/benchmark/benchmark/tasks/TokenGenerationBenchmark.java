package com.javaosc.benchmark.benchmark.tasks;

import com.javaosc.benchmark.benchmark.BenchmarkExecutionContext;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.BenchmarkTaskResult;

import java.security.MessageDigest;
import java.util.HexFormat;

public final class TokenGenerationBenchmark implements BenchmarkTask {
    @Override
    public String name() {
        return "Token Generation";
    }

    @Override
    public String category() {
        return "CPU";
    }

    @Override
    public BenchmarkTaskResult execute(BenchmarkExecutionContext context) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        long deadline = System.nanoTime() + context.targetDuration().toNanos();
        long tokens = 0;
        String lastToken = "";
        while (System.nanoTime() < deadline) {
            byte[] input = ("token:" + tokens + ":" + context.random().nextLong()).getBytes();
            lastToken = HexFormat.of().formatHex(digest.digest(input));
            tokens++;
        }
        double seconds = context.targetDuration().toMillis() / 1000.0;
        return new BenchmarkTaskResult(tokens / seconds, "tokens/sec", "Last token " + lastToken.substring(0, 12));
    }
}
