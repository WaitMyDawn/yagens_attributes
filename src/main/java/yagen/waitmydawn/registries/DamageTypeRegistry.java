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

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(SLASH_STATUS_DAMAGE_TYPE, new DamageType(
                SLASH_STATUS_DAMAGE_TYPE.location().getPath(),
                DamageScaling.NEVER,
                0f));
        }

}
