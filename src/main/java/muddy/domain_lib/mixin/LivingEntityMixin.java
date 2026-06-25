package muddy.domain_lib.mixin;

import muddy.domain_lib.MuddysDomainLib;
import muddy.domain_lib.block.custom.DomainAirBlock;
import muddy.domain_lib.block.custom.DomainBarrierBlock;
import muddy.domain_lib.entity.custom.DomainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    public void domain$tick(CallbackInfo ci) {
        Level level = ((LivingEntity) (Object) this).level();
        if (level != null) {
            domain$inDomainAirBlock(level);
        }
    }


    @Unique
    public void domain$inDomainAirBlock(Level level) {
        LivingEntity thisEntity = ((LivingEntity) (Object) this);
        BlockPos entityBlockPos = thisEntity.blockPosition();

        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainAirBlock domainAir) {
            UUID ownerUUID = domainAir.getDomainOwnerUUID();
            if (ownerUUID != null) {
                if (!thisEntity.getUUID().equals(ownerUUID)) {
                    if (!domainAir.getDomainEffect().equals(null)) {
                        if (!thisEntity.hasEffect(domainAir.getDomainEffect())) {

                            thisEntity.addEffect(new MobEffectInstance(domainAir.getDomainEffect(),
                                    domainAir.getDomainEffectLength(),
                                    0,
                                    false,
                                    false));
                        }
                    }
                }
            }
        }
    }
}
