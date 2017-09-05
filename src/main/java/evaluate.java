package activelearning.evaluate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVWriter;

public class EvaluateALResults {
	static String[] filezilla_categories = { "Reliability", "Capability","Requirements", "Security", "Usability", "Performance","Documentation", "Installability" };
	static String[] prismstream_categories = { "Reliability", "Capability", "Requirements", "Security", "Usability" };
	static List<String> categories = new ArrayList<String>();

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String project = args[0];
		int iterationNum = 0;
		if (project.equals("filezilla")) {
			iterationNum = 750;
			categories = Arrays.asList(filezilla_categories);
		}
		if (project.equals("prismstream")) {
			iterationNum = 242;
			categories = Arrays.asList(prismstream_categories);
		}

		CSVWriter writer = new CSVWriter(new FileWriter(project + "_activelearning_results.csv"), ',');
		if(project.equals("filezilla"))
			writer.writeNext(filezilla_categories);
		if(project.equals("prismstream"))
			writer.writeNext(prismstream_categories);
		for (int i = 1; i <= iterationNum; i++) {
			String[] fscoreArray = new String[categories.size()];
			for (String category : categories) {
				int classIndex = categories.indexOf(category) + 1;

				double total_tp = 0.0;
				double total_fp = 0.0;
				double total_fn = 0.0;

				for (int fold = 0; fold < 5; fold++) {
					BufferedReader br1 = new BufferedReader(new FileReader(project + "\\" + "test_" + fold + ".dat"));
					ArrayList<Integer> trueLabels = new ArrayList<Integer>();

					BufferedReader br2 = new BufferedReader(new FileReader(project + "\\" + fold + "\\" + "output_" + fold + "_" + i));
					ArrayList<Integer> predictedLabels = new ArrayList<Integer>();

					String line;
					while ((line = br1.readLine()) != null) {
						String[] split = line.split(" ");
						trueLabels.add(Integer.valueOf(split[0]));
					}

					while ((line = br2.readLine()) != null) {
						 String[] split = line.split(" ");
						predictedLabels.add(Integer.valueOf(split[0]));
					}

					for (int l = 0; l < predictedLabels.size(); l++) {
						// System.out.println(predictedLabels.get(l) + ", " +
						// trueLabels.get(l) + ", " + classIndex);
						if (predictedLabels.get(l) == classIndex && trueLabels.get(l) == classIndex) total_tp++;
						if (predictedLabels.get(l) == classIndex && trueLabels.get(l) != classIndex) total_fp++;
						if (predictedLabels.get(l) != classIndex && trueLabels.get(l) == classIndex) total_fn++;
					}
				}

				double recall = total_tp / (total_tp + total_fn);
				double precision = total_tp / (total_tp + total_fp);
				double fscore = 2.0 * recall * precision / (precision + recall);
				fscoreArray[classIndex - 1] = String.valueOf(fscore);

			}
			writer.writeNext(fscoreArray);
		}
		writer.close();
	}

}
