package preprocessing;

/**
 * Created by Mingrui on 8/31/2017.
 */
public class StemmerFactory{
    public Stemmer getStemmer(String stemmerType){
        if("porter".equals(stemmerType)){
            return new PorterStemmer();
        }
        return null;
    }
}
