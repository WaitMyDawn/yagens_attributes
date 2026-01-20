package yagen.waitmydawn.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.api.item.IMod;
import yagen.waitmydawn.api.mods.AbstractMod;
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

    public static @NotNull ModData getModSlotFromStack(ItemStack itemStack) {
        return IModContainer.getOrCreate(itemStack).getModAtIndex(0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        InteractionResultHolder<ItemStack> result = InteractionResultHolder.pass(player.getItemInHand(usedHand));
        if(usedHand != InteractionHand.MAIN_HAND) return result;
        ItemStack stack = player.getItemInHand(usedHand);
        AbstractMod mod = getModSlotFromStack(stack).getMod();
        if (mod != null)
            result = mod.onRightClick(level, player, usedHand, stack);

        return result;
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
