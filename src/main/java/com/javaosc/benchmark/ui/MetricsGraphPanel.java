package com.javaosc.benchmark.ui;

import com.javaosc.benchmark.model.BenchmarkSample;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MetricsGraphPanel extends JPanel {
    private static final int MAX_SAMPLES = 160;
    private final List<BenchmarkSample> samples = new ArrayList<>();

    public MetricsGraphPanel() {
        setPreferredSize(new Dimension(720, 390));
        setBackground(Color.WHITE);
    }

    public void addSample(BenchmarkSample sample) {
        if (samples.size() >= MAX_SAMPLES) {
            samples.remove(0);
        }
        samples.add(sample);
        repaint();
    }

    public void clear() {
        samples.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int left = 54;
        int right = getWidth() - 24;
        int top = 28;
        int bottom = getHeight() - 46;

        g.setColor(new Color(235, 238, 243));
        for (int i = 0; i <= 4; i++) {
            int y = top + (bottom - top) * i / 4;
            g.drawLine(left, y, right, y);
        }
        g.setColor(new Color(53, 61, 75));
        g.drawString("CPU load and memory usage", left, 18);

        if (samples.isEmpty()) {
            drawCentered(g, "Run benchmarks to see live system metrics", getWidth(), getHeight());
            g.dispose();
            return;
        }

        drawCpuLine(g, left, right, top, bottom);
        drawMemoryLine(g, left, right, top, bottom);
        drawLegend(g, left, bottom + 28);
        BenchmarkSample latest = samples.get(samples.size() - 1);
        g.setColor(new Color(75, 85, 99));
        g.drawString("Active: " + latest.activeBenchmark() + " | Threads: " + latest.liveThreads(), left + 230, bottom + 28);
        g.dispose();
    }

    private void drawCpuLine(Graphics2D g, int left, int right, int top, int bottom) {
        g.setColor(new Color(33, 118, 174));
        g.setStroke(new BasicStroke(2.4f));
        drawLine(g, left, right, top, bottom, sample -> sample.cpuLoad());
    }

    private void drawMemoryLine(Graphics2D g, int left, int right, int top, int bottom) {
        long maxMemory = samples.stream().mapToLong(BenchmarkSample::usedMemoryBytes).max().orElse(1);
        g.setColor(new Color(46, 139, 87));
        g.setStroke(new BasicStroke(2.4f));
        drawLine(g, left, right, top, bottom, sample -> sample.usedMemoryBytes() / (double) maxMemory);
    }

    private void drawLine(Graphics2D g, int left, int right, int top, int bottom, ValueExtractor extractor) {
        int previousX = -1;
        int previousY = -1;
        for (int i = 0; i < samples.size(); i++) {
            double value = Math.max(0, Math.min(1, extractor.value(samples.get(i))));
            int x = samples.size() == 1 ? left : left + (right - left) * i / (samples.size() - 1);
            int y = bottom - (int) ((bottom - top) * value);
            if (previousX >= 0) {
                g.drawLine(previousX, previousY, x, y);
            }
            previousX = x;
            previousY = y;
        }
    }

    private void drawLegend(Graphics2D g, int x, int y) {
        BenchmarkSample latest = samples.get(samples.size() - 1);
        g.setColor(new Color(33, 118, 174));
        g.fillRect(x, y - 10, 12, 12);
        g.setColor(new Color(53, 61, 75));
        g.drawString(String.format(Locale.US, "CPU %.1f%%", latest.cpuLoad() * 100), x + 18, y);
        g.setColor(new Color(46, 139, 87));
        g.fillRect(x + 110, y - 10, 12, 12);
        g.setColor(new Color(53, 61, 75));
        g.drawString(String.format(Locale.US, "Memory %.1f MiB", latest.usedMemoryBytes() / 1024.0 / 1024.0), x + 128, y);
    }

    private void drawCentered(Graphics2D g, String text, int width, int height) {
        g.setColor(new Color(107, 114, 128));
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (width - metrics.stringWidth(text)) / 2, height / 2);
    }

    private interface ValueExtractor {
        double value(BenchmarkSample sample);
    }
}
