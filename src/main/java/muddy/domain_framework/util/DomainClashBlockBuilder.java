package muddy.domain_framework.util;


import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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

    public static void buildSurfaceWithRandomSections(Level level, BlockPos centerPos, int radius, int playerCount, List<String> clashingShaderPaths) {
        DomainBarrierBlock domainBarrierBlock = (DomainBarrierBlock) ModBlocks.DOMAIN_BARRIER_BLOCK;
        domainBarrierBlock.setCenterOfDomain(centerPos);

        for (int playerIndex = playerCount - 1; playerIndex > 0; playerIndex--) {
            RandomSource randomSource = RandomSource.create();
            int random = randomSource.nextInt(0, playerCount - 1);

            domainBarrierBlock.setShaderName(clashingShaderPaths.get(random));

            for (int x = 0; x <= radius; x++) {
                for (int z = 0; z <= radius; z++) {
                    BlockPos pos = centerPos.offset(x, centerPos.getY(), z);

                    level.removeBlockEntity(pos);
                    level.setBlockAndUpdate(pos, domainBarrierBlock.defaultBlockState());
                }

            }
        }
    }

    public static void buildHollowSphereDynamicallyWithRandomSections(Level level, BlockPos centerPos, int radius, int yValue, int playerCount, List<String> clashingShaderPaths) {
        int outerSquare = radius * radius;
        int innerSquare = (radius - 1) * (radius - 1);

        DomainBarrierBlock barrierBlock = (DomainBarrierBlock) ModBlocks.DOMAIN_BARRIER_BLOCK;
        barrierBlock.setCenterOfDomain(centerPos);

        for (int playerIndex = playerCount - 1; playerIndex > 0; playerIndex--) {
            RandomSource randomSource = RandomSource.create();
            int random = randomSource.nextInt(0, playerCount - 1);

            barrierBlock.setShaderName(clashingShaderPaths.get(random));

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    if (y <= yValue) {
                        for (int z = -radius; z <= radius; z++) {
                            int distanceSquare = x * x + y * y + z * z;

                            if (y < 0) {
                                if (distanceSquare <= radius * radius) {
                                    BlockPos pos = centerPos.offset(x, y, z);

                                    level.setBlockAndUpdate(pos, barrierBlock.defaultBlockState());
                                }
                            } else if (distanceSquare <= outerSquare && distanceSquare >= innerSquare) {
                                BlockPos pos = centerPos.offset(x, y, z);

                                level.setBlockAndUpdate(pos, barrierBlock.defaultBlockState());
                            }

                        }
                    }

                }
            }

        }

    }
}
