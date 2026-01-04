import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SystemMonitorGUI {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());


    private JFrame mainFrame;
    private JTabbedPane tabbedPane;
    private JTextArea systemInfoArea;
    private JTextArea memoryInfoArea;
    private JTable processesTable;
    private JTextArea diskInfoArea;

    public SystemMonitorGUI() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {

        mainFrame = new JFrame("Системный монитор");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null); // Центрируем окно

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Общее", createGeneralTab());
        tabbedPane.addTab("Память", createMemoryTab());
        tabbedPane.addTab("Процессы", createProcessesTab());
        tabbedPane.addTab("Диски", createDiskTab());
        tabbedPane.addTab("Полный отчет", createFullReportTab());
        JButton refreshButton = new JButton("Обновить все");
        refreshButton.addActionListener(e -> refreshAllTabs());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(tabbedPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        refreshAllTabs();

        mainFrame.setVisible(true);
    }


    private JPanel createGeneralTab() {
        JPanel panel = new JPanel(new BorderLayout());

        systemInfoArea = new JTextArea();
        systemInfoArea.setEditable(false);
        systemInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(systemInfoArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> updateSystemInfo());
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMemoryTab() {
        JPanel panel = new JPanel(new BorderLayout());

        memoryInfoArea = new JTextArea();
        memoryInfoArea.setEditable(false);
        memoryInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(memoryInfoArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> updateMemoryInfo());
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProcessesTab() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"PID", "Имя", "Владелец", "Время запуска"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        processesTable = new JTable(tableModel);
        processesTable.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(processesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> updateProcessesInfo());
        JButton killButton = new JButton("Завершить процесс");
        killButton.addActionListener(e -> killSelectedProcess());
        buttonPanel.add(refreshButton);
        buttonPanel.add(killButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }


    private JPanel createDiskTab() {
        JPanel panel = new JPanel(new BorderLayout());

        diskInfoArea = new JTextArea();
        diskInfoArea.setEditable(false);
        diskInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(diskInfoArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> updateDiskInfo());
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createFullReportTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea fullReportArea = new JTextArea();
        fullReportArea.setEditable(false);
        fullReportArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(fullReportArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Создать отчет");
        refreshButton.addActionListener(e -> {
            String report = getFullReport();
            fullReportArea.setText(report);
        });
        JButton saveButton = new JButton("Сохранить в файл");
        saveButton.addActionListener(e -> saveReportToFile(fullReportArea.getText()));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(saveButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateSystemInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ИНФОРМАЦИЯ О СИСТЕМЕ ===\n\n");
        sb.append(String.format("ОС: %s\n", System.getProperty("os.name")));
        sb.append(String.format("Версия: %s\n", System.getProperty("os.version")));
        sb.append(String.format("Архитектура: %s\n", System.getProperty("os.arch")));
        sb.append(String.format("Имя пользователя: %s\n", System.getProperty("user.name")));
        sb.append(String.format("Домашняя папка: %s\n", System.getProperty("user.home")));
        sb.append(String.format("Временная папка: %s\n", System.getProperty("java.io.tmpdir")));
        sb.append(String.format("Версия Java: %s\n", System.getProperty("java.version")));
        sb.append(String.format("Вендор Java: %s\n", System.getProperty("java.vendor")));

        systemInfoArea.setText(sb.toString());
    }

    private void updateMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        StringBuilder sb = new StringBuilder();
        sb.append("=== ИНФОРМАЦИЯ О ПАМЯТИ ===\n\n");
        sb.append(String.format("Используется JVM: %,.2f MB\n", usedMemory / 1024.0 / 1024.0));
        sb.append(String.format("Доступно JVM: %,.2f MB\n", freeMemory / 1024.0 / 1024.0));
        sb.append(String.format("Всего для JVM: %,.2f MB\n", totalMemory / 1024.0 / 1024.0));
        sb.append(String.format("Макс. для JVM: %,.2f MB\n",
                maxMemory == Long.MAX_VALUE ? Double.POSITIVE_INFINITY : maxMemory / 1024.0 / 1024.0));
        sb.append(String.format("Использование JVM: %.1f%%\n",
                ((double) usedMemory / totalMemory) * 100));


        try {
            Process process = Runtime.getRuntime().exec("wmic OS get TotalVisibleMemorySize,FreePhysicalMemory");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            long totalPhysical = 0;
            long freePhysical = 0;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                if (lineCount == 1) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        totalPhysical = Long.parseLong(parts[0]);
                        freePhysical = Long.parseLong(parts[1]);
                    }
                }
                lineCount++;
            }

            if (totalPhysical > 0) {
                long usedPhysical = totalPhysical - freePhysical;
                sb.append("\n=== ФИЗИЧЕСКАЯ ПАМЯТЬ ===\n");
                sb.append(String.format("Всего памяти: %,.2f GB\n", totalPhysical / 1024.0 / 1024.0));
                sb.append(String.format("Свободно памяти: %,.2f GB\n", freePhysical / 1024.0 / 1024.0));
                sb.append(String.format("Использовано памяти: %,.2f GB\n", usedPhysical / 1024.0 / 1024.0));
                sb.append(String.format("Использование памяти: %.1f%%\n",
                        ((double) usedPhysical / totalPhysical) * 100));
            }

            reader.close();
        } catch (Exception e) {
            sb.append("\nДополнительная информация о памяти недоступна\n");
        }

        memoryInfoArea.setText(sb.toString());
    }

    private void updateProcessesInfo() {
        DefaultTableModel model = (DefaultTableModel) processesTable.getModel();
        model.setRowCount(0);
        try {
            List<ProcessHandle> processes = ProcessHandle.allProcesses()
                    .filter(p -> p.info().command().isPresent())
                    .sorted(Comparator.comparing(ProcessHandle::pid))
                    .limit(50)
                    .collect(Collectors.toList());

            for (ProcessHandle process : processes) {
                String pid = String.valueOf(process.pid());
                String name = getProcessName(process);
                String user = process.info().user().orElse("Неизвестно");
                String uptime = process.info().startInstant()
                        .map(instant -> TIME_FORMATTER.format(instant))
                        .orElse("Неизвестно");

                model.addRow(new Object[]{pid, name, user, uptime});
            }

            JOptionPane.showMessageDialog(mainFrame,
                    String.format("Загружено %d процессов", processes.size()),
                    "Информация", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Не удалось получить информацию о процессах: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDiskInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ИНФОРМАЦИЯ О ДИСКАХ ===\n\n");

        java.io.File[] roots = java.io.File.listRoots();

        if (roots == null || roots.length == 0) {
            sb.append("Диски не найдены\n");
        } else {
            sb.append(String.format("%-10s %12s %12s %12s %12s\n",
                    "Диск", "Всего(ГБ)", "Свободно(ГБ)", "Использовано(ГБ)", "Использовано%"));
            sb.append("-".repeat(70)).append("\n");

            for (java.io.File root : roots) {
                long total = root.getTotalSpace();
                long free = root.getFreeSpace();
                long used = total - free;
                double usedPercent = total > 0 ? ((double) used / total) * 100 : 0;

                sb.append(String.format("%-10s %12.1f %12.1f %12.1f %12.1f\n",
                        root.getPath(),
                        total / (1024.0 * 1024.0 * 1024.0),
                        free / (1024.0 * 1024.0 * 1024.0),
                        used / (1024.0 * 1024.0 * 1024.0),
                        usedPercent));
            }
        }

        diskInfoArea.setText(sb.toString());
    }

    private String getFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("ПОЛНЫЙ ОТЧЕТ О СИСТЕМЕ\n");
        sb.append("=".repeat(60)).append("\n\n");


        sb.append("1. СИСТЕМНАЯ ИНФОРМАЦИЯ:\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("ОС: %s\n", System.getProperty("os.name")));
        sb.append(String.format("Версия ОС: %s\n", System.getProperty("os.version")));
        sb.append(String.format("Архитектура: %s\n", System.getProperty("os.arch")));
        sb.append(String.format("Пользователь: %s\n", System.getProperty("user.name")));
        sb.append(String.format("Время: %s\n", java.time.LocalDateTime.now())).append("\n");


        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        sb.append("2. ПАМЯТЬ JVM:\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("Используется: %,.2f MB\n", usedMemory / 1024.0 / 1024.0));
        sb.append(String.format("Свободно: %,.2f MB\n", freeMemory / 1024.0 / 1024.0));
        sb.append(String.format("Всего: %,.2f MB\n", totalMemory / 1024.0 / 1024.0));
        sb.append(String.format("Использование: %.1f%%\n",
                ((double) usedMemory / totalMemory) * 100)).append("\n");


        sb.append("3. ПРОЦЕССОР:\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("Доступных ядер: %d\n", Runtime.getRuntime().availableProcessors())).append("\n");


        sb.append("4. ДИСКИ:\n");
        sb.append("-".repeat(40)).append("\n");
        java.io.File[] roots = java.io.File.listRoots();
        if (roots != null) {
            for (java.io.File root : roots) {
                long total = root.getTotalSpace();
                long free = root.getFreeSpace();
                long used = total - free;
                double usedPercent = total > 0 ? ((double) used / total) * 100 : 0;

                sb.append(String.format("%s: Всего=%.1fГБ, Свободно=%.1fГБ, Использовано=%.1fГБ (%.1f%%)\n",
                        root.getPath(),
                        total / (1024.0 * 1024.0 * 1024.0),
                        free / (1024.0 * 1024.0 * 1024.0),
                        used / (1024.0 * 1024.0 * 1024.0),
                        usedPercent));
            }
        }

        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("Отчет сгенерирован: ").append(java.time.LocalDateTime.now()).append("\n");

        return sb.toString();
    }

    private void killSelectedProcess() {
        int selectedRow = processesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Выберите процесс из таблицы",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pid = (String) processesTable.getValueAt(selectedRow, 0);
        String name = (String) processesTable.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                String.format("Вы уверены, что хотите завершить процесс?\nPID: %s\nИмя: %s", pid, name),
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Для Windows
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");
                } else {
                    // Для Linux/Mac
                    Runtime.getRuntime().exec("kill -9 " + pid);
                }

                JOptionPane.showMessageDialog(mainFrame,
                        "Процесс завершен",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);


                updateProcessesInfo();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Ошибка при завершении процесса: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveReportToFile(String report) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить отчет");
        fileChooser.setSelectedFile(new java.io.File("system_report.txt"));

        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(report);
                writer.close();

                JOptionPane.showMessageDialog(mainFrame,
                        "Отчет сохранен в: " + file.getAbsolutePath(),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Ошибка при сохранении: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshAllTabs() {
        updateSystemInfo();
        updateMemoryInfo();
        updateProcessesInfo();
        updateDiskInfo();
    }

    private String getProcessName(ProcessHandle process) {
        String command = process.info().command().orElse("");
        if (command.contains("/")) {
            return command.substring(command.lastIndexOf('/') + 1);
        } else if (command.contains("\\")) {
            return command.substring(command.lastIndexOf('\\') + 1);
        }
        return command.isEmpty() ? "N/A" : command;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new SystemMonitorGUI();
        });
    }
}