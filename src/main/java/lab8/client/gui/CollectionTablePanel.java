package lab8.client.gui;

import lab8.collectionItems.SpaceMarine;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;

/**
 * Панель с таблицей коллекции SpaceMarine.
 * Поддерживает фильтрацию по каждой колонке и сортировку по клику на заголовок.
 * Фильтрация и сортировка реализованы с использованием Streams API.
 */
public class CollectionTablePanel extends JPanel {

    private final CollectionTableModel tableModel;
    private JTable table;
    private JPanel filterPanel;
    private JTextField[] filterFields;

    private MarineActionListener actionListener;

    public CollectionTablePanel(CollectionTableModel model) {
        this.tableModel = model;
        initComponents();
        applyLocale();
        LocaleManager.getInstance().addLocaleChangeListener(locale -> applyLocale());
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Фильтр панель
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        int colCount = tableModel.getColumnCount();
        filterFields = new JTextField[colCount];
        for (int i = 0; i < colCount; i++) {
            JTextField field = new JTextField(6);
            final int colIdx = i;
            field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    tableModel.setFilter(colIdx, field.getText());
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    tableModel.setFilter(colIdx, field.getText());
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    tableModel.setFilter(colIdx, field.getText());
                }
            });
            filterFields[i] = field;
            filterPanel.add(field);
        }

        JButton clearFilterBtn = new JButton();
        clearFilterBtn.addActionListener(e -> {
            for (JTextField f : filterFields) f.setText("");
            tableModel.clearFilters();
        });
        filterPanel.add(clearFilterBtn);
        filterPanel.putClientProperty("clearBtn", clearFilterBtn);

        add(filterPanel, BorderLayout.NORTH);

        // Таблица
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        // Сортировка по клику на заголовок
        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col >= 0) {
                    tableModel.setSort(col);
                }
            }
        });

        // Контекстное меню и двойной клик для редактирования
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem();
        JMenuItem deleteItem = new JMenuItem();
        editItem.addActionListener(e -> editSelectedMarine());
        deleteItem.addActionListener(e -> deleteSelectedMarine());
        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        table.setComponentPopupMenu(popupMenu);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedMarine();
                }
            }
        });

        // Подсветка объектов текущего пользователя
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    SpaceMarine marine = tableModel.getMarineAt(row);
                    if (marine != null && marine.getOwner() != null) {
                        String currentUser = NetworkManagerHolder.getNetworkManager().getLogin();
                        if (currentUser != null && currentUser.equals(marine.getOwner())) {
                            c.setBackground(new Color(230, 255, 230));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    }
                }
                return c;
            }
        });

        // Сохраняем ссылки для локализации
        putClientProperty("editItem", editItem);
        putClientProperty("deleteItem", deleteItem);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void applyLocale() {
        LocaleManager lm = LocaleManager.getInstance();
        JButton clearBtn = (JButton) filterPanel.getClientProperty("clearBtn");
        if (clearBtn != null) clearBtn.setText(lm.getString("table.clearFilter"));

        JMenuItem editItem = (JMenuItem) getClientProperty("editItem");
        if (editItem != null) editItem.setText(lm.getString("edit.update"));

        JMenuItem deleteItem = (JMenuItem) getClientProperty("deleteItem");
        if (deleteItem != null) deleteItem.setText(lm.getString("edit.delete"));

        // Обновляем заголовки колонок
        table.getColumnModel().getColumn(0); // trigger model update
        tableModel.fireTableStructureChanged();
    }

    private void editSelectedMarine() {
        int row = table.getSelectedRow();
        SpaceMarine marine = tableModel.getMarineAt(row);
        if (marine == null) return;

        if (actionListener != null) {
            actionListener.onEdit(marine);
        }
    }

    private void deleteSelectedMarine() {
        int row = table.getSelectedRow();
        SpaceMarine marine = tableModel.getMarineAt(row);
        if (marine == null) return;

        if (actionListener != null) {
            actionListener.onDelete(marine);
        }
    }

    public void setMarineActionListener(MarineActionListener listener) {
        this.actionListener = listener;
    }

    public CollectionTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Интерфейс для обработки действий над объектами из таблицы.
     */
    public interface MarineActionListener {
        void onEdit(SpaceMarine marine);
        void onDelete(SpaceMarine marine);
    }

    /**
     * Вспомогательный класс для доступа к NetworkManager из рендерера.
     */
    static class NetworkManagerHolder {
        private static NetworkManager networkManager;

        static void setNetworkManager(NetworkManager nm) {
            networkManager = nm;
        }

        static NetworkManager getNetworkManager() {
            return networkManager;
        }
    }
}
