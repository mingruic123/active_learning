package action;

import algorithm.AbstractConfidenceStrategy;
import common.Settings;
import data.Category;
import data.Data;
import data.Dataset;
import data.InstanceImpl;
import model.Model;
import view.Status;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Create a fold and run experiments on the fold
 */
public class Fold implements Runnable{
	private int fold;
	private Dataset dataset;
	private Data data;
	private Map<Integer, Integer> train_test = new LinkedHashMap<Integer, Integer>();
	private Map<Integer, int[]> train_pool = new LinkedHashMap<Integer, int[]>();
	private Category category;
	private int maxPoolSize;
	private AbstractConfidenceStrategy confidenceStrategy;
	private Settings settings;
	
	public Fold(int foldNum, Dataset dataset, Data data, Settings settings, Map<Integer, Integer> train_test, Map<Integer, int[]> train_pool, int maxPoolSize, Category category, AbstractConfidenceStrategy confidenceStrategy) {
		this.fold = foldNum;
		this.dataset = dataset;
		this.train_pool = train_pool;
		this.train_test = train_test;
		this.data = data;
		this.maxPoolSize = maxPoolSize;
		this.category = category;
		this.confidenceStrategy = confidenceStrategy;
		this.settings = settings;
	}

	public void run() {
		new File(settings.getProjectName() + "\\" + fold).mkdirs();
		Model model = new Model(data.getInstanceList(), category, settings);
		int testFoldNum = train_test.get(fold);
		try {
			dataset.createTestingSet(fold, testFoldNum);
			for (int iteration = 1; iteration <= maxPoolSize; iteration++) {
				Status.getStatus().printProgress(fold, iteration);

				dataset.createTrainingSet(fold);
				dataset.createPoolSet(fold, train_pool.get(fold));
				model.train(fold);
				model.pool(fold);

				InstanceImpl update_instance = model.getLeastConfidentInstance(confidenceStrategy, fold);
				if (update_instance != null) {
					dataset.updateTrainingAndPoolSet(fold, train_pool, update_instance);
					dataset.removeFromPoolSet(train_pool.get(fold), update_instance);
				}

				model.train(fold);
				model.test(fold, iteration);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
