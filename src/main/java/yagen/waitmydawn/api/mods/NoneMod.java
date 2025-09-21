package yagen.waitmydawn.api.mods;

import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.YagensAttributes;

public class NoneMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "none");

    public NoneMod() {
        super(1,"Cth",ModRarity.COMMON);
        this.baseCapacityCost = 0;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}