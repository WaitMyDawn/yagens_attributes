package yagen.waitmydawn.api.attribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.Registries;
import yagen.waitmydawn.YagensAttributes;

public class HealthMaterialUtils {
    public static final TagKey<EntityType<?>> BONE_MATERIAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "bone_material"));
    public static final TagKey<EntityType<?>> FLESH_MATERIAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "flesh_material"));
    public static final TagKey<EntityType<?>> METAL_MATERIAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "metal_material"));
    public static final TagKey<EntityType<?>> ARTHROPOD_MATERIAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "arthropod_material"));
    public static final TagKey<EntityType<?>> GHOST_MATERIAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "ghost_material"));
    public static HealthMaterialType getMaterialType(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (type.is(HealthMaterialUtils.BONE_MATERIAL)) return HealthMaterialType.BONE;
        if (type.is(HealthMaterialUtils.FLESH_MATERIAL)) return HealthMaterialType.FLESH;
        if (type.is(HealthMaterialUtils.METAL_MATERIAL)) return HealthMaterialType.METAL;
        if (type.is(HealthMaterialUtils.ARTHROPOD_MATERIAL)) return HealthMaterialType.ARTHROPOD;
        if (type.is(HealthMaterialUtils.GHOST_MATERIAL)) return HealthMaterialType.GHOST;
        // flesh default
        return HealthMaterialType.FLESH;
    }
}