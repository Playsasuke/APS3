import java.awt.*;

public class WasteItem {
    public final String name;
    public final WasteType type;

    public int x, y, w, h;
    public int startX, startY;

    public WasteItem(String name, WasteType type, int x, int y, int w, int h) {
        this.name = name;
        this.type = type;
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.startX = x; this.startY = y;
    }

    public Rectangle bounds() { return new Rectangle(x, y, w, h); }
    public int centerX() { return x + w / 2; }
    public int centerY() { return y + h / 2; }

    public void resetPosition() { this.x = startX; this.y = startY; }
    public void setPosition(int newX, int newY) { this.x = newX; this.y = newY; }

    public void draw(Graphics2D g) {
        // sombra
        g.setColor(new Color(0, 0, 0, 22));
        g.fillRoundRect(x + 3, y + 5, w, h, 22, 22);

        // card
        g.setColor(new Color(252, 252, 252));
        g.fillRoundRect(x, y, w, h, 22, 22);

        g.setColor(Theme.BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w, h, 22, 22);

        // texto central (nome)
        g.setFont(Theme.semibold(16));
        g.setColor(Theme.TEXT);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(name);
        int tx = x + (w - tw) / 2;
        int ty = y + (h / 2) + (fm.getAscent() / 2) - 2;
        g.drawString(name, tx, ty);

        // subtexto central
        String sub = "Arraste para a lixeira correta";
        g.setFont(Theme.regular(12));
        g.setColor(Theme.MUTED);
        FontMetrics fm2 = g.getFontMetrics();
        int sw = fm2.stringWidth(sub);
        int sx = x + (w - sw) / 2;
        int sy = y + h - 14;
        g.drawString(sub, sx, sy);
    }
}
