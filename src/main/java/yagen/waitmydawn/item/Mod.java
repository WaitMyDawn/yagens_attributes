package yagen.waitmydawn.item;

import yagen.waitmydawn.api.item.IMod;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModData;
import yagen.waitmydawn.util.MinecraftInstanceHelper;
import yagen.waitmydawn.util.TooltipsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Mod extends Item implements IMod {

    public Mod() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    private @NotNull ModData getModSlotFromStack(ItemStack itemStack) {
        return IModContainer.getOrCreate(itemStack).getModAtIndex(0);
    }

    @Override
    public @Nullable String getCreatorModId(ItemStack itemStack) {
        var spell = getModSlotFromStack(itemStack).getMod();
        var id = ModRegistry.REGISTRY.getKey(spell);
        return id == null ? super.getCreatorModId(itemStack) : id.getNamespace();
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return getModSlotFromStack(itemStack).getDisplayName();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, lines, flag);
        MinecraftInstanceHelper.ifPlayerPresent(player -> {
            lines.addAll(TooltipsUtils.formatModTooltip(itemStack, player));
        });
    }
}
