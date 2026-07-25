package muddy.domain_framework.util;

import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import muddy.domain_framework.block.custom.DomainAirBlock;

public interface DomainAirSignalGetter extends BlockGetter {
    Direction[] DIRECTIONS = Direction.values();

    default boolean getIfNextToDomainAir(BlockPos blockPos) {
        if (this.getBlockState(blockPos).getBlock() != null) {
            return this.getBlockState(blockPos).getBlock() instanceof DomainAirBlock;
        }

        return false;
    }

    default boolean getDirectionWhereNextToDomainAir(BlockPos blockPos, Direction direction) {
        boolean up = false;
        boolean down = false;
        boolean north = false;
        boolean south = false;
        boolean east = false;
        boolean west = false;
        
        for (Direction DIRECTION : DIRECTIONS) {
            if (DIRECTION == Direction.UP) {
                up=getIfNextToDomainAir(blockPos.above());
            }
            if (DIRECTION == Direction.DOWN) {
                down=getIfNextToDomainAir(blockPos.below());
            }
            if (DIRECTION == Direction.NORTH) {
                north = getIfNextToDomainAir(blockPos.north());
            }
            if (DIRECTION == Direction.SOUTH) {
                south = getIfNextToDomainAir(blockPos.south());
            }
            if (DIRECTION == Direction.EAST) {
                east = getIfNextToDomainAir(blockPos.east());
            }
            if (DIRECTION == Direction.WEST) {
                west = getIfNextToDomainAir(blockPos.west());
            }
        }

        if (direction == Direction.UP) {
            return getIfNextToDomainAir(blockPos.above());
        }
        if (direction == Direction.DOWN) {
            return getIfNextToDomainAir(blockPos.below());
        }
        if (direction == Direction.NORTH) {
            return getIfNextToDomainAir(blockPos.north());
        }
        if (direction == Direction.SOUTH) {
            return getIfNextToDomainAir(blockPos.south());
        }
        if (direction == Direction.EAST) {
            return getIfNextToDomainAir(blockPos.east());
        }
        if (direction == Direction.WEST) {
            return getIfNextToDomainAir(blockPos.west());
        }
        
        return false;
    }
    
}
