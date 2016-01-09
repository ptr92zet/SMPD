import Jama.Matrix;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public abstract class AbstractFeatureSelector implements FeatureSelector {
    protected String inputDataFileName;
    protected int featureCount;
    protected double bestFeatureFLD;
    protected int[] featureWinnersFLD;
    protected boolean isDataSetRead = false;
    protected boolean isDataSetParsed = false;
    protected int selectedDimension;

    protected ArrayList<Tuple<String, double[]>> features = new ArrayList<>();
    protected HashMap<String, Integer> objectsCount = new HashMap<>();
    protected ArrayList<Matrix> classMatrixes = new ArrayList<>();

    @Override
    public String getInputDataFileName() {
        return this.inputDataFileName;
    }

    @Override
    public int getFeatureCount() {
        return this.featureCount;
    }

    @Override
    public int[] getFeatureWinnersFLD() {
        return this.featureWinnersFLD;
    }

    @Override
    public double getBestFeatureFLD() {
        return this.bestFeatureFLD;
    }

    @Override
    public boolean isDataSetRead() {
        return this.isDataSetRead;
    }

    @Override
    public boolean isDataSetParsed() {
        return this.isDataSetParsed;
    }

    @Override
    public void setSelectedDimension(int dimension) {
        this.selectedDimension = dimension;
    }

    @Override
    public int getSelectedDimension() {
        return this.selectedDimension;
    }
    
    @Override
    public String getClassesNames() {
        String classes = "";
        for (String name : objectsCount.keySet()) {
            classes += name + "\r\n";
        }
        return classes;
    }
}