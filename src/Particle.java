import java.awt.*;

public class Particle {
    private double x, y;
    private double vx, vy;
    private double life = 0.5; // segundos
    private double age = 0.0;

    public Particle(double x, double y, double vx, double vy) {
        this.x = x; this.y = y;
        this.vx = vx; this.vy = vy;
    }

    public void update(double dt) {
        age += dt;
        x += vx * dt;
        y += vy * dt;

        // gravidade leve
        vy += 240 * dt;

        // atrito
        vx *= 0.985;
        vy *= 0.985;
    }

    public boolean isDead() {
        return age >= life;
    }

    public void draw(Graphics2D g) {
        double t = 1.0 - (age / life);
        int a = (int) (255 * Math.max(0, Math.min(1, t)));
        g.setColor(new Color(255, 255, 255, a));

        int size = (int) (6 + 6 * t);
        g.fillOval((int) x - size/2, (int) y - size/2, size, size);
    }
}
