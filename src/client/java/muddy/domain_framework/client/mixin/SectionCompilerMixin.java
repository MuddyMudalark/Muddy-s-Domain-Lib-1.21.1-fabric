package muddy.domain_framework.client.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.client.render.ModRenderTypes;
import muddy.domain_framework.util.HasDomainExpanded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {
    @Shadow
    @Final
    @Mutable
    private final BlockRenderDispatcher blockRenderer;

    @Unique
    SectionCompiler thisClassInstance = ((SectionCompiler) (Object) this);

    public SectionCompilerMixin(BlockRenderDispatcher blockRenderer) {
        this.blockRenderer = blockRenderer;
    }

    @Shadow
    private BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType) {
        return null;
    }

    @Shadow
    private <E extends BlockEntity> void handleBlockEntity(SectionCompiler.Results results, E blockEntity) {}

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    public void domain$compile(
            @NotNull SectionPos sectionPos, RenderChunkRegion renderChunkRegion, VertexSorting vertexSorting, SectionBufferBuilderPack sectionBufferBuilderPack, CallbackInfoReturnable<SectionCompiler.Results> cir
    ) {
        SectionCompiler.Results results = new SectionCompiler.Results();
        BlockPos blockPos = sectionPos.origin();
        BlockPos blockPos2 = blockPos.offset(15, 15, 15);
        VisGraph visGraph = new VisGraph();
        PoseStack poseStack = new PoseStack();
        ModelBlockRenderer.enableCaching();
        Map<RenderType, BufferBuilder> map = new Reference2ObjectArrayMap<>(RenderType.chunkBufferLayers().size());
        RandomSource randomSource = RandomSource.create();

        for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
            BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
            if (blockState.isSolidRender(renderChunkRegion, blockPos3)) {
                visGraph.setOpaque(blockPos3);
            }

            if (blockState.hasBlockEntity()) {
                BlockEntity blockEntity = renderChunkRegion.getBlockEntity(blockPos3);
                if (blockEntity != null) {
                    this.handleBlockEntity(results, blockEntity);
                }
            }

            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                BufferBuilder bufferBuilder = this.getOrBeginLayer(map, sectionBufferBuilderPack, renderType);
                this.blockRenderer.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, blockState, fluidState);
            }

            if (blockState.getRenderShape() == RenderShape.MODEL) {
                RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
                BufferBuilder bufferBuilder = this.getOrBeginLayer(map, sectionBufferBuilderPack, renderType);

                poseStack.pushPose();
                poseStack.translate(
                        SectionPos.sectionRelative(blockPos3.getX()), SectionPos.sectionRelative(blockPos3.getY()), SectionPos.sectionRelative(blockPos3.getZ())
                );
                this.blockRenderer.renderBatched(blockState, blockPos3, renderChunkRegion, poseStack, bufferBuilder, true, randomSource);
                poseStack.popPose();
            }
        }

        for (Map.Entry<RenderType, BufferBuilder> entry : map.entrySet()) {
            RenderType renderType2 = entry.getKey();
            MeshData meshData = entry.getValue().build();
            if (meshData != null) {
                if (renderType2 == RenderType.translucent()) {
                    results.transparencyState = meshData.sortQuads(sectionBufferBuilderPack.buffer(RenderType.translucent()), vertexSorting);
                }

                results.renderedLayers.put(renderType2, meshData);
            }
        }

        ModelBlockRenderer.clearCache();
        results.visibilitySet = visGraph.resolve();

        cir.setReturnValue(results);

        cir.cancel();
    }
}
