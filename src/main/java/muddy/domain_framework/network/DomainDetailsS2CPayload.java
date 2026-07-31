package muddy.domain_framework.network;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.util.Domain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record DomainDetailsS2CPayload(Domain domain) implements CustomPacketPayload {
    public static final ResourceLocation RENDER_DOMAIN_PAYLOAD = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "domain");
    public static final CustomPacketPayload.Type<DomainDetailsS2CPayload> ID = new CustomPacketPayload.Type<>(RENDER_DOMAIN_PAYLOAD);
    public static final StreamCodec<RegistryFriendlyByteBuf, DomainDetailsS2CPayload> CODEC = StreamCodec.composite(
            Domain.STREAM_CODEC,
            DomainDetailsS2CPayload::domain,
            DomainDetailsS2CPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
