package yagen.waitmydawn.capabilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.List;

public class PlayerBossData {
    private final List<EntityType<?>> bosses;

    public PlayerBossData() {
        this(new ArrayList<>());
    }

    public PlayerBossData(List<EntityType<?>> bosses) {
        this.bosses = new ArrayList<>(bosses);
    }

    public boolean addBoss(EntityType<? extends Monster> boss) {
        if (!bosses.contains(boss)) {
            bosses.add(boss);
            return true;
        }
        return false;
    }

    public boolean removeBoss(EntityType<? extends Monster> boss) {
        if (bosses.contains(boss)) {
            bosses.remove(boss);
            return true;
        }
        return false;
    }

    public void removeLatestBoss() {
        bosses.removeLast();
    }

    public int getSize() {
        return bosses.size();
    }

    public List<EntityType<?>> getList() {
        return bosses;
    }

    public static final Codec<PlayerBossData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                    .listOf()
                    .fieldOf("bosses")
                    .forGetter(PlayerBossData::getList)
    ).apply(instance, PlayerBossData::new));

}