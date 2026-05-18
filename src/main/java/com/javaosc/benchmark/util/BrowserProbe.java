package com.javaosc.benchmark.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class BrowserProbe {
    private BrowserProbe() {
    }

    public static Optional<BrowserCommand> detectBrowser() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return detectMacBrowser();
        }
        return detectLinuxBrowser();
    }

    private static Optional<BrowserCommand> detectMacBrowser() {
        List<Path> candidates = List.of(
                Path.of("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"),
                Path.of("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge"),
                Path.of("/Applications/Firefox.app/Contents/MacOS/firefox"),
                Path.of("/Applications/Safari.app/Contents/MacOS/Safari")
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return Optional.of(new BrowserCommand(candidate.getFileName().toString(), List.of(candidate.toString(), "--version")));
            }
        }
        return Optional.empty();
    }

    private static Optional<BrowserCommand> detectLinuxBrowser() {
        List<String> candidates = List.of("google-chrome", "chromium", "chromium-browser", "firefox", "microsoft-edge");
        for (String candidate : candidates) {
            if (isOnPath(candidate)) {
                return Optional.of(new BrowserCommand(candidate, List.of(candidate, "--version")));
            }
        }
        return Optional.empty();
    }

    private static boolean isOnPath(String command) {
        String path = System.getenv("PATH");
        if (path == null) {
            return false;
        }
        for (String segment : path.split(java.io.File.pathSeparator)) {
            if (Files.isExecutable(Path.of(segment, command))) {
                return true;
            }
        }
        return false;
    }

    public record BrowserCommand(String name, List<String> command) {
    }
}
