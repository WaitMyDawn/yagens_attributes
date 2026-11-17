package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.core.ui.HudLayout;

public enum HudElement {
    COMBO("combo", 116, 31, ClientConfigs.COMBO_HUD_X, ClientConfigs.COMBO_HUD_Y, ClientConfigs.COMBO_HUD_SCALE, ClientConfigs.COMBO_HUD),
    BUFFS("buffs", 144, 44, ClientConfigs.BUFF_HUD_X, ClientConfigs.BUFF_HUD_Y, ClientConfigs.BUFF_HUD_SCALE, ClientConfigs.BUFF_HUD);

    public record Settings(int offsetX, int offsetY, double scale, boolean visible, boolean countdown) {
        public Settings(int offsetX, int offsetY, double scale, boolean visible) { this(offsetX, offsetY, scale, visible, true); }
    }
    private final String id;
    private final int width, height;
    private final ModConfigSpec.IntValue x, y;
    private final ModConfigSpec.DoubleValue scale;
    private final ModConfigSpec.BooleanValue visible;

    HudElement(String id, int width, int height, ModConfigSpec.IntValue x, ModConfigSpec.IntValue y,
               ModConfigSpec.DoubleValue scale, ModConfigSpec.BooleanValue visible) {
        this.id = id; this.width = width; this.height = height; this.x = x; this.y = y; this.scale = scale; this.visible = visible;
    }
    public String id() { return id; }
    public Component title() { return Component.translatable("gui.yagens_attributes.hud_" + id); }
    public Settings settings() { return new Settings(x.get(), y.get(), scale.get(), visible.get(), this != BUFFS || ClientConfigs.BUFF_HUD_SECONDS.get()); }
    public Settings defaults() { return new Settings(x.getDefault(), y.getDefault(), scale.getDefault(), visible.getDefault(), this != BUFFS || ClientConfigs.BUFF_HUD_SECONDS.getDefault()); }
    public HudLayout.Placement place(int viewportWidth, int viewportHeight, Settings settings) {
        return HudLayout.place(viewportWidth, viewportHeight, this == BUFFS ? WeaponBuffHud.logicalWidth() : width,
                this == BUFFS ? WeaponBuffHud.logicalHeight(settings.countdown()) : height, settings.scale(), settings.offsetX(), settings.offsetY());
    }
    public void preview(GuiGraphics graphics) {
        preview(graphics, settings());
    }
    public void preview(GuiGraphics graphics, Settings settings) {
        switch (this) { case COMBO -> ComboHud.draw(graphics, 140, 8, .7F); case BUFFS -> WeaponBuffHud.preview(graphics, settings.countdown()); }
    }
    public void save(Settings settings) {
        if (settings.offsetX() < 0 || settings.offsetX() > HudLayout.MAX_OFFSET || settings.offsetY() < 0 || settings.offsetY() > HudLayout.MAX_OFFSET
                || !Double.isFinite(settings.scale()) || settings.scale() < HudLayout.MIN_SCALE || settings.scale() > HudLayout.MAX_SCALE)
            throw new IllegalArgumentException("Invalid HUD settings");
        var previous = settings();
        try { write(settings); ClientConfigs.SPEC.save(); }
        catch (RuntimeException exception) { write(previous); throw exception; }
    }
    private void write(Settings settings) {
        x.set(settings.offsetX()); y.set(settings.offsetY()); scale.set(settings.scale()); visible.set(settings.visible());
        if (this == BUFFS) ClientConfigs.BUFF_HUD_SECONDS.set(settings.countdown());
    }
}
