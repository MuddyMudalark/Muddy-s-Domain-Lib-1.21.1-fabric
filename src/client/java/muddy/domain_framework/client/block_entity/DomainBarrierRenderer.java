package muddy.domain_framework.client.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddy.domain_framework.block.entity.custom.DomainBarrierEntity;
import muddy.domain_framework.client.render.ModRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public class DomainBarrierRenderer implements BlockEntityRenderer<DomainBarrierEntity> {
    public DomainBarrierRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(DomainBarrierEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();
        Matrix4f matrix4f = poseStack.last().pose();

        float scale = 1.002F;

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, -0.5);

        this.renderCube(blockEntity, matrix4f, multiBufferSource.getBuffer(this.getRenderTypeFromLocation(blockEntity)));

        poseStack.popPose();

    }

    private void renderCube(DomainBarrierEntity blockEntity, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        float f = this.getOffsetDown();
        float g = this.getOffsetUp();

        this.renderFace(blockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
        this.renderFace(blockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
        this.renderFace(blockEntity, matrix4f, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
        this.renderFace(blockEntity, matrix4f, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
        this.renderFace(blockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, f, f, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
        this.renderFace(blockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, g, g, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
    }

    private void renderFace(DomainBarrierEntity blockEntity, Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, Direction direction) {
        if (blockEntity.shouldRenderInternalFace(direction)) {
            vertexConsumer.addVertex(matrix4f, f, h, j);
            vertexConsumer.addVertex(matrix4f, g, h, k);
            vertexConsumer.addVertex(matrix4f, g, i, l);
            vertexConsumer.addVertex(matrix4f, f, i, m);
        }
    }

    protected float getOffsetUp() {
        return 1.0F;
    }

    protected float getOffsetDown() {
        return 0.0F;
    }

    public RenderType getRenderTypeFromLocation(DomainBarrierEntity blockEntity) {
        RenderType renderType = ModRenderTypes.getRenderTypeFromIdentifier(blockEntity.getDomainShaderName());

        if (renderType == null) {
            return defaultRenderType();
        }

        return renderType;
    }

    protected RenderType defaultRenderType() {


        return ModRenderTypes.insideDomain();
    }
}
