package yagen.waitmydawn.api.mods;

/**
 * (Spell Data + Index) for serialization
 */
public record ModSlot(ModData modData, int index) {

    public AbstractMod getMod() {
        return modData.getMod();
    }

    public int getLevel() {
        return modData.getLevel();
    }

//    public boolean isLocked() {
//        return modData.isLocked();
//    }

    public int index() {
        return index;
    }

    public static ModSlot of(ModData data, int index) {
        return new ModSlot(data, index);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof ModSlot o && o.modData.equals(this.modData) && o.index == this.index);
    }

    @Override
    public int hashCode() {
        return modData.hashCode() * 31 + index;
    }
}
