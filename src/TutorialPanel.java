import javax.swing.*;
import java.awt.*;

public class TutorialPanel extends JPanel {

    public TutorialPanel(Runnable onBack) {
        setPreferredSize(new Dimension(1280, 720));
        setBackground(Theme.BG);
        setLayout(null);

        JLabel title = new JLabel("Tutorial — EcoMemory");
        title.setFont(Theme.bold(40));
        title.setForeground(Theme.TEXT);
        title.setBounds(90, 60, 900, 50);
        add(title);

        JLabel subtitle = new JLabel("Aprenda a jogar em poucos passos");
        subtitle.setFont(Theme.regular(18));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setBounds(90, 112, 900, 26);
        add(subtitle);

        JTextArea text = new JTextArea(
                "Objetivo do jogo\n" +
                "• Classificar corretamente os itens, arrastando cada um para a lixeira certa.\n" +
                "• O jogo dura 60 segundos. Quanto mais acertos, melhor sua pontuação.\n\n" +
                "Como jogar\n" +
                "1) Clique e arraste um item (cartão branco) que aparece na parte superior.\n" +
                "2) Leve o item até a lixeira correspondente (as lixeiras coloridas).\n" +
                "3) Solte o item em cima da lixeira.\n\n" +
                "Pontuação\n" +
                "• Acerto: +1 ponto e aumenta a sequência.\n" +
                "• Erro: -1 ponto e zera a sequência.\n\n"
        );
        text.setEditable(false);
        text.setOpaque(false);
        text.setBorder(null);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(Theme.regular(18));
        text.setForeground(Theme.TEXT);
        text.setBounds(90, 170, 1100, 430);
        add(text);

        JButton back = Theme.button("Voltar");
        back.setBounds(90, 620, 160, 52);
        back.addActionListener(e -> onBack.run());
        add(back);
    }
}
