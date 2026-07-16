package muddy.domain_framework.mixin;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.util.RandomEntityAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public class AntiEnderpearlMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void domain$tick(CallbackInfo ci) {
        ThrownEnderpearl enderpearl = ((ThrownEnderpearl)(Object)this);

        if (enderpearl.level().getBlockState(enderpearl.blockPosition()).getBlock() instanceof DomainAirBlock || enderpearl.level().getBlockState(enderpearl.blockPosition()).getBlock() instanceof DomainClashAirBlock) {
            enderpearl.level().addParticle(
                    ParticleTypes.PORTAL,
                    enderpearl.getX(),
                    enderpearl.getY() + ((RandomEntityAccessor)enderpearl).domain$antiModderArchitecture().nextDouble() * 2.0,
                    enderpearl.getZ(),
                    ((RandomEntityAccessor)enderpearl).domain$antiModderArchitecture().nextGaussian(),
                    0.0,
                    ((RandomEntityAccessor)enderpearl).domain$antiModderArchitecture().nextGaussian());

            enderpearl.discard();
        }
    }
}
