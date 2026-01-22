package yagen.waitmydawn.gui.overlays;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.config.ClientConfigs;

public class ReservoirsOverlay implements LayeredDraw.Layer {
    public static final ReservoirsOverlay instance = new ReservoirsOverlay();
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "textures/gui/overlays/reservoirs_overlay.png");
    private static final int WIDTH = 46;
    private static final int HEIGHT = 72;

    @Override
    public void render(GuiGraphics guiHelper, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.hideGui || player.isSpectator()) {
            return;
        }
        if (!shouldShowReservoirs(player))
            return;

        var screenWidth = guiHelper.guiWidth();
        var screenHeight = guiHelper.guiHeight();

        int barX = ClientConfigs.RESERVOIRS_X.get() == -1 ? (screenWidth - 50) : ClientConfigs.RESERVOIRS_X.get();
        int barY = ClientConfigs.RESERVOIRS_Y.get() == -1 ? (screenHeight - 48) : ClientConfigs.RESERVOIRS_Y.get();


        guiHelper.blit(TEXTURE, barX, barY,
                0, 24 * player.getPersistentData().getInt("reservoir_type"),
                WIDTH, HEIGHT / 3, WIDTH, HEIGHT);
    }

    public static boolean shouldShowReservoirs(Player player) {
        boolean isValidWarframe = ModCompat.isValidWarframeAbility(player.getItemBySlot(EquipmentSlot.CHEST));
        boolean hasReservoirs = ModCompat.hasReservoirs(player.getItemBySlot(EquipmentSlot.CHEST));
        return isValidWarframe && hasReservoirs;
    }
}
