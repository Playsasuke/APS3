import java.util.*;

public class TipManager {
    private final List<String> tips;
    private final Deque<Integer> recent = new ArrayDeque<>();
    private final Random rng = new Random();
    private final int noRepeatWindow;

    public TipManager(List<String> tips, int noRepeatWindow) {
        this.tips = new ArrayList<>(tips);
        this.noRepeatWindow = Math.max(1, noRepeatWindow);
    }

    public String nextTip() {
        if (tips.isEmpty()) return "";

        // tenta algumas vezes evitar repetição recente
        for (int tries = 0; tries < 12; tries++) {
            int idx = rng.nextInt(tips.size());
            if (!recent.contains(idx)) {
                pushRecent(idx);
                return tips.get(idx);
            }
        }

        // fallback: pega qualquer um, mas ainda controla histórico
        int idx = rng.nextInt(tips.size());
        pushRecent(idx);
        return tips.get(idx);
    }

    private void pushRecent(int idx) {
        recent.addLast(idx);
        while (recent.size() > noRepeatWindow) recent.removeFirst();
    }
}
