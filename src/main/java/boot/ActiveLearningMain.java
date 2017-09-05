package boot;

import action.Fold;
import common.Settings;
import data.Category;
import data.Data;
import data.Dataset;
import data.InstanceImpl;
import preprocessing.Stopword;

import java.io.File;
import java.io.IOException;
import algorithm.*;
import java.util.*;

/**
 * The main entry point
 */
public class ActiveLearningMain {
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String trainingFile = "filezilla.csv";
		String categoryFile = null;
		double per = 0.2;
		String project_name = "temp"; //default project folder to store temp files
		String confidenceStrategy = "entropy";

		for(int i = 0;i < args.length;i++) {
		     if ("-f".equals(args[i])) {
				 trainingFile = args[i+1];
		          i++;
		     }else if ("-per".equals(args[i])) {
		    	  per = Double.valueOf(args[i+1]);
		          i++;
		     }else if("-project".equals(args[i])){
				 project_name = args[i+1];
			 }else if("-c".equals(args[i])){
				 confidenceStrategy = args[i+1];
				 if(!confidenceStrategy.equals("entropy") && !confidenceStrategy.equals("variance")){
				 	System.err.println("Must choose entropy or variance as confidence selection strategy.");
				 	System.exit(0);
				 }
			 }
		}


		//create settings for the entire application
        Settings settings = new Settings();
        settings.setProjectName(project_name);
		new File(settings.getProjectName()).mkdirs();

		//create stopword instance as singleton
		Stopword.getInstance().readStopwords();

		//Create Data object for data manipulation and instance creation
        Data data = new Data.Builder(trainingFile).build();

		//Create Category object contains all the categories in the dataset
		Category categories = new Category();
        categories.setCategory(trainingFile, data.getLabelIndex());
        System.out.println(categories.getCategories());


		//Select strategy for choosing the least confident instance in the pool
        AbstractConfidenceStrategy conf; 
        if(confidenceStrategy.equals("entropy")){
            conf = new EntropyStrategy(Order.DESCENDING);
        }
        else{
            conf = new VarianceStrategy(Order.ASCENDING);
        }

		start_active_learning(data, categories, conf, settings, per);

	}

    /**
     * Run the active learning algorithm. This is a multi-thread application which uses 5 threads, and each thread is working for one fold.
     * I use 5 folds internally because it is mostly used in experimental design and evaluation.
     * @param data: the Data object to process input data
     * @param per: the percentage to determine how many instances are in the initial training set
     * @param categories: the categories of the training set
     * @param confidenceStrategy: the strategy to choose least confident instance from pool
     * @param settings: general settings of the application
     * @throws IOException
     * @throws InterruptedException
     */
	private static void start_active_learning(Data data, Category categories, AbstractConfidenceStrategy confidenceStrategy, Settings settings, double per) throws IOException, InterruptedException {
		data.createDictionary();
		data.createInstances(categories.getCategories());

		ArrayList<InstanceImpl> instances = data.getInstanceList();

		//This is a map of training fold and testing fold
		Map<Integer, Integer> train_test = new LinkedHashMap<Integer, Integer>();

		//This is a map of training fold and pool folds. A pool is a set of unlabeled instances being predicted and added to training set during active learning.
		Map<Integer, int[]> train_pool = new LinkedHashMap<Integer, int[]>();

		train_test.put(4, 0);
		train_test.put(0, 1);
		train_test.put(1, 2);
		train_test.put(2, 3);
		train_test.put(3, 4);

		int[] array0 = { 1, 2, 3 };
		train_pool.put(4, array0);

		int[] array1 = { 2, 3, 4 };
		train_pool.put(0, array1);

		int[] array2 = { 0, 3, 4 };
		train_pool.put(1, array2);

		int[] array3 = { 0, 1, 4 };
		train_pool.put(2, array3);

		int[] array4 = { 0, 1, 2 };
		train_pool.put(3, array4);

		/*
		 * Create several folds and initialize the data in each fold
		 */
		List<Dataset> datasets = new ArrayList<Dataset>();
        List<Integer> poolSizes = new ArrayList<Integer>();
		int foldNum = settings.getFoldNum();
		for(int i = 0; i < foldNum; i++){
		    Dataset dataset = new Dataset(instances, data, settings);
		    dataset.splitFolds(foldNum, per, i, train_pool.get(i));
            datasets.add(dataset);
            poolSizes.add(dataset.getPoolSize());
        }
        int maxPoolSize = Collections.max(poolSizes);

        /*
         * Create a thread for each fold to run active learning
         */
        for(int f = 0; f < foldNum; f++) {
			Fold fold = new Fold(f, datasets.get(f), data, settings, train_test, train_pool, maxPoolSize, categories, confidenceStrategy);
			Thread t = new Thread(fold);
			t.start();
			System.out.println("Thread for training fold " + f + " started...");
		}
	}
}
