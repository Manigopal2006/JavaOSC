package com.javaosc.benchmark.service;

import com.javaosc.benchmark.model.HardwareProfile;

import java.lang.management.ManagementFactory;

public final class HardwareProfileService {
    public HardwareProfile capture() {
        Runtime runtime = Runtime.getRuntime();
        java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        return new HardwareProfile(
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                System.getProperty("java.version"),
                runtime.availableProcessors(),
                runtime.maxMemory(),
                runtime.totalMemory(),
                os.getSystemLoadAverage());
    }
}
