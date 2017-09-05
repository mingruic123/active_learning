package data;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import preprocessing.Stemmer;
import preprocessing.StemmerFactory;
import preprocessing.Stopword;
import preprocessing.Tokenizer;
import java.util.*;

/**
 * A class to read data, manipulate data, generate features and create instances
 */
public class Data {
	private Set<String> dictionary = new HashSet<String>();
	private Set<String> annotations_bigram = new HashSet<String>();
	private String filePath;
	private final int idIndex;
	private final int titleIndex;
	private final int descIndex;
	private final int labelIndex;
	private final int annotationIndex;
	private ArrayList<InstanceImpl> instances = new ArrayList<InstanceImpl>();
	Stemmer stemmer = new StemmerFactory().getStemmer("porter");
	Tokenizer tokenizer = new Tokenizer();
	Stopword stopword = Stopword.getInstance();
	Map<String, ArrayList<String>> clusters = new LinkedHashMap<String, ArrayList<String>>();

	public static class Builder{
		private final String filePath;

		private int idIndex = 0;
		private int titleIndex = 1;
		private int descIndex = 2;
		private int labelIndex = 3;
		private int annotationIndex = 4;

		public Builder(final String filePath){
			this.filePath = filePath;
		}

		public Builder idIndex(int val){
			idIndex = val;
			return this;
		}

		public Builder titleIndex(int val){
			titleIndex = val;
			return this;
		}

		public Builder descIndex(int val){
			descIndex = val;
			return this;
		}

		public Builder labelIndex(int val){
			labelIndex = val;
			return this;
		}

		public Builder annotationIndex(int val){
			annotationIndex = val;
			return this;
		}

		public Data build(){
			return new Data(this);
		}
	}
	private Data(Builder builder){
		filePath = builder.filePath;
		idIndex = builder.idIndex;
		titleIndex = builder.titleIndex;
		descIndex = builder.descIndex;
		labelIndex = builder.labelIndex;
		annotationIndex = builder.annotationIndex;
	}

	public void createDictionary() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(filePath));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			String title = nextLine[titleIndex].toLowerCase();
			String desc = nextLine[descIndex].toLowerCase();

			String sentence = title + " " + desc;
			sentence = removeStopWord(sentence);
			String[] split = tokenizer.tokenize(sentence);
			for (String s : split) {
				if (s.length() == 0)
					continue;
				dictionary.add(stemmer.stem(s));
			}
		}
		reader.close();
	}

	public void createInstances(List<String> categories) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(filePath));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			InstanceImpl instance = new InstanceImpl(categories);
			
			String title = nextLine[titleIndex];
			title = title.toLowerCase();//.replaceAll("[^A-Za-z0-9 ]", "");
			
			String desc = nextLine[descIndex];
			desc = desc.toLowerCase();//.replaceAll("[^A-Za-z0-9 ]", "");
				
			
			String sentence = title + " " + desc;
			sentence = removeStopWord(sentence);
			instance.setSentence(stemSentence(sentence));
			String[] split = tokenizer.tokenize(sentence);
			
			String id = nextLine[idIndex];
			String label = nextLine[labelIndex];
			instance.setID(id); // set instance id
			instance.setLabel(label); // set instance label
			instance.setLabel(categories.indexOf(label));
			for(int i = 0; i < split.length; i++){
				if (split[i].length() == 0) continue;
				int featureIndex = getDictionary().indexOf(stemmer.stem(split[i]));
				instance.setFeatureIndex(featureIndex + 1); // set feature index to 1 if feature appears, this application uses binary features only
			}
			
			String annotation = nextLine[annotationIndex];
			instance.setAnnotations(stemSentence(annotation));
			instances.add(instance);
		}
		reader.close();
	}

	public int getIdIndex(){
		return idIndex;
	}
	public int getTitleIndex(){
		return titleIndex;
	}
	public int getDescIndex(){
		return descIndex;
	}
	public int getLabelIndex(){
		return labelIndex;
	}
	public ArrayList<InstanceImpl> getInstanceList(){
		return instances;
	}
	
	public ArrayList<String> getDictionary() {
		return new ArrayList<String>(dictionary);
	}


	public String removeStopWord(String line) {
		String[] split = line.split("\\s+");
		String temp = "";
		for (String s : split) {
			if (isStopWord(s)) {
				continue;
			} else {
				temp = temp + s + " ";
			}
		}
		return temp.trim();
	}

	private boolean isStopWord(String term) {
		if (stopword.getStopwords().contains(term)) {
			return true;
		} else {
			return false;
		}
	}
	
	private String stemSentence(String sentence){
		String[] split = sentence.split(" ");
		String stemmedSentence = "";
		for(String s : split){
			String stemmed = stemmer.stem(s);
			stemmedSentence = stemmedSentence + stemmed + " ";
		}
		return sentence;
	}

	public void createAnnotationBigram() throws IOException{
		CSVReader reader = new CSVReader(new FileReader(filePath));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			String annotation = nextLine[annotationIndex];
			String[] single_annotation = annotation.split(";");
			for(String s : single_annotation){
				String split[] = s.split(" ");
				String bigram = "";
				if(split.length == 1){
					bigram = split[0];
					annotations_bigram.add(bigram);
				}
				else{
					for(int i = 0; i < split.length - 1; i++){
						bigram = split[i] + split[i+1];
						annotations_bigram.add(bigram);
					}
				}
			}
		}
		reader.close();
	}

	public void createClusters(String project, String clusterFiles) throws IOException{
		File[] filelist = new File(project + "\\" + clusterFiles).listFiles();
		for(File f : filelist){
			CSVReader reader = new CSVReader(new FileReader(f));
			String[] nextLine;
			int clusterID = 0;
			while ((nextLine = reader.readNext()) != null) {
				clusterID++;
				ArrayList<String> cluster = new ArrayList<String>();
				for(String s : nextLine){
					if(s.length() == 0) continue; // skip null string
					cluster.add(s);
				}

				String clusterName = f.getName() + "_" + clusterID;
				clusters.put(clusterName, cluster);
			}
			reader.close();
		}
	}

	public ArrayList<String> getAnnotationBigram() {
		return new ArrayList<String>(annotations_bigram);
	}

	public Map<String, ArrayList<String>> getClusters(){
		return clusters;
	}

}
