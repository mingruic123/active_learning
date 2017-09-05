package algorithm;

import java.util.List;

/**
 * Use entropy as strategy to compute confidence
 */
public class EntropyStrategy extends AbstractConfidenceStrategy {
    public EntropyStrategy(Order order){
        super(order);
    }

    public double getConfidence(List<Double> distribution){
        double entropy = 0.0;
        for(double probability : distribution){
            if(probability == 0.0) continue;
            entropy += probability * Math.log(probability);
            //System.out.println(entropy);
        }
        entropy *= -1;
        return entropy;
    }
}
