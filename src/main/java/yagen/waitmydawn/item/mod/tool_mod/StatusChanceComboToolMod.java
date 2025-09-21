package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class StatusChanceComboToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "status_chance_combo_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.combo_bonus_sc_add", String.format("%.2f", 0.18f * modLevel))
        );
    }

    public StatusChanceComboToolMod() {
        super(5, "Cth", ModRarity.UNCOMMON);
        this.baseCapacityCost = 6;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
