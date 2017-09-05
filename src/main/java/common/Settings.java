package common;

/**
 * Settings contain general information about this project
 */
public class Settings {
    private String project_name;
    private String stopwordFilePath;
    private int foldNum = 5;

    //parameter of SVM is preset to c = 100000 and gamma = 0.01 based on previous experience
    private double cost = 100000.00;
    private double gamma = 0.01;

    public void setProjectName(String project_name){
        this.project_name = project_name;
    }

    public String getProjectName(){
        return project_name;
    }

    public void setCost(double cost){
        this.cost = cost;
    }

    public double getCost(){
        return cost;
    }

    public void setGamma(double gamma){
        this.gamma = gamma;
    }

    public double getGamma(){
        return gamma;
    }

    public void setStopwordFilePath(String path){
        this.stopwordFilePath = path;
    }

    public String getStopwordFilePath(){
        return this.stopwordFilePath;
    }

    public void setFoldNum(int n){
        foldNum = n;
    }
    public int getFoldNum(){
        return foldNum;
    }


}
