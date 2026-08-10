package muddy.domain_framework.command;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static final GameRules.Key<GameRules.IntegerValue> CLASH_LENGTH =
            GameRuleRegistry.register(
                    "clash_length",
                    GameRules.Category.MISC,
                    GameRuleFactory.createIntRule(2400)
            );

    public static final GameRules.Key<GameRules.IntegerValue> CLASH_WIN_SCORE =
            GameRuleRegistry.register(
                    "clash_win_score",
                    GameRules.Category.MISC,
                    GameRuleFactory.createIntRule(20)
            );


    private ModGameRules() {
    }

    public static void initialize() {
        // Calling this method forces the static field above to initialize.
    }
}
