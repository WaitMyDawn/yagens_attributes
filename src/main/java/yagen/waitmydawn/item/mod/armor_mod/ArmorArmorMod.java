package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class ArmorArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "armor_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.armor_multibase", 5f * modLevel)
        );
    }

    public ArmorArmorMod() {
        super(10, "Nod", ModRarity.COMMON);
        this.baseCapacityCost = 2;
    }

    @Override
    public boolean isReservoir() { return true; }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
