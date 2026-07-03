package muddy.domain_framework.block.custom;

import net.minecraft.world.level.block.AirBlock;

public class DomainClashAirBlock extends AirBlock {
    private boolean havePlayersBeenTeleported;

    public DomainClashAirBlock(Properties properties) {
        super(properties);
    }

    public void of(boolean havePlayersBeenTeleported) {
        this.havePlayersBeenTeleported = havePlayersBeenTeleported;
    }

    public boolean havePlayersBeenTeleported() {
        return havePlayersBeenTeleported;
    }
}
