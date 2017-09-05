package data;

import java.io.FileWriter;
import java.io.IOException;
import common.Settings;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Manipulate dataset
 */

public class Dataset {

	private ArrayList<ArrayList<InstanceImpl>> allFolds = new ArrayList<ArrayList<InstanceImpl>>();
	private ArrayList<InstanceImpl> instances = new ArrayList<InstanceImpl>();
	private ArrayList<String> training_bigram_annotations = new ArrayList<String>();
	private int poolSize = 0;
	private int dictionarySize;
	private String project_name;

	public Dataset(ArrayList<InstanceImpl> instances, Data data, Settings settings) {
		for (InstanceImpl i : instances) {
			this.instances.add(i);
		}
		this.dictionarySize = data.getDictionary().size();
		this.project_name = settings.getProjectName();
	}

	public void splitFolds(int fold) {
		for (int i = 0; i < fold; i++) {
			allFolds.add(new ArrayList<InstanceImpl>());
		}
		
		for (int i = 0; i < instances.size(); i++) {
			int foldNum = i % fold;
			allFolds.get(foldNum).add(instances.get(i));
		}
	}
	
	public void splitFolds(int fold, double percentage, int trainingFold, int[] pool) {
		for (int i = 0; i < fold; i++) {
			allFolds.add(new ArrayList<InstanceImpl>());
		}
		
		for (int i = 0; i < instances.size(); i++) {
			int foldNum = i % fold;
			allFolds.get(foldNum).add(instances.get(i));
		}

		int moveNum =(int)((double)instances.size() * percentage);
		int splitFoldNum = allFolds.get(trainingFold).size() / moveNum;
		int insNum = 0;
		Iterator<InstanceImpl> it = allFolds.get(trainingFold).iterator();
		while (it.hasNext()) {
			InstanceImpl i = it.next();
		    if (insNum % splitFoldNum == 1) {
		    	allFolds.get((pool[0])).add(i);
		        it.remove();
		    }
		    insNum++;
		}
		
		System.out.println("# of Training: " + allFolds.get(trainingFold).size());
		for(int p : pool){
			poolSize += allFolds.get(p).size();
		}
		System.out.println("# of Pool: " + poolSize);
	}

	public int getPoolSize(){
		return poolSize;
	}
	
	public void createTrainingSet(int train_fold_num) throws IOException {
		FileWriter fw = new FileWriter(project_name + "\\" + "train_" + train_fold_num + ".dat");
		ArrayList<InstanceImpl> train = allFolds.get(train_fold_num);
		writeInstanceToFile(train, fw);
		fw.flush();
		fw.close();
	}
	
	public void updateTrainingAndPoolSet(int train_fold_num, Map<Integer, int[]> poolFoldsMap, InstanceImpl newIns) throws IOException {
		allFolds.get(train_fold_num).add(newIns);
		//System.out.println("# of training instaces " + allFolds.get(trainfoldnum).size());
		int[] poolfoldsNum = poolFoldsMap.get(train_fold_num);
		removeFromPoolSet(poolfoldsNum, newIns);
		createTrainingSet(train_fold_num);
	}

	public void createPoolSet(int train_fold_num, int[] poolfoldnum) throws IOException {
		FileWriter fw = new FileWriter(project_name + "\\" + "pool_" + train_fold_num + ".dat");
		for (int fold : poolfoldnum) {
			ArrayList<InstanceImpl> pool = allFolds.get(fold);
			writeInstanceToFile(pool, fw);
		}
		fw.flush();
		fw.close();
	}

	public void removeFromPoolSet(int[] pool_fold_num, InstanceImpl instance_remove){
		for (int fold : pool_fold_num) {
			Iterator<InstanceImpl> it = allFolds.get(fold).iterator();
			while (it.hasNext()) {
			    if (it.next().getID().equals(instance_remove.getID())) {
			    	//System.out.println(it.next().getID() + " removed");
			        it.remove();
			        return;
			    }
			}
		}
	}

	public void createTestingSet(int trainfoldnum, int testfoldnum) throws IOException {
		//System.out.println("Creating testing data");
		FileWriter fw = new FileWriter(project_name + "\\" + "test_" + trainfoldnum + ".dat");
		ArrayList<InstanceImpl> test = allFolds.get(testfoldnum);
		writeInstanceToFile(test, fw);
		fw.flush();
		fw.close();
	}

	private void writeInstanceToFile(ArrayList<InstanceImpl> fold, FileWriter fw) throws IOException{
		for (InstanceImpl i : fold) {
			int label = i.getNumericLabel();
			fw.write(label + " ");
			ArrayList<Integer> featureIndex = i.getFeatureIndex();

			for (int index : featureIndex) {
				fw.write(index + ":1 ");
			}
			fw.write("#" + i.getID());
			fw.write("\n");
		}
	}

	private ArrayList<Integer> getExt2FeatureFromTesting(InstanceImpl test){
		ArrayList<Integer> ext2Testing = new ArrayList<Integer>();
		String sentence = test.getSentence();
		String[] split = sentence.split("\\s+");
		for(int i = 0; i < split.length - 1; i++){
			String bigram = "";		
			bigram = split[i] + split[i+1];
			int index = training_bigram_annotations.indexOf(bigram);
			if(index != -1){
				ext2Testing.add(index + dictionarySize + 1);
			}
		}
		return ext2Testing;
	}
	
	
	public ArrayList<InstanceImpl> getTestingSet(int testfoldnum) {
		return allFolds.get(testfoldnum);
	}
	
	public ArrayList<InstanceImpl> getDevSet(int devfoldnum) {
		return allFolds.get(devfoldnum);
	}
}
