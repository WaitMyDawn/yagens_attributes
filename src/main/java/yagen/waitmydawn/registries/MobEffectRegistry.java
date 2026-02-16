package yagen.waitmydawn.registries;

import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, YagensAttributes.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> PUNCTURE_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "puncture_status", () -> new PunctureStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> IMPACT_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "impact_status", () -> new ImpactStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> SLASH_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "slash_status", () -> new SlashStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> COLD_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "cold_status", () -> new ColdStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> TOXIN_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "toxin_status", () -> new ToxinStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> CORROSIVE_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "corrosive_status", () -> new CorrosiveStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> ELECTRICITY_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "electricity_status", () -> new ElectricityStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> GAS_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "gas_status", () -> new GasStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> HEAT_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "heat_status", () -> new HeatStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> VIRAL_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "viral_status", () -> new ViralStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> RADIATION_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "radiation_status", () -> new RadiationStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> MAGNETIC_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "magnetic_status", () -> new MagneticStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> BLAST_STATUS = MOB_EFFECT_DEFERRED_REGISTER.register(
            "blast_status", () -> new BlastStatusEffect(MobEffectCategory.HARMFUL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> SCOPE = MOB_EFFECT_DEFERRED_REGISTER.register(
            "scope", () -> new ScopeEffect(MobEffectCategory.BENEFICIAL, 0xbea925));

    public static final DeferredHolder<MobEffect, MobEffect> NOURISH = MOB_EFFECT_DEFERRED_REGISTER.register(
            "nourish", () -> new NourishEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> BLADE_STORM = MOB_EFFECT_DEFERRED_REGISTER.register(
            "blade_storm", () -> new NourishEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> PUMILUM = MOB_EFFECT_DEFERRED_REGISTER.register(
            "pumilum", () -> new PumilumEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> GRANDIFLORUM = MOB_EFFECT_DEFERRED_REGISTER.register(
            "grandiflorum", () -> new GrandiflorumEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> HYDRANGEA = MOB_EFFECT_DEFERRED_REGISTER.register(
            "hydrangea", () -> new HydrangeaEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> THERMAL_SUNDER = MOB_EFFECT_DEFERRED_REGISTER.register(
            "thermal_sunder", () -> new ThermalSunderStatusEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> REDLINE = MOB_EFFECT_DEFERRED_REGISTER.register(
            "redline", () -> new RedlineEffect(MobEffectCategory.BENEFICIAL, 0xbea925));

    public static final DeferredHolder<MobEffect, MobEffect> MULTISHOT_GALVANIZED = MOB_EFFECT_DEFERRED_REGISTER.register(
            "multishot_galvanized", () -> new MultishotGalvanizedEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> FURY_KILL = MOB_EFFECT_DEFERRED_REGISTER.register(
            "fury_kill", () -> new FuryKillEffect(MobEffectCategory.BENEFICIAL, 0xbea925));
    public static final DeferredHolder<MobEffect, MobEffect> SCOPE_GALVANIZED = MOB_EFFECT_DEFERRED_REGISTER.register(
            "scope_galvanized", () -> new ScopeGalvanizedEffect(MobEffectCategory.BENEFICIAL, 0xbea925));

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

}