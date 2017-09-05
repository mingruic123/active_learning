package preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A stopword class
 */
public class Stopword {
    private String path = "stopwords.txt";
    private Set<String> stop_words = new HashSet<String>();
    private static Stopword instance = null;
    private Stopword(){}

    public static Stopword getInstance(){
        if(instance == null){
            instance = new Stopword();
        }
        return instance;
    }

    /*
     *When a new path for stopword file is set, force to read the file again.
     */
    public void setPath(String newPath) throws IOException{
        this.path = newPath;
        readStopwords();
    }

    public void readStopwords() throws IOException {
        stop_words.clear();
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String line;
        while((line = br.readLine()) != null){
            stop_words.add(line);
        }
        br.close();
    }

    public void addStopwords(String stopword){
        stop_words.add(stopword);
    }

    public Set<String> getStopwords(){
        return stop_words;
    }
}
