package muddy.domain_framework.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddy.domain_framework.client.MuddysDomainFrameworkClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

@Environment(EnvType.CLIENT)
public class ModRenderTypes {
    private static final RenderStateShard.ShaderStateShard DOMAIN_SHADER =
            new RenderStateShard.ShaderStateShard(() -> MuddysDomainFrameworkClient.DOMAIN_SHADER);

    private static final RenderType DOMAIN_INSIDE = RenderType.create(
            "domain_inside",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(DOMAIN_SHADER)
                    .setTextureState(
                            RenderStateShard.MultiTextureStateShard.builder()
                                    .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                                    .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                                    .build()
                    )
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(false)
    );




    public static RenderType insideDomain() {
        return DOMAIN_INSIDE;
    }

}
