package muddy.domain_framework.block.custom;

import com.mojang.serialization.MapCodec;
import muddy.domain_framework.block.entity.custom.DomainBarrierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DomainBarrierBlock extends BaseEntityBlock {
    private BlockPos centerOfDomain;
    private String shaderName;

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

    public void setShaderName(String shaderName) {
        this.shaderName = shaderName;
    }

    public String getShaderName() {
        return shaderName;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        DomainBarrierEntity domainBarrierEntity = new DomainBarrierEntity(blockPos, blockState);
        domainBarrierEntity.setDomainShaderPath(this.getShaderName());

        return domainBarrierEntity;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
