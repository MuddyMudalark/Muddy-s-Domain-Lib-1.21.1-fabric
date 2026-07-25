package muddy.domain_framework.client.mixin;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.util.HasDomainExpanded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    public void domain$fuckClientLevels(CallbackInfo ci) {
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

        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainAirBlock) {
            if (level.isClientSide()) {
                if (thisEntity instanceof Player player) {
                    if (!((HasDomainExpanded)player).domain$hasDomainExpanded() && Minecraft.getInstance().player.getUUID().equals(player.getUUID())) {
                        player.setDeltaMovement(Vec3.ZERO);
                        player.setPos(entityBlockPos.getBottomCenter());
                    }
                }
            }
        }
        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainClashAirBlock) {
            if (level.isClientSide()) {
                if (thisEntity instanceof Player player) {
                    if (!((HasDomainExpanded)player).domain$hasDomainExpanded()) {
                        if (!((HasDomainExpanded)player).domain$hasDomainExpanded() && Minecraft.getInstance().player.getUUID().equals(player.getUUID())) {
                            player.setDeltaMovement(Vec3.ZERO);
                            player.setPos(entityBlockPos.getBottomCenter());
                        }
                    }
                }
            }
        }

    }

}
