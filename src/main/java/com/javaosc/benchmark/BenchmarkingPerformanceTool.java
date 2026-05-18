package com.javaosc.benchmark;

import com.javaosc.benchmark.benchmark.BenchmarkListener;
import com.javaosc.benchmark.model.BenchmarkResult;
import com.javaosc.benchmark.model.BenchmarkSample;
import com.javaosc.benchmark.service.ApplicationContext;
import com.javaosc.benchmark.ui.MainFrame;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

public final class BenchmarkingPerformanceTool {
    private BenchmarkingPerformanceTool() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--headless".equals(args[0])) {
            runHeadless();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            ApplicationContext context = ApplicationContext.create(Path.of("data", "benchmark-history.tsv"));
            MainFrame frame = new MainFrame(context);
            frame.setVisible(true);
        });
    }

    private static void runHeadless() {
        ApplicationContext context = ApplicationContext.create(Path.of("data", "benchmark-history.tsv"));
        context.benchmarkRunner().runAll(new BenchmarkListener() {
            @Override
            public void onLog(String message) {
                System.out.println(message);
            }

            @Override
            public void onSample(BenchmarkSample sample) {
                // Console mode keeps samples silent so benchmark output stays readable.
            }

            @Override
            public void onResult(BenchmarkResult result) {
                System.out.println(result.benchmarkName() + ": " + result.scoreLabel() + " (" + result.durationMillis() + " ms)");
            }
        });
    }
}
