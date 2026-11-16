package yagen.waitmydawn.yagens_attributes.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.network.*;

@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    public static final KeyMapping MOD_SPACE = new KeyMapping("key.yagens_attributes.mod_space", KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.yagens_attributes");
    public static final KeyMapping WEAPON_PANEL = new KeyMapping("key.yagens_attributes.weapon_panel", KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.yagens_attributes");
    private ClientEvents() {}

    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.register(MOD_SPACE); event.register(WEAPON_PANEL); }
    @SubscribeEvent public static void commands(RegisterClientCommandsEvent event) {
        ClientUiCommands.register(event.getDispatcher(), element -> Minecraft.getInstance().setScreen(new HudEditorScreen(element)));
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientPackets.mods(ClientMods::accept, ClientMods::catalog);
            ClientPackets.shield(ShieldHud::accept);
            ClientPackets.combo(ComboHud::accept);
            ClientPackets.visual(StatusVisuals::accept);
            ClientPackets.markers(yagen.waitmydawn.yagens_attributes.render.StatusMarkers::accept);
            ClientPackets.ranged(ClientRangedUse::accept, ClientRangedUse::start, ClientRangedUse::tick, ClientRangedUse::stop);
            ClientPackets.install(payload -> { ClientProfiles.accept(payload); if (payload.last()) WeaponInspectionCache.clear(); }, payload -> {
                WeaponInspectionCache.accept(payload);
                if (Minecraft.getInstance().screen instanceof ModPanelScreen screen) screen.accept(payload);
            });
            ClientPackets.inspection(WeaponInspectionCache::accept, payload -> {
                WeaponInspectionCache.accept(payload);
                if (Minecraft.getInstance().screen instanceof ModPanelScreen screen) screen.acceptProgress(payload);
            });
        });
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        ShieldHud.tick();
        ComboHud.tick();
        yagen.waitmydawn.yagens_attributes.render.StatusMarkers.tick();
        Minecraft minecraft = Minecraft.getInstance();
        while (MOD_SPACE.consumeClick()) if (minecraft.player != null && minecraft.screen == null) minecraft.setScreen(new ModPanelScreen(PanelRequest.STORAGE));
        while (WEAPON_PANEL.consumeClick()) if (minecraft.player != null && minecraft.screen == null) minecraft.setScreen(new ModPanelScreen(PanelRequest.WEAPON));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ClientProfiles.clear(); WeaponInspectionCache.clear(); ClientRangedUse.clear(); ShieldHud.clear(); ComboHud.clear(); ClientMods.clear(); yagen.waitmydawn.yagens_attributes.render.StatusMarkers.clear(); }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null || !event.getEntity().level().isClientSide()) return;
        var profile = ClientProfiles.get(event.getItemStack());
        if (profile == null) return;
        var lines = event.getToolTip();
        if (!Screen.hasShiftDown()) {
            lines.add(Component.translatable("tooltip.yagens_attributes.hold_shift").withStyle(ChatFormatting.DARK_GRAY)); return;
        }
        var inspection = WeaponInspectionCache.get(event.getItemStack());
        boolean preview = inspection == null && event.getItemStack().get(yagen.waitmydawn.yagens_attributes.registries.Components.WEAPON_ID.get()) == null && WeaponInspectionCache.menuSlot(event.getItemStack()) < 0;
        lines.addAll(WeaponTooltip.lines(profile, inspection, preview));
        lines.add(Component.translatable("tooltip.yagens_attributes.weapon_panel", WEAPON_PANEL.getTranslatedKeyMessage()).withStyle(ChatFormatting.DARK_GRAY));
    }
    @SubscribeEvent public static void renderedTooltip(net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents event) {
        if (Screen.hasShiftDown() && !event.getItemStack().isEmpty() && ClientProfiles.get(event.getItemStack()) != null)
            WeaponInspectionCache.request(event.getItemStack());
    }
}
