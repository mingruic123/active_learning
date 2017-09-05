package data;

import java.util.ArrayList;

/**
 * An instance stores features, labels and other relevant information of a training or testing instance
 */
public interface Instance {
     void setSentence(String sentence);
     void setFeatureIndex(int index);
     void setLabel(String s_label);
     void setLabel(int b_label);
     void setID(String instanceID);

     String getSentence();
     String getStringLabel();
     int getNumericLabel();
     String getID();
     ArrayList<Integer> getFeatureIndex();
}
