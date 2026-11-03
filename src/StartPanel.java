import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StartPanel extends JPanel {

    // DICAS (agora multiline, não corta mais)
    private final JTextArea tipArea = new JTextArea();
    private final TipManager tipManager;
    private final Timer tipTimer;

    // HISTÓRICO
    private final JTextArea historyArea = new JTextArea();
    private final JScrollPane historyScroll;

    public StartPanel(Runnable onStart, Runnable onTutorial) {
        setPreferredSize(new Dimension(1280, 720));
        setBackground(Theme.BG);
        setLayout(null);

        // Garante que o histórico esteja carregado
        GameHistory.loadOnce();

        JLabel title = new JLabel("EcoMemory");
        title.setFont(Theme.bold(56));
        title.setForeground(Theme.TEXT);
        title.setBounds(90, 70, 900, 70);
        add(title);

        JLabel subtitle = new JLabel("Educação Ambiental em contexto de cidade (coleta seletiva + descarte especial)");
        subtitle.setFont(Theme.regular(18));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setBounds(90, 140, 1000, 28);
        add(subtitle);

        JTextArea rules = new JTextArea(
                "Como jogar:\n" +
                "• Arraste os itens para a lixeira correta.\n" +
                "• Acertou: +1 ponto.\n" +
                "• Errou: -1 ponto.\n" +
                "• Tempo: 60 segundos.\n" +
                "• Dê o seu melhor para ajudar a salvar o mundo!"
        );
        rules.setEditable(false);
        rules.setOpaque(false);
        rules.setBorder(null);
        rules.setFont(Theme.regular(18));
        rules.setForeground(Theme.TEXT);
        rules.setBounds(90, 220, 560, 220);
        add(rules);

        // -------------------------
        // Dicas (menu) - MULTILINE
        // -------------------------
        JLabel dicaTitle = new JLabel("Dicas (Para usar na sua vida!):");
        dicaTitle.setFont(Theme.semibold(16));
        dicaTitle.setForeground(Theme.TEXT);
        dicaTitle.setBounds(90, 450, 560, 24);
        add(dicaTitle);

        tipArea.setEditable(false);
        tipArea.setOpaque(false);
        tipArea.setBorder(null);
        tipArea.setLineWrap(true);
        tipArea.setWrapStyleWord(true);
        tipArea.setFont(Theme.regular(16));
        tipArea.setForeground(Theme.MUTED);
        // Aumentei altura para permitir 2-3 linhas
        tipArea.setBounds(90, 480, 560, 70);
        add(tipArea);

        tipManager = new TipManager(List.of(
                "Lave embalagens antes de reciclar quando estiverem muito sujas.",
                "Óleo de cozinha usado nunca deve ir na pia: leve a pontos de coleta.",
                "Pilhas e eletrônicos devem ir para descarte especial (ecopontos).",
                "Vidro quebrado deve ser embalado com segurança antes do descarte.",
                "Amassar latas e garrafas ajuda a economizar espaço na coleta.",
                "Separar orgânicos permite compostagem e reduz lixo no aterro.",
                "Reduzir e reutilizar também é sustentabilidade: reciclar é a terceira etapa.",
                "Evite misturar papel com comida/óleo — isso pode impedir a reciclagem."
        ), 5);

        tipArea.setText("• " + tipManager.nextTip());
        tipTimer = new Timer(5000, e -> tipArea.setText("• " + tipManager.nextTip()));
        tipTimer.start();

        // -------------------------
        // Botões
        // -------------------------
        JButton start = Theme.button("Começar");
        start.setBounds(90, 560, 220, 52);
        start.addActionListener(e -> {
            tipTimer.stop();
            onStart.run();
        });
        add(start);

        JButton tutorial = Theme.outlineButton("Tutorial");
        tutorial.setBounds(325, 560, 160, 52);
        tutorial.addActionListener(e -> onTutorial.run());
        add(tutorial);

        JButton exit = Theme.outlineButton("Sair");
        exit.setBounds(500, 560, 140, 52);
        exit.addActionListener(e -> System.exit(0));
        add(exit);

        // -------------------------
        // Histórico (últimos 10 jogos)
        // -------------------------
        JLabel histTitle = new JLabel("Histórico (últimos 10 jogos):");
        histTitle.setFont(Theme.semibold(16));
        histTitle.setForeground(Theme.TEXT);
        histTitle.setBounds(720, 220, 500, 24);
        add(histTitle);

        historyArea.setEditable(false);
        historyArea.setOpaque(false);
        historyArea.setBorder(null);
        historyArea.setFont(Theme.regular(14));
        historyArea.setForeground(Theme.TEXT);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);

        historyScroll = new JScrollPane(historyArea);
        historyScroll.setBounds(720, 250, 470, 330);
        historyScroll.setOpaque(false);
        historyScroll.getViewport().setOpaque(false);
        historyScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 2, true));
        historyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(historyScroll);

        refreshHistory();

        JLabel footer = new JLabel("Feito em Java (Swing)");
        footer.setFont(Theme.regular(14));
        footer.setForeground(Theme.MUTED);
        footer.setBounds(90, 645, 600, 24);
        add(footer);
    }

    public void refreshHistory() {
        GameHistory.loadOnce();
        var list = GameHistory.getLast10NewestFirst();

        if (list.isEmpty()) {
            historyArea.setText("Nenhum jogo finalizado ainda.\nJogue uma partida completa para aparecer aqui.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (var r : list) {
            sb.append(GameHistory.formatResultLine(i, r)).append("\n");
            i++;
        }
        historyArea.setText(sb.toString());
        historyArea.setCaretPosition(0);
    }
}
