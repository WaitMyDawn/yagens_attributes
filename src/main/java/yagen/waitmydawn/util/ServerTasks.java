package yagen.waitmydawn.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import java.util.ArrayList;
import java.util.List;

public final class ServerTasks {
    private static final List<Task> QUEUE = new ArrayList<>();

    private record Task(int dueTick, Runnable job) {}

    public static void runLater(ServerLevel level, int ticks, Runnable job) {
        QUEUE.add(new Task(level.getServer().getTickCount() + ticks, job));
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTickCount();
        QUEUE.removeIf(t -> {
            if (now >= t.dueTick) {
                t.job.run();
                return true;
            }
            return false;
        });
    }
}