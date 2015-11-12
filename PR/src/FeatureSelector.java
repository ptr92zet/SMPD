
import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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
        String line="";
        double[] values;
       // boolean isNewClass = true;
        StringBuilder dataset = new StringBuilder();
        List<Matrix> classMatrixes = new ArrayList<Matrix>();
        
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
                    if (isNewClass == true) {
                        NameList.add(className);
                    }
                    
                    String classFeatures = line.substring(line.indexOf(",")+1);
                    values = getDoubleValues(classFeatures.split(","));
                    Tuple<String, double[]> tuple = new Tuple<String, double[]>(className, values);
                    features.add(tuple);
//                    for (String name : NameList) {
//                        if (className.equals(name)) {
//                            isNewClass = false;
//                        } else {
//                            isNewClass = true;
//                            NameList.add(className);
//                            featuresValuesString = classFeatures.split(",");
//                            for(String value: featuresValuesString) {
//                                featuresValues.add(Double.parseDouble(value));
//
//                            }
//                            classMatrixes.add(new Matrix((double[][])featuresValues.toArray()));
//                        }
//                    }
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
    
    public void getDatasetParameters() throws Exception{
        Iterator it = features.iterator();
        while(it.hasNext()) {
            Tuple tuple = (Tuple)it.next();
            String className = (String)tuple.getKey();
            for (String name : NameList) {
                if (className.equals(name)) {
                    isNewClass = false;
                } else {
                    isNewClass = true;
                    NameList.add(className);
                }                        
            }
        }
        
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
        
        boolean isNewClass;
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
        // TODO: compute for higher dimensional spaces, use e.g. SFS for candidate selection
    }
    
    private double computeFLD(double[] vec) {
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
    private double computeFLD(Matrix featuresMatrix) {
        // nD, 2-classes
        Matrix meanMatrix = null;
        Matrix diffMatrix = null;
        Matrix sMatrix = null;
        Matrix currentXMatrix = null;
        double FLD=-1;
        int currentRow, nextRow;
        int rowDim = featuresMatrix.getRowDimension();
        int colDim = featuresMatrix.getColumnDimension();
        int[] allColumnsIndices = new int[colDim];
        for (int i=0; i<colDim; i++) {
            allColumnsIndices[i] = i;
        }
        
        for (currentRow=0; currentRow<rowDim-1; currentRow++) {
            for (nextRow=currentRow+1; nextRow<rowDim; nextRow++) {
                currentXMatrix = featuresMatrix.getMatrix(currentRow, nextRow, allColumnsIndices);
                meanMatrix = createMeanMatrix(createMeanVector(currentXMatrix), colDim);
                diffMatrix = currentXMatrix.minus(meanMatrix);
                sMatrix = diffMatrix.times(diffMatrix.transpose());
                //System.out.println("Rows:");
                //twoRowsMatrix.print(4,3);
                //System.out.println("\n\n");
                
            }
        }

        return FLD;
    }
    
    private double[][] createMeanVector(Matrix x) {
        double firstRowSum=0, secondRowSum=0;
        double[][] currentXArray = x.getArray();
        double[][] currentMeanVector = null;
        int colDim = x.getColumnDimension();
        for (int i=0; i<colDim; i++) {
            firstRowSum += currentXArray[0][i];
            secondRowSum += currentXArray[1][i];
            currentMeanVector[0][0] = firstRowSum / colDim;
            currentMeanVector[1][0] = secondRowSum / colDim;
        }
        return currentMeanVector;
    }
    private Matrix createMeanMatrix(double[][] meanVector, int colDim) {
        double[][] meanArray = new double[2][colDim];
        for (int i=0; i<colDim; i++) {
            meanArray[0][i] = meanVector[0][1];
            meanArray[1][i] = meanVector[1][1];
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
}
