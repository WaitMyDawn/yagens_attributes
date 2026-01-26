package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class PreShootArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "pre_shoot_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("warframe.yagens_attributes.passive"),
                Component.translatable("warframe.yagens_attributes.pre_shoot_armor_mod.1"),
                Component.translatable("warframe.yagens_attributes.pre_shoot_armor_mod.2"),
                Component.translatable("warframe.yagens_attributes.pre_shoot_armor_mod.3")
        );
    }

    public PreShootArmorMod() {
        super(1, "Nya", ModRarity.WARFRAME);
        this.baseCapacityCost = 9;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
