package yagen.waitmydawn.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.registries.ComponentRegistry;

public class FormaItem extends Item {

    public FormaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        if (!level.isClientSide) {
            ItemStack formaStack = player.getItemInHand(hand);
            String type = formaStack.getOrDefault(ComponentRegistry.FORMA_TYPE, "");
            if (!type.isEmpty()) {
                FormaType newType = FormaType.values()[(FormaType.fromString(type).ordinal() + 1) % FormaType.values().length];
                formaStack.set(ComponentRegistry.FORMA_TYPE.get(), newType.getValue());
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }
}