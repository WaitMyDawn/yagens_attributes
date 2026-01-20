package yagen.waitmydawn.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.ArrayList;
import java.util.List;

public class ReservoirEntity extends Entity {
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(ReservoirEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(ReservoirEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RANGE = SynchedEntityData.defineId(ReservoirEntity.class, EntityDataSerializers.FLOAT);

    private int lifespan = 0;

    public ReservoirEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    public void setReservoirType(int type) {
        this.entityData.set(TYPE, type);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    public void setRange(float range) {
        this.entityData.set(RANGE, range);
    }

    public void setLifespan(int ticks) {
        this.lifespan = ticks;
    }

    public int getReservoirType() {
        return this.entityData.get(TYPE);
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    public float getRange() {
        return this.entityData.get(RANGE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TYPE, 0);
        builder.define(DURATION, 0);
        builder.define(RANGE, 4.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (lifespan-- <= 0) {
                this.discard();
                return;
            }
            if (lifespan % 2 == 0) return;
            double r = getRange();
            AABB searchBox = this.getBoundingBox().inflate(r, 2.0, r);
            List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);

            for (Player player : players) {
                double distSqr = player.distanceToSqr(this.getX(), this.getY(), this.getZ());
                if (distSqr <= r * r) {
                    applyEffect(player, getReservoirType(), getDuration());
                }
            }
        } else {
            spawnRingParticles();
        }
    }

    private void applyEffect(Player player, int type, int duration) {
        Holder<MobEffect> effectHolder = switch(type) {
            case 0 -> MobEffectRegistry.GRANDIFLORUM;
            case 1 -> MobEffectRegistry.PUMILUM;
            default -> MobEffectRegistry.HYDRANGEA;
        };
        player.addEffect(new MobEffectInstance(effectHolder, duration, 0));

        DataAttachmentRegistry.ReservoirBuffs currentData = player.getData(DataAttachmentRegistry.RESERVOIR_BUFFS);
        for (StoredModifier storedModifier : this.storedModifiers) {
            AttributeInstance attrInstance = player.getAttribute(storedModifier.attribute);

            if (attrInstance != null) {
                attrInstance.removeModifier(storedModifier.modifier.id());
                attrInstance.addPermanentModifier(storedModifier.modifier);
                currentData = currentData.add(effectHolder, storedModifier.attribute, storedModifier.modifier.id());
            }
        }

        player.setData(DataAttachmentRegistry.RESERVOIR_BUFFS, currentData);
    }

    private void spawnRingParticles() {
        int type = getReservoirType();
        float radius = getRange();

        Vector3f color = switch (type) {
            case 1 -> new Vector3f(1.0f, 0.0f, 0.0f);
            case 2 -> new Vector3f(0.0f, 1.0f, 0.0f);
            default -> new Vector3f(0.0f, 0.5f, 1.0f);
        };

        int particleCount = 10;
        for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double x = this.getX() + radius * Math.cos(angle);
            double z = this.getZ() + radius * Math.sin(angle);
            double y = this.getY() + 0.2;

            this.level().addParticle(new DustParticleOptions(color, 1.0f), x, y, z, 0, 0, 0);
        }
    }

    public record StoredModifier(Holder<Attribute> attribute, AttributeModifier modifier) {}

    private final List<StoredModifier> storedModifiers = new ArrayList<>();

    public void addModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
        this.storedModifiers.add(new StoredModifier(attribute, modifier));
    }

    public List<StoredModifier> getStoredModifiers() {
        return this.storedModifiers;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setReservoirType(tag.getInt("Type"));
        this.setRange(tag.getFloat("Range"));
        this.setDuration(tag.getInt("Duration"));
        this.lifespan = tag.getInt("Lifespan");

        if (tag.contains("StoredModifiers")) {
            this.storedModifiers.clear();
            ListTag modList = tag.getList("StoredModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < modList.size(); i++) {
                CompoundTag compound = modList.getCompound(i);

                ResourceLocation attrLoc = ResourceLocation.tryParse(compound.getString("attr"));
                BuiltInRegistries.ATTRIBUTE.getHolder(attrLoc).ifPresent(attrHolder -> {
                    ResourceLocation modId = ResourceLocation.tryParse(compound.getString("id"));
                    double amount = compound.getDouble("amount");
                    int opId = compound.getInt("op");
                    AttributeModifier.Operation op = AttributeModifier.Operation.BY_ID.apply(opId);

                    this.storedModifiers.add(new StoredModifier(attrHolder, new AttributeModifier(modId, amount, op)));
                });
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Type", getReservoirType());
        tag.putInt("Duration", getDuration());
        tag.putFloat("Range", getRange());
        tag.putInt("Lifespan", lifespan);

        ListTag modList = new ListTag();
        for (StoredModifier sm : storedModifiers) {
            CompoundTag compound = new CompoundTag();
            compound.putString("attr", sm.attribute.getKey().location().toString());

            AttributeModifier mod = sm.modifier;
            compound.putString("id", mod.id().toString());
            compound.putDouble("amount", mod.amount());
            compound.putInt("op", mod.operation().id());

            modList.add(compound);
        }
        tag.put("StoredModifiers", modList);
    }
}
