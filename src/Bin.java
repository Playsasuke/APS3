import java.awt.*;

public class Bin {
    public final WasteType type;
    public final String label;
    public final Color color;

    public int x, y, w, h;

    // animação pulse
    private double scale = 1.0;
    private double vel = 0.0;

    public Bin(WasteType type, String label, Color color, int x, int y, int w, int h) {
        this.type = type;
        this.label = label;
        this.color = color;
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    public Rectangle bounds() { return new Rectangle(x, y, w, h); }
    public int centerX() { return x + w / 2; }
    public int centerY() { return y + h / 2; }

    public void resetAnim() { scale = 1.0; vel = 0.0; }
    public void pulse() { vel -= 3.4; }

    public void update(double dt) {
        double k = 34.0;
        double damping = 10.0;
        double acc = -k * (scale - 1.0) - damping * vel;
        vel += acc * dt;
        scale += vel * dt;
        scale = Math.max(0.88, Math.min(1.05, scale));
    }

    public void draw(Graphics2D g) {
        int cx = centerX();
        int cy = centerY();

        Graphics2D gg = (Graphics2D) g.create();
        gg.translate(cx, cy);
        gg.scale(scale, scale);
        gg.translate(-cx, -cy);

        // sombra
        gg.setColor(new Color(0,0,0,28));
        gg.fillRoundRect(x + 3, y + 5, w, h, 22, 22);

        gg.setColor(color);
        gg.fillRoundRect(x, y, w, h, 22, 22);

        gg.setColor(new Color(25, 25, 25));
        gg.setStroke(new BasicStroke(2f));
        gg.drawRoundRect(x, y, w, h, 22, 22);

        // texto central
        gg.setFont(Theme.bold(16));
        gg.setColor(new Color(20,20,20));
        FontMetrics fm = gg.getFontMetrics();
        int tw = fm.stringWidth(label);
        int tx = x + (w - tw) / 2;
        int ty = y + (h / 2) - 4;
        gg.drawString(label, tx, ty);

        gg.setFont(Theme.regular(12));
        gg.setColor(new Color(20,20,20,190));
        String sub = type.name();
        FontMetrics fm2 = gg.getFontMetrics();
        int sw = fm2.stringWidth(sub);
        int sx = x + (w - sw) / 2;
        int sy = ty + 22;
        gg.drawString(sub, sx, sy);

        gg.dispose();
    }
}
