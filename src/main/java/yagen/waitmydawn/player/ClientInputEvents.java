package yagen.waitmydawn.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.gui.ComboPositionScreen;
import yagen.waitmydawn.network.AddNourishEffectPacket;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState COMBO_COUNT_STATE = register(KeyMappings.COMBO_COUNT_KEYMAP);
    private static final KeyState ABILITY_STATE = register(KeyMappings.ABILITY_KEYMAP);

    private static int useKeyId = Integer.MIN_VALUE;
    public static boolean isUseKeyDown;
    public static boolean isShiftKeyDown;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleInputEvent(event.getKey(), event.getAction());
    }

    private static void handleInputEvent(int button, int action) {
        var minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        if (button == InputConstants.KEY_LSHIFT) {
            isShiftKeyDown = action >= InputConstants.PRESS;
        }

        if (COMBO_COUNT_STATE.wasPressed() && minecraft.screen == null) {
            minecraft.setScreen(new ComboPositionScreen());
        }

        boolean isAbility = false;
        String ability = null;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (IModContainer.isModContainer(chest)) {
            var container = IModContainer.get(chest);
            for (ModSlot slot : container.getActiveMods()) {
                ability = whichAbility(slot.getMod().getModName());
                if (ability != null) {
                    isAbility = true;
                    break;
                }
            }
        }
        if (ABILITY_STATE.wasPressed() && abilityCooldown <= 0 && isAbility) {
            PacketDistributor.sendToServer(new AddNourishEffectPacket());
            switch (ability){
                case "nourish_armor_mod": abilityCooldown = NOURISH_COOLDOWN;
            }
        } else if (ABILITY_STATE.wasPressed()) {
            Minecraft.getInstance().gui.setOverlayMessage(
                    Component.translatable("overlay.yagens_attributes.ability_cooldown", abilityCooldown / 20), false);
        }
        update();
    }

    public static String whichAbility(String modName) {
        for (String ability : ABILITIES) {
            if (ability.equals(modName)) {
                return ability;
            }
        }
        return null;
    }

    public static final List<String> ABILITIES = List.of(
            "nourish_armor_mod"
    );

    private static int abilityCooldown = 0;
    private static final int NOURISH_COOLDOWN = 900;

    private static void update() {
        for (KeyState k : KEY_STATES) {
            k.update();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (abilityCooldown > 0) abilityCooldown--;
    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }
}