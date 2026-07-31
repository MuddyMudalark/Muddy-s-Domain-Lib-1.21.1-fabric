package muddy.domain_framework.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.client.entity.DomainClashRenderer;
import muddy.domain_framework.client.entity.DomainRenderer;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.network.DomainDetailsS2CPayload;
import muddy.domain_framework.util.DomainCenterPosition;
import muddy.domain_framework.util.HasDomainExpanded;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MuddysDomainFrameworkClient implements ClientModInitializer {
	@Nullable
	public static ShaderInstance DOMAIN_SHADER;

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

		ClientPlayNetworking.registerGlobalReceiver(DomainDetailsS2CPayload.ID, (payload, context) -> {
			ClientLevel level = context.client().level;
			if (level == null) {
				return;
			}

			((DomainCenterPosition)context.player()).domain$setDomain(payload.domain());
		});

		CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                ResourceLocation.fromNamespaceAndPath(
                    MuddysDomainFramework.MOD_ID,
                    "domain_default"
                ),
                DefaultVertexFormat.BLOCK,
                shader -> DOMAIN_SHADER = shader
            );

			Vec3 cameraPos =
					Minecraft.getInstance()
							.gameRenderer
							.getMainCamera()
							.getPosition();

			if (DOMAIN_SHADER != null) {
				Uniform cameraUniform = DOMAIN_SHADER.getUniform("CameraPosition");

				if (cameraUniform != null) {
					cameraUniform.set(
							(float) cameraPos.x,
							(float) cameraPos.y,
							(float) cameraPos.z
					);
				}


			}
        });

		ClientTickEvents.END_CLIENT_TICK.register(listener -> {
			LocalPlayer player = listener.player;

			if (player != null) {
				Vec3 centerOfDomain = ((DomainCenterPosition)player).domain$getDomain().centerAsVector;

				Uniform domainCenterUniform = DOMAIN_SHADER.getUniform("DomainCenterPosition");

				if (domainCenterUniform != null) {
					domainCenterUniform.set(
							(float) centerOfDomain.x,
							(float) centerOfDomain.y,
							(float) centerOfDomain.z
					);
				}
			}
		});

	}
}