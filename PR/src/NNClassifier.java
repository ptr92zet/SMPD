
import Jama.Matrix;
import java.util.Arrays;
import javax.swing.JOptionPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ptr
 */
public class NNClassifier extends AbstractClassifier {
    
    private double closestDistance, tmpDistanceA = 0, tmpDistanceB = 0;
    private double[][] trainArrayA, trainArrayB, testArrayA, testArrayB;
    private int[][] allRowIndexes; // 0-trainingA, 1-trainingB, 2-testA, 3-testB
    private boolean isClassA, isClassB;
    
    public NNClassifier()
    {
        super.resetClassificationCounters();
        this.isClassA = false;
        this.isClassB = false;
        super.isDataSetTrained = false;
    }
    
           //super.generateTrainingAndTestSets(trainRatio, selector);
    
    @Override
    public void classify() {
        int instanceCount = 0, correctCount = 0;
        resetClassificationCounters();
        if (this.bestFeaturesIndexes != null) {
            getDerivedFeatures();
            classifyOneTestArrayNN(testArrayA, "A");
            classifyOneTestArrayNN(testArrayB, "B");
            instanceCount = classACount + classBCount;
            System.out.println("\n\nEND!\nAll samples to classify was: " + Integer.toString(instanceCount));
            correctCount = correctlyClassifiedA + correctlyClassifiedB;
            System.out.println("Correctly classified samples: " + Integer.toString(correctCount));
            System.out.println("Percentage: " + Double.toString(((double)correctCount/(double)instanceCount)*100.0) + "%");
        }
        else {
            JOptionPane.showMessageDialog(null, "You need to derive feature space first!");
        }
    }
    
    private void getDerivedFeatures() {
        getAllRowIndexes();
        getArraysBestFeaturesOnly();
    }
    
    private void getAllRowIndexes() {
        this.allRowIndexes = new int[4][1];
        int[] indexes;
        
        indexes = new int[trainMatrixA.getRowDimension()];
        for(int i=0; i<trainMatrixA.getRowDimension(); i++) {
            indexes[i] = i;
        }
        allRowIndexes[0] = indexes;
        
        indexes = new int[trainMatrixB.getRowDimension()];
        for(int i=0; i<trainMatrixB.getRowDimension(); i++) {
            indexes[i] = i;
        }
        allRowIndexes[1] = indexes;
        
        indexes = new int[testMatrixA.getRowDimension()];
        for(int i=0; i<testMatrixA.getRowDimension(); i++) {
            indexes[i] = i;
        }
        allRowIndexes[2] = indexes;
        
        indexes = new int[testMatrixB.getRowDimension()];
        for(int i=0; i<testMatrixB.getRowDimension(); i++) {
            indexes[i] = i;
        }
        allRowIndexes[3] = indexes;
    }
    
    private void getArraysBestFeaturesOnly() {
        System.out.println("BEST FEATURES: " + Arrays.toString(bestFeaturesIndexes));
        trainArrayA = trainMatrixA.getMatrix(allRowIndexes[0], bestFeaturesIndexes).getArrayCopy();
        trainArrayB = trainMatrixB.getMatrix(allRowIndexes[1], bestFeaturesIndexes).getArrayCopy();
        testArrayA = testMatrixA.getMatrix(allRowIndexes[2], bestFeaturesIndexes).getArrayCopy();
        testArrayB = testMatrixB.getMatrix(allRowIndexes[3], bestFeaturesIndexes).getArrayCopy();
    }
    
    private void classifyOneTestArrayNN(double[][] testArray, String className) {
        System.out.println("*********************\n" +
                           "Starting function classifyOneTestArrayNN for class: " + className + 
                           "\n*********************");
        
        for (double[] testInstance: testArray) { // for each test instance of current class
            isClassA = false;
            isClassB = false;
            System.out.println("Classifying sample from " + className + ": " + Arrays.toString(testInstance));
            
            closestDistance = Double.MAX_VALUE;
            for (double[] trainInstanceA : trainArrayA) { // for each training instance of A-class
                //System.out.println("Calculating distance to the sample from class A: " + Arrays.toString(trainInstanceA));
                checkForNearestNeighbor(trainInstanceA, testInstance);
            }
            tmpDistanceA = closestDistance;
            System.out.println("Now closestDistance to the samples for class A is calculated: " + Double.toString(tmpDistanceA));
            
            closestDistance = Double.MAX_VALUE;
            for (double[] trainInstanceB : trainArrayB) { // for each training instance of B-class
                //System.out.println("Calculating distance to the sample from class B: " + Arrays.toString(trainInstanceB));
                checkForNearestNeighbor(trainInstanceB, testInstance);
            }
            tmpDistanceB = closestDistance;
            System.out.println("Now closestDistance to the samples for class B is calculated: " + Double.toString(tmpDistanceB));
            
            checkWhichClass(className, tmpDistanceA, tmpDistanceB);
            System.out.println("");
        }
    }
    
    private void checkForNearestNeighbor(double[] trainInstance, double[] testInstance) {
        double tmpDist = countDistance(trainInstance, testInstance);
        if (tmpDist < closestDistance) {
            closestDistance = tmpDist;
        }
    }
    
    private double countDistance(double[] trainInstance, double[] testInstance) {
        double dist = 0;        
        if (trainInstance.length == testInstance.length) {
            for (int i=0; i<trainInstance.length; i++) {// for each feature within the given dimension
                dist += (testInstance[i]- trainInstance[i])*(testInstance[i]- trainInstance[i]);
            }
            dist = Math.sqrt(dist); // distance between current test and training instance
        }
        else {
            JOptionPane.showMessageDialog(null, "Row lengths differ!\nTest row: " + 
                                                Arrays.toString(testInstance) + "\nTraining row: " +
                                                Arrays.toString(trainInstance));
            System.exit(-1);
        }
        return dist;
    }
    
    private void checkWhichClass(String className, double distA, double distB) {
        switch (className) {
            case "A":
                classACount++;
                if (distA < distB) {
                    isClassA = true;
                    correctlyClassifiedA++;
                    System.out.println("Correctly classified as class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA) +
                                       ", unknownA: " + Integer.toString(unknownA));
                }
                else if (distA > distB) {
                    incorrectlyClassifiedA++;
                    System.out.println("INCORRECTLY classified as class B! It was from class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA) +
                                       ", unknownA: " + Integer.toString(unknownA));
                }
                else {
                    unknownA++;
                    System.out.println("UNKNOWN (distances the same)!! It was from class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA) +
                                       ", unknownA: " + Integer.toString(unknownA));
                }
                break;
            case "B":
                classBCount++;
                if (distB < distA) {
                    isClassB = true;
                    correctlyClassifiedB++;
                    System.out.println("Correctly classified as class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB) +
                                       ", unknownB: " + Integer.toString(unknownB));
                }
                else if (distB > distA){
                    incorrectlyClassifiedB++;
                    System.out.println("INCORRECTLY classified as class A! It was from class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB) +
                                       ", unknownB: " + Integer.toString(unknownB));
                }
                else {
                    unknownB++;
                    System.out.println("The instance is UNKNOWN!! It was from class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB) +
                                       ", unknownB: " + Integer.toString(unknownB));                  
                }
                break;
        }
    }
}
                