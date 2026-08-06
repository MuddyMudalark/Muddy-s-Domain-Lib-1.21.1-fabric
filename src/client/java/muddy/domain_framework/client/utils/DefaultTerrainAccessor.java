package muddy.domain_framework.client.utils;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

public interface DefaultTerrainAccessor {
    TerrainRenderPass domain$getDefaultDomainTerrain();
}
