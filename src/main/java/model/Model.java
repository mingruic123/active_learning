package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import algorithm.*;
import common.Settings;
import data.Category;
import data.InstanceImpl;
import java.util.*;

/**
 * Doing the actual learning and predicting by calling SVM executables
 */
public class Model {

    private String project_name;
    private List<InstanceImpl> instance_list;
    private Category category;

    private double c;
    private double g;

    public Model(List<InstanceImpl> instance_list, Category category, Settings settings){
        this.instance_list = instance_list;
        this.category = category;
        this.project_name = settings.getProjectName();
        this.c = settings.getCost();
        this.g = settings.getGamma();
    }

    public void train(int fold) throws IOException, InterruptedException {
        //System.out.println("Training SVM...");
        ProcessBuilder pb = new ProcessBuilder("svm-train.exe", "-q", "-c", String.valueOf(c), "-g", String.valueOf(g), "-t", "2", "-b", "1", project_name + "\\" + "train_" + fold + ".dat", project_name + "\\" + "model_" + fold);
        Process process = pb.start();
        process.waitFor();

    }

    public void pool(int fold) throws IOException, InterruptedException {
        //System.out.println("Testing...");
        Process process = new ProcessBuilder("svm-predict.exe", "-b", "1", project_name + "\\" + "pool_" + fold + ".dat", project_name + "\\" + "model_" + fold, project_name + "\\" + "output_" + fold).start();
        process.waitFor();

    }

    public void test(int fold, int iteration) throws IOException, InterruptedException {
        //System.out.println("Testing...");

        Process process = new ProcessBuilder("svm-predict.exe", project_name + "\\" + "test_" + fold + ".dat", project_name + "\\" + "model_" + fold, project_name + "\\" + fold + "\\" + "output_" + fold + "_" + iteration).start();
        process.waitFor();

    }


    public InstanceImpl getLeastConfidentInstance(AbstractConfidenceStrategy confidenceStrategy, int fold) throws IOException {
        int categorySize = category.getCategorySize();
        int poolSize = 0;
        ArrayList<Integer> instanceIDinPoolSet = new ArrayList<Integer>();

        BufferedReader br1 = new BufferedReader(new FileReader(project_name + "\\" + "pool_" + fold + ".dat"));
        String line;
        while ((line = br1.readLine()) != null) {
            poolSize++;
            String[] split = line.split("\\s+");
            String id_token = split[split.length - 1];
            int id = Integer.valueOf(id_token.replace("#", ""));
            instanceIDinPoolSet.add(id);

        }
        br1.close();

        double[][] predictedConf = new double[poolSize][categorySize];

        BufferedReader br2 = new BufferedReader(new FileReader(project_name + "\\" + "output_" + fold));
        int rowNum = 0;
        boolean header = true;
        while ((line = br2.readLine()) != null) {
            if (header == true) {
                header = false;
                continue;
            }
            String[] split = line.split(" ");
            for (int columnNum = 0; columnNum < categorySize; columnNum++) {
                predictedConf[rowNum][columnNum] = Double.valueOf(split[columnNum + 1]);
            }
            rowNum++;
        }
        br2.close();


        double[] predictedConfidence = new double[poolSize];
        for (int row = 0; row < poolSize; row++) {
            ArrayList<Double> confs = new ArrayList<Double>();
            for (int column = 0; column < categorySize; column++) {
                confs.add(predictedConf[row][column]);
            }

            double confidenceVar = confidenceStrategy.getConfidence(confs);
            predictedConfidence[row] = confidenceVar;
        }

        Map<Integer, Double> id_conf = new LinkedHashMap<Integer, Double>();
        for (int i = 0; i < instanceIDinPoolSet.size(); i++) {
            double conf = predictedConfidence[i];
            id_conf.put(instanceIDinPoolSet.get(i), conf);
        }

        // sort in descending order if using entropy, the first element (with maximum entropy) is the least confident instance
        if(confidenceStrategy.getOrder() == Order.DESCENDING)
            id_conf = ModelUtil.sortByValue(id_conf, true);

        // sort in ascending order if using variance, the first element (with minimum variance) is the least confident instance
        if(confidenceStrategy.getOrder() == Order.ASCENDING)
            id_conf = ModelUtil.sortByValue(id_conf, false);


        int leastConfInsID = 0;
        //Get the first element in the sorted map, which is the least confident instance in the pool
        for (int key : id_conf.keySet()) {
            leastConfInsID = key;
            break;
        }

        //This mimic the behavior of human who labels the least confident instance from the pool
        for (InstanceImpl i : this.instance_list) {
            if (Integer.valueOf(i.getID()) == leastConfInsID) {
                //System.out.println(i.getID());
                return i;
            }
        }
        return null;
    }



    public double[] evaluate(ArrayList<InstanceImpl> test, int classIndex) throws IOException {
        int[] trueLabels = new int[test.size()];
        int columnSize = 0;
        if (project_name.equals("filezilla")) {
            columnSize = 8;
        }
        if (project_name.equals("prismstream")) {
            columnSize = 6;
        }

        double[][] probability_distribution = new double[test.size()][columnSize];


        BufferedReader br = new BufferedReader(new FileReader(project_name + "\\" + "output"));
        String line;
        int rowNum = -1;

        while ((line = br.readLine()) != null) {
            if (rowNum == -1) {
                rowNum++;
                continue;
            }
            String[] distribution = line.split(" ");
            for (int columnNum = 1; columnNum < distribution.length; columnNum++) {
                probability_distribution[rowNum][columnNum - 1] = Double.valueOf(distribution[columnNum - 1]);
            }
            rowNum++;
        }

        br.close();

        int[] predictedLabels = new int[test.size()];
        for (int row = 0; row < test.size(); row++) {
            ArrayList<Double> confs = new ArrayList<Double>();
            for (int column = 0; column < columnSize; column++) {
                confs.add(probability_distribution[row][column]);
                //System.out.print(predictedConf[row][column]+ ", ");
            }
            //System.out.println();
            Double max = Collections.max(confs);
            int maxIndex = confs.indexOf(max);
            predictedLabels[row] = maxIndex;
        }


        for (int i = 0; i < test.size(); i++) {
            trueLabels[i] = test.get(i).getNumericLabel();
        }

        double tp = 0;
        double fp = 0;
        double fn = 0;
        for (int i = 0; i < predictedLabels.length; i++) {
            if (predictedLabels[i] == classIndex && trueLabels[i] == classIndex) tp++;
            if (predictedLabels[i] == classIndex && trueLabels[i] != classIndex) fp++;
            if (predictedLabels[i] != classIndex && trueLabels[i] == classIndex) fn++;

        }

        double[] metrics = new double[3];
        metrics[0] = tp;
        metrics[1] = fp;
        metrics[2] = fn;

        return metrics;
    }


}
