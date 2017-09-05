package data;

import java.util.*;

/**
 * The implementation of Instance
 */
public class InstanceImpl implements Instance {
    private String instanceID;
    private String sentence;
    private Set<Integer> featureIndex;
    private String s_label;
    private int b_label;
    private ArrayList<String> annotations;
    private List<String> categories;
    boolean isPseudo;

    public InstanceImpl(List<String> categories) {
        featureIndex = new LinkedHashSet<Integer>();
        annotations = new ArrayList<String>();
        this.categories = categories;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void setFeatureIndex(int index) {
        featureIndex.add(index);
    }

    public void setLabel(String s_label) {
        this.s_label = s_label;
    }

    public void setLabel(int b_label) {
        this.b_label = b_label;
    }

    public void setID(String instanceID) {
        this.instanceID = instanceID;
    }

    public void setAnnotations(String cell) {
        String[] split = cell.split(";");
        for (String s : split) {
            annotations.add(s);
        }
    }

    public void setAnnotations(ArrayList<String> annotations) {
        this.annotations = annotations;
    }

    public void resetFeatureIndex() {
        featureIndex.clear();
    }

    public void setPseudo(boolean isPseudo) {
        this.isPseudo = isPseudo;
    }

    public boolean getPseudo() {
        return isPseudo;
    }

    public String getSentence() {
        return sentence;
    }

    public String getStringLabel() {
        return this.s_label;
    }

    public int getNumericLabel() {
        return categories.indexOf(this.s_label) + 1;
    }

    public String getID() {
        return this.instanceID;
    }

    public ArrayList<Integer> getFeatureIndex() {
        ArrayList<Integer> featureIndexList = new ArrayList<Integer>(featureIndex);
        Collections.sort(featureIndexList);
        return featureIndexList;
    }

    public int getBinaryLabel() {
        return this.b_label;
    }

    public ArrayList<String> getAnnotations() {
        return annotations;
    }

    /*
     *Pseudo instance is introduced in the Paper: AutoODC: Automated Generation of Orthogonal Defect Classifications.
     *It is not intended to be used by general users because it needs human labeled data
     */
    public ArrayList<InstanceImpl> createPseudoInstance(Data data) {
        ArrayList<InstanceImpl> pseudoInstanceList = new ArrayList<InstanceImpl>();
        for (String pseudo_feature : annotations) {
            InstanceImpl pseudo = new InstanceImpl(categories);
            pseudo.setAnnotations(annotations);
            pseudo.setID(this.getID());
            pseudo.setLabel(this.getNumericLabel());
            pseudo.setLabel(this.getStringLabel());
            pseudo.setPseudo(true);
            String[] split = pseudo_feature.split(" ");

            pseudo.resetFeatureIndex();
            for (int i = 0; i < split.length; i++) {
                if (split[i].length() == 0)
                    continue;
                int featureIndex = data.getDictionary().indexOf(split[i]);
                pseudo.setFeatureIndex(featureIndex + 1); // set feature index to 1 if feature appears
            }
            pseudoInstanceList.add(pseudo);
        }

        return pseudoInstanceList;

    }

    /*
     *Extension is introduced in the Paper: AutoODC: Automated Generation of Orthogonal Defect Classifications.
     *It is not intended to be used by general users because it needs human labeled data
     */
    public ArrayList<Integer> getExt2FeatureVector(Data data, ArrayList<String> training_annotation_bigram) {
        int dictionarySize = data.getDictionary().size();
        Set<Integer> ext2_feature_set = new HashSet<Integer>();
        for (String a : annotations) {
            String split[] = a.split(" ");
            String bigram = "";
            if (split.length == 1) {
                bigram = split[0];
                int bigramIndex = training_annotation_bigram.indexOf(bigram);
                ext2_feature_set.add(bigramIndex + dictionarySize + 1);
            } else {
                for (int i = 0; i < split.length - 1; i++) {
                    bigram = split[i] + split[i + 1];
                    int bigramIndex = training_annotation_bigram.indexOf(bigram);
                    ext2_feature_set.add(bigramIndex + dictionarySize + 1);
                }
            }
        }

        ArrayList<Integer> ext2_feature_list = new ArrayList<Integer>(ext2_feature_set);
        Collections.sort(ext2_feature_list);

        return ext2_feature_list;
    }


}
