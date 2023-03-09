package com.ilmusu.musuen.mixins.mixin;

import com.ilmusu.musuen.Resources;
import com.ilmusu.musuen.callbacks.*;
import com.ilmusu.musuen.mixins.interfaces._IEntityTrackableDrops;
import com.ilmusu.musuen.utils.ModUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

public abstract class CustomCallbacksMixins
{
    @Mixin(TridentEntity.class)
    public abstract static class TridentEntityCallbacks
    {
        @Inject(method = "onEntityHit", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/LivingEntity;getGroup()Lnet/minecraft/entity/EntityGroup;"
        ))
        private void beforeComputingEnchantmentDamage(EntityHitResult result, CallbackInfo ci)
        {
            TridentEntity trident = (TridentEntity)(Object)this;
            Entity owner = ((TridentEntity)(Object)this).getOwner();
            PlayerAttackCallback.BEFORE_ENCHANTMENT_DAMAGE.invoker().handler(owner, trident.tridentStack, result.getEntity(), Hand.MAIN_HAND);
        }

        @Inject(method = "onEntityHit", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/enchantment/EnchantmentHelper;getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F",
                shift = At.Shift.AFTER
        ))
        private void afterComputingEnchantmentDamage(EntityHitResult result, CallbackInfo ci)
        {
            TridentEntity trident = (TridentEntity)(Object)this;
            Entity owner = ((TridentEntity)(Object)this).getOwner();
            PlayerAttackCallback.AFTER_ENCHANTMENT_DAMAGE.invoker().handler(owner, trident.tridentStack, result.getEntity(), Hand.MAIN_HAND);
        }
    }

    @Mixin(TridentItem.class)
    public abstract static class TridentItemCallbacks
    {
        @Inject(method = "onStoppedUsing", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                shift = At.Shift.AFTER
        ))
        private void afterShootingTrident(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci,
                                          PlayerEntity player, int i, int j, TridentEntity projectile)
        {
            ProjectileShotCallback.AFTER.invoker().handler(user, projectile.tridentStack, projectile);
        }
    }

    @Mixin(BowItem.class)
    public abstract static class ArrowItemCallbacks
    {
        @Inject(method = "onStoppedUsing", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                shift = At.Shift.AFTER
        ))
        private void afterArrowEntityCreated(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci,
                                             PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem,
                                             PersistentProjectileEntity projectile)
        {
            ProjectileShotCallback.AFTER.invoker().handler(user, stack, projectile);
        }
    }

    @Mixin(CrossbowItem.class)
    public abstract static class CrossbowItemCallbacks
    {
        @Inject(method = "loadProjectiles", at = @At("HEAD"))
        private static void beforeLoadingProjectiles(LivingEntity shooter, ItemStack crossbow, CallbackInfoReturnable<Boolean> cir)
        {
            ProjectileLoadCallback.BEFORE.invoker().handler(shooter, crossbow);
        }

        @Inject(method = "shoot", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                shift = At.Shift.AFTER
        ))
        private static void afterShootingProjectile(World world, LivingEntity shooter, Hand hand, ItemStack crossbow,
                                                    ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated,
                                                    CallbackInfo ci, boolean isFirework, ProjectileEntity projectileEntity)
        {
            ProjectileShotCallback.AFTER.invoker().handler(shooter, crossbow, projectileEntity);
        }

        @Inject(method = "postShoot", at = @At("HEAD"))
        private static void afterShootingAllProjectiles(World world, LivingEntity shooter, ItemStack stack, CallbackInfo ci)
        {
            ProjectileShotCallback.AFTER_MULTIPLE.invoker().handler(shooter, stack, null);
        }
    }

    @Mixin(PersistentProjectileEntity.class)
    public abstract static class PersistentProjectileEntityCallbacks
    {
        private static boolean isArrowReflectedHook = false;

        @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setFireTicks(I)V"))
        private void afterProjectileReflectedHook(EntityHitResult entityHitResult, CallbackInfo ci)
        {
            PersistentProjectileEntityCallbacks.isArrowReflectedHook = true;
        }

        @Inject(method = "onEntityHit", at = @At("TAIL"))
        private void afterProjectileReflected(EntityHitResult hit, CallbackInfo ci)
        {
            if(!PersistentProjectileEntityCallbacks.isArrowReflectedHook)
                return;

            PersistentProjectileEntityCallbacks.isArrowReflectedHook = false;
            ProjectileReflectionCallback.AFTER.invoker().handler(hit, (PersistentProjectileEntity)(Object)this);
        }
    }

    @Mixin(PlayerEntity.class)
    public abstract static class PlayerCallbacks
    {
        @Inject(method = "attack", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D"
        ))
        private void beforeComputingEnchantmentDamage(Entity target, CallbackInfo ci)
        {
            PlayerEntity player = (PlayerEntity)(Object)this;
            PlayerAttackCallback.BEFORE_ENCHANTMENT_DAMAGE.invoker().handler(player, player.getMainHandStack(), target, Hand.MAIN_HAND);
        }

        @Inject(method = "attack", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackCooldownProgress(F)F"
        ))
        private void afterComputingEnchantmentDamage(Entity target, CallbackInfo ci)
        {
            PlayerEntity player = (PlayerEntity)(Object)this;
            PlayerAttackCallback.AFTER_ENCHANTMENT_DAMAGE.invoker().handler(player, player.getMainHandStack(), target, Hand.MAIN_HAND);
        }

        @ModifyVariable(method = "getBlockBreakingSpeed", at = @At(
                value = "TAIL",
                shift = At.Shift.BEFORE
        ))
        private float afterComputingBlockBreakingSpeed(float speed)
        {
            PlayerEntity player = (PlayerEntity)(Object)this;
            BlockPos pos = ModUtils.getCurrentMiningPos(player);

            // If the player is not actually mining a block, return the default speed
            if(pos == null)
                return speed;

            // Multiplying the speed with the computed multiplier
            float multiplier = PlayerBreakSpeedCallback.AFTER.invoker().handler(player, player.getMainHandStack(), pos);
            return speed * multiplier;
        }

        @Inject(method = "handleFallDamage", at = @At("HEAD"))
        private void onPlayerLandingOnBlock(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir)
        {
            PlayerLandCallback.EVENT.invoker().handler((PlayerEntity)(Object)this, fallDistance);
        }

        @Inject(method = "tick", at = @At("HEAD"))
        private void afterPlayerTick(CallbackInfo ci)
        {
            PlayerTickCallback.BEFORE.invoker().handler((PlayerEntity)(Object)this);
        }

        @Inject(method = "tick", at = @At("TAIL"))
        private void beforePlayerTick(CallbackInfo ci)
        {
            PlayerTickCallback.AFTER.invoker().handler((PlayerEntity)(Object)this);
        }

        @Inject(method = "equipStack", at = @At("TAIL"))
        private void afterEquippingStack(EquipmentSlot slot, ItemStack stack, CallbackInfo ci)
        {
            if(slot.getType() == EquipmentSlot.Type.ARMOR)
                PlayerEquipCallback.ARMOR.invoker().handler((PlayerEntity) (Object) this, stack, slot);
            else if(slot == EquipmentSlot.MAINHAND)
                PlayerEquipCallback.MAINHAND.invoker().handler((PlayerEntity) (Object) this, stack, slot);
        }

        @Inject(method = "dropInventory", at = @At("TAIL"))
        private void afterDroppingInventory(CallbackInfo ci)
        {
            PlayerDropInventoryCallback.AFTER.invoker().handler((PlayerEntity)(Object)this);
        }
    }

    @Mixin(PlayerInventory.class)
    public abstract static class PlayerInventoryCallbacks
    {
        @Inject(method = "setStack", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"
        ))
        private void afterSettingStackInPlayerInventory(int slot, ItemStack stack, CallbackInfo ci, DefaultedList<ItemStack> list)
        {
            PlayerInventory inventory = (PlayerInventory)(Object)this;
            if(list != inventory.armor)
                return;

            EquipmentSlot equipmentSlot = EquipmentSlot.values()[2+slot];
            PlayerEquipCallback.ARMOR.invoker().handler(inventory.player, stack, equipmentSlot);
        }
    }

    @Mixin(FireworkRocketEntity.class)
    public abstract static class FireworkRocketEntityCallbacks
    {
        @Shadow private @Nullable LivingEntity shooter;

        @ModifyVariable(method = "tick", at = @At("STORE"))
        private Vec3d beforeComputingElytraVelocity(Vec3d rotation)
        {
            return FireworkElytraSpeedCallback.EVENT.invoker().handler(this.shooter, (FireworkRocketEntity)(Object)this, rotation);
        }
    }

    @Mixin(ProjectileEntity.class)
    public abstract static class ProjectileEntityCallback
    {
        @Inject(method = "onCollision", at = @At("TAIL"))
        private void afterCollision(HitResult hitResult, CallbackInfo ci)
        {
            ProjectileHitCallback.AFTER.invoker().handler((ProjectileEntity)(Object)this, hitResult);
        }
    }

    @Mixin(LivingEntity.class)
    public abstract static class LivingEntityCallbacks
    {
        @Shadow public abstract boolean isFallFlying();
        @Shadow protected abstract void jump();

        @Shadow private int jumpingCooldown;

        private static boolean hasCheckedForJumpAndFailed = false;
        private static DamageSource shieldHitDamageSourceHook;

        @Inject(method = "travel", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/LivingEntity;setFlag(IZ)V",
                shift = At.Shift.AFTER
        ))
        private void beforeElytraLanding(CallbackInfo ci)
        {
            if(this.isFallFlying())
                return;
            LivingEntityElytraLandCallback.EVENT.invoker().handler((LivingEntity)(Object)this);
        }

        @ModifyVariable(method = "modifyAppliedDamage", at = @At(value = "LOAD", ordinal = 4), argsOnly = true)
        private float beforeApplyingProtectionToDamage(float damage, DamageSource source)
        {
            if(damage <= 0)
                return damage;

            LivingEntity entity = (LivingEntity)(Object)this;
            return LivingEntityDamageCallback.BEFORE_PROTECTION.invoker().handler(entity, source, damage);
        }

        @ModifyVariable(method = "handleFallDamage", at = @At(value = "LOAD", ordinal = 0))
        private int beforeApplyingFallDamageToLiving(int damage)
        {
            if(damage <= 0)
                return damage;

            LivingEntity entity = (LivingEntity)(Object)this;
            return (int)LivingEntityDamageCallback.BEFORE_FALL.invoker().handler(entity, DamageSource.FALL, damage);
        }

        @Redirect(method = "tickMovement", at = @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/LivingEntity;onGround:Z",
                opcode = Opcodes.GETFIELD,
                ordinal = 2
        ))
        private boolean beforeOnGroundJumpCheck(LivingEntity instance)
        {
            LivingEntityCallbacks.hasCheckedForJumpAndFailed = !instance.isOnGround();
            return instance.isOnGround();
        }

        @Inject(method = "tickMovement", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
                ordinal = 2
        ))
        private void afterJumpCheck(CallbackInfo ci)
        {
            if(!LivingEntityCallbacks.hasCheckedForJumpAndFailed)
                return;

            LivingEntityCallbacks.hasCheckedForJumpAndFailed = false;
            LivingEntity entity = (LivingEntity)(Object)this;
            if(LivingEntityJumpCheckCallback.EVENT.invoker().handler(entity, this.jumpingCooldown))
            {
                this.jump();
                this.jumpingCooldown = 10;
            }
        }

        @Inject(method = "jump", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z")
        )
        private void onLivingJump(CallbackInfo ci)
        {
            LivingEntity entity = (LivingEntity)(Object)this;
            Vec3d velocity = LivingEntityJumpCallback.EVENT.invoker().handler(entity, entity.getVelocity());
            entity.setVelocity(velocity);
        }

        @Inject(method = "blockedByShield", at = @At("HEAD"))
        private void onShieldBlockHook(DamageSource source, CallbackInfoReturnable<Boolean> cir)
        {
            LivingEntityCallbacks.shieldHitDamageSourceHook = source;
        }

        @ModifyConstant(method = "blockedByShield", constant = @Constant(doubleValue = 0.0, ordinal = 1))
        private double onShieldBlock(double constant)
        {
            DamageSource source = LivingEntityCallbacks.shieldHitDamageSourceHook;
            LivingEntityCallbacks.shieldHitDamageSourceHook = null;

            LivingEntity user = (LivingEntity)(Object)this;
            ItemStack stack = user.getActiveItem();
            if(!(stack.getItem() instanceof ShieldItem))
                return constant;

            return ShieldCoverageAngleCallback.BEFORE.invoker().handler(user, stack, source);
        }
    }

    @Mixin(Entity.class)
    public abstract static class EntityCallbacks
    {
        @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;",
                locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
        ))
        private void beforeEntityDropsItemStack(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir, ItemEntity item)
        {
            DamageSource source = ((_IEntityTrackableDrops)this).getDeathDamageSource();
            Entity entity = (Entity)(Object)this;
            boolean shouldDrop = EntityDropCallback.EVENT.invoker().handler(entity, item, source);
            if(!shouldDrop)
                cir.setReturnValue(null);
        }
    }

    @Mixin(HeldItemRenderer.class)
    public abstract static class HeldItemRendererMixins
    {
        @Shadow private ItemStack mainHand;
        @Shadow private ItemStack offHand;

        @Inject(method = "updateHeldItems", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
                ordinal = 0
        ))
        private void beforeCheckingStackEquality(CallbackInfo ci)
        {
            PlayerEntity player = MinecraftClient.getInstance().player;

            // First check is for the main hand item
            ItemStack newMainHand = player.getMainHandStack();
            if(newMainHand != this.mainHand && newMainHand.getItem() == this.mainHand.getItem())
                if(shouldPreventNbtChangeAnimation(this.mainHand, newMainHand))
                    this.mainHand = newMainHand;

            // Then for the off hand item
            ItemStack newOffHand = player.getOffHandStack();
            if(newOffHand != this.offHand && newOffHand.getItem() == this.mainHand.getItem())
                if(shouldPreventNbtChangeAnimation(this.offHand, newOffHand))
                    this.offHand = newOffHand;
        }

        private static boolean shouldPreventNbtChangeAnimation(ItemStack prevStack, ItemStack newStack)
        {
            return  (newStack.hasNbt() && newStack.getNbt().getBoolean(Resources.DONT_ANIMATE_TAG)) ||
                    (prevStack.hasNbt() && prevStack.getNbt().getBoolean(Resources.DONT_ANIMATE_TAG));
        }
    }

    @Mixin(EntityRenderDispatcher.class)
    public abstract static class EntityRenderDispatcherCallbacks<E extends Entity>
    {
        @Inject(method = "render", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                shift = At.Shift.AFTER
        ))
        private void afterRenderingEntity(E entity, double x, double y, double z, float yaw, float tickDelta,
                                          MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
        {
            EntityRendererCallback.AFTER.invoker().handler(entity, matrices, tickDelta, vertexConsumers, light);
        }
    }

    @Mixin(GameRenderer.class)
    public static abstract class GameRendererCallbacks
    {
        private PlayerFovMultiplierCallback.FovParams fovMultiplierParamsHook;

        @ModifyVariable(method = "updateFovMultiplier", at = @At("STORE"))
        protected float afterComputingNewFovMultiplier(float multiplier)
        {
            PlayerEntity player = MinecraftClient.getInstance().player;
            this.fovMultiplierParamsHook = PlayerFovMultiplierCallback.AFTER.invoker().handler(player);

            if(this.fovMultiplierParamsHook.shouldNotChange())
                return multiplier;

            return multiplier * this.fovMultiplierParamsHook.getMultiplier();
        }

        @ModifyConstant(method = "updateFovMultiplier", constant = @Constant(floatValue = 0.5F))
        protected float beforeUpdatingCurrentFovMultiplier(float constant)
        {
            return this.fovMultiplierParamsHook.getUpdateVelocityOr(constant);
        }

        @ModifyConstant(method = "updateFovMultiplier", constant = @Constant(floatValue = 1.5F))
        protected float beforeClampingCurrentFovMultiplier(float constant)
        {
            if(this.fovMultiplierParamsHook.isUnclamped())
                return 2000.0F;
            return constant;
        }
    }
}