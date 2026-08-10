package muddy.domain_framework.block.entity.custom;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DomainBarrierEntity extends BlockEntity {
    private String domainShaderName;

    public DomainBarrierEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.DOMAIN_BLOCK_ENTITY, blockPos, blockState);
    }

    public List<Direction> getInternalFaces() {
        List<Direction> renderedDirections = new ArrayList<>();

        for (Direction direction1 :Direction.values()) {
            if (this.shouldRenderInternalFace(direction1)) {
                renderedDirections.add(direction1);
            }
        }

        return renderedDirections;
    }

    public boolean shouldRenderInternalFace(Direction direction) {
        assert this.level != null;
        return this.level.getBlockState(this.getBlockPos().relative(direction)).getBlock() instanceof DomainAirBlock
                /*|| this.level.getBlockState(this.getBlockPos().relative(direction)).getBlock() instanceof DomainClashAirBlock*/;
    }

    public void setDomainShaderPath(String domainShaderName) {
        this.domainShaderName = domainShaderName;
    }

    public String getDomainShaderName() {
        return domainShaderName;
    }

    public boolean shouldRenderExternalFace(Direction direction) {
        assert this.level != null;
        boolean isInternal = this.level.getBlockState(this.getBlockPos().relative(direction)).getBlock() instanceof DomainAirBlock;

        return !isInternal && Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), direction, this.getBlockPos().relative(direction));
    }

    public boolean shouldRenderFace(Direction direction) {
        assert this.level != null;
        return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), direction, this.getBlockPos().relative(direction));
    }

    @Override
    public @Nullable Level getLevel() {
        return super.getLevel();
    }
}
