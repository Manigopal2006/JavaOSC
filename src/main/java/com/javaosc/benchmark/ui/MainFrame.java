package com.javaosc.benchmark.ui;

import com.javaosc.benchmark.benchmark.BenchmarkListener;
import com.javaosc.benchmark.model.BenchmarkResult;
import com.javaosc.benchmark.model.BenchmarkRun;
import com.javaosc.benchmark.model.BenchmarkSample;
import com.javaosc.benchmark.model.HardwareProfile;
import com.javaosc.benchmark.model.RegressionReport;
import com.javaosc.benchmark.service.ApplicationContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class MainFrame extends JFrame {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private final ApplicationContext context;
    private final JButton runButton = new JButton("Run Benchmarks");
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel hardwareLabel = new JLabel();
    private final DefaultTableModel resultModel = tableModel("Benchmark", "Category", "Score", "Duration", "Memory", "CPU", "Status");
    private final DefaultTableModel historyModel = tableModel("Time", "Run", "Benchmark", "Score", "Duration", "Status");
    private final DefaultTableModel regressionModel = tableModel("Benchmark", "Current", "Historical Avg", "Change", "Status");
    private final JTextArea logArea = new JTextArea();
    private final MetricsGraphPanel graphPanel = new MetricsGraphPanel();

    public MainFrame(ApplicationContext context) {
        super("Benchmarking and Performance Analysis Tool");
        this.context = context;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationByPlatform(true);
        configureLook();
        setContentPane(createContent());
        loadHardwareProfile();
        loadHistory();
        pack();
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.add(createTopBar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Live Run", createLivePanel());
        tabs.addTab("History", tablePanel(new JTable(historyModel)));
        tabs.addTab("Regression", tablePanel(new JTable(regressionModel)));
        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        hardwareLabel.setFont(hardwareLabel.getFont().deriveFont(Font.PLAIN, 12f));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        runButton.addActionListener(event -> runBenchmarks());
        actions.add(statusLabel);
        actions.add(runButton);
        top.add(new JLabel("Cross-platform benchmark dashboard"), BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        top.add(hardwareLabel, BorderLayout.SOUTH);
        return top;
    }

    private JSplitPane createLivePanel() {
        JTable resultTable = new JTable(resultModel);
        resultTable.setRowHeight(26);
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(graphPanel, BorderLayout.CENTER);
        left.add(tablePanel(resultTable), BorderLayout.SOUTH);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logPane = new JScrollPane(logArea);
        logPane.setPreferredSize(new Dimension(360, 300));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, logPane);
        split.setResizeWeight(0.72);
        split.setBorder(BorderFactory.createEmptyBorder());
        return split;
    }

    private JScrollPane tablePanel(JTable table) {
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(214, 219, 226)));
        return scrollPane;
    }

    private void runBenchmarks() {
        runButton.setEnabled(false);
        statusLabel.setText("Running...");
        resultModel.setRowCount(0);
        regressionModel.setRowCount(0);
        graphPanel.clear();
        logArea.setText("");

        SwingWorker<BenchmarkRun, Runnable> worker = new SwingWorker<>() {
            @Override
            protected BenchmarkRun doInBackground() {
                return context.benchmarkRunner().runAll(new BenchmarkListener() {
                    @Override
                    public void onLog(String message) {
                        publish(() -> appendLog(message));
                    }

                    @Override
                    public void onSample(BenchmarkSample sample) {
                        publish(() -> graphPanel.addSample(sample));
                    }

                    @Override
                    public void onResult(BenchmarkResult result) {
                        publish(() -> addResultRow(result));
                    }
                });
            }

            @Override
            protected void process(List<Runnable> chunks) {
                chunks.forEach(Runnable::run);
            }

            @Override
            protected void done() {
                try {
                    BenchmarkRun run = get();
                    renderRegression(context.analysisService().analyze(run.results()));
                    loadHistory();
                    statusLabel.setText("Completed " + run.results().size() + " benchmarks");
                } catch (Exception ex) {
                    appendLog("Run failed: " + ex.getMessage());
                    statusLabel.setText("Failed");
                } finally {
                    runButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void addResultRow(BenchmarkResult result) {
        resultModel.addRow(new Object[]{
                result.benchmarkName(),
                result.category(),
                result.scoreLabel(),
                result.durationMillis() + " ms",
                humanBytes(result.memoryDeltaBytes()),
                String.format(Locale.US, "%.1f%%", result.cpuLoad() * 100),
                result.success() ? "OK" : "Failed"
        });
    }

    private void renderRegression(List<RegressionReport> reports) {
        regressionModel.setRowCount(0);
        for (RegressionReport report : reports) {
            regressionModel.addRow(new Object[]{
                    report.benchmarkName(),
                    String.format(Locale.US, "%.2f", report.currentScore()),
                    String.format(Locale.US, "%.2f", report.historicalAverage()),
                    String.format(Locale.US, "%+.1f%%", report.percentChange()),
                    report.status()
            });
        }
    }

    private void loadHardwareProfile() {
        HardwareProfile profile = context.hardwareProfileService().capture();
        hardwareLabel.setText(profile.displayName() + " | CPU cores: " + profile.availableProcessors() + " | Max JVM memory: " + humanBytes(profile.maxMemoryBytes()));
    }

    private void loadHistory() {
        SwingUtilities.invokeLater(() -> {
            historyModel.setRowCount(0);
            for (BenchmarkResult result : context.analysisService().history()) {
                historyModel.addRow(new Object[]{
                        TIME_FORMAT.format(result.startedAt()),
                        result.runId().substring(0, 8),
                        result.benchmarkName(),
                        result.scoreLabel(),
                        result.durationMillis() + " ms",
                        result.success() ? "OK" : "Failed"
                });
            }
        });
    }

    private void appendLog(String message) {
        logArea.append("[" + TIME_FORMAT.format(java.time.Instant.now()) + "] " + message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private String humanBytes(long bytes) {
        double abs = Math.abs((double) bytes);
        String sign = bytes < 0 ? "-" : "";
        if (abs < 1024) {
            return bytes + " B";
        }
        if (abs < 1024 * 1024) {
            return sign + String.format(Locale.US, "%.1f KiB", abs / 1024.0);
        }
        return sign + String.format(Locale.US, "%.1f MiB", abs / 1024.0 / 1024.0);
    }

    private void configureLook() {
        getContentPane().setBackground(new Color(248, 249, 251));
        runButton.setFocusPainted(false);
    }
}
