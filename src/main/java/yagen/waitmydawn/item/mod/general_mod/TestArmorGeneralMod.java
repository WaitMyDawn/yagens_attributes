package yagen.waitmydawn.item.mod.general_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;

import java.util.List;

public class TestArmorGeneralMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "test_armor_general_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of(
                Component.translatable("tooltips.yagens_attributes.armor_add", 10f * modLevel),
                Component.translatable("tooltips.yagens_attributes.armor_multibase", 100f * modLevel),
                Component.translatable("tooltips.yagens_attributes.armor_multiply", 200f * modLevel),
                Component.translatable("tooltips.yagens_attributes.attack_speed_add", 1f * modLevel),
                Component.translatable("tooltips.yagens_attributes.armor_toughness_add", 10f * modLevel)
        );
    }

    //public static final TestGeneralMod INSTANCE = new TestGeneralMod();

    public TestArmorGeneralMod() {
        super(1, "Cth", ModRarity.RIVEN);
        this.baseCapacityCost = 2;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
