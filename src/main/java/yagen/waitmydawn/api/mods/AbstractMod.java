package yagen.waitmydawn.api.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;


public abstract class AbstractMod {
    public static final Style ELDRITCH_OBFUSCATED_STYLE = Style.EMPTY.withObfuscated(true).withFont(ResourceLocation.withDefaultNamespace("alt"));

    private String modID = null;
    private String modName = null;
    private String modPolarity = null;
    private ModRarity modRarity = null;
    protected int maxLevel;
    protected int baseCapacityCost;

    public AbstractMod(int maxLevel, String modPolarity, ModRarity modRarity) {
        this.maxLevel = maxLevel;
        this.modPolarity = modPolarity;
        this.modRarity = modRarity;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getModPolarity() {
        return modPolarity;
    }

    public int getBaseCapacityCost() {
        return baseCapacityCost;
    }

    public final String getModName() {
        if (modName == null) {
            var resourceLocation = Objects.requireNonNull(getModResource());
            modName = resourceLocation.getPath().intern();
        }
        return modName;
    }

    public final String getModId() {
        if (modID == null) {
            var resourceLocation = Objects.requireNonNull(getModResource());
            modID = resourceLocation.toString().intern();
        }
        return modID;
    }

    public final ResourceLocation getModIconResource() {
        return ResourceLocation.fromNamespaceAndPath(getModResource().getNamespace(), "textures/item/" + getModName() + ".png");
    }

    public MutableComponent getDisplayName(Player player) {
        return Component.translatable(getComponentId());
    }

    public String getComponentId() {
        return String.format("mod.%s.%s", getModResource().getNamespace(), getModName());
    }

    public abstract ResourceLocation getModResource();

    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of();
    }

    public ModRarity getRarity() {
        return modRarity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AbstractMod other) {
            return this.getModResource().equals(other.getModResource());
        }

        return false;
    }

    public InteractionResultHolder<ItemStack> onRightClick(Level level, Player player, InteractionHand hand, ItemStack stack) {
        return InteractionResultHolder.pass(stack);
    }

    public boolean isReservoir() { return false;}

    public boolean isActive() { return false;}

    public double energyCost() { return 0; }

    @Override
    public int hashCode() {
        return this.getModResource().hashCode();
    }

    public boolean obfuscateStats(@Nullable Player player) {
        return false;
    }
}
