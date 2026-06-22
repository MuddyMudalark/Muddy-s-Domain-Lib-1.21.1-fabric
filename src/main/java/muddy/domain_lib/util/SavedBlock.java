package muddy.domain_lib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record SavedBlock(BlockPos pos, BlockState state, @Nullable CompoundTag blockEntityNbt) {}
