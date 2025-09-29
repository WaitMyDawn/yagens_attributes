package yagen.waitmydawn.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
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
import yagen.waitmydawn.network.AddBladeStormEffectPacket;
import yagen.waitmydawn.network.AddNourishEffectPacket;
import yagen.waitmydawn.network.SendBladeStormTargetPacket;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState COMBO_COUNT_STATE = register(KeyMappings.COMBO_COUNT_KEYMAP);
    private static final KeyState ABILITY_1_STATE = register(KeyMappings.ABILITY_1_KEYMAP);
    private static final KeyState ABILITY_2_STATE = register(KeyMappings.ABILITY_2_KEYMAP);

    private static final KeyState[] abilityStates = {
            ABILITY_1_STATE,
            ABILITY_2_STATE
    };

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
        String[] ability = new String[2];
        int abilityIndex = 0;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (IModContainer.isModContainer(chest)) {
            var container = IModContainer.get(chest);
            for (ModSlot slot : container.getActiveMods()) {
//                if (ability[0] == null) {
//                    ability[0] = whichAbility(slot.getMod().getModName());
//                    if (ability[0] != null) {
//                        isAbility = true;
//                    }
//                }
//                else if (ability[1] == null) {
//                    ability[1] = whichAbility(slot.getMod().getModName());
//                    if (ability[1] != null) {
//                        break;
//                    }
//                }
                if (abilityIndex >= ability.length) {
                    break;
                }

                String newAbility = whichAbility(slot.getMod().getModName());
                if (newAbility != null) {
                    ability[abilityIndex] = newAbility;
                    abilityIndex++;
                    isAbility = true;
                }
            }
        }

        abilityIndex = Math.min(ability.length, abilityIndex);

        for (int abilityStateIndex = 0;
             abilityStateIndex < abilityIndex;
             abilityStateIndex++) {
            boolean isBladeStormEffect = false;
            if (player.hasEffect(MobEffectRegistry.BLADE_STORM))
                isBladeStormEffect = true;
            if (abilityStates[abilityStateIndex].wasPressed()
                    && abilityCooldown[0] <= 0 && isAbility) {
                switch (ability[abilityStateIndex]) {
                    case "nourish_armor_mod": {
                        PacketDistributor.sendToServer(new AddNourishEffectPacket(BASIC_NOURISH_DURATION));
                        abilityCooldown[abilityStateIndex] = NOURISH_COOLDOWN;
                    }
                    case "blade_storm_armor_mod": {
                        if(!isBladeStormEffect){
                            PacketDistributor.sendToServer(new AddBladeStormEffectPacket(BASIC_BLADE_STORM_DURATION));
                            abilityCooldown[abilityStateIndex] = BLADE_STORM_COOLDOWN;
                        }
                    }
                }
            } else if (abilityStates[abilityStateIndex].wasPressed()
                    && abilityCooldown[abilityStateIndex] > 0
            && !isBladeStormEffect) {
                player.sendSystemMessage(
                        Component.translatable("overlay.yagens_attributes.ability_cooldown",
                                abilityStateIndex,
                                Component.translatable("mod.yagens_attributes." + ability[abilityStateIndex]),
                                abilityCooldown[abilityStateIndex] / 20));
            }
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
            "nourish_armor_mod",
            "blade_storm_armor_mod"
    );

    private static int[] abilityCooldown = {0, 0};

    private static final int BASIC_NOURISH_DURATION = 600;
    private static final int BASIC_BLADE_STORM_DURATION = 80;

    private static final int NOURISH_COOLDOWN = 900;
    private static final int BLADE_STORM_COOLDOWN = 85;

    private static void update() {
        for (KeyState k : KEY_STATES) {
            k.update();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (abilityCooldown[0] > 0) abilityCooldown[0]--;
        if (abilityCooldown[1] > 0) abilityCooldown[1]--;
    }

    private static final int MAX_BLADE_STORM_RANGE = 20;

    @SubscribeEvent
    public static void getBladeStormTargets(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled() || event.isAttack()) return;
        Minecraft instance = Minecraft.getInstance();
        if (instance.hitResult == null || instance.hitResult.getType() != HitResult.Type.ENTITY)
            return;

        Entity target = ((EntityHitResult) instance.hitResult).getEntity();
        if (!(target instanceof LivingEntity living) || living == instance.player) return;

        double dist = instance.player.distanceTo(target);
        if (dist > MAX_BLADE_STORM_RANGE) return;

        if (!instance.player.hasEffect(MobEffectRegistry.BLADE_STORM)) return;

        PacketDistributor.sendToServer(new SendBladeStormTargetPacket(target.getId()));
        event.setCanceled(true);
    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }
}