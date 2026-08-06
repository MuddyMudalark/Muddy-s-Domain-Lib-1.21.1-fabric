package muddy.domain_framework.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddy.domain_framework.client.MuddysDomainFrameworkClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
    }

    private static final ResourceLocation DOMAIN_LAYER_1 =
            ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png");

    private static final ResourceLocation DOMAIN_LAYER_0 =
            ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");

    public static final RenderStateShard.ShaderStateShard DOMAIN_SHADER =
            new RenderStateShard.ShaderStateShard(() -> MuddysDomainFrameworkClient.DOMAIN_SHADER);

    public static final RenderType DOMAIN_INSIDE = RenderType.create(
            "domain_inside",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            4194304,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(DOMAIN_SHADER)
                    .setTextureState(
                            new MultiTextureStateShard.Builder()
                                    .add(DOMAIN_LAYER_0, false, false)
                                    .add(DOMAIN_LAYER_1, false, false)
                                    .build()
                    )
                    .setCullState(NO_CULL)
                    .createCompositeState(true)
    );

    public static RenderType insideDomain() {
        return DOMAIN_INSIDE;
    }

}
