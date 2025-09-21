package yagen.waitmydawn.api.mods;

import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.capabilities.ModContainer;
import yagen.waitmydawn.registries.ComponentRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface IModContainer {
    @NotNull ModSlot[] getAllMods();

    @NotNull List<ModSlot> getActiveMods();

    int getMaxModCount();
    
    int getActiveModCount();

    int getNextAvailableIndex();

    boolean mustEquip();

    boolean isImproved();

    boolean isModWheel();

    @NotNull ModData getModAtIndex(int index);

    int getIndexForMod(AbstractMod mod);

    boolean isEmpty();

    IModContainerMutable mutableCopy();

    /*
     * Static Helpers
     */
    static boolean isModContainer(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.has(ComponentRegistry.MOD_CONTAINER);
    }

    static IModContainer create(int maxMods, boolean addsToModWheel, boolean mustBeEquipped) {
        return new ModContainer(maxMods, addsToModWheel, mustBeEquipped);
    }

    static IModContainer createModContainer(AbstractMod mod, int modLevel, ItemStack itemStack) {
        var modContainer = create(1, false, false).mutableCopy();
        modContainer.addModAtIndex(mod, modLevel, 0);
        var i = modContainer.toImmutable();
        IModContainer.set(itemStack, i);
        return i;
    }

    static IModContainer createImbuedContainer(AbstractMod mod, int modLevel, ItemStack itemStack) {
        var modContainer = create(1, true, (itemStack.getItem() instanceof ArmorItem)).mutableCopy();
        modContainer.addModAtIndex(mod, modLevel, 0);
        var i = modContainer.toImmutable();
        IModContainer.set(itemStack, i);
        return i;
    }

    static IModContainer get(ItemStack itemStack) {
        return itemStack.get(ComponentRegistry.MOD_CONTAINER);
    }

    static IModContainer getOrCreate(ItemStack itemStack) {
        return itemStack.getOrDefault(ComponentRegistry.MOD_CONTAINER, new ModContainer(1, true, false));
    }

    static void set(ItemStack stack, IModContainer container){
        stack.set(ComponentRegistry.MOD_CONTAINER, container);
    }

    Map<DamageType,Float> getDamageProfile();

    void setDamageProfile(Map<DamageType,Float> profile);

    ComponentRegistry.AttributeProfile getAttributeProfile();

    void setAttributeProfile(ComponentRegistry.AttributeProfile profile);

}
