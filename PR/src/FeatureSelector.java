
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
    private double[][] featureMatrix, FNew; // original feature matrix and transformed feature matrix
    private int bestFeatureNum1, bestFeatureNum2;
    private double bestFeatureFLD;
    private boolean isDataSetRead = false;
    private boolean isDataSetParsed = false;
    private boolean isFeatureSpaceDerived = false;

    ArrayList<Tuple<String, double[]>> features = new ArrayList<Tuple<String, double[]>>();
    HashMap<String, Integer> objectsCount = new HashMap<String, Integer>();
    ArrayList<Matrix> classMatrixes = new ArrayList<Matrix>();
    
    public String getInputDataFileName() {
        return this.inputDataFileName;
    }
    public int getFeatureCount() {
        return this.featureCount;
    }
    public int getBestFeatureNum1() {
        return this.bestFeatureNum1;
    }
    public int getBestFeatureNum2() {
        return this.bestFeatureNum2;
    }
    public double getBestFeatureFLD() {
        return this.bestFeatureFLD;
    }
    public boolean isDataSetRead() {
        return this.isDataSetRead;
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
        System.out.println("[" + (new Date().toString()) + "] I'm in function: selectFeatures");
        if(featureSpaceCount==1){
            double FLD=0, tmp;
            int max_ind=-1;        
            for(int i=0; i<featureCount; i++){
                if((tmp=computeFLD(featureMatrix[i]))>FLD){
                    FLD=tmp;
                    max_ind = i;
                }
            }
            bestFeatureNum1=max_ind;
            bestFeatureFLD=FLD;
        }
        else {
            try {
                bestFeatureFLD = findBestFLD(classMatrixes.get(0), classMatrixes.get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public double findBestFLD(Matrix matrixA, Matrix matrixB) throws Exception{
        System.out.println("[" + (new Date().toString()) + "] I'm in function: findBestFLD");
        Matrix currentXMatrixA, currentXMatrixB;
        Tuple<Double, double[]> tupleA;
        Tuple<Double, double[]> tupleB;
        
        int currRowIndex, nextRowIndex;
        int rowDim;
        int[][] colIndices = new int[2][1];
        int[] rowIndices = new int[2];
        
        double FLD = 0, tmp;
        
        try {
            colIndices = compareAndGetColDimensions(matrixA, matrixB);
            rowDim = matrixA.getRowDimension();
            for (currRowIndex=0; currRowIndex<rowDim-1; currRowIndex++) {
                for (nextRowIndex=currRowIndex+1; nextRowIndex<rowDim; nextRowIndex++) {
                    rowIndices[0] = currRowIndex;
                    rowIndices[1] = nextRowIndex;
                    currentXMatrixA = matrixA.getMatrix(rowIndices, colIndices[0]);
                    currentXMatrixB = matrixB.getMatrix(rowIndices, colIndices[1]);
                    tupleA = computeDetAndMeanMatrix(currentXMatrixA);
                    tupleB = computeDetAndMeanMatrix(currentXMatrixB);
                    double[] meanVectorA = tupleA.getValue();
                    double[] meanVectorB = tupleB.getValue();
                    double diffA = meanVectorA[0] - meanVectorB[0];
                    double diffB = meanVectorA[1] - meanVectorB[1];
                    tmp = Math.sqrt((diffA * diffA) + (diffB * diffB)) / (tupleA.getKey() + tupleB.getKey());
                    if (tmp > FLD) {
                        FLD = tmp;
                        bestFeatureNum1 = currRowIndex;
                        bestFeatureNum2 = nextRowIndex;
                    }
                }
            }
        } catch (Exception e){
            System.out.println("EXCEPTION!!! " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Found best FLD: " + FLD);
        System.out.println("The winners are: " + bestFeatureNum1 + ", " + bestFeatureNum2);
        return FLD;
    }
    
    private double computeFLD(double[] vec) {
        System.out.println("[" + (new Date().toString()) + "] I'm in function: createClassMatrixes - 1D, 2 classes");
        // 1D, 2-classes
        double FLD=-1;
        double mA=0, mB=0, sA=0, sB=0;
        for(int i=0; i<vec.length; i++){
            if(classLabels[i]==0) {
                mA += vec[i];
                sA += vec[i]*vec[i];
            }
            else {
                mB += vec[i];
                sB += vec[i]*vec[i];
            }
        }
        mA /= sampleCount[0];
        mB /= sampleCount[1];
        sA = sA/sampleCount[0] - mA*mA;
        sB = sB/sampleCount[1] - mB*mB;
        FLD = Math.abs(mA-mB)/(Math.sqrt(sA)+Math.sqrt(sB));
        return FLD;
    }
    private Tuple<Double, double[]> computeDetAndMeanMatrix(Matrix currentXMatrix) {
        System.out.println("[" + (new Date().toString()) + "] I'm in function: computeDetAndMeanMatrix");
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
        double firstRowSum=0, secondRowSum=0, firstRowMean=0, secondRowMean=0;
        double[][] currentXArray = currentXMatrix.getArray();
        double[] currentMeanVector = new double[2];
        int colDim = currentXMatrix.getColumnDimension();
        
        for (int i=0; i<colDim; i++) {
            firstRowSum += currentXArray[0][i];
            secondRowSum += currentXArray[1][i];
        }
        firstRowMean = firstRowSum / colDim;
        secondRowMean = secondRowSum / colDim;
        for (int i=0; i<colDim; i++) {
            currentMeanVector[0] = firstRowMean;
            currentMeanVector[1] = secondRowMean;
        }
        return currentMeanVector;
    }
    
    private Matrix createMeanMatrix(double[] meanVector, int colDim) {
        double[][] meanArray = new double[2][colDim];
        for (int i=0; i<colDim; i++) {
            meanArray[0][i] = meanVector[0];
            meanArray[1][i] = meanVector[1];
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
    
    private int[][] compareAndGetColDimensions(Matrix matrixA, Matrix matrixB) throws Exception {
        int rowDimA = matrixA.getRowDimension();
        int rowDimB = matrixB.getRowDimension();
        if (rowDimA != rowDimB) {
            throw new Exception("Row dimensions of Matrix A and Matrix B are different!"
                    + " MatrixA rows: " + rowDimA + ", MatrixB rows: " + rowDimB);
        }
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
