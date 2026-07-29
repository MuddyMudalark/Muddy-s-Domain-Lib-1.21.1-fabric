package muddy.domain_framework.mixin;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.util.RandomEntityAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ThrownEnderpearl.class)
public class AntiEnderpearlMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void domain$tick(CallbackInfo ci) {
        ThrownEnderpearl enderpearl = ((ThrownEnderpearl)(Object)this);

        RandomSource random = RandomSource.create();

        if (enderpearl.level().getBlockState(enderpearl.blockPosition()).getBlock() instanceof DomainAirBlock || enderpearl.level().getBlockState(enderpearl.blockPosition()).getBlock() instanceof DomainClashAirBlock) {
            enderpearl.level().addParticle(
                    ParticleTypes.PORTAL,
                    enderpearl.getX(),
                    enderpearl.getY() + random.nextDouble() * 2.0,
                    enderpearl.getZ(),
                    random.nextGaussian(),
                    0.0,
                    random.nextGaussian());

            enderpearl.discard();
        }
    }
}
