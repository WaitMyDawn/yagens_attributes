package yagen.waitmydawn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class LineFileUtil {
    private static final Map<Path, Set<String>> CACHE = new WeakHashMap<>();

    public static void appendLine(Path file, String line) throws IOException {
        Files.createDirectories(file.getParent());
        Set<String> set = CACHE.computeIfAbsent(file, k -> loadLines(k));
        synchronized (set) {
            if (set.add(line)) {
                rewrite(file, set);
            }
        }
    }

    private static Set<String> loadLines(Path file) {
        if (!Files.exists(file)) return new LinkedHashSet<>();
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Set<String> set = new LinkedHashSet<>();
            String l;
            while ((l = r.readLine()) != null) {
                l = l.strip();
                if (!l.isEmpty()) set.add(l);
            }
            return set;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void rewrite(Path file, Set<String> lines) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String l : lines) {
                w.write(l);
                w.newLine();
            }
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}