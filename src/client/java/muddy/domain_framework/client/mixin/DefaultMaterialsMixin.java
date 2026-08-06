package muddy.domain_framework.client.mixin;

import muddy.domain_framework.client.MuddysDomainFrameworkClient;
import muddy.domain_framework.client.render.ModRenderTypes;
import muddy.domain_framework.client.utils.DefaultTerrainAccessor;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultMaterials.class)
public class DefaultMaterialsMixin {

    @Shadow @Final
    public static Material SOLID;
    @Shadow @Final
    public static Material CUTOUT;
    @Shadow @Final
    public static Material CUTOUT_MIPPED;
    @Shadow @Final
    public static Material TRANSLUCENT;
    @Shadow @Final
    public static Material TRIPWIRE;
    @Unique
    private static final Material DOMAIN = new Material(
            ((DefaultTerrainAccessor)new DefaultTerrainRenderPasses()).domain$getDefaultDomainTerrain(),
            AlphaCutoffParameter.ZERO,
            true);

    @Inject(method = "forRenderLayer", at = @At("HEAD"), cancellable = true)
    private static void domain$fuckOffSodiumOverrides(RenderType layer, CallbackInfoReturnable<Material> cir) {
        if (layer == RenderType.solid()) {
            cir.setReturnValue(SOLID);
        } else if (layer == RenderType.cutout()) {
            cir.setReturnValue(CUTOUT);
        } else if (layer == RenderType.cutoutMipped()) {
            cir.setReturnValue(CUTOUT_MIPPED);
        } else if (layer == RenderType.tripwire()) {
            cir.setReturnValue(TRIPWIRE);
        } else if (layer == RenderType.translucent()) {
            cir.setReturnValue(TRANSLUCENT);
        } else if (layer == ModRenderTypes.insideDomain()) {
            cir.setReturnValue(DOMAIN);
        } else {
            throw new IllegalArgumentException("No material mapping exists for " + String.valueOf(layer));
        }

        cir.cancel();
    }

}
