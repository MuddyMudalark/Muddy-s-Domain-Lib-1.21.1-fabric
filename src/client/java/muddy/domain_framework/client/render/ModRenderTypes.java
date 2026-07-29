package muddy.domain_framework.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

@Environment(EnvType.CLIENT)
public class ModRenderTypes {
//    private static final RenderType DOMAIN_INSIDE = RenderType.create(
//            "domain_inside",
//            DefaultVertexFormat.BLOCK,
//            VertexFormat.Mode.QUADS,
//            0x400000,
//            true,
//            false,
//            RenderType.CompositeState.builder()
//                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
//                    .setShaderState(RenderStateShard.RENDERTYPE_SOLID_SHADER)
//                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
//                    .createCompositeState(true)
//    );

    private static final RenderType DOMAIN_INSIDE = RenderType.create(
            "domain_inside",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_SOLID_SHADER)
                    .setTextureState(
                            RenderStateShard.MultiTextureStateShard.builder()
                                    .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                                    .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                                    .build()
                    )
                    .createCompositeState(false)
    );

    public static RenderType insideDomain() {
        return DOMAIN_INSIDE;
    }

}
