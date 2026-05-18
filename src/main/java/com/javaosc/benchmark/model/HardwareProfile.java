package com.javaosc.benchmark.model;

public record HardwareProfile(
        String osName,
        String osVersion,
        String architecture,
        String javaVersion,
        int availableProcessors,
        long maxMemoryBytes,
        long totalMemoryBytes,
        double systemLoadAverage
) {
    public String displayName() {
        return osName + " " + osVersion + " / " + architecture + " / Java " + javaVersion;
    }
}
