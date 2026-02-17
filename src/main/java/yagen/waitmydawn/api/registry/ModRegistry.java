package yagen.waitmydawn.api.registry;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import yagen.waitmydawn.api.mods.NoneMod;
import yagen.waitmydawn.api.mods.RivenMod;
import yagen.waitmydawn.item.mod.armor_mod.*;
import yagen.waitmydawn.item.mod.tool_mod.*;

import java.util.List;
import java.util.function.Supplier;

public class ModRegistry {
    public static final ResourceKey<Registry<AbstractMod>> MOD_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mods"));
    private static final DeferredRegister<AbstractMod> MODS = DeferredRegister.create(MOD_REGISTRY_KEY, YagensAttributes.MODID);

    public static final Registry<AbstractMod> REGISTRY = new RegistryBuilder<>(MOD_REGISTRY_KEY).create();

    private static final NoneMod noneMod = new NoneMod();

    public static NoneMod none() {
        return noneMod;
    }

    public static void register(IEventBus eventBus) {
        MODS.register(eventBus);
    }

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    private static Supplier<AbstractMod> registerMod(AbstractMod mod) {
        return MODS.register(mod.getModName(), () -> mod);
    }

    public static List<AbstractMod> getEnabledMods() {
        return ModRegistry.REGISTRY
                .stream()
                .toList();
    }

    public static AbstractMod getMod(String modId) {
        return getMod(ResourceLocation.parse(modId));
    }

    public static AbstractMod getMod(ResourceLocation resourceLocation) {
        var mod = REGISTRY.get(resourceLocation);
        if (mod == null) {
            return noneMod;
        }
        return mod;
//        return REGISTRY.get(resourceLocation);
    }

    /**
     * COMMON
     */
    public static final Supplier<AbstractMod> REACH_TOOL_MOD = registerMod(new ReachToolMod());
    public static final Supplier<AbstractMod> STATUS_DURATION_TOOL_MOD = registerMod(new StatusDurationToolMod());
    public static final Supplier<AbstractMod> STATUS_CHANCE_TOOL_MOD = registerMod(new StatusChanceToolMod());

    public static final Supplier<AbstractMod> HEALTH_ARMOR_MOD = registerMod(new HealthArmorMod());
    public static final Supplier<AbstractMod> ARMOR_ARMOR_MOD = registerMod(new ArmorArmorMod());
    public static final Supplier<AbstractMod> ARMOR_TOUGHNESS_ARMOR_MOD = registerMod(new ArmorToughnessArmorMod());
    public static final Supplier<AbstractMod> PILOT_ARMOR_MOD = registerMod(new PilotArmorMod());

    /**
     * UNCOMMON
     */
    public static final Supplier<AbstractMod> HEAT_TOOL_MOD = registerMod(new HeatToolMod());
    public static final Supplier<AbstractMod> COLD_TOOL_MOD = registerMod(new ColdToolMod());
    public static final Supplier<AbstractMod> ELECTRICITY_TOOL_MOD = registerMod(new ElectricityToolMod());
    public static final Supplier<AbstractMod> TOXIN_TOOL_MOD = registerMod(new ToxinToolMod());

    public static final Supplier<AbstractMod> FURY_TOOL_MOD = registerMod(new FuryToolMod());
    public static final Supplier<AbstractMod> CRITICAL_HIT_TOOL_MOD = registerMod(new CriticalHitToolMod());
    public static final Supplier<AbstractMod> CRITICAL_CHANCE_COMBO_TOOL_MOD = registerMod(new CriticalChanceComboToolMod());
    public static final Supplier<AbstractMod> STATUS_CHANCE_COMBO_TOOL_MOD = registerMod(new StatusChanceComboToolMod());
    public static final Supplier<AbstractMod> MOVEMENT_SPEED_ARMOR_MOD = registerMod(new MovementSpeedArmorMod());

    /**
     * RARE
     */
    public static final Supplier<AbstractMod> SLASH_TOOL_MOD = registerMod(new SlashToolMod());
    public static final Supplier<AbstractMod> IMPACT_TOOL_MOD = registerMod(new ImpactToolMod());
    public static final Supplier<AbstractMod> PUNCTURE_TOOL_MOD = registerMod(new PunctureToolMod());

    public static final Supplier<AbstractMod> HEAT_STATUS_TOOL_MOD = registerMod(new HeatStatusToolMod());
    public static final Supplier<AbstractMod> COLD_STATUS_TOOL_MOD = registerMod(new ColdStatusToolMod());
    public static final Supplier<AbstractMod> TOXIN_STATUS_TOOL_MOD = registerMod(new ToxinStatusToolMod());
    public static final Supplier<AbstractMod> ELECTRICITY_STATUS_TOOL_MOD = registerMod(new ElectricityStatusToolMod());
    public static final Supplier<AbstractMod> MULTISHOT_TOOL_MOD = registerMod(new MultishotToolMod());
    public static final Supplier<AbstractMod> CRITICAL_RARE_TOOL_MOD = registerMod(new CriticalRareToolMod());
    public static final Supplier<AbstractMod> SCATTERSHOT_TOOL_MOD = registerMod(new ScattershotToolMod());
    public static final Supplier<AbstractMod> COMBO_DURATION_TOOL_MOD = registerMod(new ComboDurationToolMod());
    public static final Supplier<AbstractMod> FIRERATE_TOOL_MOD = registerMod(new FirerateToolMod());
    public static final Supplier<AbstractMod> SCOPE_TOOL_MOD = registerMod(new ScopeToolMod());
    public static final Supplier<AbstractMod> FURY_KILL_TOOL_MOD = registerMod(new FuryKillToolMod());
    public static final Supplier<AbstractMod> JUMP_STRENGTH_ARMOR_MOD = registerMod(new JumpStrengthArmorMod());
    public static final Supplier<AbstractMod> XP_ARMOR_MOD = registerMod(new XpArmorMod());
    public static final Supplier<AbstractMod> GRACE_ARMOR_MOD = registerMod(new GraceArmorMod());
    public static final Supplier<AbstractMod> HEALTH_TRANS_ARMOR_MOD = registerMod(new HealthTransArmorMod());
    public static final Supplier<AbstractMod> FLOW_ARMOR_MOD = registerMod(new FlowArmorMod());
    public static final Supplier<AbstractMod> EFFICIENCY_ARMOR_MOD = registerMod(new EfficiencyArmorMod());

    /**
     * LEGENDARY
     */
    public static final Supplier<AbstractMod> SLASH_UP_TOOL_MOD = registerMod(new SlashUpToolMod());
    public static final Supplier<AbstractMod> MULTISHOT_GALVANIZED_TOOL_MOD = registerMod(new MultishotGalvanizedToolMod());
    public static final Supplier<AbstractMod> SCOPE_GALVANIZED_TOOL_MOD = registerMod(new ScopeGalvanizedToolMod());
    public static final Supplier<AbstractMod> HEALTH_HEAL_TOOL_MOD = registerMod(new HealthHealToolMod());
    public static final Supplier<AbstractMod> STATUS_OVERLOAD_TOOL_MOD = registerMod(new StatusOverloadToolMod());

    /**
     * RIVEN
     */
    public static final Supplier<AbstractMod> RIVEN_MOD = registerMod(new RivenMod());

    /**
     * WARFRAME
     */
    public static final Supplier<AbstractMod> NOURISH_ARMOR_MOD = registerMod(new NourishArmorMod());
    public static final Supplier<AbstractMod> COLLABORATIVE_PROFICIENCY_ARMOR_MOD = registerMod(new CollaborativeProficiencyArmorMod());
    public static final Supplier<AbstractMod> BLADE_STORM_ARMOR_MOD = registerMod(new BladeStormArmorMod());
    public static final Supplier<AbstractMod> THERMAL_SUNDER_ARMOR_MOD = registerMod(new ThermalSunderArmorMod());
    public static final Supplier<AbstractMod> PRE_SHOOT_TOOL_MOD = registerMod(new PreShootArmorMod());
    public static final Supplier<AbstractMod> RESERVOIRS_TOOL_MOD = registerMod(new ReservoirsArmorMod());
}
