package yagen.waitmydawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.network.EnergyPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.EntityRegistry;

public class EnergyOrbEntity extends Entity {
    private static final EntityDataAccessor<Integer> VALUE = SynchedEntityData.defineId(EnergyOrbEntity.class, EntityDataSerializers.INT);

    public int age;
    public int pickupDelay;
    private int health = 5;
    private Player followingPlayer;

    public EnergyOrbEntity(EntityType<? extends EnergyOrbEntity> entityType, Level level) {
        super(entityType, level);
    }

    public EnergyOrbEntity(Level level, double x, double y, double z, int value) {
        this(EntityRegistry.ENERGY_ORB.get(), level);
        this.setPos(x, y, z);
        this.setYRot((float) (this.random.nextDouble() * 360.0D));
        this.setDeltaMovement(
                (this.random.nextDouble() * 0.2D - 0.1D) * 2.0D,
                this.random.nextDouble() * 0.2D * 2.0D,
                (this.random.nextDouble() * 0.2D - 0.1D) * 2.0D);
        this.setValue(value);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(VALUE, 1);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.pickupDelay > 0) {
            --this.pickupDelay;
        }

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (this.isEyeInFluidType(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value())) {
            this.setUnderwaterMovement();
        } else {
            this.applyGravity();
        }

        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement(
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    0.2D,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        }

        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
        }

        if (this.tickCount % 20 == 1) {
            this.scanForPlayer();
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        if (this.followingPlayer != null) {
            Vec3 targetVec = new Vec3(
                    this.followingPlayer.getX() - this.getX(),
                    this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() / 2.0D - this.getY(),
                    this.followingPlayer.getZ() - this.getZ()
            );
            double distSqr = targetVec.lengthSqr();
            if (distSqr < 64.0D) {
                double speedFactor = 1.0D - Math.sqrt(distSqr) / 8.0D;
                this.setDeltaMovement(this.getDeltaMovement().add(targetVec.normalize().scale(speedFactor * speedFactor * 0.1D)));
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        float friction = 0.98F;
        if (this.onGround()) {
            BlockPos pos = this.getBlockPosBelowThatAffectsMyMovement();
            friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));
        }

        ++this.age;
        if (this.age >= 6000) {
            this.discard();
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide) {
            if (this.pickupDelay == 0) {
                player.take(this, 1);

                double currentEnergy = DataAttachmentRegistry.getEnergy(player);
                double addAmount = this.getValue();
                double maxEnergy = player.getAttributeValue(YAttributes.MAX_ENERGY);
                double newEnergy = Math.min(maxEnergy, currentEnergy + addAmount);

                DataAttachmentRegistry.setEnergy(player, newEnergy);
                PacketDistributor.sendToPlayer((ServerPlayer) player, new EnergyPacket(newEnergy));

                this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F, 0.5F * ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.8F));
                this.discard();
            }
        }
    }

    private void scanForPlayer() {
        if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0D) {
            this.followingPlayer = this.level().getNearestPlayer(this, 8.0D);
        }
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.99F, Math.min(vec3.y + 5.0E-4F, 0.06F), vec3.z * 0.99F);
    }

    public int getValue() {
        return this.entityData.get(VALUE);
    }

    public void setValue(int value) {
        this.entityData.set(VALUE, value);
    }

    public boolean isLarge() {
        return this.getValue() >= 50;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (this.level().isClientSide) {
            return true;
        } else {
            this.markHurt();
            this.health = (int) ((float) this.health - amount);
            if (this.health <= 0) {
                this.discard();
            }

            return true;
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) ||
                source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.health = tag.getShort("Health");
        this.age = tag.getShort("Age");
        this.setValue(tag.getInt("Value"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("Health", (short) this.health);
        tag.putShort("Age", (short) this.age);
        tag.putInt("Value", this.getValue());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
}