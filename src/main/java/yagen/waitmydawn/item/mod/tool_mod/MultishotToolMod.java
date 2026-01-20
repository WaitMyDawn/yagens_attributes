package yagen.waitmydawn.item.mod.tool_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class MultishotToolMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "multishot_tool_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.multishot_multibase", 18f * modLevel)
        );
    }

    public MultishotToolMod() {
        super(5, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 10;
    }

    @Override
    public boolean isReservoir() { return true; }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
