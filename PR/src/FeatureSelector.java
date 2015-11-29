
import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Piotr
 */
public class FeatureSelector {
    private String inputDataFileName;
    private int featureCount=0;
    //private double[][] featureMatrix, FNew; // original feature matrix and transformed feature matrix
    private double bestFeatureFLD;
    private int[] featureWinnersFLD;
    private boolean isDataSetRead = false;
    private boolean isDataSetParsed = false;
    private boolean isFeatureSpaceDerived = false;
    private int selectedDimension;
    private int[] featureMatrixRowIndexes;
    private int featureMatrixRowDim;
    private int[][] featureMatrixColIndexes;
    private long startTime, stopTime;
    private long stepCounter = 0;
    
    ArrayList<Tuple<String, double[]>> features = new ArrayList<Tuple<String, double[]>>();
    HashMap<String, Integer> objectsCount = new HashMap<String, Integer>();
    ArrayList<Matrix> classMatrixes = new ArrayList<Matrix>();
    
    public String getInputDataFileName() {
        return this.inputDataFileName;
    }
    public int getFeatureCount() {
        return this.featureCount;
    }
    public int[] getFeatureWinnersFLD() {
        return this.featureWinnersFLD;
    }
    public double getBestFeatureFLD() {
        return this.bestFeatureFLD;
    }
    public boolean isDataSetRead() {
        return this.isDataSetRead;
    }
    public boolean isDataSetParsed() {
        return this.isDataSetParsed;
    }
    public void setSelectedDimension(int dimension) {
        this.selectedDimension = dimension;
    }
    public int getSelectedDimension() {
        return this.selectedDimension;
    }

    
    public void readDataSetFromFile() {
        isDataSetParsed = false;
        System.out.println("[" + (new Date().toString()) + "] I'm in function: readDataSetFromFile");
        String line="";
        double[] values;

        JFileChooser fileChooser;
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                                            "Datasets - plain text files", "txt");
        fileChooser.setFileFilter(filter);
        
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                while((line=reader.readLine())!= null) {
                    String className = line.split(",")[0].split(" ")[0];
                    String classFeatures = line.substring(line.indexOf(",")+1);
                    values = getDoubleValues(classFeatures.split(","));
                    featureCount = values.length;
                    Tuple<String, double[]> tuple = new Tuple<String, double[]>(className, values);
                    if (objectsCount.size() <= 0) {
                        objectsCount.put(className, 1);
                    }
                    else {
                        if (objectsCount.containsKey(className)) {
                            int countForClass = objectsCount.get(className)+1;
                            objectsCount.put(className, countForClass);
                        }
                        else {
                            objectsCount.put(className, 1);
                        }
                    }
                    features.add(tuple);
                }
                reader.close();
                inputDataFileName=fileChooser.getSelectedFile().getName();
                System.out.println("End of readDataSet. " + objectsCount.toString());
                isDataSetRead = true;
            } catch (Exception e) { e.printStackTrace();       }
        }
    }

    public void createClassMatrixes() {
        System.out.println("[" + (new Date().toString()) + "] I'm in function: createClassMatrixes");
        
        Iterator setIterator = objectsCount.keySet().iterator();
        while (setIterator.hasNext()) {
            String className = (String)setIterator.next();
            int noOfInstances = objectsCount.get(className);
            double[][] dataRows = new double[noOfInstances][];
            int i=0;
            Iterator it = features.iterator();
            while(it.hasNext() && i < noOfInstances) {
                Tuple tuple = (Tuple)it.next();
                String classNameFromEntry = (String)tuple.getKey();
                if (classNameFromEntry.equals(className)) {
                    double[] row = (double[])tuple.getValue();
                    dataRows[i] = row;
                    i++;
                }                
            }
            Matrix classMatrix = new Matrix(dataRows);
            classMatrixes.add(classMatrix.transpose()); // instances are rows and features are columns - so transposing
            System.out.println("End of createClassMatrixes");
            isDataSetParsed = true;
        }
    }
    
    public void selectFeatures(int featureSpaceCount) {
        startTime = System.nanoTime();
        stepCounter = 0;
        System.out.println("[" + (new Date().toString()) + "] I'm in function: selectFeatures");
        System.out.println("COMPUTATION START TIME: " + new Date().toString());
        if (selectedDimension == 1) {
//            double FLD=0, tmp;
//            int max_ind=-1;        
//            for(int i=0; i<featureCount; i++){
//                if((tmp=computeFLD(featureMatrix[i]))>FLD){
//                    FLD=tmp;
//                    max_ind = i;
//                }
//            }
//            bestFeatureNum1=max_ind;
//            bestFeatureFLD=FLD;
            try {
                findBestFLD(classMatrixes.get(0), classMatrixes.get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (selectedDimension == 2) {
            try {
                findBestFLD(classMatrixes.get(0), classMatrixes.get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                findBestFLD(classMatrixes.get(0), classMatrixes.get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopTime = System.nanoTime() - startTime;
        System.out.println("COMPUTATION STOP TIME: " + new Date().toString());
        System.out.println("ELAPSED TIME: " + (stopTime/1000000000.0) + " s");
    }
    
    public void findBestFLD(Matrix matrixA, Matrix matrixB) throws Exception{
        System.out.println("[" + (new Date().toString()) + "] I'm in function: findBestFLD");

        featureMatrixRowIndexes = new int[selectedDimension];
        featureMatrixRowDim = matrixA.getRowDimension();
        featureMatrixColIndexes = compareAndGetColDimensions(matrixA, matrixB);
        bestFeatureFLD = 0;
        
        try {
            for (featureMatrixRowIndexes[0] = 0;
                 featureMatrixRowIndexes[0] < featureMatrixRowDim - 1;
                 featureMatrixRowIndexes[0]++) {
                goThroughEachFeature(0);
                
//            for (currRowIndex=0; currRowIndex<rowDim-1; currRowIndex++) {
//                for (nextRowIndex=currRowIndex+1; nextRowIndex<rowDim; nextRowIndex++) {
//                    for (nextNextRowIndex = nextRowIndex+1; nextNextRowIndex < rowDim; nextNextRowIndex++) {
//                        rowIndices[0] = currRowIndex;
//                        rowIndices[1] = nextRowIndex;
//                        rowIndices[2] = nextNextRowIndex;
//                        
//                        currentXMatrixA = matrixA.getMatrix(rowIndices, colIndices[0]);
//                        currentXMatrixB = matrixB.getMatrix(rowIndices, colIndices[1]);
//                        
//                        tupleA = computeDetAndMeanMatrix(currentXMatrixA);
//                        tupleB = computeDetAndMeanMatrix(currentXMatrixB);
//                        
//                        double[] meanVectorA = tupleA.getValue();
//                        double[] meanVectorB = tupleB.getValue();
//                        
//                        double diffA = meanVectorA[0] - meanVectorB[0];
//                        double diffB = meanVectorA[1] - meanVectorB[1];
//                        double diffC = meanVectorA[2] - meanVectorB[2];
//                        
//                        tmp = Math.sqrt((diffA * diffA) + (diffB * diffB) + (diffC * diffC)) / (tupleA.getKey() + tupleB.getKey());
//                        if (tmp > FLD) {
//                            FLD = tmp;
//                            bestFeatureNum1 = currRowIndex;
//                            bestFeatureNum2 = nextRowIndex;
//                            bestFeatureNum3 = nextNextRowIndex;
//                        }
//                    }
//                }
            }
        } catch (Exception e){
            System.out.println("EXCEPTION!!! " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Found best FLD: " + bestFeatureFLD);
        System.out.println("The winners are: " + Arrays.toString(featureWinnersFLD));
    }
    
    private void goThroughEachFeature(int depth) {
        int indexForSelectedDimension = selectedDimension - 1;
        if (depth != indexForSelectedDimension) {
            int nextRowIndex = depth + 1;
            for (featureMatrixRowIndexes[nextRowIndex] = featureMatrixRowIndexes[depth] + 1;
                 featureMatrixRowIndexes[nextRowIndex] < featureMatrixRowDim;
                 featureMatrixRowIndexes[nextRowIndex]++) {
                
                goThroughEachFeature(depth + 1);
            }
        }
        
        else {
            Matrix matrixA = classMatrixes.get(0);
            Matrix matrixB = classMatrixes.get(1);
            
            Matrix currentXMatrixA = matrixA.getMatrix(featureMatrixRowIndexes, featureMatrixColIndexes[0]);
            Matrix currentXMatrixB = matrixB.getMatrix(featureMatrixRowIndexes, featureMatrixColIndexes[1]);

            Tuple<Double, double[]> tupleA = computeDetAndMeanMatrix(currentXMatrixA);
            Tuple<Double, double[]> tupleB = computeDetAndMeanMatrix(currentXMatrixB);

            double[] meanVectorA = tupleA.getValue();
            double[] meanVectorB = tupleB.getValue();
            double[] diffArray = new double[selectedDimension];
            
            for (int i = 0; i < selectedDimension; i++) {
                diffArray[i] = meanVectorA[i] - meanVectorB[i];
                diffArray[i] = diffArray[i] * diffArray[i];
            }
            
            double sumOfSquaresOfDiffs = 0;
            for (double diff : diffArray) {
                sumOfSquaresOfDiffs += diff;
            }
            
            double tmp = Math.sqrt(sumOfSquaresOfDiffs) / (tupleA.getKey() + tupleB.getKey());
            if (tmp > bestFeatureFLD) {
                bestFeatureFLD = tmp;
                featureWinnersFLD = featureMatrixRowIndexes.clone();
            }
        }
    }
    
//    private double computeFLD(double[] vec) {
//        System.out.println("[" + (new Date().toString()) + "] I'm in function: createClassMatrixes - 1D, 2 classes");
//        // 1D, 2-classes
//        double FLD=-1;
//        double mA=0, mB=0, sA=0, sB=0;
//        for(int i=0; i<vec.length; i++){
//            if(classLabels[i]==0) {
//                mA += vec[i];
//                sA += vec[i]*vec[i];
//            }
//            else {
//                mB += vec[i];
//                sB += vec[i]*vec[i];
//            }
//        }
//        mA /= sampleCount[0];
//        mB /= sampleCount[1];
//        sA = sA/sampleCount[0] - mA*mA;
//        sB = sB/sampleCount[1] - mB*mB;
//        FLD = Math.abs(mA-mB)/(Math.sqrt(sA)+Math.sqrt(sB));
//        return FLD;
//    }
    
    private Tuple<Double, double[]> computeDetAndMeanMatrix(Matrix currentXMatrix) {
        System.out.println("[" + (new Date().toString()) + "] I'm in function: computeDetAndMeanMatrix, step: " + ++stepCounter);
        Matrix meanMatrix = null;
        Matrix diffMatrix = null;
        Matrix sMatrix = null;
        double[] meanVector;

        meanVector = createMeanVector(currentXMatrix);
        meanMatrix = createMeanMatrix(meanVector, currentXMatrix.getColumnDimension());
        diffMatrix = currentXMatrix.minus(meanMatrix);
        sMatrix = diffMatrix.times(diffMatrix.transpose());
        Tuple<Double, double[]> tuple = new Tuple<Double, double[]>(sMatrix.det(), meanVector);
        
        return tuple;
    }
    
    private double[] createMeanVector(Matrix currentXMatrix) {
        double[] rowSums = new double[selectedDimension];
        double[][] currentXArray = currentXMatrix.getArray();
        double[] currentMeanVector = new double[selectedDimension];
        int colDim = currentXMatrix.getColumnDimension();
        
        for (int i=0; i<colDim; i++) {
            for (int j = 0; j < selectedDimension; j++) {
                rowSums[j] += currentXArray[j][i];
            }
        }
        
        for (int i = 0; i < selectedDimension; i++) {
            currentMeanVector[i] = rowSums[i] / colDim;
        }
        return currentMeanVector;
    }
    
    private Matrix createMeanMatrix(double[] meanVector, int colDim) {
        double[][] meanArray = new double[selectedDimension][colDim];
        for (int i=0; i<colDim; i++) {
            for (int j=0; j<selectedDimension; j++) {
                meanArray[j][i] = meanVector[j];
            }
        }
        return new Matrix(meanArray);
    }
    
    private double[] getDoubleValues(String[] featuresVals) {
        double[] values = new double[featuresVals.length];
        for(int i=0; i<values.length; i++) {
            values[i] = Double.parseDouble(featuresVals[i]);
        }
        return values;
    }
    
    private int[][] compareAndGetColDimensions(Matrix matrixA, Matrix matrixB) {
        int colDimA = matrixA.getColumnDimension();
        int colDimB = matrixB.getColumnDimension();
        
        int[] allColumnsIndicesA = new int[colDimA];
        int[] allColumnsIndicesB = new int[colDimB];
        
        for (int i=0; i<colDimA; i++) {
            allColumnsIndicesA[i] = i;
        }
        for (int i=0; i<colDimB; i++) {
            allColumnsIndicesB[i] = i;
        }
        int[][] colDimensions = new int[2][1];
        colDimensions[0] = allColumnsIndicesA;
        colDimensions[1] = allColumnsIndicesB;
        
        return colDimensions;
    }
}
