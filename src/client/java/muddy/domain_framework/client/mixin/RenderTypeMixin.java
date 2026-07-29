package muddy.domain_framework.client.mixin;

import com.google.common.collect.ImmutableList;
import muddy.domain_framework.client.render.ModRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Shadow
    @Final
    @Mutable
    private static ImmutableList<RenderType> CHUNK_BUFFER_LAYERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyChunkLayers(CallbackInfo ci) {
        CHUNK_BUFFER_LAYERS = ImmutableList.<RenderType>builder()
                .addAll(CHUNK_BUFFER_LAYERS)
                .add(ModRenderTypes.insideDomain())
                .build();
    }
}
