package yagen.waitmydawn.api.events;

import yagen.waitmydawn.api.mods.ModData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class OperationModEvent extends PlayerEvent implements ICancellableEvent {
    private final ModData modData;

    public OperationModEvent(Player player, ModData modData) {
        super(player);
        this.modData = modData;
    }

    public ModData getModData() {
        return this.modData;
    }
}
