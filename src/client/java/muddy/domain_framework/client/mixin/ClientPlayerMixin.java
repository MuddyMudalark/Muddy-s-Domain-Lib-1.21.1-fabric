package muddy.domain_framework.client.mixin;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.client.utils.DomainCenterPosition;
import muddy.domain_framework.util.HasDomainExpanded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerMixin implements DomainCenterPosition {
    @Unique
    BlockPos currentDomainCenter = BlockPos.ZERO;

    @Inject(method = "tick", at = @At("TAIL"))
    public void domain$fuckClientLevels(CallbackInfo ci) {
        LivingEntity thisEntity = ((LivingEntity) (Object) this);
        Level level = thisEntity.level();

        if (level != null) {
            domain$inDomainAirBlock(level);

            Block block = level.getBlockState(thisEntity.blockPosition()).getBlock();

            boolean insideBlockOfDomains = (block instanceof DomainAirBlock) || (block instanceof DomainBarrierBlock);

            if (insideBlockOfDomains) {
                if (block instanceof DomainAirBlock domainAirBlock) {
                    currentDomainCenter = domainAirBlock.getCenterPosition();
                }
                if (block instanceof DomainBarrierBlock domainBarrierBlock) {
                    currentDomainCenter = domainBarrierBlock.getCenterOfDomain();
                }
            } else {
                assert Minecraft.getInstance().player != null;
                ((HasDomainExpanded) Minecraft.getInstance().player).domain$setHasDomainExpanded(false);
            }
        }
    }

    @Unique
    public void domain$inDomainAirBlock(Level level) {
        LivingEntity thisEntity = ((LivingEntity) (Object) this);
        BlockPos entityBlockPos = thisEntity.blockPosition();

        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainAirBlock) {
            if (level.isClientSide()) {
                if (thisEntity instanceof Player player) {
                    if (!((HasDomainExpanded) player).domain$hasDomainExpanded()) {
                        assert Minecraft.getInstance().player != null;
                        if (Minecraft.getInstance().player.getUUID().equals(player.getUUID())) {
                            player.setDeltaMovement(Vec3.ZERO);
                            player.setPos(entityBlockPos.getBottomCenter());
                        }
                    }
                }
            }
        }
        if (level.getBlockState(entityBlockPos).getBlock() instanceof DomainClashAirBlock) {
            if (level.isClientSide()) {
                if (thisEntity instanceof Player player) {
                    if (!((HasDomainExpanded)player).domain$hasDomainExpanded()) {
                        if (!((HasDomainExpanded) player).domain$hasDomainExpanded()) {
                            assert Minecraft.getInstance().player != null;
                            if (Minecraft.getInstance().player.getUUID().equals(player.getUUID())) {
                                player.setDeltaMovement(Vec3.ZERO);
                                player.setPos(entityBlockPos.getBottomCenter());
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public BlockPos domain$getDomainCenter() {
        return currentDomainCenter;
    }

    @Override
    public boolean domain$shouldRenderInternalDomain() {
        LivingEntity thisEntity = ((LivingEntity) (Object) this);

        return false;
    }

    @Override
    public void domain$setDomainCenter(BlockPos domainCenter) {
        this.currentDomainCenter = domainCenter;
    }
}
