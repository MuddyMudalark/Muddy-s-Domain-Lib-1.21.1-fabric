package muddy.domain_framework.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DomainSectionDispatcher extends SectionRenderDispatcher {
    public DomainSectionDispatcher(ClientLevel clientLevel, LevelRenderer levelRenderer, Executor executor, RenderBuffers renderBuffers, BlockRenderDispatcher blockRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(clientLevel, levelRenderer, executor, renderBuffers, blockRenderDispatcher, blockEntityRenderDispatcher);

    }

    @Environment(EnvType.CLIENT)
    public class RenderDomainSection extends RenderSection {
        public final AtomicReference<CompiledSection> compiled;
        private static final ImmutableList<RenderType> DOMAIN_BUFFER_LAYERS = ImmutableList.of(RenderType.solid(), ModRenderTypes.insideDomain());
        private static final Map<RenderType, VertexBuffer> buffers =
                DOMAIN_BUFFER_LAYERS.stream().collect(
                        Collectors.toMap(
                                (renderType) -> renderType,
                                (renderType) -> new VertexBuffer(VertexBuffer.Usage.STATIC))
                );

        public RenderDomainSection(int i, int j, int k, int l) {
            super(i, j, k, l);
            this.compiled = new AtomicReference<>(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
        }

        public VertexBuffer getDomainBuffer(RenderType renderType) {
            return (VertexBuffer) this.buffers.get(renderType);
        }
    }
}
