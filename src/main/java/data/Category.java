package data;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Category class holds the categories of training instances
 */
public class Category {
    private Set<String> categories = new LinkedHashSet<String>();

    /**
     * Set categories by reading from a file
     * @param filePath: the file contains categories
     */
    public void setCategory (String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        int lineNum = 0;
        while((line = br.readLine()) != null){
            lineNum++;
            if(line.split("\\s+").length > 1){
                System.err.println("Warning: it seems there is more than one category at line " + lineNum);
            }
            categories.add(line);
        }
    }

    /**
     * Set categories by read labels from input raw data file
     * @param filePath
     * @param labelIndex
     * @throws IOException
     */
    public void setCategory (String filePath, int labelIndex) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(filePath));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            String category = nextLine[labelIndex];
            categories.add(category);
        }
        reader.close();
    }

    public List<String> getCategories(){
        List<String> categoryList = new ArrayList<String>(categories);
        return categoryList;
    }
    public int getCategorySize(){
        return categories.size();
    }
}
