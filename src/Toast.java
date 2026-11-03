import java.awt.*;

public class Toast {
    private final String text;
    private final Color color;

    private double age = 0.0;
    private final double duration = 1.0; // total (1s)
    private final double fadeIn = 0.15;
    private final double fadeOut = 0.30;

    public Toast(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public void update(double dt) {
        age += dt;
    }

    public boolean isDead() {
        return age >= duration;
    }

    private float alpha() {
        if (age < fadeIn) {
            return (float) (age / fadeIn);
        }
        double holdEnd = duration - fadeOut;
        if (age < holdEnd) {
            return 1f;
        }
        double t = (age - holdEnd) / fadeOut;
        return (float) Math.max(0.0, 1.0 - t);
    }

    public void draw(Graphics2D g, int w, int h) {
        float a = alpha();
        if (a <= 0f) return;

        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        gg.setFont(Theme.font(42, true));
        FontMetrics fm = gg.getFontMetrics();

        int padX = 26;
        int padY = 16;

        int tw = fm.stringWidth(text);
        int boxW = tw + padX * 2;
        int boxH = fm.getHeight() + padY * 2;

        int x = (w - boxW) / 2;
        int y = (h / 2) - boxH - 30;

        // sombra
        gg.setColor(new Color(0, 0, 0, 60));
        gg.fillRoundRect(x + 4, y + 6, boxW, boxH, 22, 22);

        // fundo
        gg.setColor(new Color(248, 246, 242));
        gg.fillRoundRect(x, y, boxW, boxH, 22, 22);

        // borda
        gg.setColor(new Color(30, 30, 30));
        gg.setStroke(new BasicStroke(2f));
        gg.drawRoundRect(x, y, boxW, boxH, 22, 22);

        // texto
        gg.setColor(color);
        int tx = x + padX;
        int ty = y + padY + fm.getAscent();
        gg.drawString(text, tx, ty);

        gg.dispose();
    }
}
