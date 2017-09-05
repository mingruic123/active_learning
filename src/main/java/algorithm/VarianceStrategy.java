package algorithm;

import java.util.List;

/**
 * Use variance as strategy to compute confidence
 */
public class VarianceStrategy extends AbstractConfidenceStrategy {
    public VarianceStrategy(Order order){
        super(order);
    }
    public double getConfidence(List<Double> distribution){
        double[] inputDataArray = new double[distribution.size()];
        for(int i = 0; i < distribution.size(); i++){
            inputDataArray[i] = distribution.get(i);
        }

        int count = getCount(inputDataArray);
        double average = getAverage(inputDataArray);
        double variance = 0.0;
        for(double d : distribution){
            variance = variance + (d - average) * (d - average);
        }
        variance = variance / count;
        return variance;
    }


    private double getAverage(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double result;
        result = getSum(inputData) / len;

        return result;
    }

    private int getCount(double[] inputData) {
        if (inputData == null)
            return -1;
        return inputData.length;
    }

    private double getSum(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double sum = 0;
        for (int i = 0; i < len; i++) {
            sum = sum + inputData[i];
        }

        return sum;

    }
}
