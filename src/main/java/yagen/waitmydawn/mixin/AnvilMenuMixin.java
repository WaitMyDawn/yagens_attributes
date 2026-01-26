package yagen.waitmydawn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AnvilMenu.class, priority = 500)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    public AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Unique
    private int consumeCount(ItemStack left, ItemStack right) {
        if (!left.is(Items.ENCHANTED_BOOK) || !right.is(Items.ENCHANTED_BOOK)) return 1;
        return Math.min(left.getCount(), right.getCount());
    }

    @ModifyArg(method = "onTake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 0), index = 1)
    private ItemStack shrinkLeft(ItemStack original) {
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        int consume = consumeCount(left, right);
        if (left.is(Items.ENCHANTED_BOOK) && left.getCount() >= consume) {
            return left;
        }
        return original;
    }

    @ModifyArg(method = "onTake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 3), index = 1)
    private ItemStack shrinkRight(ItemStack original) {
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        int consume = consumeCount(left, right);
        if (right.is(Items.ENCHANTED_BOOK)) {
            right.shrink(consume);
            if (left.is(Items.ENCHANTED_BOOK)) {
                left.shrink(consume);
            }
            return right;
        }
        return original;
    }

    @ModifyArg(method = "createResult",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 3), index = 1)
    private ItemStack setOutputCount(ItemStack stack) {
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        int count = consumeCount(left, right);
        if (stack.is(Items.ENCHANTED_BOOK)) {
            stack.setCount(count);
        }
        return stack;
    }

    @Shadow
    @Final
    private DataSlot cost;

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int removeAnvilLevelCap(int oldLimit) {
        return Integer.MAX_VALUE;
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void recalculateCost(CallbackInfo ci) {
        ItemStack left = this.inputSlots.getItem(0);
        ItemStack right = this.inputSlots.getItem(1);
        ItemStack output = this.resultSlots.getItem(0);

        if (!output.isEmpty() && left.is(Items.ENCHANTED_BOOK) && right.is(Items.ENCHANTED_BOOK)) {
            int count = consumeCount(left, right);
            if (count >= 1) {
                int baseCost = calculateBaseCost(left, right);
                int newCost = baseCost + count - 1;

                this.cost.set(newCost);
            }
        }
    }

    @Unique
    private int calculateBaseCost(ItemStack left, ItemStack right) {
        int cost = 0;

        int repairCostLeft = left.getOrDefault(DataComponents.REPAIR_COST, 0);
        int repairCostRight = right.getOrDefault(DataComponents.REPAIR_COST, 0);
        cost = cost + repairCostLeft + repairCostRight;

        ItemEnchantments rightEnchants = EnchantmentHelper.getEnchantmentsForCrafting(right);
        ItemEnchantments leftEnchants = EnchantmentHelper.getEnchantmentsForCrafting(left);

        for (var entry : rightEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int rightLevel = entry.getIntValue();
            int leftLevel = leftEnchants.getLevel(enchantment);

            int targetLevel = leftLevel == rightLevel ? leftLevel + 1 : Math.max(leftLevel, rightLevel);
            int rarityCost = enchantment.value().getAnvilCost();
            cost = cost + targetLevel * rarityCost;
        }
        return Math.max(1, cost);
    }

    @Redirect(
            method = "createResult",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;instabuild:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean forceCreativeCheck(Abilities instance) {
        return true;
    }

    @WrapOperation(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void changeLevelCostToPointCost(Player player, int levels, Operation<Void> original) {
        int costInLevels = -levels;
        if (costInLevels > 0) {
            int costPoints = 0;
            for (costInLevels--; costInLevels >= 0; costInLevels--)
                costPoints += getXpNeedForNextLevel(costInLevels);
            player.giveExperiencePoints(-costPoints);
        } else {
            original.call(player, levels);
        }
    }

    @Unique
    private int getXpNeedForNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }
}

