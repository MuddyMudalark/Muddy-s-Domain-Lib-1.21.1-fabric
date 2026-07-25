package muddy.domain_framework.block.custom;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.util.DomainAirSignalGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DomainBarrierBlock extends Block {
    private BlockPos centerOfDomain;

    public static final BooleanProperty POSITIVE_X = BooleanProperty.create("pos_x");
    public static final BooleanProperty NEGATIVE_X = BooleanProperty.create("min_x");
    public static final BooleanProperty POSITIVE_Y = BooleanProperty.create("pos_y");
    public static final BooleanProperty NEGATIVE_Y = BooleanProperty.create("min_y");
    public static final BooleanProperty POSITIVE_Z = BooleanProperty.create("pos_z");
    public static final BooleanProperty NEGATIVE_Z = BooleanProperty.create("min_z");

    public static final Map<Direction, BooleanProperty> DIRECTION_SIDES =
            Map.of(
                    Direction.EAST, POSITIVE_X,
                    Direction.WEST, NEGATIVE_X,
                    Direction.UP, POSITIVE_Y,
                    Direction.DOWN, NEGATIVE_Y,
                    Direction.SOUTH, POSITIVE_Z,
                    Direction.NORTH, NEGATIVE_Z
            );

    public static final List<BooleanProperty> SIDES = List.of(POSITIVE_X, NEGATIVE_X, POSITIVE_Y, NEGATIVE_Y, POSITIVE_Z, NEGATIVE_Z);

    public DomainBarrierBlock(Properties properties) {
        super(properties);

        for (BooleanProperty SIDE : SIDES) {
            registerDefaultState(defaultBlockState().setValue(SIDE, false));
        }
    }

    public void setCenterOfDomain(BlockPos centerOfDomain) {
        this.centerOfDomain = centerOfDomain;
    }

    public BlockPos getCenterOfDomain() {
        return centerOfDomain;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        for (Map.Entry<Direction, BooleanProperty> DIRECTION_SIDE : DIRECTION_SIDES.entrySet()) {
            boolean isDomainAirOnSide = ((DomainAirSignalGetter)blockPlaceContext.getLevel()).getDirectionWhereNextToDomainAir(blockPlaceContext.getClickedPos(), DIRECTION_SIDE.getKey());

            this.defaultBlockState().setValue(DIRECTION_SIDE.getValue(), isDomainAirOnSide);

//            MuddysDomainFramework.LOGGER.info("Block Pos: {}, Direction: {}, Side Active: {}", blockPlaceContext.getClickedPos(), DIRECTION_SIDE.getKey().getName(), isDomainAirOnSide);
        }

        return this.defaultBlockState();
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide) {
            for (Map.Entry<Direction, BooleanProperty> DIRECTION_SIDE : DIRECTION_SIDES.entrySet()) {
                boolean bl2 = blockState.getValue(DIRECTION_SIDE.getValue());

                if (((DomainAirSignalGetter)level).getDirectionWhereNextToDomainAir(blockPos, DIRECTION_SIDE.getKey())) {
                    if (bl2) {
                        level.scheduleTick(blockPos, this, 4);
                    } else {
                        level.setBlock(blockPos, blockState.cycle(DIRECTION_SIDE.getValue()), 2);
                    }
                }
            }

        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        for (Map.Entry<Direction, BooleanProperty> DIRECTION_SIDE : DIRECTION_SIDES.entrySet()) {
            BooleanProperty SIDE = DIRECTION_SIDE.getValue();
            Direction DIRECTION = DIRECTION_SIDE.getKey();

            if (blockState.getValue(SIDE) && !((DomainAirSignalGetter)serverLevel).getDirectionWhereNextToDomainAir(blockPos, DIRECTION)) {
                MuddysDomainFramework.LOGGER.info("SETTING {} SIDE TO FALSE", DIRECTION.getName());

                serverLevel.setBlock(blockPos, blockState.setValue(SIDE, false), 2);
            } else {
                MuddysDomainFramework.LOGGER.info("SETTING {} SIDE TO TRUE", DIRECTION.getName());

                serverLevel.setBlock(blockPos, blockState.setValue(SIDE, true), 2);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (BooleanProperty SIDE : SIDES) {
            builder.add(SIDE);
        }
    }

    public void addShaderToSides(Level level, BlockPos blockPos) {
        List<BlockPos> insideDomainBlockPositons = new ArrayList<>();
        BlockState blockState = level.getBlockState(blockPos);

        MuddysDomainFramework.LOGGER.info("Adding ShaderToSides!");

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos neighbourPos = blockPos.offset(x, y, z);
                    BlockState blockNeighbour = level.getBlockState(neighbourPos);

                    if (blockNeighbour.getBlock() instanceof DomainAirBlock) {
                        MuddysDomainFramework.LOGGER.info("Air Block Found At: {}", neighbourPos);

                        insideDomainBlockPositons.add(neighbourPos);
                    }
                }
            }
        }

        if (!level.isClientSide) {
            for (BlockPos insidePos : insideDomainBlockPositons) {
                if (insidePos.getX() > blockPos.getX()) {
                    MuddysDomainFramework.LOGGER.info("Updated: pos_x");

                    level.setBlockAndUpdate(blockPos, blockState.setValue(POSITIVE_X, true));
                } else if (insidePos.getX() < blockPos.getX()) {
                    MuddysDomainFramework.LOGGER.info("Updated: min_x");

                    level.setBlockAndUpdate(blockPos, blockState.setValue(NEGATIVE_X, true));
                }

                if (insidePos.getY() > blockPos.getY()) {
                    MuddysDomainFramework.LOGGER.info("Updated: pos_y");

                    level.setBlockAndUpdate(blockPos, blockState.setValue(POSITIVE_Y, true));
                } else if (insidePos.getY() < blockPos.getY()) {
                    MuddysDomainFramework.LOGGER.info("Updated: min_y");

                    level.setBlockAndUpdate(blockPos, blockState.setValue(NEGATIVE_Y, true));
                }

                if (insidePos.getZ() > blockPos.getZ()) {
                    MuddysDomainFramework.LOGGER.info("Updated: pos_z");

                    level.setBlockAndUpdate(blockPos, blockState.setValue(POSITIVE_Z, true));
                } else if (insidePos.getZ() < blockPos.getZ()) {
                    MuddysDomainFramework.LOGGER.info("Updated: min_z");

                    level.setBlockAndUpdate(blockPos, blockState.setValue(NEGATIVE_Z, true));
                }
            }
        }
    }
}
