import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Theme {

    public static final Color BG = new Color(246, 244, 238);
    public static final Color BG2 = new Color(236, 232, 222);
    public static final Color TEXT = new Color(20, 32, 26);
    public static final Color MUTED = new Color(62, 82, 72);
    public static final Color BORDER = new Color(27, 50, 40);

    public static final Color SUCCESS = new Color(18, 110, 70);
    public static final Color DANGER  = new Color(150, 45, 45);

    public static final Color BLUE   = new Color(45, 110, 210);
    public static final Color RED    = new Color(210, 70, 70);
    public static final Color YELLOW = new Color(226, 196, 60);
    public static final Color GREEN  = new Color(55, 150, 85);
    public static final Color BROWN  = new Color(125, 88, 58);
    public static final Color GRAY   = new Color(110, 110, 110);
    public static final Color ORANGE = new Color(224, 132, 42);

    public static final Color BTN = new Color(18, 64, 50);

    // Fontes carregadas do arquivo (se existir)
    private static Font comfortaaRegular = null;
    private static Font comfortaaBold = null;

    // Compatibilidade com códigos antigos do projeto
    public static Font font(int size, boolean bold) {
    return bold ? Theme.bold(size) : Theme.regular(size);
}

    public static void init() {
        comfortaaRegular = loadTtf("assets/fonts/Comfortaa-Regular.ttf");
        comfortaaBold    = loadTtf("assets/fonts/Comfortaa-Bold.ttf");

        // Se não achou Bold, usa o Regular como base
        if (comfortaaBold == null) comfortaaBold = comfortaaRegular;

        // Fallback se não achou nada
        if (comfortaaRegular == null) {
            comfortaaRegular = new Font("SansSerif", Font.PLAIN, 16);
            comfortaaBold = new Font("SansSerif", Font.BOLD, 16);
        }
    }

    private static Font loadTtf(String path) {
        try {
            // lê do arquivo local (funciona rodando via javac/java)
            File f = new File(path);
            if (!f.exists()) return null;

            try (InputStream is = new FileInputStream(f)) {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                return base;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static Font regular(int size) {
        return comfortaaRegular.deriveFont(Font.PLAIN, (float) size);
    }

    public static Font bold(int size) {
        return comfortaaBold.deriveFont(Font.BOLD, (float) size);
    }

    // “semi-bold”: se você não tiver um arquivo SemiBold, usamos Bold em tamanho levemente menor
    public static Font semibold(int size) {
        // se tiver bold real, usa ele
        return comfortaaBold.deriveFont(Font.BOLD, (float) size);
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(BTN);
        b.setForeground(new Color(248, 246, 242));
        b.setFont(semibold(16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton outlineButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(TEXT);
        b.setFont(semibold(16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void drawCard(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(0,0,0,35));
        g.fillRoundRect(x + 4, y + 6, w, h, 22, 22);

        g.setColor(BG);
        g.fillRoundRect(x, y, w, h, 22, 22);

        g.setColor(BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w, h, 22, 22);
    }

    public static void drawButton(Graphics2D g, Rectangle r, String label, boolean outline) {
        g.setColor(new Color(0,0,0,22));
        g.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 14, 14);

        if (!outline) {
            g.setColor(BTN);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            g.setColor(new Color(248, 246, 242));
        } else {
            g.setColor(BG);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            g.setColor(TEXT);
        }

        g.setFont(semibold(14));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(label, tx, ty);
    }
}
