package muddy.domain_framework.network;

import io.netty.buffer.ByteBuf;
import muddy.domain_framework.MuddysDomainFramework;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClashWinScoreGameRuleS2CPayload(int winScore) implements CustomPacketPayload {
    public static final ResourceLocation CLASH_WIN_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "clash_win_score");
    public static final CustomPacketPayload.Type<ClashWinScoreGameRuleS2CPayload> ID = new CustomPacketPayload.Type<>(CLASH_WIN_PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, ClashWinScoreGameRuleS2CPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ClashWinScoreGameRuleS2CPayload::winScore,
            ClashWinScoreGameRuleS2CPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}
