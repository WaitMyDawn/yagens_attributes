package yagen.waitmydawn.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import yagen.waitmydawn.api.util.ModCompat;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.gui.ClientConfigsScreen;
import yagen.waitmydawn.network.*;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;
import yagen.waitmydawn.util.RayUtils;

import java.util.ArrayList;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientInputEvents {
    private static final ArrayList<KeyState> KEY_STATES = new ArrayList<>();

    private static final KeyState CONFIG_SCREEN_STATE = register(KeyMappings.CONFIG_SCREEN);
    private static final KeyState AIR_BRAKE_STATE = register(KeyMappings.AIR_BRAKE);
    private static final KeyState ABILITY_1_STATE = register(KeyMappings.ABILITY_1_KEYMAP);
    private static final KeyState ABILITY_2_STATE = register(KeyMappings.ABILITY_2_KEYMAP);
    private static final KeyState ABILITY_3_STATE = register(KeyMappings.ABILITY_3_KEYMAP);
    private static final KeyState ABILITY_4_STATE = register(KeyMappings.ABILITY_4_KEYMAP);

    private static final KeyState[] abilityStates = {
            ABILITY_1_STATE,
            ABILITY_2_STATE,
            ABILITY_3_STATE,
            ABILITY_4_STATE
    };

    public static boolean isUseKeyDown;
    public static boolean isShiftKeyDown;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleInputEvent(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event) {
        handleInputEvent(event.getButton(), event.getAction());
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

        if (CONFIG_SCREEN_STATE.wasPressed() && minecraft.screen == null) {
            minecraft.setScreen(new ClientConfigsScreen());
        }

        if (!player.isSpectator() && ClientConfigs.IF_AIR_BRAKE.get() && ServerConfigs.IF_AIR_BRAKE.get()) {
            if (!player.onGround() && AIR_BRAKE_STATE.wasPressed()) {
                PacketDistributor.sendToServer(new AirBrakePacket(player.getUUID(), true));
                player.getPersistentData().putBoolean("AirBrake", true);
            }
            if (!player.onGround() && AIR_BRAKE_STATE.isHeld()) {
                PacketDistributor.sendToServer(new AirBrakePacket(player.getUUID(), true));
                player.getPersistentData().putBoolean("AirBrake", true);
            }
            if (AIR_BRAKE_STATE.wasReleased()) {
                PacketDistributor.sendToServer(new AirBrakePacket(player.getUUID(), false));
                player.getPersistentData().putBoolean("AirBrake", false);
            }
        }

        if (!player.isSpectator() && ClientConfigs.IF_BULLET_JUMP.get() && ServerConfigs.IF_BULLET_JUMP.get()
                && player.onGround() && isShiftKeyDown && minecraft.options.keyJump.isDown()) {
            PacketDistributor.sendToServer(new BulletJumpPacket(player.getUUID(), true));
            player.getPersistentData().putBoolean("BulletJump", true);
        }

        boolean isAbility = false;
        String[] ability = new String[4];
        double[] abilityCost = new double[4];
        int abilityIndex = 0;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (IModContainer.isModContainer(chest)) {
            var container = IModContainer.get(chest);
            for (ModSlot slot : container.getActiveMods()) {
                if (abilityIndex >= ability.length) {
                    break;
                }
                if (slot.getMod().isActive()) {
                    ability[abilityIndex] = slot.getMod().getModName();
                    abilityCost[abilityIndex] = slot.getMod().energyCost();
                    abilityIndex++;
                    isAbility = true;
                }
            }
        }

        abilityIndex = Math.min(ability.length, abilityIndex);
        double energy = DataAttachmentRegistry.getEnergy(player);
        for (int abilityStateIndex = 0;
             abilityStateIndex < abilityIndex;
             abilityStateIndex++) {
            boolean isBladeStormEffect = false;
            if (player.hasEffect(MobEffectRegistry.BLADE_STORM) && ability[abilityStateIndex].equals("blade_storm_armor_mod"))
                isBladeStormEffect = true;
            if (abilityStates[abilityStateIndex].wasPressed() && !ModCompat.isValidWarframeAbility(chest)) {
                player.sendSystemMessage(Component.translatable("overlay.yagens_attributes.not_valid_ability"));
            } else if (abilityStates[abilityStateIndex].wasPressed()
                    && abilityCost[abilityStateIndex] <= energy && isAbility) {
                switch (ability[abilityStateIndex]) {
                    case "nourish_armor_mod": {
                        PacketDistributor.sendToServer(new AddNourishEffectPacket());
                        PacketDistributor.sendToServer(new EnergyPacket(-(abilityCost[abilityStateIndex])));
                        break;
                    }
                    case "blade_storm_armor_mod": {
                        if (!isBladeStormEffect) {
                            PacketDistributor.sendToServer(new AddBladeStormEffectPacket());
                            PacketDistributor.sendToServer(new EnergyPacket(-(abilityCost[abilityStateIndex])));
                        }
                        break;
                    }
                }
            } else if (abilityStates[abilityStateIndex].wasReleased() // for abilities more than one type
                    && abilityCost[abilityStateIndex] <= energy
                    && isAbility) {
                switch (ability[abilityStateIndex]) {
                    case "reservoirs_armor_mod": {
                        if (abilityStates[abilityStateIndex].heldTicks() < 4) {
                            PacketDistributor.sendToServer(
                                    new CreateReservoirPacket(
                                            player.getPersistentData().getInt("reservoir_type")));
                            PacketDistributor.sendToServer(new EnergyPacket(-(abilityCost[abilityStateIndex])));
                        }
                        break;
                    }
                }
            } else if (abilityStates[abilityStateIndex].isHeld()) {// change ability type
                if (abilityStates[abilityStateIndex].heldTicks() == 4) {
                    switch (ability[abilityStateIndex]) {
                        case "reservoirs_armor_mod": {
                            int oldType = player.getPersistentData().getInt("reservoir_type");
                            if (oldType >= 2) player.getPersistentData().putInt("reservoir_type", 0);
                            else player.getPersistentData().putInt("reservoir_type", oldType + 1);
                            break;
                        }
                    }
                }
            } else if (abilityStates[abilityStateIndex].wasPressed()) {
                if (abilityCost[abilityStateIndex] > energy) {
                    if (isBladeStormEffect) {// execute in advance
                        PacketDistributor.sendToServer(ExecuteBladeStormPacket.INSTANCE);
                    } else {
                        player.sendSystemMessage(
                                Component.translatable("overlay.yagens_attributes.ability_cost",
                                        abilityStateIndex + 1,
                                        Component.translatable("mod.yagens_attributes." + ability[abilityStateIndex]),
                                        abilityCost[abilityStateIndex]));
                    }
                } else {
                    if (isBladeStormEffect) {// execute in advance
                        PacketDistributor.sendToServer(ExecuteBladeStormPacket.INSTANCE);
                    }
                }
            }
        }

        update();
    }

    private static void update() {
        for (KeyState k : KEY_STATES) {
            k.update();
        }
    }

    private static final int MAX_BLADE_STORM_RANGE = 20;

    @SubscribeEvent
    public static void getBladeStormTargets(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled() || event.isAttack()) return;
        Minecraft instance = Minecraft.getInstance();
        Player player = instance.player;

        if (!player.hasEffect(MobEffectRegistry.BLADE_STORM)) return;

        LivingEntity target = RayUtils.getTargetedLiving(player, MAX_BLADE_STORM_RANGE);

        if (target == null || target == player) return;

        if (player.distanceTo(target) > MAX_BLADE_STORM_RANGE) return;

        PacketDistributor.sendToServer(new SendBladeStormTargetPacket(target.getId()));
        event.setCanceled(true);
    }

    private static KeyState register(KeyMapping key) {
        var k = new KeyState(key);
        KEY_STATES.add(k);
        return k;
    }
}