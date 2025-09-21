package yagen.waitmydawn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Path;

public class ComboOverlayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            Minecraft.getInstance().gameDirectory.toPath().resolve("config/yagens_combo.json");

    public int x = -1;
    public int y = -1;

    public static ComboOverlayConfig load() {
        if (!PATH.toFile().exists()) return new ComboOverlayConfig();
        try (Reader r = new FileReader(PATH.toFile())) {
            return GSON.fromJson(r, ComboOverlayConfig.class);
        } catch (IOException e) {
            return new ComboOverlayConfig();
        }
    }

    public void save() {
        PATH.toFile().getParentFile().mkdirs();
        try (Writer w = new FileWriter(PATH.toFile())) {
            GSON.toJson(this, w);
        } catch (IOException ignored) {
            System.out.println("Mod operation screen render error:" + ignored);
        }
    }
}