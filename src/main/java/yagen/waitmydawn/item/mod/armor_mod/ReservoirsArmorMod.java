package yagen.waitmydawn.item.mod.armor_mod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.gui.reservoirs.ReservoirsMenu;
import yagen.waitmydawn.registries.ComponentRegistry;

import java.util.List;

import static yagen.waitmydawn.player.KeyMappings.ABILITY_1_KEYMAP;
import static yagen.waitmydawn.player.KeyMappings.ABILITY_2_KEYMAP;

public class ReservoirsArmorMod extends AbstractMod {
    private final ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "reservoirs_armor_mod");

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel) {
        return List.of(
                Component.translatable("warframe.yagens_attributes.active", ABILITY_1_KEYMAP.getTranslatedKeyMessage().getString(), ABILITY_2_KEYMAP.getTranslatedKeyMessage().getString()),
                Component.translatable("warframe.yagens_attributes.reservoirs_armor_mod.1"),
                Component.translatable("warframe.yagens_attributes.reservoirs_armor_mod.2"),
                Component.translatable("warframe.yagens_attributes.reservoirs_armor_mod.3")
        );
    }

    public ReservoirsArmorMod() {
        super(1, FormaType.NOD.getValue(), ModRarity.WARFRAME);
        this.baseCapacityCost = 9;
    }

    @Override
    public InteractionResultHolder<ItemStack> onRightClick(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (!level.isClientSide) {
            var cap = stack.getCapability(Capabilities.ItemHandler.ITEM);

            if (stack.get(ComponentRegistry.RESERVOIRS_ATTRIBUTES.get()) == null) {
                stack.set(ComponentRegistry.RESERVOIRS_ATTRIBUTES.get(), new ComponentRegistry.ReservoirsAttributes(
                        player.getRandom().nextDouble() * 5.0,
                        player.getRandom().nextDouble() * 5.0,
                        player.getRandom().nextDouble() * 5.0
                ));
            }

            if (cap instanceof ItemStackHandler itemHandler) {

                player.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new ReservoirsMenu(id, inv, itemHandler),
                        Component.translatable("mod.yagens_attributes.reservoirs_armor_mod")
                ), buf -> {
                });

                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public double energyCost() {
        return 25.0;
    }

    @Override
    public ResourceLocation getModResource() {
        return modId;
    }
}
