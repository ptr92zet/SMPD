
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
    
    private String inputData;
    private String inputDataFileName;
    int classCount=0, featureCount=0;
    double[][] featureMatrix, FNew; // original feature matrix and transformed feature matrix
    int[] classLabels, sampleCount;
    String[] classNames;
    int bestFeatureNum;
    double bestFeatureFLD;
    
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
    public int getClassCount() {
        return this.classCount;
    }
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
        String line, dataset="";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                                            "Datasets - plain text files", "txt");
        fileChooser.setFileFilter(filter);
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                while((line=reader.readLine())!= null) {
                    dataset += line + '$';
                }
                this.inputData=dataset;
                reader.close();
        //        datasetFilenameField.setText(fileChooser.getSelectedFile().getName());
                inputDataFileName=fileChooser.getSelectedFile().getName();
            } catch (Exception e) {        }
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
        
        boolean isNewClass;
        int classIndex = -1;
        List<String> NameList = new ArrayList<String>();
        List<Integer> CountList = new ArrayList<Integer>();
        List<Integer> LabelList = new ArrayList<Integer>();
        
        while(dataLines.length()>1){
            className = dataLines.substring(0,dataLines.indexOf(' '));
            isNewClass = true; 
            classIndex++; // new class index
            for(int i=0; i<NameList.size(); i++) 
                if(className.equals(NameList.get(i))) {
                    isNewClass=false;
                    classIndex = i; // class index
                }
            if(isNewClass) {
                NameList.add(className);
                CountList.add(0);
            }
            else{
                CountList.set(classIndex, CountList.get(classIndex).intValue()+1);
            }           
            LabelList.add(classIndex); // class index for current row
            dataLines = dataLines.substring(dataLines.indexOf('$')+1);
        }
        // based on results of the above analysis, create variables
        classNames = new String[NameList.size()];
        for(int i=0; i<classNames.length; i++) {
            classNames[i]=NameList.get(i);
        }
        
        sampleCount = new int[CountList.size()];
        for(int i=0; i<sampleCount.length; i++) {
            sampleCount[i] = CountList.get(i).intValue()+1;
        }
        
        classLabels = new int[LabelList.size()];
        for(int i=0; i<classLabels.length; i++) {
            classLabels[i] = LabelList.get(i).intValue();
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
    
    private double computeFisherLD(double[] vec) {
        // 1D, 2-classes
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
        return Math.abs(mA-mB)/(Math.sqrt(sA)+Math.sqrt(sB));
    }

    public void selectFeatures(int[] flags, int d) {
        // for now: check all individual features using 1D, 2-class Fisher criterion

        if(d==1){
            double FLD=0, tmp;
            int max_ind=-1;        
            for(int i=0; i<featureCount; i++){
                if((tmp=computeFisherLD(featureMatrix[i]))>FLD){
                    FLD=tmp;
                    max_ind = i;
                }
            }
            bestFeatureNum=max_ind;
            bestFeatureFLD=FLD;
        }
        // to do: compute for higher dimensional spaces, use e.g. SFS for candidate selection
    }
}
