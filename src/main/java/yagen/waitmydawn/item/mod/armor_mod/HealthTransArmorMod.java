package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class HealthTransArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "health_trans_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("functions.yagens_attributes.health_trans_armor_mod.1",
                        String.format("%.1f", ServerConfigs.MOD_RARE_THORN_AURA_DECREASE.get().floatValue() * modLevel),
                        String.format("%.1f", ServerConfigs.MOD_RARE_THORN_AURA_INCREASE.get().floatValue() * modLevel))
        );
    }

    public HealthTransArmorMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 4;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
