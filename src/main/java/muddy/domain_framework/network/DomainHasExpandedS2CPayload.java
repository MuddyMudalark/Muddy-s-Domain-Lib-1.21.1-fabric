package muddy.domain_framework.network;

import com.mojang.serialization.Codec;
import muddy.domain_framework.MuddysDomainFramework;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record DomainHasExpandedS2CPayload(boolean hasFullyExpanded) implements CustomPacketPayload {
    public static final ResourceLocation DOMAIN_EXPANDED_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "domain_has_expanded");
    public static final CustomPacketPayload.Type<DomainHasExpandedS2CPayload> ID = new CustomPacketPayload.Type<>(DOMAIN_EXPANDED_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, DomainHasExpandedS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            DomainHasExpandedS2CPayload::hasFullyExpanded,
            DomainHasExpandedS2CPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
