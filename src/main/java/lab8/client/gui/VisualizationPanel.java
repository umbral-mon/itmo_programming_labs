package lab8.client.gui;

import lab8.collectionItems.SpaceMarine;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;

public class VisualizationPanel extends JPanel {

    private List<SpaceMarine> marines = Collections.emptyList();
    private final Map<String, Color> ownerColors = new HashMap<>();
    private final Map<Integer, Double> animationProgress = new HashMap<>();
    private final Timer animationTimer;
    private final Set<Integer> previousIds = new HashSet<>();

    private static final Color[] PALETTE = {
            new Color(70, 130, 180),   // Steel Blue
            new Color(220, 20, 60),    // Crimson
            new Color(50, 205, 50),    // Lime Green
            new Color(255, 165, 0),    // Orange
            new Color(148, 103, 189),  // Purple
            new Color(255, 127, 80),   // Coral
            new Color(0, 206, 209),    // Dark Turquoise
            new Color(255, 215, 0),    // Gold
            new Color(186, 85, 211),   // Medium Orchid
            new Color(60, 179, 113),   // Medium Sea Green
            new Color(233, 150, 122),  // Dark Salmon
            new Color(100, 149, 237),  // Cornflower Blue
    };

    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
    private Point dragStart = null;

    private MarineActionListener actionListener;

    public VisualizationPanel() {
        setBackground(new Color(30, 30, 45));

        animationTimer = new Timer(30, e -> {
            boolean needsRepaint = false;
            for (Map.Entry<Integer, Double> entry : animationProgress.entrySet()) {
                if (entry.getValue() < 1.0) {
                    entry.setValue(Math.min(1.0, entry.getValue() + 0.05));
                    needsRepaint = true;
                }
            }
            if (needsRepaint) {
                repaint();
            }
        });
        animationTimer.start();

        addMouseWheelListener(e -> {
            double delta = e.getPreciseWheelRotation() > 0 ? 0.9 : 1.1;
            scale *= delta;
            scale = Math.max(0.1, Math.min(10.0, scale));
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SpaceMarine marine = findMarineAt(e.getPoint());
                    if (marine != null) {
                        if (actionListener != null) {
                            actionListener.onEdit(marine);
                        }
                    }
                } else if (e.getClickCount() == 1) {
                    SpaceMarine marine = findMarineAt(e.getPoint());
                    if (marine != null) {
                        showMarineInfo(marine, e.getPoint());
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    offsetX += dx;
                    offsetY += dy;
                    //System.out.println(offsetX + " " + offsetY);
                    dragStart = e.getPoint();
                    repaint();
                }
            }
        });
    }

    public void go(double x, double y){
        offsetX = - (x * 10 * scale);
        offsetY = (y * 10 * scale);

        repaint();
    }


    public void setMarines(List<SpaceMarine> marines) {
        Set<Integer> newIds = new HashSet<>();
        if (marines != null) {
            for (SpaceMarine m : marines) {
                newIds.add(m.getID());
                if (!previousIds.contains(m.getID())) {
                    animationProgress.put(m.getID(), 0.0);
                }
            }
        }

        for (Integer oldId : previousIds) {
            if (!newIds.contains(oldId)) {
                animationProgress.remove(oldId);
            }
        }

        previousIds.clear();
        previousIds.addAll(newIds);

        this.marines = marines != null ? new ArrayList<>(marines) : Collections.emptyList();
        repaint();
    }

    private Color getOwnerColor(String owner) {
        if (owner == null) return Color.GRAY;
        return ownerColors.computeIfAbsent(owner, o -> {
            int idx = ownerColors.size() % PALETTE.length;
            return PALETTE[idx];
        });
    }

    private Point2D.Double worldToScreen(long worldX, long worldY) {
        double screenX = (worldX * 10 * scale) + getWidth() / 2.0 + offsetX;
        double screenY = (-worldY * 10 * scale) + getHeight() / 2.0 + offsetY;
        return new Point2D.Double(screenX, screenY);
    }

    private SpaceMarine findMarineAt(Point clickPoint) {
        for (SpaceMarine marine : marines) {
            if (marine.getCoordinates() == null) continue;
            Point2D.Double center = worldToScreen(marine.getCoordinates().getX(), marine.getCoordinates().getY());
            double size = getMarineSize(marine);
            double dist = Math.hypot(clickPoint.x - center.x, clickPoint.y - center.y);
            if (dist <= size * scale / 2 + 5) {
                return marine;
            }
        }
        return null;
    }

    private double getMarineSize(SpaceMarine marine) {
        double health = marine.getHealth() != null ? marine.getHealth() : 50;
        return Math.max(15, Math.min(50, health / 5 + 10));
    }

    private void showMarineInfo(SpaceMarine marine, Point location) {
        Locale locale = LocaleManager.getInstance().getCurrentLocale();
        NumberFormat numFmt = NumberFormat.getInstance(locale);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);

        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>ID:</b> ").append(marine.getID()).append("<br>");
        sb.append("<b>").append(LocaleManager.getInstance().getString("table.name"))
                .append(":</b> ").append(marine.getName()).append("<br>");
        if (marine.getCoordinates() != null) {
            sb.append("<b>X:</b> ").append(numFmt.format(marine.getCoordinates().getX()));
            sb.append(", <b>Y:</b> ").append(numFmt.format(marine.getCoordinates().getY())).append("<br>");
        }
        if (marine.getCreationDate() != null) {
            sb.append("<b>").append(LocaleManager.getInstance().getString("table.creationDate"))
                    .append(":</b> ").append(marine.getCreationDate().format(dateFmt)).append("<br>");
        }
        sb.append("<b>").append(LocaleManager.getInstance().getString("table.health"))
                .append(":</b> ").append(numFmt.format(marine.getHealth())).append("<br>");
        if (marine.getCategory() != null) {
            sb.append("<b>").append(LocaleManager.getInstance().getString("table.category"))
                    .append(":</b> ").append(marine.getCategory()).append("<br>");
        }
        if (marine.getWeaponType() != null) {
            sb.append("<b>").append(LocaleManager.getInstance().getString("table.weapon"))
                    .append(":</b> ").append(marine.getWeaponType()).append("<br>");
        }
        if (marine.getMeleeWeapon() != null) {
            sb.append("<b>").append(LocaleManager.getInstance().getString("table.meleeWeapon"))
                    .append(":</b> ").append(marine.getMeleeWeapon()).append("<br>");
        }
        if (marine.getChapter() != null) {
            sb.append("<b>").append(LocaleManager.getInstance().getString("table.chapterName"))
                    .append(":</b> ").append(marine.getChapter().getName()).append("<br>");
        }
        sb.append("<b>").append(LocaleManager.getInstance().getString("table.owner"))
                .append(":</b> ").append(marine.getOwner());
        sb.append("</html>");

        JLabel label = new JLabel(sb.toString());
        label.setOpaque(true);
        label.setBackground(new Color(40, 40, 60, 230));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getOwnerColor(marine.getOwner()), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        Point screenLoc = getLocationOnScreen();
        SwingUtilities.convertPointToScreen(location, this);
        Popup popup = PopupFactory.getSharedInstance().getPopup(this, label, location.x + 10, location.y + 10);
        popup.show();

        // Скрываем через 3 секунды
        Timer hideTimer = new Timer(3000, e -> popup.hide());
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawGrid(g2d);

        for (SpaceMarine marine : marines) {
            drawMarine(g2d, marine);
        }

        drawLegend(g2d);

        g2d.dispose();
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(50, 50, 70));
        g2d.setStroke(new BasicStroke(0.5f));

        // Оси
        Point2D.Double origin = worldToScreen(0, 0);
        g2d.setColor(new Color(80, 80, 100));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine((int) origin.x, 0, (int) origin.x, getHeight());
        g2d.drawLine(0, (int) origin.y, getWidth(), (int) origin.y);

        // Линии сетки
        g2d.setColor(new Color(40, 40, 60));
        g2d.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, new float[]{3f, 6f}, 0f));
        int gridStep = (int) (50 * scale);
        if (gridStep < 20) gridStep = 20;

        for (int x = (int) (origin.x % gridStep); x < getWidth(); x += gridStep) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = (int) (origin.y % gridStep); y < getHeight(); y += gridStep) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawMarine(Graphics2D g2d, SpaceMarine marine) {
        if (marine.getCoordinates() == null) return;

        Point2D.Double center = worldToScreen(marine.getCoordinates().getX(), marine.getCoordinates().getY());
        double baseSize = getMarineSize(marine) * scale;

        // Прогресс анимации (от 0 до 1)
        double progress = animationProgress.getOrDefault(marine.getID(), 1.0);
        double animSize = baseSize * easeOutBack(progress);

        Color baseColor = getOwnerColor(marine.getOwner());
        float alpha = (float) progress;

        // Свечение
        if (progress < 1.0) {
            RadialGradientPaint glow = new RadialGradientPaint(
                    (float) center.x, (float) center.y, (float) (animSize * 1.8f),
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (80 * alpha)),
                            new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0)
                    }
            );
            g2d.setPaint(glow);
            g2d.fill(new Ellipse2D.Double(
                    center.x - animSize * 1.8, center.y - animSize * 1.8,
                    animSize * 3.6, animSize * 3.6
            ));
        }

        // Щит (основная форма - шестиугольник)
        double s = animSize / 2;
        Path2D.Double shield = new Path2D.Double();
        shield.moveTo(center.x, center.y - s);
        shield.lineTo(center.x + s * 0.9, center.y - s * 0.4);
        shield.lineTo(center.x + s * 0.7, center.y + s * 0.6);
        shield.lineTo(center.x, center.y + s);
        shield.lineTo(center.x - s * 0.7, center.y + s * 0.6);
        shield.lineTo(center.x - s * 0.9, center.y - s * 0.4);
        shield.closePath();

        // Заливка щита
        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (180 * alpha)));
        g2d.fill(shield);

        // Обводка щита
        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (255 * alpha)));
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(shield);

        // Внутренний крест на щите
        g2d.setColor(new Color(255, 255, 255, (int) (120 * alpha)));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine((int) center.x, (int) (center.y - s * 0.5), (int) center.x, (int) (center.y + s * 0.5));
        g2d.drawLine((int) (center.x - s * 0.4), (int) center.y, (int) (center.x + s * 0.4), (int) center.y);

        // Меч сверху
        g2d.setColor(new Color(200, 200, 220, (int) (200 * alpha)));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawLine((int) center.x, (int) (center.y - s), (int) center.x, (int) (center.y - s * 1.6));
        // Гарда
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine((int) (center.x - s * 0.3), (int) (center.y - s), (int) (center.x + s * 0.3), (int) (center.y - s));

        // Имя объекта
        g2d.setColor(new Color(220, 220, 240, (int) (220 * alpha)));
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 10f));
        FontMetrics fm = g2d.getFontMetrics();
        String name = marine.getName();
        if (name.length() > 10) name = name.substring(0, 9) + "…";
        int textWidth = fm.stringWidth(name);
        g2d.drawString(name, (int) (center.x - textWidth / 2.0), (int) (center.y + s + 14));

        // ID
        g2d.setColor(new Color(180, 180, 200, (int) (180 * alpha)));
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 8f));
        String idStr = "#" + marine.getID();
        int idWidth = g2d.getFontMetrics().stringWidth(idStr);
        g2d.drawString(idStr, (int) (center.x - idWidth / 2.0), (int) (center.y - s * 1.6 - 5));
    }

    private double easeOutBack(double t) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    private void drawLegend(Graphics2D g2d) {
        if (ownerColors.isEmpty()) return;

        int x = getWidth() - 180;
        int y = 10;
        int lineHeight = 18;

        g2d.setColor(new Color(20, 20, 35, 200));
        g2d.fillRoundRect(x - 10, y - 5, 180, ownerColors.size() * lineHeight + 15, 8, 8);
        g2d.setColor(new Color(80, 80, 120));
        g2d.drawRoundRect(x - 10, y - 5, 180, ownerColors.size() * lineHeight + 15, 8, 8);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 11f));
        for (Map.Entry<String, Color> entry : ownerColors.entrySet()) {
            g2d.setColor(entry.getValue());
            g2d.fillRect(x, y, 12, 12);
            g2d.setColor(new Color(80, 80, 120));
            g2d.drawRect(x, y, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.drawString(entry.getKey(), x + 18, y + 11);
            y += lineHeight;
        }
    }

    public void setMarineActionListener(MarineActionListener listener) {
        this.actionListener = listener;
    }

    public interface MarineActionListener {
        void onEdit(SpaceMarine marine);
    }

    public void resetView() {
        offsetX = 0;
        offsetY = 0;
        scale = 1.0;
        repaint();
    }
}
