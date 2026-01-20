package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class HealthArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "health_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.max_health_multibase", 15f * modLevel)
        );
    }

    public HealthArmorMod() {
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
