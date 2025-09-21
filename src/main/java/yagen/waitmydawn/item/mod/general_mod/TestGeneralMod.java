package yagen.waitmydawn.item.mod.general_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class TestGeneralMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "test_general_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltip.yagens_attributes.slash_multiply", 10000 * modLevel),
                Component.translatable("tooltip.yagens_attributes.cold_multiply", 10000 * modLevel)
        );
    }

    public TestGeneralMod() {
        super(1, "Cth", ModRarity.RARE);
        this.baseCapacityCost = 2;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
