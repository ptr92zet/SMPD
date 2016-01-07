/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ptr
 */
public interface Classifier {
    public void generateTrainingAndTestSets(double trainRatio);
    public void getDerivedFeaturesFromSelector();

    public void classify();

    public void resetClassificationCounters();
    public boolean isDataSetTrained();
    public double countDistance(double[] trainInstance, double[] testInstance);
}