import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int W = 1280;
    private static final int H = 720;

    private static final int HEADER_H = 96;
    private static final int ITEMS_Y = 140;
    private static final int ITEMS_GAP = 26;

    private static final int BINS_TOP_Y = 420;
    private static final int BINS_BOTTOM_Y = 560;

    private final List<Bin> bins = new ArrayList<>();
    private final List<WasteItem> items = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    private WasteItem dragging = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private int score = 0;         // +1 / -1
    private int streak = 0;
    private int correctCount = 0;

    private int timeLeft = 60;
    private boolean gameOver = false;

    private final javax.swing.Timer countdownTimer;
    private final javax.swing.Timer animTimer;

    private final Runnable onBackToStart;

    private final Rectangle btnRestart = new Rectangle(W - 280, 26, 120, 38);
    private final Rectangle btnBack = new Rectangle(W - 150, 26, 120, 38);

    private final int maxItemsOnScreen = 3;

    private Toast toast = null;

    // --------- Histórico / score anterior ----------
    private Integer prevGameScore = null;
    private boolean resultRecorded = false;

    // ----- Deck sem repetição -----
    private record ItemTemplate(String name, WasteType type) {}
    private final List<ItemTemplate> templates = new ArrayList<>();
    private final Deque<ItemTemplate> deck = new ArrayDeque<>();

    // ----- Countdown "Prepare-se!" -----
    private boolean preGame = true;
    private double preAge = 0.0; // 0..5
    private int preSeconds = 5;

    public GamePanel(Runnable onBackToStart) {
        this.onBackToStart = onBackToStart;

        setPreferredSize(new Dimension(W, H));
        setBackground(Theme.BG);
        setFocusable(true);

        addMouseListener(this);
        addMouseMotionListener(this);

        GameHistory.loadOnce();

        setupBins();
        buildTemplates();
        refillDeck();
        setupInitialItems();

        countdownTimer = new javax.swing.Timer(1000, e -> {
            if (gameOver || preGame) return;
            timeLeft--;
            if (timeLeft <= 0) {
                timeLeft = 0;
                endGame();
            }
            repaint();
        });

        animTimer = new javax.swing.Timer(16, e -> {
            updateAnimations(0.016);
            repaint();
        });
    }

    public void startNewGame() {
        // captura “pontuação anterior” antes de começar
        prevGameScore = GameHistory.getLastScore();
        resultRecorded = false;

        score = 0;
        streak = 0;
        correctCount = 0;

        timeLeft = 60;
        gameOver = false;

        toast = null;
        particles.clear();
        for (Bin b : bins) b.resetAnim();

        refillDeck();
        setupInitialItems();

        // inicia pre-game
        preGame = true;
        preAge = 0.0;
        preSeconds = 5;

        countdownTimer.stop();
        animTimer.stop();
        countdownTimer.start();
        animTimer.start();
        repaint();
    }

    private void endGame() {
        gameOver = true;
        countdownTimer.stop();

        // grava histórico 1 vez
        if (!resultRecorded) {
            GameHistory.addResult(score, correctCount);
            resultRecorded = true;
        }

        showToast("Tempo!", Theme.MUTED);
    }

    private void updateAnimations(double dt) {
        for (Bin b : bins) b.update(dt);

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(dt);
            if (p.isDead()) particles.remove(i);
        }

        if (toast != null) {
            toast.update(dt);
            if (toast.isDead()) toast = null;
        }

        // pré-jogo: conta 5 segundos
        if (preGame && !gameOver) {
            preAge += dt;
            int newSec = 5 - (int) Math.floor(preAge);
            newSec = Math.max(0, newSec);
            preSeconds = newSec;

            if (preAge >= 5.0) {
                preGame = false;
            }
        }
    }

    private void showToast(String msg, Color c) {
        toast = new Toast(msg, c);
    }

    private void setupBins() {
        bins.clear();

        int binW = 200, binH = 110;
        int gap = 18;

        int row1TotalW = (binW * 5) + (gap * 4);
        int row1StartX = (W - row1TotalW) / 2;

        bins.add(new Bin(WasteType.PAPEL, "PAPEL", Theme.BLUE, row1StartX + (binW + gap) * 0, BINS_TOP_Y, binW, binH));
        bins.add(new Bin(WasteType.PLASTICO, "PLÁSTICO", Theme.RED, row1StartX + (binW + gap) * 1, BINS_TOP_Y, binW, binH));
        bins.add(new Bin(WasteType.METAL, "METAL", Theme.YELLOW, row1StartX + (binW + gap) * 2, BINS_TOP_Y, binW, binH));
        bins.add(new Bin(WasteType.VIDRO, "VIDRO", Theme.GREEN, row1StartX + (binW + gap) * 3, BINS_TOP_Y, binW, binH));
        bins.add(new Bin(WasteType.ORGANICO, "ORGÂNICO", Theme.BROWN, row1StartX + (binW + gap) * 4, BINS_TOP_Y, binW, binH));

        int row2TotalW = (binW * 2) + gap;
        int row2StartX = (W - row2TotalW) / 2;

        bins.add(new Bin(WasteType.ELETRONICOS, "ELETRÔNICOS", Theme.GRAY, row2StartX + (binW + gap) * 0, BINS_BOTTOM_Y, binW, binH));
        bins.add(new Bin(WasteType.OLEO, "ÓLEO", Theme.ORANGE, row2StartX + (binW + gap) * 1, BINS_BOTTOM_Y, binW, binH));
    }

    private void buildTemplates() {
        templates.clear();

        // PLÁSTICO
        templates.add(new ItemTemplate("Garrafa PET", WasteType.PLASTICO));
        templates.add(new ItemTemplate("Sacola de mercado", WasteType.PLASTICO));
        templates.add(new ItemTemplate("Copo descartável", WasteType.PLASTICO));
        templates.add(new ItemTemplate("Plástico Bolha", WasteType.PLASTICO));
        templates.add(new ItemTemplate("Embalagem plástica", WasteType.PLASTICO));
        templates.add(new ItemTemplate("Canudo", WasteType.PLASTICO));

        // PAPEL
        templates.add(new ItemTemplate("Jornal", WasteType.PAPEL));
        templates.add(new ItemTemplate("Folha sulfite", WasteType.PAPEL));
        templates.add(new ItemTemplate("Tubo de batata", WasteType.PAPEL));
        templates.add(new ItemTemplate("Revista", WasteType.PAPEL));
        templates.add(new ItemTemplate("Envelope de carta", WasteType.PAPEL));
        templates.add(new ItemTemplate("Caixa de leite", WasteType.PAPEL));

        // METAL
        templates.add(new ItemTemplate("Lata de atum", WasteType.METAL));
        templates.add(new ItemTemplate("Lata de refrigerante", WasteType.METAL));
        templates.add(new ItemTemplate("Prego/parafuso", WasteType.METAL));
        templates.add(new ItemTemplate("Arame", WasteType.METAL));
        templates.add(new ItemTemplate("Lata de milho", WasteType.METAL));

        // VIDRO
        templates.add(new ItemTemplate("Garrafa de cerveja", WasteType.VIDRO));
        templates.add(new ItemTemplate("Pote de vidro", WasteType.VIDRO));
        templates.add(new ItemTemplate("Frasco de perfume", WasteType.VIDRO));
        templates.add(new ItemTemplate("Copo de vidro", WasteType.VIDRO));

        // ORGÂNICO
        templates.add(new ItemTemplate("Casca de banana", WasteType.ORGANICO));
        templates.add(new ItemTemplate("Restos de comida", WasteType.ORGANICO));
        templates.add(new ItemTemplate("Papel higiênico usado", WasteType.ORGANICO));
        templates.add(new ItemTemplate("Casca de ovo", WasteType.ORGANICO));
        templates.add(new ItemTemplate("Restos de frutas", WasteType.ORGANICO));

        // ELETRÔNICOS
        templates.add(new ItemTemplate("Pilhas/baterias", WasteType.ELETRONICOS));
        templates.add(new ItemTemplate("Carregador", WasteType.ELETRONICOS));
        templates.add(new ItemTemplate("Fone quebrado", WasteType.ELETRONICOS));
        templates.add(new ItemTemplate("Mouse velho", WasteType.ELETRONICOS));
        templates.add(new ItemTemplate("Celular antigo", WasteType.ELETRONICOS));

        // ÓLEO
        templates.add(new ItemTemplate("Óleo de cozinha usado", WasteType.OLEO));
        templates.add(new ItemTemplate("Gordura/óleo (resto)", WasteType.OLEO));
    }

    private void refillDeck() {
        deck.clear();
        List<ItemTemplate> copy = new ArrayList<>(templates);
        Collections.shuffle(copy, rng);
        for (ItemTemplate t : copy) deck.addLast(t);
    }

    private WasteItem nextItemFromDeck(int x, int y, int w, int h) {
        if (deck.isEmpty()) refillDeck();
        ItemTemplate t = deck.removeFirst();
        return new WasteItem(t.name(), t.type(), x, y, w, h);
    }

    private void setupInitialItems() {
        items.clear();

        int cardW = 300;
        int cardH = 78;

        int totalW = (cardW * maxItemsOnScreen) + (ITEMS_GAP * (maxItemsOnScreen - 1));
        int startX = (W - totalW) / 2;

        for (int i = 0; i < maxItemsOnScreen; i++) {
            int x = startX + i * (cardW + ITEMS_GAP);
            items.add(nextItemFromDeck(x, ITEMS_Y, cardW, cardH));
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHeader(g);
        drawBins(g);
        drawItems(g);
        drawParticles(g);

        if (toast != null) toast.draw(g, W, H);
        if (gameOver) drawGameOverOverlay(g);
        if (preGame && !gameOver) drawPreGameOverlay(g);
    }

    private void drawHeader(Graphics2D g) {
        g.setColor(Theme.BG2);
        g.fillRect(0, 0, W, HEADER_H);

        g.setColor(Theme.BORDER);
        g.drawLine(0, HEADER_H - 1, W, HEADER_H - 1);

        g.setFont(Theme.bold(26));
        g.setColor(Theme.TEXT);
        g.drawString("EcoMemory", 36, 42);

        g.setFont(Theme.regular(14));
        g.setColor(Theme.MUTED);
        g.drawString("Coleta seletiva e descarte especial • Cidade", 36, 68);

        g.setFont(Theme.semibold(16));
        g.setColor(Theme.TEXT);
        g.drawString("Pontos: " + score, 520, 44);
        g.drawString("Sequência: " + streak, 520, 70);

        // Pontuação anterior
        g.setFont(Theme.regular(14));
        g.setColor(Theme.MUTED);
        String prev = (prevGameScore == null) ? "Anterior: -" : ("Anterior: " + prevGameScore + " pts");
        g.drawString(prev, 520, 88);

        g.setFont(Theme.semibold(16));
        g.setColor(Theme.TEXT);
        g.drawString("Tempo: " + timeLeft + "s", 720, 44);

        Theme.drawButton(g, btnRestart, "Reiniciar", false);
        Theme.drawButton(g, btnBack, "Início", true);
    }

    private void drawBins(Graphics2D g) {
        for (Bin b : bins) b.draw(g);
    }

    private void drawItems(Graphics2D g) {
        for (WasteItem it : items) {
            if (it != dragging) it.draw(g);
        }
        if (dragging != null) dragging.draw(g);
    }

    private void drawParticles(Graphics2D g) {
        for (Particle p : particles) p.draw(g);
    }

    private Bin findBinByDrop(Point dropPoint, WasteItem item) {
        for (Bin b : bins) if (b.bounds().contains(dropPoint)) return b;
        Point center = new Point(item.centerX(), item.centerY());
        for (Bin b : bins) if (b.bounds().contains(center)) return b;
        return null;
    }

    private void spawnSparkles(int cx, int cy) {
        for (int i = 0; i < 18; i++) {
            double ang = rng.nextDouble() * Math.PI * 2;
            double spd = 70 + rng.nextDouble() * 140;
            double vx = Math.cos(ang) * spd;
            double vy = Math.sin(ang) * spd;
            particles.add(new Particle(cx, cy, vx, vy));
        }
    }

    private void handleDrop(WasteItem item, Point dropPoint) {
        if (gameOver || preGame) { item.resetPosition(); return; }

        Bin b = findBinByDrop(dropPoint, item);
        if (b == null) {
            item.resetPosition();
            showToast("Solte em uma lixeira", Theme.MUTED);
            return;
        }

        if (b.type == item.type) {
            score += 1;
            streak += 1;
            correctCount += 1;

            b.pulse();
            spawnSparkles(b.centerX(), b.centerY());
            showToast("+1 ponto! ", Theme.SUCCESS);

            int idx = items.indexOf(item);
            items.remove(item);

            int cardW = 300, cardH = 78;
            int totalW = (cardW * maxItemsOnScreen) + (ITEMS_GAP * (maxItemsOnScreen - 1));
            int startX = (W - totalW) / 2;
            int newX = startX + idx * (cardW + ITEMS_GAP);

            items.add(idx, nextItemFromDeck(newX, ITEMS_Y, cardW, cardH));
        } else {
            score -= 1;
            streak = 0;

            item.resetPosition();
            showToast("-1 ponto ):", Theme.DANGER);
        }
    }

    private String rankName(int correct) {
        return GameHistory.rankName(correct);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, W, H);

        int boxW = 560, boxH = 270;
        int x = (W - boxW) / 2;
        int y = (H - boxH) / 2;

        Theme.drawCard(g, x, y, boxW, boxH);

        g.setFont(Theme.bold(26));
        g.setColor(Theme.TEXT);
        g.drawString("Fim de jogo!", x + 32, y + 54);

        g.setFont(Theme.regular(18));
        g.setColor(Theme.MUTED);
        g.drawString("Pontos finais: " + score, x + 32, y + 94);
        g.drawString("Itens corretos: " + correctCount, x + 32, y + 124);

        g.setFont(Theme.semibold(20));
        g.setColor(Theme.TEXT);
        g.drawString("Nível: " + rankName(correctCount), x + 32, y + 160);

        Rectangle r1 = new Rectangle(x + 32, y + 195, 220, 46);
        Rectangle r2 = new Rectangle(x + 268, y + 195, 260, 46);

        Theme.drawButton(g, r1, "Jogar novamente", false);
        Theme.drawButton(g, r2, "Voltar ao início", true);

        putClientProperty("GO_RESTART", r1);
        putClientProperty("GO_BACK", r2);
    }

    // Overlay “Prepare-se!” (sem fade)
    private void drawPreGameOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 105));
        g.fillRect(0, 0, W, H);

        double frac = preAge - Math.floor(preAge); // 0..1 dentro do segundo atual
        double scale = 0.95 + 0.07 * Math.sin(frac * Math.PI);

        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boxW = 520;
        int boxH = 240;
        int bx = (W - boxW) / 2;
        int by = (H - boxH) / 2;

        Theme.drawCard(gg, bx, by, boxW, boxH);

        String title = "Prepare-se!";
        gg.setFont(Theme.bold(34));
        gg.setColor(Theme.TEXT);
        FontMetrics fm = gg.getFontMetrics();
        int tx = bx + (boxW - fm.stringWidth(title)) / 2;
        gg.drawString(title, tx, by + 70);

        String num = String.valueOf(preSeconds);
        gg.setFont(Theme.bold(82));
        FontMetrics fm2 = gg.getFontMetrics();
        int nx = bx + (boxW - fm2.stringWidth(num)) / 2;
        int ny = by + 160;

        Graphics2D nG = (Graphics2D) gg.create();
        nG.translate(bx + boxW / 2, by + 140);
        nG.scale(scale, scale);
        nG.translate(-(bx + boxW / 2), -(by + 140));
        nG.setFont(Theme.bold(82));
        nG.setColor(new Color(18, 110, 70));
        nG.drawString(num, nx, ny);
        nG.dispose();

        gg.setFont(Theme.regular(16));
        gg.setColor(Theme.MUTED);
        String sub = "Começando em instantes...";
        FontMetrics fm3 = gg.getFontMetrics();
        int sx = bx + (boxW - fm3.stringWidth(sub)) / 2;
        gg.drawString(sub, sx, by + 205);

        gg.dispose();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();

        if (btnRestart.contains(p)) { startNewGame(); return; }

        if (btnBack.contains(p)) {
            countdownTimer.stop();
            animTimer.stop();
            onBackToStart.run();
            return;
        }

        if (gameOver) {
            Rectangle r1 = (Rectangle) getClientProperty("GO_RESTART");
            Rectangle r2 = (Rectangle) getClientProperty("GO_BACK");
            if (r1 != null && r1.contains(p)) { startNewGame(); return; }
            if (r2 != null && r2.contains(p)) {
                countdownTimer.stop();
                animTimer.stop();
                onBackToStart.run();
                return;
            }
            return;
        }

        if (preGame) return;

        for (int i = items.size() - 1; i >= 0; i--) {
            WasteItem it = items.get(i);
            if (it.bounds().contains(p)) {
                dragging = it;
                dragOffsetX = p.x - it.x;
                dragOffsetY = p.y - it.y;
                return;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging == null) return;

        int nx = e.getX() - dragOffsetX;
        int ny = e.getY() - dragOffsetY;

        nx = Math.max(18, Math.min(nx, W - dragging.w - 18));
        ny = Math.max(HEADER_H + 12, Math.min(ny, H - dragging.h - 18));

        dragging.setPosition(nx, ny);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging == null) return;

        handleDrop(dragging, e.getPoint());
        dragging = null;
        repaint();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
