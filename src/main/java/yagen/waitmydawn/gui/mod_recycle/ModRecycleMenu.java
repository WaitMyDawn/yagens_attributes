package yagen.waitmydawn.gui.mod_recycle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModData;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.registries.BlockRegistry;
import yagen.waitmydawn.registries.ItemRegistry;
import yagen.waitmydawn.registries.MenuRegistry;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static yagen.waitmydawn.api.util.ModCompat.TRANSFORM_POOL_BY_RARITY;

public class ModRecycleMenu extends AbstractContainerMenu {

    private final Player player;
    private final Level level;
    private final Slot[] modSlots = new Slot[16];
    private final Slot[] resultSlots = new Slot[9];

    protected final Container modContainer = new SimpleContainer(16) {
        @Override
        public void setChanged() {
            super.setChanged();
            ModRecycleMenu.this.slotsChanged(this); // 通知菜单容器变化
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.is(ItemRegistry.MOD);
        }
    };

    protected final Container resultContainer = new SimpleContainer(9) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return false;
        }
    };

    public ModRecycleMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, ContainerLevelAccess.NULL);
    }

    protected final ContainerLevelAccess access; // container access permission control

    public ModRecycleMenu(int containerId, Inventory inv, ContainerLevelAccess access) {
        super(MenuRegistry.MOD_RECYCLE_MENU.get(), containerId);
        this.access = access;
//        checkContainerSize(inv, 3);
        this.level = inv.player.level();
        this.player = inv.player;

        // default player inventory
        addPlayerInventory(inv);
        addPlayerHotbar(inv);

//        // put in mod
//        modSlot = new Slot(modContainer, 0, 7, 7) {
//            @Override
//            public boolean mayPlace(ItemStack stack) {
//                return stack.is(ItemRegistry.MOD.get()); // only mod
//            }
//        };
//
//        // 初始化结果槽位
//        resultSlot = new Slot(resultContainer, 2, 101, 16) {
//            @Override
//            public boolean mayPlace(ItemStack stack) {
//                return false;
//            }
//
//            @Override
//            public void onTake(Player player, ItemStack stack) {
//            }
//        };

        /* 4×4 输入槽：左上角 8,8 起始 */
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int index = x + y * 4;
                Slot slot = new Slot(modContainer, index,
                        8 + x * 18,
                        18 + y * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return modContainer.canPlaceItem(this.getSlotIndex(), stack);
                    }
                };
                this.modSlots[index] = slot;   // 保存引用
                this.addSlot(slot);
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int index = x + y * 3;
                Slot slot = new Slot(resultContainer, index,
                        102 + x * 18,
                        27 + y * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                    }
                };
                this.resultSlots[index] = slot;
                this.addSlot(slot);
            }
        }

    }

    // 添加玩家物品栏槽位
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 102 + i * 18));
            }
        }
    }

    // 添加玩家快捷栏槽位
    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
        }
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId == -1) {
            doRecycle();
        } else if (pId == -2) {
            doTransform();
        }
        return true;
    }

    public void doRecycle() {
        float get = 0;
        for (Slot slot : modSlots) {
            ItemStack stack = slot.getItem();
            if (!stack.is(ItemRegistry.MOD.get())) continue;

            int count = stack.getCount();
            var modContainer = IModContainer.get(stack);
            if (modContainer == null) continue;
            ModData modData = modContainer.getModAtIndex(0);
            get = get + (float) ((modData.getRarity().getValue() + 1) * Math.pow(2, modData.getLevel() - 1)) * count / 4;
            stack.shrink(count);
        }
        int recycle = (int) Math.ceil(get);
        if (recycle <= 0) return;

        ItemStack essence = new ItemStack(ItemRegistry.MOD_ESSENCE.get(), recycle);

        for (int i = 0; i < 9 && recycle > 0; i++) {
            int put = Math.min(recycle, essence.getMaxStackSize());
            resultContainer.setItem(i, essence.copyWithCount(put));
            recycle -= put;
        }

        if (recycle > 0) {
            Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(),
                    essence.copyWithCount(recycle));
        }
    }

    public void doTransform() {
        List<ModData> consumedMod = new ArrayList<>(4);
        for (Slot slot : modSlots) {
            ItemStack stack = slot.getItem();
            if (!stack.is(ItemRegistry.MOD.get())) continue;

            int need = 4 - consumedMod.size();
            int take = Math.min(need, stack.getCount());

            var modContainer = IModContainer.get(stack);
            ModData mod = modContainer.getModAtIndex(0);
            for (int i = 0; i < take; i++) consumedMod.add(mod);

            stack.shrink(take);
            if (consumedMod.size() == 4) break;
        }
        if (consumedMod.size() < 4) return;

        Map<ModRarity, Integer> weight = new EnumMap<>(ModRarity.class);
        for (ModData mod : consumedMod) {
            int w = 4 - mod.getRarity().getValue();
            weight.merge(mod.getRarity(), w, Integer::sum);
        }

        int totalWeight = weight.values().stream().mapToInt(Integer::intValue).sum();
        int roll = player.level().random.nextInt(totalWeight);
        ModRarity chosen = null;
        int sum = 0;
        for (var e : weight.entrySet()) {
            sum += e.getValue();
            if (roll < sum) {
                chosen = e.getKey();
                break;
            }
        }

        List<AbstractMod> pool = TRANSFORM_POOL_BY_RARITY.get(chosen);
        if (pool == null || pool.isEmpty()) return;

        AbstractMod newMod = pool.get(player.level().random.nextInt(pool.size()));

        ItemStack result = new ItemStack(ItemRegistry.MOD.get());
        IModContainer.createModContainer(newMod, 1, result);

        for (Slot slot : resultSlots) {
            if (!slot.hasItem()) {
                slot.set(result);
                return;
            }
        }
    }

    public Slot[] getModSlots() {
        return modSlots;
    }

    public Slot[] getResultSlots() {
        return resultSlots;
    }

    /* 区域常量 */
    final int PLAYER_INV_START = 0;
    final int PLAYER_INV_END = 35;
    final int MOD_SLOT_START = 36;
    final int MOD_SLOT_END = 51;
    final int RESULT_SLOT_START = 52;
    final int RESULT_SLOT_END = 60;

    @Override
    public @NotNull ItemStack quickMoveStack(Player pPlayer, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index >= RESULT_SLOT_START && index <= RESULT_SLOT_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END + 1, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index >= MOD_SLOT_START && index <= MOD_SLOT_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INV_START && index <= PLAYER_INV_END) {
            if (!moveItemStackTo(stack, MOD_SLOT_START, MOD_SLOT_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.access.evaluate((level, blockPos) -> {
            return level.getBlockState(blockPos).is(BlockRegistry.MOD_RECYCLE_BLOCK.get()) && pPlayer.distanceToSqr((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    @Override
    public void removed(Player pPlayer) {
        if (pPlayer instanceof ServerPlayer) {
            super.removed(pPlayer);
            this.access.execute((p_39796_, p_39797_) -> {
                this.clearContainer(pPlayer, this.modContainer);
                this.clearContainer(pPlayer, this.resultContainer);
            });
        }
    }
}
