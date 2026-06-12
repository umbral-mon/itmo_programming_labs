package lab8.client;

import lab8.client.gui.LoginDialog;
import lab8.client.gui.MainFrame;
import lab8.client.gui.NetworkManager;

import javax.swing.*;
import java.awt.*;

/**
 * Главный класс клиентского приложения.
 * Запускает графический интерфейс на основе Swing.
 * Сначала отображается окно авторизации/регистрации,
 * затем — главное окно приложения.
 */
public class ClientMain implements Runnable {

    public void run(){
        // Устанавливаем системный look-and-feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Используем стандартный
        }

        // Запуск GUI в Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            NetworkManager networkManager = new NetworkManager();

            try {
                networkManager.connect();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Cannot connect to server: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Показываем диалог авторизации
            MainFrame mainFrame = null;
            //while (true) {
                LoginDialog loginDialog = null;
                    loginDialog = new LoginDialog(null, networkManager);
                    loginDialog.setVisible(true);


                if (loginDialog != null &&  !loginDialog.isSucceeded()) {
                    System.exit(0);
                }

                // Открываем главное окно
                    mainFrame = new MainFrame(networkManager);
                    mainFrame.setVisible(true);



            //}
        });
    }
}
