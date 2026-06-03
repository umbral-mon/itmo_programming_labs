package lab8.client.gui;

import lab8.collectionItems.SpaceMarine;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Модель таблицы для отображения коллекции SpaceMarine.
 * Поддерживает фильтрацию и сортировку через Streams API.
 * Форматирует числа и даты в соответствии с текущей локалью.
 */
public class CollectionTableModel extends AbstractTableModel {

    private final String[] COLUMN_KEYS = {
            "table.id", "table.name", "table.coordX", "table.coordY",
            "table.creationDate", "table.health", "table.category",
            "table.weapon", "table.meleeWeapon", "table.chapterName",
            "table.parentLegion", "table.marinesCount", "table.owner"
    };

    private List<SpaceMarine> allMarines = new ArrayList<>();
    private List<SpaceMarine> filteredMarines = new ArrayList<>();
    private final Map<String, String> filterTexts = new HashMap<>();
    private int sortColumn = -1;
    private boolean sortAscending = true;

    public CollectionTableModel() {
        LocaleManager.getInstance().addLocaleChangeListener(locale -> fireTableStructureChanged());
    }

    /**
     * Устанавливает данные коллекции.
     */
    public void setData(List<SpaceMarine> marines) {
        this.allMarines = new ArrayList<>(marines != null ? marines : Collections.emptyList());
        applyFilterAndSort();
    }

    /**
     * Устанавливает текст фильтра для указанной колонки.
     */
    public void setFilter(int columnIndex, String text) {
        if (text == null || text.trim().isEmpty()) {
            filterTexts.remove(String.valueOf(columnIndex));
        } else {
            filterTexts.put(String.valueOf(columnIndex), text.trim().toLowerCase());
        }
        applyFilterAndSort();
    }

    /**
     * Очищает все фильтры.
     */
    public void clearFilters() {
        filterTexts.clear();
        applyFilterAndSort();
    }

    /**
     * Устанавливает сортировку по колонке.
     */
    public void setSort(int columnIndex) {
        if (sortColumn == columnIndex) {
            sortAscending = !sortAscending;
        } else {
            sortColumn = columnIndex;
            sortAscending = true;
        }
        applyFilterAndSort();
    }

    /**
     * Применяет фильтрацию и сортировку с использованием Streams API.
     */
    private void applyFilterAndSort() {
        Locale locale = LocaleManager.getInstance().getCurrentLocale();

        // Фильтрация через Streams API
        java.util.stream.Stream<SpaceMarine> stream = allMarines.stream();

        for (Map.Entry<String, String> entry : filterTexts.entrySet()) {
            int colIdx = Integer.parseInt(entry.getKey());
            String filterValue = entry.getValue();
            stream = stream.filter(marine -> {
                String cellValue = getRawCellValue(marine, colIdx);
                if (cellValue == null) return true;
                return cellValue.toLowerCase().contains(filterValue);
            });
        }

        // Сортировка через Streams API
        if (sortColumn >= 0) {
            final int sortCol = sortColumn;
            stream = stream.sorted((m1, m2) -> {
                String v1 = getRawCellValue(m1, sortCol);
                String v2 = getRawCellValue(m2, sortCol);
                if (v1 == null && v2 == null) return 0;
                if (v1 == null) return sortAscending ? -1 : 1;
                if (v2 == null) return sortAscending ? 1 : -1;

                // Пытаемся числовое сравнение
                try {
                    double d1 = Double.parseDouble(v1);
                    double d2 = Double.parseDouble(v2);
                    return sortAscending ? Double.compare(d1, d2) : Double.compare(d2, d1);
                } catch (NumberFormatException e) {
                    // Строковое сравнение
                    return sortAscending ? v1.compareTo(v2) : v2.compareTo(v1);
                }
            });
        }

        filteredMarines = stream.collect(Collectors.toList());
        fireTableDataChanged();
    }

    /**
     * Получает неформатированное значение ячейки для фильтрации/сортировки.
     */
    private String getRawCellValue(SpaceMarine marine, int columnIndex) {
        if (marine == null) return "";
        switch (columnIndex) {
            case 0: return marine.getID() != null ? String.valueOf(marine.getID()) : "";
            case 1: return marine.getName() != null ? marine.getName() : "";
            case 2: return marine.getCoordinates() != null ? String.valueOf(marine.getCoordinates().getX()) : "";
            case 3: return marine.getCoordinates() != null ? String.valueOf(marine.getCoordinates().getY()) : "";
            case 4: return marine.getCreationDate() != null ? marine.getCreationDate().toString() : "";
            case 5: return marine.getHealth() != null ? String.valueOf(marine.getHealth()) : "";
            case 6: return marine.getCategory() != null ? marine.getCategory().name() : "";
            case 7: return marine.getWeaponType() != null ? marine.getWeaponType().name() : "";
            case 8: return marine.getMeleeWeapon() != null ? marine.getMeleeWeapon().name() : "";
            case 9: return marine.getChapter() != null ? marine.getChapter().getName() : "";
            case 10: return marine.getChapter() != null && marine.getChapter().getParentLegion() != null
                    ? marine.getChapter().getParentLegion() : "";
            case 11: return marine.getChapter() != null && marine.getChapter().getMarinesCount() != null
                    ? String.valueOf(marine.getChapter().getMarinesCount()) : "";
            case 12: return marine.getOwner() != null ? marine.getOwner() : "";
            default: return "";
        }
    }

    @Override
    public int getRowCount() {
        return filteredMarines.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_KEYS.length;
    }

    @Override
    public String getColumnName(int column) {
        return LocaleManager.getInstance().getString(COLUMN_KEYS[column]);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= filteredMarines.size()) return "";
        SpaceMarine marine = filteredMarines.get(rowIndex);
        Locale locale = LocaleManager.getInstance().getCurrentLocale();

        switch (columnIndex) {
            case 0: return marine.getID();
            case 1: return marine.getName();
            case 2: return marine.getCoordinates() != null ? marine.getCoordinates().getX() : "";
            case 3: return marine.getCoordinates() != null ? marine.getCoordinates().getY() : "";
            case 4:
                if (marine.getCreationDate() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(locale);
                    return marine.getCreationDate().format(formatter);
                }
                return "";
            case 5:
                if (marine.getHealth() != null) {
                    return String.format(locale, "%.2f", marine.getHealth());
                }
                return "";
            case 6: return marine.getCategory() != null ? marine.getCategory().name() : "";
            case 7: return marine.getWeaponType() != null ? marine.getWeaponType().name() : "";
            case 8: return marine.getMeleeWeapon() != null ? marine.getMeleeWeapon().name() : "";
            case 9: return marine.getChapter() != null ? marine.getChapter().getName() : "";
            case 10: return marine.getChapter() != null && marine.getChapter().getParentLegion() != null
                    ? marine.getChapter().getParentLegion() : "";
            case 11:
                if (marine.getChapter() != null && marine.getChapter().getMarinesCount() != null) {
                    return String.format(locale, "%d", marine.getChapter().getMarinesCount());
                }
                return "";
            case 12: return marine.getOwner() != null ? marine.getOwner() : "";
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * Возвращает SpaceMarine по индексу строки.
     */
    public SpaceMarine getMarineAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < filteredMarines.size()) {
            return filteredMarines.get(rowIndex);
        }
        return null;
    }

    /**
     * Возвращает все отображаемые объекты.
     */
    public List<SpaceMarine> getFilteredMarines() {
        return Collections.unmodifiableList(filteredMarines);
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }
}
