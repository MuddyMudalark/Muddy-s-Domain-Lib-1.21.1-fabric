package muddy.domain_framework.network;

import io.netty.buffer.ByteBuf;
import muddy.domain_framework.MuddysDomainFramework;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record UpdateClientClashScoreS2CPayload(int clashScore) implements CustomPacketPayload {
    public static final ResourceLocation CLASH_WIN_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "player_clash_score");
    public static final CustomPacketPayload.Type<UpdateClientClashScoreS2CPayload> ID = new CustomPacketPayload.Type<>(CLASH_WIN_PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, UpdateClientClashScoreS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UpdateClientClashScoreS2CPayload::clashScore,
            UpdateClientClashScoreS2CPayload::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
