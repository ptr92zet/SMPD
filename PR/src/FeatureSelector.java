
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
import java.util.Map;
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
    
    boolean isNewClass = true;
    private String inputData;
    private String inputDataFileName;
    int featureCount=0;
    double[][] featureMatrix, FNew; // original feature matrix and transformed feature matrix
    int bestFeatureNum;
    double bestFeatureFLD;
    
    int[] classLabels, sampleCount;
    String[] classNames;
    List<String> NameList = new ArrayList<String>();
    List<Integer> CountList = new ArrayList<Integer>();
    List<Integer> LabelList = new ArrayList<Integer>();
    ArrayList<Tuple<String, double[]>> features = new ArrayList<Tuple<String, double[]>>();
    HashMap<String, Integer> objectsCount = new HashMap<String, Integer>();
    ArrayList<Matrix> classMatrixes = new ArrayList<Matrix>();
    
    public String getInputData() {
        return this.inputData;
    }
    public void setInputData(String input) {
        this.inputData=input;
    }
    public String getInputDataFileName() {
        return this.inputDataFileName;
    }
    public void setInputDataFileName(String filename) {
        this.inputDataFileName=filename;
    }
    // only getters
    public int getFeatureCount() {
        return this.featureCount;
    }
    //public int getClassCount() {
      //  return this.classCount;
   // }
    public int[] getClassLabels() {
        return this.classLabels;
    }
    public int[] getSampleCount() {
        return this.sampleCount;
    }
    public String[] getClassNames() {
        return this.classNames;
    }
    public int getBestFeatureNum() {
        return this.bestFeatureNum;
    }
    public double getBestFeatureFLD() {
        return this.bestFeatureFLD;
    }


    
    public void readDataSetFromFile() {
        System.out.println("[" + (new Date().toString()) + "] I'm in function: readDataSetFromFile");
        String line="";
        double[] values;
       // boolean isNewClass = true;
        StringBuilder dataset = new StringBuilder();

        
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
                    Tuple<String, double[]> tuple = new Tuple<String, double[]>(className, values);
                    
                    if (objectsCount.size() <= 0) {
                        objectsCount.put(className, 1);
                    }
                    else {
                        if (objectsCount.containsKey(className)) {
                            int countForClass = objectsCount.get(className)+1;
                            objectsCount.put(className, countForClass);
                            break;
                        }
                        else {
                            objectsCount.put(className, 1);
                        }
                    }
                    features.add(tuple);

                    System.out.println("CLASS: " + tuple.getKey() + " | FEATURES: " + Arrays.toString(tuple.getValue()));
                    
                    dataset.append(line).append('$');
                }
                this.inputData=dataset.toString();
                reader.close();
        //        datasetFilenameField.setText(fileChooser.getSelectedFile().getName());
                inputDataFileName=fileChooser.getSelectedFile().getName();
            } catch (Exception e) {        }
        }
    }
    // ArrayList<Tuple<String, double[]>> features
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
        }
    }
    
    public void getDatasetParameters() throws Exception{
 
        
        String dataLines=inputData, firstLnSubstr="", className="";
        firstLnSubstr = inputData.substring(inputData.indexOf(',')+1, inputData.indexOf('$'));
        
        if(firstLnSubstr.length()==0) {
            throw new Exception("The first line is empty");
        }
        
        int featureNum=0;
        while(firstLnSubstr.indexOf(',') > 0){
            firstLnSubstr = firstLnSubstr.substring(firstLnSubstr.indexOf(',')+1);            
            featureNum++;
        }
        featureCount = featureNum+1;
        
        int classIndex = -1;
        
        while(dataLines.length()>1){
            className = dataLines.substring(0,dataLines.indexOf(' '));
            //String className2 = dataLines.split(" ", 1)[0];
            //String classFeatures = dataLines.split(" ", 1)[1];
            //dataLines = dataLines.substring(dataLines.indexOf('$')+1);
            //System.out.println(className2);
            isNewClass = true; 
            classIndex++; // new class index
            for(int i=0; i<NameList.size(); i++) 
                if(className.equals(NameList.get(i))) {
                    isNewClass=false;
                    classIndex = i; // class index
                }
            if(isNewClass) {
                NameList.add(className);
                CountList.add(0); // how many object of a class
            }
            else{
                CountList.set(classIndex, CountList.get(classIndex)+1);
            }           
            LabelList.add(classIndex); // which feature row is for which class, e.g. 0,1,1,1,0,1...
            dataLines = dataLines.substring(dataLines.indexOf('$')+1);
        }
        // based on results of the above analysis, create ARRAY variables
        classNames = new String[NameList.size()];
        for(int i=0; i<classNames.length; i++) {
            classNames[i]=NameList.get(i);
        }
        
        sampleCount = new int[CountList.size()];
        for(int i=0; i<sampleCount.length; i++) {
            sampleCount[i] = CountList.get(i)+1;
        }
        
        classLabels = new int[LabelList.size()];
        for(int i=0; i<classLabels.length; i++) {
            classLabels[i] = LabelList.get(i);
        }
    }
    
    public void fillFeatureMatrix() throws Exception {
        int samples = 0;
        String line, dataLines = inputData;
        
        for(int i=0; i<sampleCount.length; i++) {
            samples += sampleCount[i];
        }
        if(samples <= 0) {
            throw new Exception("No samples found!");
        }
        // TODO: separation between feature matrix for A and B
        featureMatrix = new double[featureCount][samples]; // features-rows, samples-columns (features x samples)
        for(int cols=0; cols<samples; cols++) {
            line = dataLines.substring(0,dataLines.indexOf('$'));
            line = line.substring(dataLines.indexOf(',')+1);
            
            for(int rows=0; rows<featureCount-1; rows++) {
                featureMatrix[rows][cols] = Double.parseDouble(line.substring(0,line.indexOf(',')));
                line = line.substring(line.indexOf(',')+1);
            }
            featureMatrix[featureCount-1][cols] = Double.parseDouble(line);
            dataLines = dataLines.substring(dataLines.indexOf('$')+1);
        }
    }
    
    public void selectFeatures(int[] flags, int d) {
        // for now: check all individual features using 1D, 2-class Fisher criterion

        if(d==1){
            double FLD=0, tmp;
            int max_ind=-1;        
            for(int i=0; i<featureCount; i++){
                if((tmp=computeFLD(featureMatrix[i]))>FLD){
                    FLD=tmp;
                    max_ind = i;
                }
            }
            bestFeatureNum=max_ind;
            bestFeatureFLD=FLD;
        }
        else {
            try {
                bestFeatureFLD = findBestFLD(classMatrixes.get(0), classMatrixes.get(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // TODO: compute for higher dimensional spaces, use e.g. SFS for candidate selection
    }
    
    public double findBestFLD(Matrix matrixA, Matrix matrixB) throws Exception{
        Matrix currentXMatrixA, currentXMatrixB;
        Tuple<Double, double[]> tupleA;
        Tuple<Double, double[]> tupleB;
        
        int currRowIndex, nextRowIndex;
        int rowDim;
        int[][] colDimensions = new int[2][1];
        
        double FLD = 0, tmp;
        
        try {
            colDimensions = compareAndGetColDimensions(matrixA, matrixB);
            rowDim = matrixA.getRowDimension();
            for (currRowIndex=0; currRowIndex<rowDim-1; currRowIndex++) {
                for (nextRowIndex=currRowIndex+1; nextRowIndex<rowDim; nextRowIndex++) {
                    currentXMatrixA = matrixA.getMatrix(currRowIndex, nextRowIndex, colDimensions[0]);
                    currentXMatrixB = matrixB.getMatrix(currRowIndex, nextRowIndex, colDimensions[1]);
                    tupleA = computeDetAndMeanMatrix(currentXMatrixA);
                    tupleB = computeDetAndMeanMatrix(currentXMatrixB);
                    double[] meanVectorA = tupleA.getValue();
                    double[] meanVectorB = tupleB.getValue();
                    double diffA = meanVectorA[0] - meanVectorB[0];
                    double diffB = meanVectorA[1] - meanVectorB[1];
                    tmp = Math.sqrt((diffA * diffA) + (diffB * diffB)) / (tupleA.getKey() + tupleB.getKey());
                    if (tmp > FLD) {
                        FLD = tmp;
                    }
                }
            }
        } catch (Exception e){
            System.out.println("EXCEPTION!!! " + e.getMessage());
        }

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
        // nD, 2-classes
        Matrix meanMatrix = null;
        Matrix diffMatrix = null;
        Matrix sMatrix = null;
        double[] meanVector;
       // Matrix currentXMatrix = null;
       // double FLD=-1;
       // int rowDim = currentXMatrix.getRowDimension();
       // int colDim = currentXMatrix.getColumnDimension();
       // int[] allColumnsIndices = new int[colDim];
       // for (int i=0; i<colDim; i++) {
       //     allColumnsIndices[i] = i;
       // }
        
    //    for (currRowIndex=0; currRowIndex<rowDim-1; currRowIndex++) {
    //        for (currRowIndex=currRowIndex+1; currRowIndex<rowDim; currRowIndex++) {
 //               currentXMatrix = featuresMatrix.getMatrix(currRowIndex, currRowIndex, allColumnsIndices);
                meanVector = createMeanVector(currentXMatrix);
                meanMatrix = createMeanMatrix(meanVector, currentXMatrix.getColumnDimension());
                diffMatrix = currentXMatrix.minus(meanMatrix);
                sMatrix = diffMatrix.times(diffMatrix.transpose());
                //System.out.println("Rows:");
                //twoRowsMatrix.print(4,3);
                //System.out.println("\n\n");
                
       //     }
       // }
        Tuple<Double, double[]> tuple = new Tuple<Double, double[]>(sMatrix.det(), meanVector);
        return tuple;
    }
    
    private double[] createMeanVector(Matrix currentXMatrix) {
        double firstRowSum=0, secondRowSum=0, firstRowMean=0, secondRowMean=0;
        double[][] currentXArray = currentXMatrix.getArray();
        double[] currentMeanVector = null;
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
        //Matrix meanVector = new Matrix(currentMeanVector, 1).transpose();
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
