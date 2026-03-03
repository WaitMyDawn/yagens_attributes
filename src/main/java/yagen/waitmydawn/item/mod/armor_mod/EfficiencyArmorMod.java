package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.config.ServerConfigs;

import java.util.List;

public class EfficiencyArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "efficiency_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, Player player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.ability_efficiency_multibase", String.format("%.2f", ServerConfigs.MOD_RARE_ANCIENT_STABILIZER.get().floatValue() * modLevel))
        );
    }

    public EfficiencyArmorMod() {
        super(6, "Nya", ModRarity.RARE);
        this.baseCapacityCost = 3;
    }

    @Override
    public boolean isReservoir() {
        return true;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
