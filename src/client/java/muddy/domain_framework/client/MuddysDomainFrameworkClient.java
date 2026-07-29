package muddy.domain_framework.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.client.entity.DomainClashRenderer;
import muddy.domain_framework.client.entity.DomainRenderer;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.util.HasDomainExpanded;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MuddysDomainFrameworkClient implements ClientModInitializer {
	@Nullable
	private static ShaderInstance rendertypeSolidShader;

	@Override
	public void onInitializeClient() {

		EntityRendererRegistry.register(ModEntities.DOMAIN_ENTITY, DomainRenderer::new);
		EntityRendererRegistry.register(ModEntities.DOMAIN_CLASH_ENTITY, DomainClashRenderer::new);

//		CoreShaderRegistrationCallback.EVENT.register(
//				ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, ShaderInstance.SHADER_PATH),
//				context -> {
//			context.register(ResourceLocation.fromNamespaceAndPath(
//					MuddysDomainFramework.MOD_ID,
//					"rendertype_solid_end"),
//					DefaultVertexFormat.BLOCK,
//                    shaderInstance -> {
//
//					});
//		});


		ClientPlayNetworking.registerGlobalReceiver(DomainHasExpandedS2CPayload.ID, (payload, context) -> {
			ClientLevel level = context.client().level;
			if (level == null) {
				return;
			}

			((HasDomainExpanded)context.player()).domain$setHasDomainExpanded(payload.hasFullyExpanded());
		});

	}
}