package yagen.waitmydawn.api.mods;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IModContainerMutable {
    void setMaxModCount(int maxMods);

    void setImproved(boolean improved);

//    boolean addModAtIndex(AbstractMod mod, int level, int index, boolean locked);
    boolean addModAtIndex(AbstractMod mod, int level, int index);

//    boolean addMod(AbstractMod mod, int level, boolean locked);
    boolean addMod(AbstractMod mod, int level);

    boolean removeModAtIndex(int index);

    boolean removeMod(AbstractMod mod);

    @NotNull ModSlot[] getAllMods();

    @NotNull List<ModSlot> getActiveMods();

    /*
    Getters. Currently, a copy of IModContainer
     */

    int getMaxModCount();


    int getActiveModCount();

    int getNextAvailableIndex();

    boolean mustEquip();

    boolean isImproved();

    boolean isModWheel();

    @NotNull ModData getModAtIndex(int index);

    int getIndexForMod(AbstractMod mod);

    boolean isEmpty();

    IModContainer toImmutable();
}

