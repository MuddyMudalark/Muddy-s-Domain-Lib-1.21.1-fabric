package muddy.domain_framework.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import muddy.domain_framework.entity.custom.DomainClashEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DomainClashRenderer extends EntityRenderer<DomainClashEntity> {
    public DomainClashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DomainClashEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.scale(1, 1, 1);

        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DomainClashEntity entity) {
        return null;
    }
}
