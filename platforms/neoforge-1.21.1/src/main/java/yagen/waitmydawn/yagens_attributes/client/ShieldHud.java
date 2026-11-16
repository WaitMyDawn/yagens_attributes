package yagen.waitmydawn.yagens_attributes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.network.ShieldPayload;
import yagen.waitmydawn.yagens_attributes.setup.ShieldService;
import yagen.waitmydawn.yagens_attributes.core.ui.ShieldPainter;

/** Code-drawn pixels keep the shield overlay aligned with vanilla nine-pixel heart cells. */
public final class ShieldHud {
    private static int gate;
    private ShieldHud() {}
    public static void accept(ShieldPayload payload) { gate = payload.gateTicks(); }
    public static void tick() { if (gate > 0) gate--; }
    public static void clear() { gate = 0; }
    public static boolean locked() { return gate > 0 && ClientConfigs.SHOW_SHIELD_GATE_LOCK.get(); }
    public static boolean shield(GuiGraphics graphics, int x, int y, boolean half, boolean highlight) {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        boolean overload = overloaded(player);
        if (vanillaColor(overload)) return false;
        ShieldPainter.heart(graphics::fill, x, y, half, highlight, overload ? ShieldPainter.Style.OVERLOAD : ShieldPainter.Style.SHIELD);
        return true;
    }
    public static int amountColor() {
        var player = Minecraft.getInstance().player;
        boolean overload = player != null && overloaded(player);
        return vanillaColor(overload) ? 0xfff7d14b : overload ? 0xffa478ea : 0xff86cafa;
    }
    private static boolean overloaded(Player player) {
        double natural = ShieldService.natural(player);
        return natural > 0 && player.getAbsorptionAmount() > natural;
    }
    private static boolean vanillaColor(boolean overload) {
        return !ClientConfigs.SHIELD_HEARTS.get()
                || (overload ? ClientConfigs.RESTORE_OVERSHIELD_COLOR.get() : ClientConfigs.RESTORE_NORMAL_SHIELD_COLOR.get());
    }
    public static void lock(GuiGraphics graphics, int x, int y) {
        ShieldPainter.lock(graphics::fill, x, y);
    }
}
