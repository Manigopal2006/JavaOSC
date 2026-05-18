package com.javaosc.benchmark.benchmark.tasks;

import com.javaosc.benchmark.benchmark.BenchmarkExecutionContext;
import com.javaosc.benchmark.benchmark.BenchmarkTask;
import com.javaosc.benchmark.benchmark.BenchmarkTaskResult;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public final class CompressionBenchmark implements BenchmarkTask {
    @Override
    public String name() {
        return "Compiled Format Compression";
    }

    @Override
    public String category() {
        return "CPU";
    }

    @Override
    public BenchmarkTaskResult execute(BenchmarkExecutionContext context) throws Exception {
        byte[] bytecodeLikePayload = createBytecodeLikePayload();
        long deadline = System.nanoTime() + context.targetDuration().toNanos();
        long bytesProcessed = 0;
        int iterations = 0;
        while (System.nanoTime() < deadline) {
            compress(bytecodeLikePayload);
            bytesProcessed += bytecodeLikePayload.length;
            iterations++;
        }
        double mib = bytesProcessed / 1024.0 / 1024.0;
        double seconds = context.targetDuration().toMillis() / 1000.0;
        return new BenchmarkTaskResult(mib / seconds, "MiB/sec", iterations + " compression passes");
    }

    private byte[] createBytecodeLikePayload() {
        StringBuilder builder = new StringBuilder(256_000);
        for (int i = 0; i < 12_000; i++) {
            builder.append("CAFEBABE method_").append(i)
                    .append(" aload_0 invokestatic ldc invokevirtual ifnonnull goto line ")
                    .append(i % 256).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void compress(byte[] input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length / 2);
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(input);
        }
    }
}
