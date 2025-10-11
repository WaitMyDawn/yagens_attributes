package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class PilotArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "pilot_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("functions.yagens_attributes.pilot_armor_mod.1", 8 * modLevel)
        );
    }

    public PilotArmorMod() {
        super(5, "Nod", ModRarity.COMMON);
        this.baseCapacityCost = 4;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
