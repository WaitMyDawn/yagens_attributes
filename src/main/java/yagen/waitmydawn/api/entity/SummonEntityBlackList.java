package yagen.waitmydawn.api.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import yagen.waitmydawn.YagensAttributes;

public class SummonEntityBlackList {
    public static final TagKey<EntityType<?>> MONSTER_BLACK_LIST =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "monster_black_list"));
    public static final TagKey<EntityType<?>> BOSS_BLACK_LIST =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "boss_black_list"));
}
