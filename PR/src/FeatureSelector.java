/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public interface FeatureSelector {
    String getInputDataFileName();
    int getFeatureCount();
    int[] getFeatureWinnersFLD();
    double getBestFeatureFLD();
    
    boolean isDataSetRead();
    boolean isDataSetParsed();
    
    void setSelectedDimension(int dimension);
    int getSelectedDimension();
    
    void readDataSetFromFile();
    void createClassMatrixes();
    void selectFeatures();
    String getClassesNames();
}
