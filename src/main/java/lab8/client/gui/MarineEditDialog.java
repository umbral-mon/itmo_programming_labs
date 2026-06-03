package lab8.client.gui;

import lab8.collectionItems.*;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Диалог для добавления и редактирования объектов SpaceMarine.
 * Поддерживает валидацию полей, локализацию и форматирование чисел.
 */
public class MarineEditDialog extends JDialog {

    private final LocaleManager localeManager = LocaleManager.getInstance();

    private JTextField nameField;
    private JTextField coordXField;
    private JTextField coordYField;
    private JTextField healthField;
    private JComboBox<AstartesCategory> categoryCombo;
    private JComboBox<Weapon> weaponCombo;
    private JComboBox<MeleeWeapon> meleeWeaponCombo;
    private JTextField chapterNameField;
    private JTextField parentLegionField;
    private JTextField marinesCountField;

    private JButton saveButton;
    private JButton cancelButton;
    private JButton deleteButton;

    private SpaceMarine editingMarine;
    private boolean isEditing = false;
    private boolean deleted = false;
    private boolean saved = false;
    private SpaceMarine resultMarine;

    private final boolean canEdit;

    public MarineEditDialog(Frame owner, SpaceMarine marine, boolean canEdit) {
        super(owner, true);
        this.editingMarine = marine;
        this.isEditing = marine != null;
        this.canEdit = canEdit;
        initComponents();
        if (isEditing) {
            populateFields(marine);
        }
        applyLocale();
        localeManager.addLocaleChangeListener(locale -> applyLocale());
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(450, 420);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel nameLabel = new JLabel();
        nameLabel.putClientProperty("i18nKey", "edit.name");
        mainPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nameField = new JTextField(20);
        mainPanel.add(nameField, gbc);

        row++;
        // CoordX
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel coordXLabel = new JLabel();
        coordXLabel.putClientProperty("i18nKey", "edit.coordX");
        mainPanel.add(coordXLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        coordXField = new JTextField(20);
        mainPanel.add(coordXField, gbc);

        row++;
        // CoordY
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel coordYLabel = new JLabel();
        coordYLabel.putClientProperty("i18nKey", "edit.coordY");
        mainPanel.add(coordYLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        coordYField = new JTextField(20);
        mainPanel.add(coordYField, gbc);

        row++;
        // Health
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel healthLabel = new JLabel();
        healthLabel.putClientProperty("i18nKey", "edit.health");
        mainPanel.add(healthLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        healthField = new JTextField(20);
        mainPanel.add(healthField, gbc);

        row++;
        // Category
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel categoryLabel = new JLabel();
        categoryLabel.putClientProperty("i18nKey", "edit.category");
        mainPanel.add(categoryLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        categoryCombo = new JComboBox<>(AstartesCategory.values());
        categoryCombo.insertItemAt(null, 0);
        mainPanel.add(categoryCombo, gbc);

        row++;
        // Weapon
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel weaponLabel = new JLabel();
        weaponLabel.putClientProperty("i18nKey", "edit.weapon");
        mainPanel.add(weaponLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        weaponCombo = new JComboBox<>(Weapon.values());
        mainPanel.add(weaponCombo, gbc);

        row++;
        // MeleeWeapon
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel meleeLabel = new JLabel();
        meleeLabel.putClientProperty("i18nKey", "edit.meleeWeapon");
        mainPanel.add(meleeLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        meleeWeaponCombo = new JComboBox<>(MeleeWeapon.values());
        meleeWeaponCombo.insertItemAt(null, 0);
        mainPanel.add(meleeWeaponCombo, gbc);

        row++;
        // Chapter Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel chapterNameLabel = new JLabel();
        chapterNameLabel.putClientProperty("i18nKey", "edit.chapterName");
        mainPanel.add(chapterNameLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        chapterNameField = new JTextField(20);
        mainPanel.add(chapterNameField, gbc);

        row++;
        // Parent Legion
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel parentLegionLabel = new JLabel();
        parentLegionLabel.putClientProperty("i18nKey", "edit.parentLegion");
        mainPanel.add(parentLegionLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        parentLegionField = new JTextField(20);
        mainPanel.add(parentLegionField, gbc);

        row++;
        // Marines Count
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel marinesCountLabel = new JLabel();
        marinesCountLabel.putClientProperty("i18nKey", "edit.marinesCount");
        mainPanel.add(marinesCountLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        marinesCountField = new JTextField(20);
        mainPanel.add(marinesCountField, gbc);

        // Buttons
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        saveButton = new JButton();
        saveButton.addActionListener(e -> doSave());

        cancelButton = new JButton();
        cancelButton.addActionListener(e -> dispose());

        deleteButton = new JButton();
        deleteButton.addActionListener(e -> doDelete());
        deleteButton.setVisible(isEditing && canEdit);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);
        mainPanel.add(buttonPanel, gbc);

        // Если нет прав на редактирование, делаем поля нередактируемыми
        if (!canEdit && isEditing) {
            setFieldsEditable(false);
        }

        setContentPane(mainPanel);
    }

    private void applyLocale() {
        setTitle(localeManager.getString("edit.title"));
        saveButton.setText(isEditing ? localeManager.getString("edit.update") : localeManager.getString("edit.add"));
        cancelButton.setText(localeManager.getString("edit.cancel"));
        deleteButton.setText(localeManager.getString("edit.delete"));

        // Обновляем все метки
        if (getContentPane() instanceof JPanel) {
            updateLabels((JPanel) getContentPane());
        }
    }

    private void updateLabels(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                String key = (String) ((JLabel) c).getClientProperty("i18nKey");
                if (key != null) {
                    ((JLabel) c).setText(localeManager.getString(key) + ":");
                }
            }
            if (c instanceof Container) {
                updateLabels((Container) c);
            }
        }
    }

    private void populateFields(SpaceMarine marine) {
        nameField.setText(marine.getName());
        if (marine.getCoordinates() != null) {
            Locale locale = localeManager.getCurrentLocale();
            NumberFormat numFmt = NumberFormat.getInstance(locale);
            coordXField.setText(numFmt.format(marine.getCoordinates().getX()));
            coordYField.setText(numFmt.format(marine.getCoordinates().getY()));
        }
        if (marine.getHealth() != null) {
            Locale locale = localeManager.getCurrentLocale();
            NumberFormat numFmt = NumberFormat.getInstance(locale);
            healthField.setText(numFmt.format(marine.getHealth()));
        }
        categoryCombo.setSelectedItem(marine.getCategory());
        weaponCombo.setSelectedItem(marine.getWeaponType());
        meleeWeaponCombo.setSelectedItem(marine.getMeleeWeapon());
        if (marine.getChapter() != null) {
            chapterNameField.setText(marine.getChapter().getName());
            parentLegionField.setText(marine.getChapter().getParentLegion());
            if (marine.getChapter().getMarinesCount() != null) {
                Locale locale = localeManager.getCurrentLocale();
                NumberFormat numFmt = NumberFormat.getInstance(locale);
                marinesCountField.setText(numFmt.format(marine.getChapter().getMarinesCount()));
            }
        }
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        coordXField.setEditable(editable);
        coordYField.setEditable(editable);
        healthField.setEditable(editable);
        categoryCombo.setEnabled(editable);
        weaponCombo.setEnabled(editable);
        meleeWeaponCombo.setEnabled(editable);
        chapterNameField.setEditable(editable);
        parentLegionField.setEditable(editable);
        marinesCountField.setEditable(editable);
    }

    private void doSave() {
        try {
            Locale locale = localeManager.getCurrentLocale();
            NumberFormat numFmt = NumberFormat.getInstance(locale);

            String name = nameField.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException(localeManager.getString("edit.name") + " - required");

            long coordX;
            try {
                coordX = numFmt.parse(coordXField.getText().trim()).longValue();
            } catch (Exception e) {
                throw new IllegalArgumentException(localeManager.getString("edit.coordX") + " - invalid number");
            }

            long coordY;
            try {
                coordY = numFmt.parse(coordYField.getText().trim()).longValue();
            } catch (Exception e) {
                throw new IllegalArgumentException(localeManager.getString("edit.coordY") + " - invalid number");
            }

            double health;
            try {
                health = numFmt.parse(healthField.getText().trim()).doubleValue();
            } catch (Exception e) {
                throw new IllegalArgumentException(localeManager.getString("edit.health") + " - invalid number");
            }

            String chapterName = chapterNameField.getText().trim();
            if (chapterName.isEmpty()) chapterName = "Default";

            String parentLegion = parentLegionField.getText().trim();

            Integer marinesCount = null;
            String mcText = marinesCountField.getText().trim();
            if (!mcText.isEmpty()) {
                try {
                    marinesCount = numFmt.parse(mcText).intValue();
                } catch (Exception e) {
                    throw new IllegalArgumentException(localeManager.getString("edit.marinesCount") + " - invalid number");
                }
            }

            AstartesCategory category = (AstartesCategory) categoryCombo.getSelectedItem();
            Weapon weapon = (Weapon) weaponCombo.getSelectedItem();
            MeleeWeapon meleeWeapon = (MeleeWeapon) meleeWeaponCombo.getSelectedItem();

            if (weapon == null) weapon = Weapon.MELTAGUN;

            Chapter chapter = new Chapter(chapterName, parentLegion, marinesCount);
            Coordinates coordinates = new Coordinates(coordX, coordY);

            String owner = isEditing && editingMarine != null ? editingMarine.getOwner()
                    : CollectionTablePanel.NetworkManagerHolder.getNetworkManager().getLogin();

            if (isEditing) {
                resultMarine = new SpaceMarine(
                        editingMarine.getID(), name, coordinates, editingMarine.getCreationDate(),
                        health, category, weapon, meleeWeapon, chapter, owner
                );
            } else {
                resultMarine = new SpaceMarine(name, coordinates, health, category, weapon, meleeWeapon, chapter, owner);
            }
            saved = true;
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    localeManager.getString("edit.error.validation") + "\n" + ex.getMessage(),
                    localeManager.getString("edit.error.validation"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        int confirm = JOptionPane.showConfirmDialog(this,
                localeManager.getString("edit.confirmDelete"),
                localeManager.getString("confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            deleted = true;
            dispose();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public SpaceMarine getResultMarine() {
        return resultMarine;
    }

    public SpaceMarine getEditingMarine() {
        return editingMarine;
    }
}
