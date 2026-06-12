package lab8.client.gui;

import lab8.collectionItems.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class MainFrame extends JFrame {

    private final NetworkManager networkManager;
    private final LocaleManager localeManager;

    private CollectionTableModel tableModel;
    private CollectionTablePanel tablePanel;
    private VisualizationPanel visualizationPanel;
    private JTextArea commandOutputArea;
    private JTextField commandInputField;

    private JLabel userLabel;
    private JComboBox<String> languageCombo;
    private JMenuBar menuBar;

    private final Timer refreshTimer;
    private static final int REFRESH_INTERVAL_MS = 5000;

    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();

    public MainFrame(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.localeManager = LocaleManager.getInstance();

        CollectionTablePanel.NetworkManagerHolder.setNetworkManager(networkManager);

        initComponents();
        applyLocale();
        localeManager.addLocaleChangeListener(locale -> applyLocale());

        // Таймер автоматического обновления коллекции
        refreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> refreshCollection());
        refreshTimer.start();

        // Первоначальная загрузка
        refreshCollection();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                exitApp();
//            }
//        });
    }

    private void initComponents() {
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // верхняя панель (пользователь, язык, кнопки)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // левая часть - информация о пользователе
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userLabel = new JLabel();
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        userPanel.add(userLabel);
        topPanel.add(userPanel, BorderLayout.WEST);

        // центральная часть - язык
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        langPanel.add(new JLabel() {{
            putClientProperty("i18nKey", "main.language");
        }});
        languageCombo = new JComboBox<>(new String[]{
                "Русский", "Türkçe", "Italiano", "Español (Nicaragua)"
        });
        languageCombo.addActionListener(e -> {
            int idx = languageCombo.getSelectedIndex();
            switch (idx) {
                case 0: localeManager.setLocale(LocaleManager.RUSSIAN); break;
                case 1: localeManager.setLocale(LocaleManager.TURKISH); break;
                case 2: localeManager.setLocale(LocaleManager.ITALIAN); break;
                case 3: localeManager.setLocale(LocaleManager.SPANISH_NI); break;
            }
        });
        langPanel.add(languageCombo);
        topPanel.add(langPanel, BorderLayout.CENTER);

        // Правая часть кнопки
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton();
        refreshBtn.putClientProperty("i18nKey", "main.refresh");
        refreshBtn.addActionListener(e -> refreshCollection());
        actionPanel.add(refreshBtn);

        JButton logoutBtn = new JButton();
        logoutBtn.putClientProperty("i18nKey", "main.logout");
        logoutBtn.addActionListener(e -> exitApp());
        actionPanel.add(logoutBtn);
        topPanel.add(actionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Центральная часть (таблица + визуализация)
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Таблица
        tableModel = new CollectionTableModel();
        tablePanel = new CollectionTablePanel(tableModel);
        tablePanel.setMarineActionListener(new CollectionTablePanel.MarineActionListener() {
            @Override
            public void onEdit(SpaceMarine marine) {
                openEditDialog(marine);
            }

            @Override
            public void onDelete(SpaceMarine marine) {
                deleteMarine(marine);
            }
        });
        centerSplit.setTopComponent(new JScrollPane(tablePanel));

        // Визуализация
        visualizationPanel = new VisualizationPanel();
        visualizationPanel.setMarineActionListener(marine -> openEditDialog(marine));
        centerSplit.setBottomComponent(visualizationPanel);
        tablePanel.setTable(visualizationPanel);

        centerSplit.setResizeWeight(0.5);
        centerSplit.setDividerLocation(400);
        add(centerSplit, BorderLayout.CENTER);

        // Нижняя часть (ввод команд)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder(""));
        bottomPanel.putClientProperty("i18nTitleKey", "cmd.title");

        // Поле ввода команды
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        commandInputField = new JTextField();
        commandInputField.addActionListener(e -> executeCommand());
        inputPanel.add(commandInputField, BorderLayout.CENTER);

        JButton executeBtn = new JButton();
        executeBtn.putClientProperty("i18nKey", "cmd.execute");
        executeBtn.addActionListener(e -> executeCommand());
        inputPanel.add(executeBtn, BorderLayout.EAST);
        bottomPanel.add(inputPanel, BorderLayout.NORTH);

        // Область вывода результатов
        commandOutputArea = new JTextArea(6, 50);
        commandOutputArea.setEditable(false);
        commandOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bottomPanel.add(new JScrollPane(commandOutputArea), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Меню
        createMenuBar();
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();

        // Меню "Файл"
        JMenu fileMenu = new JMenu();
        fileMenu.putClientProperty("i18nKey", "menu.file");

        JMenuItem executeScriptItem = new JMenuItem();
        executeScriptItem.putClientProperty("i18nKey", "menu.executeScript");
        executeScriptItem.addActionListener(e -> executeScript());
        fileMenu.add(executeScriptItem);

        JMenuItem exitItem = new JMenuItem();
        exitItem.putClientProperty("i18nKey", "main.logout");
        exitItem.addActionListener(e -> exitApp());
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Меню "Команды"
        JMenu commandsMenu = new JMenu();
        commandsMenu.putClientProperty("i18nKey", "menu.commands");

        JMenuItem addItem = new JMenuItem();
        addItem.putClientProperty("i18nKey", "menu.add");
        addItem.addActionListener(e -> openAddDialog());
        commandsMenu.add(addItem);

        JMenuItem addIfMaxItem = new JMenuItem();
        addIfMaxItem.putClientProperty("i18nKey", "menu.addIfMax");
        addIfMaxItem.addActionListener(e -> openAddDialog("add_if_max"));
        commandsMenu.add(addIfMaxItem);

        JMenuItem addIfMinItem = new JMenuItem();
        addIfMinItem.putClientProperty("i18nKey", "menu.addIfMin");
        addIfMinItem.addActionListener(e -> openAddDialog("add_if_min"));
        commandsMenu.add(addIfMinItem);

        commandsMenu.addSeparator();

        JMenuItem clearItem = new JMenuItem();
        clearItem.putClientProperty("i18nKey", "menu.clear");
        clearItem.addActionListener(e -> executeCommandDirect("clear"));
        commandsMenu.add(clearItem);

        JMenuItem removeGreaterItem = new JMenuItem();
        removeGreaterItem.putClientProperty("i18nKey", "menu.removeGreater");
        removeGreaterItem.addActionListener(e -> openAddDialog("remove_greater"));
        commandsMenu.add(removeGreaterItem);

        commandsMenu.addSeparator();

        JMenuItem countLessItem = new JMenuItem();
        countLessItem.putClientProperty("i18nKey", "menu.countLessThanChapter");
        countLessItem.addActionListener(e -> openChapterInputDialog("count_less_than_chapter"));
        commandsMenu.add(countLessItem);

        JMenuItem filterNameItem = new JMenuItem();
        filterNameItem.putClientProperty("i18nKey", "menu.filterStartsWithName");
        filterNameItem.addActionListener(e -> openFilterNameDialog());
        commandsMenu.add(filterNameItem);

        commandsMenu.addSeparator();

        JMenuItem infoItem = new JMenuItem();
        infoItem.putClientProperty("i18nKey", "menu.info");
        infoItem.addActionListener(e -> executeCommandDirect("info"));
        commandsMenu.add(infoItem);

        menuBar.add(commandsMenu);

        // Меню "Справка"
        JMenu helpMenu = new JMenu();
        helpMenu.putClientProperty("i18nKey", "menu.help");

        JMenuItem aboutItem = new JMenuItem();
        aboutItem.putClientProperty("i18nKey", "menu.about");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void applyLocale() {
        setTitle(localeManager.getString("app.title"));
        userLabel.setText(localeManager.getString("main.user") + ": " + networkManager.getLogin());

        // Обновление всех элементов с i18n ключами
        updateI18nComponents(getContentPane());
        if (menuBar != null) {
            updateMenuBarI18n();
        }

        // Обновление заголовка нижней панели
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JPanel) {
                JPanel jp = (JPanel) c;
                if (jp.getClientProperty("i18nTitleKey") != null) {
                    String key = (String) jp.getClientProperty("i18nTitleKey");
                    if (jp.getBorder() instanceof javax.swing.border.TitledBorder) {
                        ((javax.swing.border.TitledBorder) jp.getBorder()).setTitle(localeManager.getString(key));
                    }
                }
            }
        }

        revalidate();
        repaint();
    }

    private void updateI18nComponents(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                String key = (String) ((JLabel) c).getClientProperty("i18nKey");
                if (key != null) {
                    ((JLabel) c).setText(localeManager.getString(key));
                }
            } else if (c instanceof JButton) {
                String key = (String) ((JButton) c).getClientProperty("i18nKey");
                if (key != null) {
                    ((JButton) c).setText(localeManager.getString(key));
                }
            } else if (c instanceof JMenuItem) {
                String key = (String) ((JMenuItem) c).getClientProperty("i18nKey");
                if (key != null) {
                    ((JMenuItem) c).setText(localeManager.getString(key));
                }
            }
            if (c instanceof Container) {
                updateI18nComponents((Container) c);
            }
        }
    }

    private void updateMenuBarI18n() {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            String key = (String) menu.getClientProperty("i18nKey");
            if (key != null) {
                menu.setText(localeManager.getString(key));
            }
            for (int j = 0; j < menu.getItemCount(); j++) {
                JMenuItem item = menu.getItem(j);
                if (item != null) {
                    String itemKey = (String) item.getClientProperty("i18nKey");
                    if (itemKey != null) {
                        item.setText(localeManager.getString(itemKey));
                    }
                }
            }
        }
    }

    private void refreshCollection() {
        commandExecutor.submit(() -> {
            try {
                List<SpaceMarine> marines = networkManager.requestCollection();
                SwingUtilities.invokeLater(() -> {
                    tableModel.setData(marines);
                    visualizationPanel.setMarines(marines);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> appendOutput(localeManager.getString("error.network") + ": " + e.getMessage()));
            }
        });
    }

    private void executeCommand() {
        String command = commandInputField.getText().trim();
        if (command.isEmpty()) return;
        commandInputField.setText("");

        // Обработка клиентских команд
        if (command.equals("exit")) {
            exitApp();
            return;
        }

        commandExecutor.submit(() -> {
            try {
                // Для команды update нужна проверка прав
                if (command.startsWith("update ")) {
                    String[] parts = command.split(" ");
                    if (parts.length >= 2) {
                        int id = Integer.parseInt(parts[1]);
                        int canUpdate = networkManager.canUpdate(id);
                        if (canUpdate == -1) {
                            SwingUtilities.invokeLater(() ->
                                    appendOutput("ID " + id + " not found"));
                            return;
                        } else if (canUpdate == -2) {
                            SwingUtilities.invokeLater(() ->
                                    appendOutput(localeManager.getString("edit.error.notOwner")));
                            return;
                        }
                    }
                }

                String response = networkManager.sendCommand(command);
                SwingUtilities.invokeLater(() -> {
                    appendOutput(">> " + command);
                    appendOutput(response);
                    refreshCollection();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        appendOutput(localeManager.getString("error.network") + ": " + e.getMessage()));
            }
        });
    }

    private void executeCommandDirect(String command) {
        commandExecutor.submit(() -> {
            try {
                String response = networkManager.sendCommand(command);
                SwingUtilities.invokeLater(() -> {
                    appendOutput(">> " + command);
                    appendOutput(response);
                    refreshCollection();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        appendOutput(localeManager.getString("error.network") + ": " + e.getMessage()));
            }
        });
    }

    private void openAddDialog() {
        openAddDialog("add");
    }

    private void openAddDialog(String command) {
        MarineEditDialog dialog = new MarineEditDialog(this, null, true);
        dialog.setVisible(true);

        if (dialog.isSaved() && dialog.getResultMarine() != null) {
            SpaceMarine marine = dialog.getResultMarine();
            String json = networkManager.getGson().toJson(marine);
            String fullCommand = command + " " + json;
            commandExecutor.submit(() -> {
                try {
                    String response = networkManager.sendCommand(fullCommand);
                    SwingUtilities.invokeLater(() -> {
                        appendOutput(">> " + command);
                        appendOutput(response);
                        refreshCollection();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                            appendOutput(localeManager.getString("error.server") + ": " + e.getMessage()));
                }
            });
        }
    }

    private void openEditDialog(SpaceMarine marine) {
        String currentUser = networkManager.getLogin();
        boolean canEdit = currentUser != null && currentUser.equals(marine.getOwner());

        MarineEditDialog dialog = new MarineEditDialog(this, marine, canEdit);
        dialog.setVisible(true);

        if (dialog.isDeleted()) {
            deleteMarine(marine);
        } else if (dialog.isSaved() && dialog.getResultMarine() != null) {
            SpaceMarine updated = dialog.getResultMarine();
            int canUpdate = networkManager.canUpdate(marine.getID());
            if (canUpdate == -2) {
                JOptionPane.showMessageDialog(this,
                        localeManager.getString("edit.error.notOwner"),
                        localeManager.getString("edit.error.validation"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String json = networkManager.getGson().toJson(updated);
            String command = "update " + marine.getID() + " " + json;
            commandExecutor.submit(() -> {
                try {
                    String response = networkManager.sendCommand(command);
                    SwingUtilities.invokeLater(() -> {
                        appendOutput(">> update " + marine.getID());
                        appendOutput(response);
                        refreshCollection();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                            appendOutput(localeManager.getString("error.server") + ": " + e.getMessage()));
                }
            });
        }
    }

    private void deleteMarine(SpaceMarine marine) {
        String currentUser = networkManager.getLogin();
        if (currentUser == null || !currentUser.equals(marine.getOwner())) {
            JOptionPane.showMessageDialog(this,
                    localeManager.getString("edit.error.notOwner"),
                    localeManager.getString("edit.error.validation"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                localeManager.getString("edit.confirmDelete"),
                localeManager.getString("confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            commandExecutor.submit(() -> {
                try {
                    String response = networkManager.sendCommand("remove_by_id " + marine.getID());
                    SwingUtilities.invokeLater(() -> {
                        appendOutput(">> remove_by_id " + marine.getID());
                        appendOutput(response);
                        refreshCollection();
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                            appendOutput(localeManager.getString("error.server") + ": " + e.getMessage()));
                }
            });
        }
    }

    /**
     * Открывает диалог выбора файла для execute_script.
     */
    private void executeScript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Script files", "txt", "script"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            Scanner sc;
            try {
                sc = new Scanner(new File(filePath));
            } catch (Exception ex) { return; }
            while (sc.hasNextLine()) {
                String command = sc.nextLine();
                //System.out.println("Команда: " + command);
                commandExecutor.submit(() -> {
                    try {
                        String response = networkManager.sendCommand(command);
                        SwingUtilities.invokeLater(() -> {
                            appendOutput(">> execute_script " + filePath);
                            appendOutput(response);
                            refreshCollection();
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() ->
                                appendOutput(localeManager.getString("error.server") + ": " + e.getMessage()));
                    }
                });
            }
        }
    }

    /**
     * Открывает диалог ввода главы для команды count_less_than_chapter.
     */
    private void openChapterInputDialog(String command) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField parentLegionField = new JTextField();
        JTextField marinesCountField = new JTextField();

        panel.add(new JLabel(localeManager.getString("edit.chapterName") + ":"));
        panel.add(nameField);
        panel.add(new JLabel(localeManager.getString("edit.parentLegion") + ":"));
        panel.add(parentLegionField);
        panel.add(new JLabel(localeManager.getString("edit.marinesCount") + ":"));
        panel.add(marinesCountField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                localeManager.getString("menu.countLessThanChapter"),
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String parentLegion = parentLegionField.getText().trim();
                Integer marinesCount = null;
                if (!marinesCountField.getText().trim().isEmpty()) {
                    marinesCount = Integer.parseInt(marinesCountField.getText().trim());
                }
                Chapter chapter = new Chapter(name.isEmpty() ? "default" : name, parentLegion, marinesCount);
                String json = networkManager.getGson().toJson(chapter);
                String fullCommand = command + " " + json;

                commandExecutor.submit(() -> {
                    try {
                        String response = networkManager.sendCommand(fullCommand);
                        SwingUtilities.invokeLater(() -> {
                            appendOutput(">> " + command);
                            appendOutput(response);
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() ->
                                appendOutput(localeManager.getString("error.server") + ": " + e.getMessage()));
                    }
                });
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        localeManager.getString("edit.error.validation") + "\n" + e.getMessage(),
                        localeManager.getString("edit.error.validation"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Открывает диалог ввода префикса имени для filter_starts_with_name.
     */
    private void openFilterNameDialog() {
        String prefix = JOptionPane.showInputDialog(this,
                localeManager.getString("menu.filterStartsWithName") + ":",
                localeManager.getString("menu.filterStartsWithName"),
                JOptionPane.QUESTION_MESSAGE);
        if (prefix != null && !prefix.trim().isEmpty()) {
            commandExecutor.submit(() -> {
                try {
                    String response = networkManager.sendCommand("filter_starts_with_name " + prefix.trim());
                    SwingUtilities.invokeLater(() -> {
                        appendOutput(">> filter_starts_with_name " + prefix.trim());
                        appendOutput(response);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() ->
                            appendOutput(localeManager.getString("error.server") + ": " + e.getMessage()));
                }
            });
        }
    }

    private void showAbout() {
        Locale locale = localeManager.getCurrentLocale();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
        JOptionPane.showMessageDialog(this,
                "SpaceMarine Manager\n" +
                        localeManager.getString("menu.about") + "\n\n" +
                        localeManager.getString("main.user") + ": " + networkManager.getLogin() + "\n" +
                        dateFmt.format(LocalDate.now()),
                localeManager.getString("menu.about"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exitApp() {
        int confirm = JOptionPane.showConfirmDialog(this,
                localeManager.getString("confirm.exit"),
                localeManager.getString("confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            LoginDialog loginDialog = null;
            loginDialog = new LoginDialog(null, networkManager);
            loginDialog.setVisible(true);
            MainFrame mainFrame = null;
            mainFrame = new MainFrame(networkManager);
            mainFrame.setVisible(true);
            //refreshTimer.stop();
            //commandExecutor.shutdownNow();
            //networkManager.disconnect();
            //System.exit(0);
        }
    }

    private void appendOutput(String text) {
        commandOutputArea.append(text + "\n");
        commandOutputArea.setCaretPosition(commandOutputArea.getDocument().getLength());
    }
}
