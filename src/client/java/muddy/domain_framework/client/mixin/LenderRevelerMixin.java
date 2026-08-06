package muddy.domain_framework.client.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import muddy.domain_framework.client.render.DomainSectionDispatcher;
import muddy.domain_framework.client.render.ModRenderTypes;
import muddy.domain_framework.client.utils.TheVisibleSectionsThingy;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LenderRevelerMixin implements TheVisibleSectionsThingy {

    @Shadow
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);

    @Nullable @Shadow
    private ClientLevel level;

    @Shadow @Final
    private Minecraft minecraft;

    @Nullable @Shadow
    private SectionRenderDispatcher sectionRenderDispatcher;

    @Inject(method = "renderLevel", at = @At("TAIL"))
    public void domain$lenderRevel(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        Vec3 cameraPos = camera.getPosition();
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        assert this.level != null;
        ProfilerFiller profilerFiller = this.level.getProfiler();

        profilerFiller.popPush("compile_sections");
        this.domain$compileDomainSections(camera);
        profilerFiller.popPush("terrain");
        this.domain$renderDomainSection(ModRenderTypes.insideDomain(), camX, camY, camZ, matrix4f, matrix4f2);
    }

    @Unique
    private void domain$compileDomainSections(Camera camera) {
        this.minecraft.getProfiler().push("populate_sections_to_compile");
        SectionRenderDispatcher domainSectionDispatcher = this.sectionRenderDispatcher;

        assert this.level != null;
        LevelLightEngine levelLightEngine = this.level.getLightEngine();
        RenderRegionCache renderRegionCache = new RenderRegionCache();
        BlockPos blockPos = camera.getBlockPosition();
        List<DomainSectionDispatcher.RenderDomainSection> list = Lists.newArrayList();
        ObjectListIterator<SectionRenderDispatcher.RenderSection> var6 = this.visibleSections.iterator();

        while(var6.hasNext()) {
            DomainSectionDispatcher.RenderDomainSection renderSection = (DomainSectionDispatcher.RenderDomainSection)var6.next();
            SectionPos sectionPos = SectionPos.of(renderSection.getOrigin());
            if (renderSection.isDirty() && levelLightEngine.lightOnInSection(sectionPos)) {
                boolean bl = false;
                if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                    BlockPos blockPos2 = renderSection.getOrigin().offset(8, 8, 8);
                    bl = blockPos2.distSqr(blockPos) < (double)768.0F || renderSection.isDirtyFromPlayer();
                } else if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                    bl = renderSection.isDirtyFromPlayer();
                }

                if (bl) {
                    this.minecraft.getProfiler().push("build_near_sync");
                    assert domainSectionDispatcher != null;
                    domainSectionDispatcher.rebuildSectionSync(renderSection, renderRegionCache);
                    renderSection.setNotDirty();
                    this.minecraft.getProfiler().pop();
                } else {
                    list.add(renderSection);
                }
            }
        }

        this.minecraft.getProfiler().popPush("upload");
        assert domainSectionDispatcher != null;
        domainSectionDispatcher.uploadAllPendingUploads();
        this.minecraft.getProfiler().popPush("schedule_async_compile");

        for(DomainSectionDispatcher.RenderDomainSection renderSection : list) {
            renderSection.rebuildSectionAsync(domainSectionDispatcher, renderRegionCache);
            renderSection.setNotDirty();
        }

        this.minecraft.getProfiler().pop();
    }

    @Unique
    public void domain$renderDomainSection(RenderType renderType, double camX, double camY, double camZ, Matrix4f matrix4f, Matrix4f matrix4f2) {
        RenderSystem.assertOnRenderThread();
        renderType.setupRenderState();

        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() -> "render_" + String.valueOf(renderType));
        ObjectListIterator<SectionRenderDispatcher.RenderSection> objectListIterator = this.visibleSections.listIterator(0);
        ShaderInstance shaderInstance = RenderSystem.getShader();
        assert shaderInstance != null;
        shaderInstance.setDefaultUniforms(VertexFormat.Mode.QUADS, matrix4f, matrix4f2, this.minecraft.getWindow());
        shaderInstance.apply();
        Uniform uniform = shaderInstance.CHUNK_OFFSET;

        while (objectListIterator.hasPrevious()) {
            DomainSectionDispatcher.RenderDomainSection renderSection2 = (DomainSectionDispatcher.RenderDomainSection)objectListIterator.next();
            if (!renderSection2.getCompiled().isEmpty(renderType)) {
                VertexBuffer vertexBuffer = renderSection2.getDomainBuffer(renderType);
                BlockPos blockPos = renderSection2.getOrigin();
                if (uniform != null) {
                    uniform.set((float) ((double) blockPos.getX() - camX), (float) ((double) blockPos.getY() - camY), (float) ((double) blockPos.getZ() - camZ));
                    uniform.upload();
                }

                vertexBuffer.bind();
                vertexBuffer.draw();
            }
        }

        if (uniform != null) {
            uniform.set(0.0F, 0.0F, 0.0F);
        }

        shaderInstance.clear();
        VertexBuffer.unbind();
        this.minecraft.getProfiler().pop();
        renderType.clearRenderState();
    }

    @Override
    public ObjectArrayList<SectionRenderDispatcher.RenderSection> domain$getVisibleSections() {
        return visibleSections;
    }
}
