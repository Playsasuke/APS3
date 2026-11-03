import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameHistory {

    public static class GameResult {
        public final long epochMillis;
        public final int score;
        public final int correctCount;

        public GameResult(long epochMillis, int score, int correctCount) {
            this.epochMillis = epochMillis;
            this.score = score;
            this.correctCount = correctCount;
        }
    }

    private static final String FILE = "history.csv";
    private static final Deque<GameResult> history = new ArrayDeque<>();
    private static boolean loaded = false;

    public static void loadOnce() {
        if (loaded) return;
        loaded = true;

        File f = new File(FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                long epoch = Long.parseLong(parts[0].trim());
                int score = Integer.parseInt(parts[1].trim());
                int correct = Integer.parseInt(parts[2].trim());

                history.addLast(new GameResult(epoch, score, correct));
            }
        } catch (Exception ignored) {
            // se der erro ao ler, só ignora e começa vazio
        }

        // garante no máximo 10
        while (history.size() > 10) history.removeFirst();
    }

    public static void addResult(int score, int correctCount) {
        loadOnce();

        history.addLast(new GameResult(System.currentTimeMillis(), score, correctCount));
        while (history.size() > 10) history.removeFirst();
        save();
    }

    public static Integer getLastScore() {
        loadOnce();
        if (history.isEmpty()) return null;
        return history.getLast().score;
    }

    public static List<GameResult> getLast10NewestFirst() {
        loadOnce();
        List<GameResult> list = new ArrayList<>(history);
        Collections.reverse(list); // mais recente primeiro
        return list;
    }

    public static String rankName(int correct) {
        if (correct >= 35) return "Guardião do Planeta :O";
        if (correct >= 20) return "Agente da Reciclagem :D";
        if (correct >= 10) return "Aprendiz Sustentável";
        if (correct >= 5)  return "Iniciante da Coleta ;)";
        return "Precisa praticar :|";
    }

    public static String formatResultLine(int index1Based, GameResult r) {
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(r.epochMillis), ZoneId.systemDefault());
        String when = dt.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        return String.format("%d) %s  |  %d pts  |  %d itens  |  %s",
                index1Based, when, r.score, r.correctCount, rankName(r.correctCount));
    }

    private static void save() {
        File f = new File(FILE);
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8))) {
            for (GameResult r : history) {
                bw.write(r.epochMillis + "," + r.score + "," + r.correctCount);
                bw.newLine();
            }
        } catch (Exception ignored) {}
    }
}
