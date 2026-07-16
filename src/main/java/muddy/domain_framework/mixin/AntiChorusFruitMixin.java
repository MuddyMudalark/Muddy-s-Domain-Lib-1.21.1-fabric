package muddy.domain_framework.mixin;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChorusFruitItem.class)
public class AntiChorusFruitMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    public void domain$finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (level.getBlockState(livingEntity.blockPosition()).getBlock() instanceof DomainAirBlock || level.getBlockState(livingEntity.blockPosition()).getBlock() instanceof DomainClashAirBlock) {
            itemStack.shrink(1);

            level.addParticle(ParticleTypes.PORTAL, 1, 1, 1, 1, 1, 1);

            cir.setReturnValue(itemStack);
        }
    }
}
