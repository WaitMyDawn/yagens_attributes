package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

import static yagen.waitmydawn.player.KeyMappings.ABILITY_KEYMAP;

public class NourishArmorlMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "nourish_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("warframe.yagens_attributes.nourish_armor_mod.1", ABILITY_KEYMAP.getTranslatedKeyMessage().getString()),
                Component.translatable("warframe.yagens_attributes.nourish_armor_mod.2"),
                Component.translatable("warframe.yagens_attributes.nourish_armor_mod.3"),
                Component.translatable("warframe.yagens_attributes.nourish_armor_mod.4")
        );
    }

    public NourishArmorlMod() {
        super(1, FormaType.NYA.getValue(), ModRarity.WARFRAME);
        this.baseCapacityCost = 9;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
