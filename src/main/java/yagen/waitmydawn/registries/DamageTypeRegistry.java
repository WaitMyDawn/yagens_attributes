package yagen.waitmydawn.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import yagen.waitmydawn.YagensAttributes;

public class DamageTypeRegistry {
    public static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, name));
    }

    public static final ResourceKey<DamageType> SLASH_STATUS_DAMAGE_TYPE = register("slash_status_damage_type");
    public static final ResourceKey<DamageType> TOXIN_STATUS_DAMAGE_TYPE = register("toxin_status_damage_type");
    public static final ResourceKey<DamageType> HEAT_STATUS_DAMAGE_TYPE = register("heat_status_damage_type");
    public static final ResourceKey<DamageType> ELECTRICITY_STATUS_DAMAGE_TYPE = register("electricity_status_damage_type");
    public static final ResourceKey<DamageType> GAS_STATUS_DAMAGE_TYPE = register("gas_status_damage_type");

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(SLASH_STATUS_DAMAGE_TYPE, new DamageType(
                SLASH_STATUS_DAMAGE_TYPE.location().getPath(),
                DamageScaling.NEVER,
                0f));
        context.register(TOXIN_STATUS_DAMAGE_TYPE, new DamageType(
                TOXIN_STATUS_DAMAGE_TYPE.location().getPath(),
                DamageScaling.NEVER,
                0f));
        context.register(HEAT_STATUS_DAMAGE_TYPE, new DamageType(
                HEAT_STATUS_DAMAGE_TYPE.location().getPath(),
                DamageScaling.NEVER,
                0f));
        context.register(ELECTRICITY_STATUS_DAMAGE_TYPE, new DamageType(
                ELECTRICITY_STATUS_DAMAGE_TYPE.location().getPath(),
                DamageScaling.NEVER,
                0f));
        context.register(GAS_STATUS_DAMAGE_TYPE, new DamageType(
                GAS_STATUS_DAMAGE_TYPE.location().getPath(),
                DamageScaling.NEVER,
                0f));
        }

}
