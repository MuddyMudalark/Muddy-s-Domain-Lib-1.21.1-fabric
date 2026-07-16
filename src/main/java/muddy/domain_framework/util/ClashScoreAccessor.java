package muddy.domain_framework.util;

public interface ClashScoreAccessor {
    int domain$getClashScore();
    void domain$incrementClashScore();
    void domain$setClashScore(int clashScore);
}
