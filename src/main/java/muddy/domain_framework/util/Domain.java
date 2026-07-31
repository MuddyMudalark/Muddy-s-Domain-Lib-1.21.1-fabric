package muddy.domain_framework.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Domain {
    public float radius;
    public BlockPos center;
    public Vec3 centerAsVector;

    public Domain(BlockPos center, float radius) {
        super();

        this.center = center;
        this.centerAsVector = center.getCenter();
        this.radius = radius;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public Vec3 getCenterAsVector() {
        return this.centerAsVector;
    }

    public float getRadius() {
        return this.radius;
    }

    public static final Codec<Domain> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("center_position").forGetter(Domain::getCenter),
            Codec.FLOAT.fieldOf("radius").forGetter(Domain::getRadius)
    ).apply(instance, Domain::new));

    public static final StreamCodec<ByteBuf, Domain> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull Domain decode(ByteBuf object) {
            return new Domain(FriendlyByteBuf.readBlockPos(object), object.readFloat());
        }

        @Override
        public void encode(ByteBuf object, Domain domain) {
            object.writeFloat(domain.getRadius());
            FriendlyByteBuf.writeBlockPos(object, domain.getCenter());
        }
    };
}
