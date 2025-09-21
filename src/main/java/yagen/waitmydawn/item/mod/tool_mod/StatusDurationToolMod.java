package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class StatusDurationToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "status_duration_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.status_duration_multibase", String.format("%.1f", 11f * modLevel))
        );
    }

    public StatusDurationToolMod() {
        super(10, "Nya", ModRarity.COMMON);
        this.baseCapacityCost = 2;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
