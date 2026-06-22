package muddy.domain_lib.mixin;

import muddy.domain_lib.block.custom.DomainAirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    public void domain$tick(CallbackInfo ci) {
        Level level = ((LivingEntity)(Object)this).level();
        if (level != null) {
            domain$inDomainAirBlock(level);
        }
    }

    @Unique
    public void domain$inDomainAirBlock(Level level) {
        BlockPos entityBlockPos = ((LivingEntity) (Object) this).blockPosition();

        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainAirBlock) {
            Block blockEntitysInsideOf = level.getBlockState(entityBlockPos).getBlock();

            if (blockEntitysInsideOf instanceof DomainAirBlock domainAir) {
                if (domainAir.getDomainEffect() != null) {
                    if (!((LivingEntity)(Object)this).hasEffect(domainAir.getDomainEffect())) {
                        ((LivingEntity)(Object)this).addEffect(new MobEffectInstance(domainAir.getDomainEffect(), 20));
                    }
                }
            }
        }

    }
}
