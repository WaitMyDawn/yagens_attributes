package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class StatusOverloadToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "status_overload_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("functions.yagens_attributes.status_overload_tool_mod.1", 12 * modLevel)
        );
    }

    public StatusOverloadToolMod() {
        super(5, "Cth", ModRarity.LEGENDARY);
        this.baseCapacityCost = 10;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
