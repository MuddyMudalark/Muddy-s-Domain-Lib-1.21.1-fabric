package muddy.domain_framework.client.mixin;

import muddy.domain_framework.client.render.ModRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LenderRevelerMixin {

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    private void renderSectionLayer(RenderType renderType, double d, double e, double f, Matrix4f matrix4f, Matrix4f matrix4f2) {}

    @Inject(method = "renderLevel", at = @At("TAIL"))
    public void domain$renderLevel(
            DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci
    ) {
        assert this.level != null;
        ProfilerFiller profilerFiller = this.level.getProfiler();
        Vec3 vec3 = camera.getPosition();
        double d = vec3.x();
        double e = vec3.y();
        double g = vec3.z();

        profilerFiller.popPush("terrain");
        this.renderSectionLayer(ModRenderTypes.insideDomain(), d, e, g, matrix4f, matrix4f2);
        profilerFiller.pop();
    }
}
