package yagen.waitmydawn.yagens_attributes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MaterialItem extends Item {
    private final List<String> descriptions;

    public MaterialItem(Properties properties, String... descriptions) {
        super(properties);
        this.descriptions = List.of(descriptions);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for (String description : descriptions) tooltip.add(Component.translatable(description).withStyle(ChatFormatting.GRAY));
    }
}
