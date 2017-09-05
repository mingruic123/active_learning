package preprocessing;

/**
 * A tokenizer class
 */
public class Tokenizer {
    public String[] tokenize(String s){
        return s.split("\\s+");
    }
}
