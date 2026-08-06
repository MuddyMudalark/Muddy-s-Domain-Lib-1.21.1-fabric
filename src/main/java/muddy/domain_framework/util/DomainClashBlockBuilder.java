package muddy.domain_framework.util;


import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

public class DomainClashBlockBuilder {
    public static void buildHollowInside(Level level, BlockPos centerPos, int radius, boolean havePlayersBeenTeleported) {
        radius -= 1;
        DomainClashAirBlock domainClashAir = (DomainClashAirBlock) ModBlocks.DOMAIN_CLASH_AIR_BLOCK;
        domainClashAir.of(havePlayersBeenTeleported);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    int distanceSquare = x * x + y * y + z * z;

                    if (distanceSquare <= radius * radius) {
                        BlockPos pos = centerPos.offset(x, y, z);

                        level.setBlockAndUpdate(pos, domainClashAir.defaultBlockState());
                    }
                }
            }
        }

    }

    public static void buildSectionOfSurface(Level level, BlockPos centerPos, int radius, int playerCount, int degreesPerPlayer, List<ResourceLocation> clashingShaderPaths) {
        DomainBarrierBlock domainBarrierBlock = (DomainBarrierBlock) ModBlocks.DOMAIN_BARRIER_BLOCK;
        domainBarrierBlock.setCenterOfDomain(centerPos);

        for (int playerIndex = playerCount; playerIndex > 0; playerIndex--) {
            int angledCutOfDomain = degreesPerPlayer * playerIndex;
            int maximum = (int) (((double) angledCutOfDomain / 360) * (Math.PI * Math.pow(radius, 2.0)));

            ResourceLocation useShaderPath = clashingShaderPaths.get(playerIndex);
            domainBarrierBlock.setShaderPath(useShaderPath);

            BlockPos maximumPoint = centerPos.offset(maximum, 0, maximum);

            for (int x = 0; x <= radius; x++) {
                if (x <= maximumPoint.getX()) {
                    for (int z = 0; z <= radius; z++) {
                        if (z <= maximumPoint.getZ()) {
                            BlockPos pos = centerPos.offset(x, centerPos.getY(), z);

                            level.removeBlockEntity(pos);
                            level.setBlockAndUpdate(pos, domainBarrierBlock.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}
