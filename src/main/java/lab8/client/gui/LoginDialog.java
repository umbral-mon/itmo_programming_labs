package lab8.client.gui;

import lab8.client.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class LoginDialog extends JDialog {

    private final NetworkManager networkManager;
    private final LocaleManager localeManager;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JComboBox<String> languageCombo;

    private boolean succeeded = false;

    public LoginDialog(Frame owner, NetworkManager networkManager) {
        super(owner, true);
        this.networkManager = networkManager;
        this.localeManager = LocaleManager.getInstance();

        initComponents();
        applyLocale();
        localeManager.addLocaleChangeListener(locale -> applyLocale());
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Language selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
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
        mainPanel.add(languageCombo, gbc);

        // логин
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        usernameLabel = new JLabel();
        mainPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // пароль
        gbc.gridx = 0;
        gbc.gridy = 2;
        passwordLabel = new JLabel();
        mainPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginButton = new JButton();
        loginButton.addActionListener(e -> doLogin());
        registerButton = new JButton();
        registerButton.addActionListener(e -> doRegister());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // энтер
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        setContentPane(mainPanel);
        setSize(400, 220);
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void applyLocale() {
        setTitle(localeManager.getString("login.title"));
        usernameLabel.setText(localeManager.getString("login.username") + ":");
        passwordLabel.setText(localeManager.getString("login.password") + ":");
        loginButton.setText(localeManager.getString("login.button"));
        registerButton.setText(localeManager.getString("register.button"));
        revalidate();
        repaint();
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    localeManager.getString("login.error.empty"),
                    localeManager.getString("login.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!networkManager.isConnected()) {
                networkManager.connect();
            }
            password = new String(passwordField.getPassword());
            String response = networkManager.sendCommandWithCredentials(
                    "login " + username + " " + password, username, password);
            System.out.println(response.trim());
            if (isSuccessResponse(response)) {
                networkManager.setCredentials(username, password);
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        localeManager.getString("login.error.auth") + "\n" + (response != null ? response : ""),
                        localeManager.getString("login.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    localeManager.getString("login.error.connection") + "\n" + ex.getMessage(),
                    localeManager.getString("login.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    localeManager.getString("login.error.empty"),
                    localeManager.getString("login.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!networkManager.isConnected()) {
                networkManager.connect();
            }
            password = BCrypt.hashpw(password, BCrypt.gensalt());
            String response = networkManager.sendCommandWithCredentials(
                    "register " + username + " " + password, username, password);
            if (isSuccessResponse(response)) {
                networkManager.setCredentials(username, password);
                succeeded = true;
                doLogin();
                //dispose();
                JOptionPane.showMessageDialog(this,
                        localeManager.getString("login.okreg"),
                        localeManager.getString("login.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        localeManager.getString("login.error.register") + "\n" + (response != null ? response : ""),
                        localeManager.getString("login.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    localeManager.getString("login.error.connection") + "\n" + ex.getMessage(),
                    localeManager.getString("login.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private boolean isSuccessResponse(String response) {
        if (response == null || response.trim().isEmpty()) return false;
        String lower = response.toLowerCase().trim();
        return lower.equals("ok");
    }
}
