package yagen.waitmydawn.yagens_attributes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.network.ClientPackets;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.setup.ModTransactions;
import yagen.waitmydawn.yagens_attributes.text.ModText;
import yagen.waitmydawn.yagens_attributes.network.PanelPayload;

import java.util.*;

public final class ModItem extends Item {
    public ModItem(Properties properties) {
        super(properties);
    }

    private static ModCatalog.Card card(ItemStack stack) {
        var data = stack.get(Components.MOD_DATA.get());
        if (data == null) return null;
        try {
            var roll = stack.get(Components.RIVEN_ROLL.get());
            if (roll != null && data.type().toString().equals(RivenRules.TYPE)) {
                var profile = yagen.waitmydawn.yagens_attributes.client.ClientProfiles.get(net.minecraft.resources.ResourceLocation.parse(roll.weapon()));
                return ClientPackets.modCatalog().rivenCard(profile == null ? roll : roll.withDisposition(profile.rivenDisposition()), data.level());
            }
            return ClientPackets.modCatalog().card(data.type().toString(), data.level());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        var card = card(stack);
        return card == null ? super.getName(stack) : ModText.localizedName(card.info());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flags) {
        var data = stack.get(Components.MOD_DATA.get());
        var card = card(stack);
        if (card == null) {
            if (data != null)
                lines.add(Component.literal(data.type() + " / " + data.level()).withStyle(ChatFormatting.GRAY));
            return;
        }
        var info = card.info();
        var bonuses = new HashMap<String, PanelPayload.ModifierView>();
        card.bonuses().forEach((stat, bonus) -> bonuses.put(stat, PanelPayload.ModifierView.of(bonus)));
        lines.addAll(ModText.properties(info, bonuses));
        lines.add(Component.translatable("item.yagens_attributes.mod.deposit").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                ModTransactions.deposit(serverPlayer, stack);
            } catch (IllegalArgumentException exception) {
                player.displayClientMessage(Component.translatable(exception.getMessage()), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
