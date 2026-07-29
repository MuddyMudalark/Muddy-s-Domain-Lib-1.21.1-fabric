package muddy.domain_framework.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    void preloadShaders() {}

    void reloadShaders(ResourceProvider resourceProvider) {}
}
