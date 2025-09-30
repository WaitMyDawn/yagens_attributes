package yagen.waitmydawn.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.server.level.ServerPlayer;
import yagen.waitmydawn.registries.EntityRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

public class BladeEntity extends ThrowableProjectile {

    public BladeEntity(EntityType<? extends ThrowableProjectile> type, Level lvl) {
        super(type, lvl);
    }

    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(BladeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final ItemStack DEFAULT = new ItemStack(ItemRegistry.BLADE.get());

    public BladeEntity(Level lvl, LivingEntity shooter) {
        super(EntityRegistry.DAGGER.get(), shooter, lvl);
        this.entityData.set(DATA_ITEM, DEFAULT.copy());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, DEFAULT.copy());
    }

    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;

        if (getOwner() instanceof ServerPlayer player &&
                result.getEntity() instanceof LivingEntity target &&
                target.isAlive()) {
            player.attack(target);
        }
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (tickCount > 60) discard();
    }
}