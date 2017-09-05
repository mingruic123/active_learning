package algorithm;

import java.util.List;

/**
 *StrategyOrder to select confidence of predicted result in the pool
 */
public interface ConfidenceStrategy {
    double getConfidence(List<Double> distribution);
}
