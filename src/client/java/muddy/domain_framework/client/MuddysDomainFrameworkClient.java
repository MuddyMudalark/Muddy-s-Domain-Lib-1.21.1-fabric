package muddy.domain_framework.client;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.client.entity.DomainClashRenderer;
import muddy.domain_framework.client.entity.DomainRenderer;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.mixin.LivingEntityMixin;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.util.HasDomainExpanded;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

public class MuddysDomainFrameworkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.DOMAIN_ENTITY, DomainRenderer::new);
		EntityRendererRegistry.register(ModEntities.DOMAIN_CLASH_ENTITY, DomainClashRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(DomainHasExpandedS2CPayload.ID, (payload, context) -> {
			ClientLevel level = context.client().level;
			if (level == null) {
				return;
			}

			((HasDomainExpanded)context.player()).domain$setHasDomainExpanded(payload.hasFullyExpanded());
		});
	}
}