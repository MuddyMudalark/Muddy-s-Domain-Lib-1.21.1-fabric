package muddy.domain_framework.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import muddy.domain_framework.entity.custom.DomainEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DomainRenderer extends EntityRenderer<DomainEntity> {
    public DomainRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DomainEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.scale(1, 1, 1);

        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(DomainEntity entity) {
        return null;
    }
}
