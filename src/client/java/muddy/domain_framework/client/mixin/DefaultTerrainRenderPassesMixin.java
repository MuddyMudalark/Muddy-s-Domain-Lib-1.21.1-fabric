package muddy.domain_framework.client.mixin;

import muddy.domain_framework.client.render.ModRenderTypes;
import muddy.domain_framework.client.utils.DefaultTerrainAccessor;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultTerrainRenderPasses.class)
public class DefaultTerrainRenderPassesMixin implements DefaultTerrainAccessor {
    @Shadow @Final
    public static final TerrainRenderPass SOLID = new TerrainRenderPass(RenderType.solid(), false, false);
    @Shadow @Final
    public static final TerrainRenderPass CUTOUT = new TerrainRenderPass(RenderType.cutoutMipped(), false, true);
    @Shadow @Final
    public static final TerrainRenderPass TRANSLUCENT = new TerrainRenderPass(RenderType.translucent(), true, false);
    @Unique
    private static final TerrainRenderPass DOMAIN_INSIDE = new TerrainRenderPass(ModRenderTypes.insideDomain(), false, false);

    @Mutable
    @Shadow @Final
    public static TerrainRenderPass[] ALL;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void domain$addExtraTerrainPasses(CallbackInfo ci) {
        ALL = new TerrainRenderPass[] {
                SOLID, CUTOUT, TRANSLUCENT, DOMAIN_INSIDE
        };
    }

    @Override
    public TerrainRenderPass domain$getDefaultDomainTerrain() {
        return DOMAIN_INSIDE;
    }
}
