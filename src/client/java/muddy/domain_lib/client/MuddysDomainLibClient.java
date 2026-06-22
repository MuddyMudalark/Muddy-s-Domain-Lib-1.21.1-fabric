package muddy.domain_lib.client;

import muddy.domain_lib.client.entity.DomainRenderer;
import muddy.domain_lib.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MuddysDomainLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		EntityRendererRegistry.register(ModEntities.DOMAIN_ENTITY, DomainRenderer::new);
	}
}