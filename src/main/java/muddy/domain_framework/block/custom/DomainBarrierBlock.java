package muddy.domain_framework.block.custom;

import com.mojang.serialization.MapCodec;
import muddy.domain_framework.block.entity.custom.DomainBarrierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DomainBarrierBlock extends BaseEntityBlock {
    private BlockPos centerOfDomain;
    private ResourceLocation shaderPath;

    public DomainBarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DomainBarrierBlock::new);
    }

    public void setCenterOfDomain(BlockPos centerOfDomain) {
        this.centerOfDomain = centerOfDomain;
    }

    public BlockPos getCenterOfDomain() {
        return centerOfDomain;
    }

    public void setShaderPath(ResourceLocation shaderPath) {
        this.shaderPath = shaderPath;
    }

    public ResourceLocation getShaderPath() {
        return shaderPath;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DomainBarrierEntity(blockPos, blockState);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
