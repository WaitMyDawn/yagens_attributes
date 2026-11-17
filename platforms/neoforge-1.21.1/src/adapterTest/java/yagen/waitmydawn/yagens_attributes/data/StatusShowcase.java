package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import yagen.waitmydawn.yagens_attributes.api.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.nio.file.*;
import java.util.*;

/** Opt-in integrated-world capture; no production command or release dependency uses this harness. */
@EventBusSubscriber(modid = "yagens_attributes", value = Dist.CLIENT)
public final class StatusShowcase {
    private static final String[] NAMES = {"slash", "electricity", "magnetic", "blast"};
    private static final double[] X = {-9, -3, 3, 9};
    private static final List<Mob> ACTORS = new ArrayList<>();
    private static final JsonArray FRAMES = new JsonArray();
    private static Path output;
    private static boolean started, finished, prepared;
    private static volatile int requested = -1, serverScene = -1;
    private static int active = -1, waited, frame, phase;
    private static long lastFrame;
    private StatusShowcase() {}
    @SubscribeEvent public static void client(ClientTickEvent.Post event) throws Exception {
        String destination = System.getProperty("yagens_attributes.statusShowcase");
        if (destination == null || finished) return;
        var client = Minecraft.getInstance();
        if (!started && client.screen instanceof TitleScreen && client.getOverlay() == null) {
            started = true; output = Path.of(destination); Files.createDirectories(output);
            Files.writeString(output.resolve("client.pid"), Long.toString(ProcessHandle.current().pid()));
            client.options.framerateLimit().set(20); client.options.renderDistance().set(4); client.options.simulationDistance().set(5);
            client.options.fov().set(55); client.options.setCameraType(CameraType.FIRST_PERSON);
            client.createWorldOpenFlows().createFreshLevel("status-showcase-" + System.currentTimeMillis(),
                    new LevelSettings("Status showcase", GameType.SPECTATOR, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                    new WorldOptions(77, false, false), registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), client.screen);
        }
        if (client.level == null || client.player == null || client.getSingleplayerServer() == null) return;
        if (requested < 0) requested = 0;
        if (active != requested && serverScene == requested) {
            if (++waited >= 30) { active = requested; frame = 0; waited = 0; lastFrame = 0; Files.createDirectories(output.resolve(NAMES[active])); }
        }
    }
    @SubscribeEvent public static void server(ServerTickEvent.Post event) {
        if (System.getProperty("yagens_attributes.statusShowcase") == null || requested < 0 || requested >= 4 || event.getServer().getPlayerList().getPlayers().isEmpty()) return;
        var level = event.getServer().overworld(); var player = event.getServer().getPlayerList().getPlayers().get(0);
        if (!prepared) {
            prepared = true;
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, event.getServer());
            level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, event.getServer());
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, event.getServer()); level.setDayTime(6000);
            for (int x = -14; x <= 14; x++) for (int z = -4; z <= 8; z++) level.setBlock(new BlockPos(x, 70, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            for (double x : X) ACTORS.add(actor(level, x, 0));
            ACTORS.add(actor(level, -4.3, -.4)); ACTORS.add(actor(level, -1.7, -.4));
        }
        if (serverScene != requested) {
            serverScene = requested; phase = 0;
            ACTORS.forEach(StatusApi::clearAll); player.setGameMode(GameType.SPECTATOR);
            player.connection.teleport(X[serverScene], 72.3, 6.5, 180, 16);
        }
        var target = ACTORS.get(serverScene);
        switch (serverScene) {
            case 0 -> { if (phase % 40 == 0) apply(target, player, yagen.waitmydawn.yagens_attributes.core.DamageType.SLASH); }
            case 1 -> { if (phase % 100 == 20) apply(target, player, yagen.waitmydawn.yagens_attributes.core.DamageType.ELECTRICITY); }
            case 2 -> {
                if (phase == 0) { for (int i = 0; i < 6; i++) apply(target, player, yagen.waitmydawn.yagens_attributes.core.DamageType.MAGNETIC); target.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20); target.setAbsorptionAmount(20); }
                if (phase == 80) target.hurt(level.damageSources().playerAttack(player), 50);
            }
            case 3 -> {
                if (phase % 100 == 20) apply(target, player, yagen.waitmydawn.yagens_attributes.core.DamageType.BLAST);
                if (phase % 100 == 70) for (int i = 0; i < 10; i++) apply(target, player, yagen.waitmydawn.yagens_attributes.core.DamageType.BLAST);
            }
        }
        phase++;
    }
    private static Mob actor(ServerLevel level, double x, double z) {
        var mob = EntityType.HUSK.create(level); mob.setNoAi(true); mob.setPersistenceRequired();
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000); mob.setHealth(10000);
        mob.moveTo(x, 71, z, 0, 0); level.addFreshEntity(mob); return mob;
    }
    private static void apply(Mob target, ServerPlayer player, yagen.waitmydawn.yagens_attributes.core.DamageType type) {
        StatusApi.apply(target, type, player.getUUID(), 2, 120, 0);
    }
    @SubscribeEvent public static void capture(RenderLevelStageEvent event) throws Exception {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || active < 0 || active != requested || finished) return;
        var client = Minecraft.getInstance();
        if (client.screen != null || client.getOverlay() != null) return;
        long stamp = System.nanoTime();
        if (lastFrame != 0 && stamp - lastFrame < 40_000_000) return;
        String name = NAMES[active] + "/frame-" + String.format(Locale.ROOT, "%04d", frame) + ".png";
        try (var image = Screenshot.takeScreenshot(client.getMainRenderTarget())) { image.writeToFile(output.resolve(name)); }
        var metadata = new JsonObject(); metadata.addProperty("file", name); metadata.addProperty("duration_ms", lastFrame == 0 ? 50 : Math.max(10, Math.round((stamp - lastFrame) / 1_000_000D)));
        FRAMES.add(metadata); lastFrame = stamp;
        if (++frame >= 100) {
            YagensAttributes.LOGGER.info("Captured native status scene {}", NAMES[active]);
            if (active < 3) { requested++; active = -1; waited = 0; }
            else {
                finished = true;
                var report = new JsonObject(); report.addProperty("passed", true); report.add("frames", FRAMES);
                Files.writeString(output.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
                client.stop();
            }
        }
    }
}
