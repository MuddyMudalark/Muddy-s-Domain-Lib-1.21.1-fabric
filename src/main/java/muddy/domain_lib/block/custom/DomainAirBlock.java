package muddy.domain_lib.block.custom;

import muddy.domain_lib.MuddysDomainLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DomainAirBlock extends AirBlock {
    private Holder<MobEffect> domainEffect;
    private Player domainOwner;
    private int domainEffectLength = 20;

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
//        MuddysDomainLib.LOGGER.info("Domain Effect: {}", domainEffect.getRegisteredName());

        super.onPlace(blockState, level, blockPos, blockState2, bl);
    }

    public int getDomainEffectLength() {
        return domainEffectLength;
    }

    public void setDomainEffectLength(int domainEffectLength) {
        this.domainEffectLength = domainEffectLength;
    }

    public Player getDomainOwner() {
        return domainOwner;
    }

    public void setDomainOwner(Player domainOwner) {
        this.domainOwner = domainOwner;
    }

    public DomainAirBlock(Properties properties) {
        super(properties);
    }

    public void setDomainEffect(Holder<MobEffect> domainEffect) {
        this.domainEffect = domainEffect;
    }

    public Holder<MobEffect> getDomainEffect() {
        return  domainEffect;
    }
}
