package yagen.waitmydawn.capabilities;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.*;

public class ModPoolData {
    public static final int SLOTS_PER_PAGE = 27;

    public static final Codec<ModPoolData> CODEC = CompoundTag.CODEC.xmap(ModPoolData::fromNbt, ModPoolData::toNbt);
    public static final StreamCodec<RegistryFriendlyByteBuf, ModPoolData> STREAM_CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .apply(ByteBufCodecs.list())
                    .map(ModPoolData::fromStackList, ModPoolData::toStackList);

    private final List<ItemStackHandler> pages = new ArrayList<>();

    public ModPoolData() {
        ensurePage(0);
    }

    private static final Comparator<ItemStack> MOD_POOL_SORTER = (a, b) -> {

        var aData = IModContainer.get(a).getModAtIndex(0);
        var bData = IModContainer.get(b).getModAtIndex(0);

        var aMod = aData.getMod();
        var bMod = bData.getMod();

        var aRarity = aMod.getRarity();
        var bRarity = bMod.getRarity();

        int rarityCompare = Integer.compare(
                bRarity.getValue(),
                aRarity.getValue()
        );
        if (rarityCompare != 0) return rarityCompare;

        if (aRarity == ModRarity.RIVEN && bRarity == ModRarity.RIVEN) {

            Item aType = a.get(ComponentRegistry.RIVEN_TYPE.get());
            Item bType = b.get(ComponentRegistry.RIVEN_TYPE.get());

            String aName = "none";
            if (aType != null) {
                aName = aType.getDescriptionId();
            }
            String bName = "none";
            if (bType != null) {
                bName = bType.getDescriptionId();
            }

            int typeCompare = aName.compareTo(bName);
            if (typeCompare != 0) return typeCompare;
        }

        int levelCompare = Integer.compare(
                bData.getLevel(),
                aData.getLevel()
        );
        if (levelCompare != 0) return levelCompare;

        if (aRarity == ModRarity.RIVEN && bRarity == ModRarity.RIVEN) {

            var aRaw = a.get(ComponentRegistry.RIVEN_RAW_INFO.get());
            var bRaw = b.get(ComponentRegistry.RIVEN_RAW_INFO.get());

            String aRawString = String.valueOf(aRaw);
            String bRawString = String.valueOf(bRaw);

            int rawCompare = aRawString.compareTo(bRawString);
            if (rawCompare != 0) return rawCompare;
        }

        return Integer.compare(
                b.getCount(),
                a.getCount()
        );
    };
    public void tidy() {

        List<ItemStack> collected = new ArrayList<>();

        for (ItemStackHandler handler : pages) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    collected.add(stack.copy());
                }
            }
        }

        if (collected.isEmpty()) return;

        Map<ModStackKey, ItemStack> merged = new LinkedHashMap<>();

        for (ItemStack stack : collected) {

            ModStackKey key = new ModStackKey(stack);

            if (merged.containsKey(key)) {
                merged.get(key).grow(stack.getCount());
            } else {
                merged.put(key, stack.copy());
            }
        }

        List<ItemStack> mergedList = new ArrayList<>(merged.values());

        mergedList.sort(MOD_POOL_SORTER);

        pages.clear();
        ensurePage(0);

        int pageIndex = 0;
        int slotIndex = 0;

        for (ItemStack stack : mergedList) {

            int total = stack.getCount();
            int max = stack.getMaxStackSize();

            while (total > 0) {

                int put = Math.min(max, total);

                ItemStack split = stack.copy();
                split.setCount(put);

                if (slotIndex >= 27) {
                    pageIndex++;
                    ensurePage(pageIndex);
                    slotIndex = 0;
                }

                pages.get(pageIndex).setStackInSlot(slotIndex, split);

                slotIndex++;
                total -= put;
            }
        }
    }

    public List<ItemStackHandler> getPages() {
        return pages;
    }

    public ItemStackHandler getPage(int index) {
        if (index >= 0 && index < pages.size()) {
            return pages.get(index);
        }
        return null;
    }

    public void cleanEmptyPages() {
        pages.removeIf(this::isHandlerEmpty);
        if (pages.isEmpty()) {
            ensurePage(0);
        }
    }

    private boolean isHandlerEmpty(ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    public void ensurePage(int index) {
        while (pages.size() <= index) {
            pages.add(new ItemStackHandler(SLOTS_PER_PAGE));
        }
    }

    public ItemStack insertMod(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !stack.is(ItemRegistry.MOD.get())) return stack;

        ItemStack remaining = stack.copy();

        for (ItemStackHandler page : pages) {
            for (int i = 0; i < page.getSlots(); i++) {
                remaining = page.insertItem(i, remaining, simulate);
                if (remaining.isEmpty()) return ItemStack.EMPTY;
            }
        }

        int pageIndex = 0;
        while (!remaining.isEmpty()) {
            ensurePage(pageIndex);
            ItemStackHandler page = pages.get(pageIndex);
            for (int i = 0; i < page.getSlots(); i++) {
                if (page.getStackInSlot(i).isEmpty()) {
                    remaining = page.insertItem(i, remaining, simulate);
                    if (remaining.isEmpty()) break;
                }
            }
            pageIndex++;
        }

        if (!simulate) cleanEmptyPages();
        return remaining;
    }


    public int getPageCount() {
        return pages.size();
    }

    private static List<List<ItemStack>> toStackList(ModPoolData data) {
        List<List<ItemStack>> allPages = new ArrayList<>();
        for (ItemStackHandler handler : data.pages) {
            List<ItemStack> pageItems = new ArrayList<>();
            for (int i = 0; i < handler.getSlots(); i++) pageItems.add(handler.getStackInSlot(i));
            allPages.add(pageItems);
        }
        return allPages;
    }

    private static ModPoolData fromStackList(List<List<ItemStack>> stackLists) {
        ModPoolData data = new ModPoolData();
        data.pages.clear();
        for (List<ItemStack> pageItems : stackLists) {
            ItemStackHandler handler = new ItemStackHandler(SLOTS_PER_PAGE);
            for (int i = 0; i < pageItems.size() && i < SLOTS_PER_PAGE; i++) {
                handler.setStackInSlot(i, pageItems.get(i));
            }
            data.pages.add(handler);
        }
        if (data.pages.isEmpty()) data.ensurePage(0);
        return data;
    }

    public static CompoundTag toNbt(ModPoolData data) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        HolderLookup.Provider provider = server != null ? server.registryAccess() : null;

        for (ItemStackHandler handler : data.pages) {
            if (provider != null) list.add(handler.serializeNBT(provider));
        }
        tag.put("Pages", list);
        return tag;
    }

    public static ModPoolData fromNbt(CompoundTag tag) {
        ModPoolData data = new ModPoolData();
        data.pages.clear();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        HolderLookup.Provider provider = server != null ? server.registryAccess() : null;

        if (tag.contains("Pages", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Pages", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStackHandler handler = new ItemStackHandler(SLOTS_PER_PAGE);
                if (provider != null) handler.deserializeNBT(provider, list.getCompound(i));
                data.pages.add(handler);
            }
        }
        if (data.pages.isEmpty()) data.ensurePage(0);
        return data;
    }

    private static class ModStackKey {

        private final String modId;
        private final int level;
        private final ModRarity rarity;

        private final Item rivenType; // 仅 Riven 使用
        private final Object rivenRaw; // 仅 Riven 使用

        public ModStackKey(ItemStack stack) {

            var modData = IModContainer.get(stack).getModAtIndex(0);
            var mod = modData.getMod();

            this.modId = mod.getModId();
            this.level = modData.getLevel();
            this.rarity = mod.getRarity();

            if (rarity == ModRarity.RIVEN) {
                this.rivenType = stack.get(ComponentRegistry.RIVEN_TYPE.get());
                this.rivenRaw = stack.get(ComponentRegistry.RIVEN_RAW_INFO.get());
            } else {
                this.rivenType = null;
                this.rivenRaw = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModStackKey key)) return false;

            if (!modId.equals(key.modId)) return false;
            if (level != key.level) return false;
            if (rarity != key.rarity) return false;

            if (rarity == ModRarity.RIVEN) {
                if (!Objects.equals(rivenType, key.rivenType)) return false;
                return Objects.equals(rivenRaw, key.rivenRaw);
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(modId, level, rarity, rivenType, rivenRaw);
        }
    }
}