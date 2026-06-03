package lab8.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Менеджер локализации. Обеспечивает переключение языков без перезапуска приложения.
 * Поддерживает русский, турецкий, итальянский и испанский (Никарагуа) языки.
 * Корректное отображение чисел, даты и времени обеспечивается через getCurrentLocale().
 */
public class LocaleManager {

    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale TURKISH = new Locale("tr");
    public static final Locale ITALIAN = new Locale("it");
    public static final Locale SPANISH_NI = new Locale("es", "NI");

    private static LocaleManager instance;

    private Locale currentLocale;
    private ResourceBundle bundle;
    private final List<LocaleChangeListener> listeners = new ArrayList<>();

    private LocaleManager() {
        setLocale(RUSSIAN);
    }

    public static synchronized LocaleManager getInstance() {
        if (instance == null) {
            instance = new LocaleManager();
        }
        return instance;
    }

    /**
     * Устанавливает текущую локаль и загружает соответствующий ResourceBundle.
     * Уведомляет всех слушателей об изменении локали.
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        try {
            this.bundle = ResourceBundle.getBundle("messages", locale);
        } catch (MissingResourceException e) {
            this.bundle = ResourceBundle.getBundle("messages", RUSSIAN);
        }
        fireLocaleChanged();
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Получает локализованную строку по ключу.
     */
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Получает локализованную строку с подстановкой аргументов.
     */
    public String getString(String key, Object... args) {
        String pattern = getString(key);
        return String.format(currentLocale, pattern, args);
    }

    public void addLocaleChangeListener(LocaleChangeListener listener) {
        listeners.add(listener);
    }

    public void removeLocaleChangeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireLocaleChanged() {
        for (LocaleChangeListener listener : listeners) {
            listener.onLocaleChanged(currentLocale);
        }
    }

    /**
     * Интерфейс слушателя изменений локали.
     */
    public interface LocaleChangeListener {
        void onLocaleChanged(Locale newLocale);
    }
}
