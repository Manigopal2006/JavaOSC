package com.javaosc.benchmark.service;

import com.javaosc.benchmark.model.BenchmarkSample;

import java.lang.management.ManagementFactory;
import java.time.Instant;

public final class SystemMetricsMonitor {
    private final com.sun.management.OperatingSystemMXBean osBean;

    public SystemMetricsMonitor() {
        this.osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public BenchmarkSample sample(String activeBenchmark) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        int liveThreads = ManagementFactory.getThreadMXBean().getThreadCount();
        return new BenchmarkSample(Instant.now(), currentCpuLoad(), usedMemory, liveThreads, activeBenchmark);
    }

    public double currentCpuLoad() {
        double load = osBean.getCpuLoad();
        if (load < 0) {
            double average = osBean.getSystemLoadAverage();
            return average < 0 ? 0 : Math.min(1, average / osBean.getAvailableProcessors());
        }
        return load;
    }
}
