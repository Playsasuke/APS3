import javax.swing.*;
import java.awt.*;

public class AppFrame {

    public static void start() {
        SwingUtilities.invokeLater(() -> {
            Theme.init();

            JFrame frame = new JFrame("EcoMemory — Educação Ambiental");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            CardLayout layout = new CardLayout();
            JPanel root = new JPanel(layout);

            // Precisamos declarar antes para usar no lambda do GamePanel
            StartPanel[] startRef = new StartPanel[1];

            GamePanel gamePanel = new GamePanel(() -> {
                // quando voltar pro menu, atualiza histórico
                if (startRef[0] != null) startRef[0].refreshHistory();
                layout.show(root, "START");
            });

            StartPanel startPanel = new StartPanel(
                    () -> { // start game
                        gamePanel.startNewGame();
                        layout.show(root, "GAME");
                        gamePanel.requestFocusInWindow();
                    },
                    () -> { // go tutorial
                        // atualiza histórico antes de ir (opcional)
                        // startPanel.refreshHistory();  // não pode referenciar aqui ainda
                        layout.show(root, "TUTORIAL");
                    }
            );

            startRef[0] = startPanel;

            TutorialPanel tutorialPanel = new TutorialPanel(() -> {
                // ao voltar do tutorial pro menu, atualiza histórico também
                startPanel.refreshHistory();
                layout.show(root, "START");
            });

            root.add(startPanel, "START");
            root.add(gamePanel, "GAME");
            root.add(tutorialPanel, "TUTORIAL");

            frame.setContentPane(root);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // garante histórico atualizado ao abrir
            startPanel.refreshHistory();
            layout.show(root, "START");
        });
    }
}
