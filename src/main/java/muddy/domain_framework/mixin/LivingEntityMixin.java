package muddy.domain_framework.mixin;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.util.HasDomainExpanded;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements HasDomainExpanded {
    @Unique
    public boolean hasDomainExpanded = false;

    @Inject(method = "tick", at = @At("TAIL"))
    public void domain$tick(CallbackInfo ci) {
        LivingEntity thisEntity = ((LivingEntity) (Object) this);
        Level level = thisEntity.level();

        if (level != null) {
            domain$inDomainAirBlock(level);
        }
    }

    @Unique
    public void domain$inDomainAirBlock(Level level) {
        LivingEntity thisEntity = ((LivingEntity) (Object) this);
        BlockPos entityBlockPos = thisEntity.blockPosition();
        BlockPos changePos = entityBlockPos;

        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainAirBlock domainAir) {
            UUID ownerUUID = domainAir.getDomainOwnerUUID();
            if (ownerUUID != null) {


                if (!thisEntity.getUUID().equals(ownerUUID)) {
                    if (!domainAir.getDomainEffect().equals(null)) {
                        if (!thisEntity.hasEffect(domainAir.getDomainEffect())) {

                            thisEntity.addEffect(new MobEffectInstance(
                                    domainAir.getDomainEffect(),
                                    domainAir.getDomainEffectLength(),
                                    0,
                                    false,
                                    false
                                    )
                            );
                        }
                    }
                }
            }
            if (!level.isClientSide()) {
                if (!domainAir.getHasExpandedFully()) {
                    thisEntity.setDeltaMovement(Vec3.ZERO);
                    thisEntity.setPos(entityBlockPos.getBottomCenter());
                }
            }
        }
        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainClashAirBlock domainClashAir) {
            if (!level.isClientSide()) {
                if (!domainClashAir.havePlayersBeenTeleported()) {
                    thisEntity.setDeltaMovement(Vec3.ZERO);
                    thisEntity.setPos(entityBlockPos.getBottomCenter());
                }
            }
        }
        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainBarrierBlock domainBarrier) {
            if (domainBarrier.getCenterOfDomain() != null) {
                if (entityBlockPos.getY() < domainBarrier.getCenterOfDomain().getY()) {
                    thisEntity.setPos(entityBlockPos.getX(), domainBarrier.getCenterOfDomain().getY(), entityBlockPos.getZ());
                } else if (thisEntity.getX() > domainBarrier.getCenterOfDomain().getX() || thisEntity.getX() < domainBarrier.getCenterOfDomain().getX()) {
                    int direction = (thisEntity.getX() > domainBarrier.getCenterOfDomain().getX() && thisEntity.getZ() != domainBarrier.getCenterOfDomain().getZ()) ? -1 : 1;

                    changePos = changePos.offset(direction, 0, 0);
                }
                if (thisEntity.getZ() > domainBarrier.getCenterOfDomain().getZ() || thisEntity.getZ() < domainBarrier.getCenterOfDomain().getZ()) {
                    int direction = (thisEntity.getZ() > domainBarrier.getCenterOfDomain().getZ() && thisEntity.getZ() != domainBarrier.getCenterOfDomain().getZ()) ? -1 : 1;

                    changePos = changePos.offset(0, 0, direction);
                }
            }
        }
        if ((level.getBlockState(changePos).getBlock() instanceof DomainClashAirBlock || level.getBlockState(changePos).getBlock() instanceof DomainAirBlock) && level.getBlockState(entityBlockPos).getBlock() instanceof DomainBarrierBlock) {
            thisEntity.setPos(changePos.getBottomCenter());
        }
    }

    @Override
    public boolean domain$hasDomainExpanded() {
        return hasDomainExpanded;
    }

    @Override
    public void domain$setHasDomainExpanded(boolean hasDomainExpanded) {
        this.hasDomainExpanded = hasDomainExpanded;
    }
}
