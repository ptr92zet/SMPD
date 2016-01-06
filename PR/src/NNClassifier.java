
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
    private int classACount = 0, classBCount = 0;
    private int correctlyClassifiedA = 0, correctlyClassifiedB = 0;
    
    public NNClassifier()
    {
        this.isClassA = false;
        this.isClassB = false;
        super.isDataSetTrained = false;
    }
    
           //super.generateTrainingAndTestSets(trainRatio, selector);
    
    @Override
    public void classify() {
        int instanceCount = 0, correctCount = 0;
        if (this.bestFeaturesIndexes != null) {
            getDerivedFeatures();
            classifyOneTestArrayNN(testArrayA, "A");
            classifyOneTestArrayNN(testArrayB, "B");
            instanceCount = classACount + classBCount;
            System.out.println("\n\nEND!\nAll samples to classifi was: " + Integer.toString(instanceCount));
            correctCount = correctlyClassifiedA + correctlyClassifiedB;
            System.out.println("Correctly classified samples: " + Integer.toString(correctCount));
            System.out.println("Percentage: " + Double.toString((correctCount/instanceCount)*100) + "%");
            
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
        trainArrayA = trainMatrixA.getMatrix(allRowIndexes[0], bestFeaturesIndexes).getArrayCopy();
        trainArrayB = trainMatrixB.getMatrix(allRowIndexes[1], bestFeaturesIndexes).getArrayCopy();
        testArrayA = testMatrixA.getMatrix(allRowIndexes[2], bestFeaturesIndexes).getArrayCopy();
        testArrayB = testMatrixB.getMatrix(allRowIndexes[3], bestFeaturesIndexes).getArrayCopy();
    }
    
    private void classifyOneTestArrayNN(double[][] testArray, String className) {
        System.out.println("Starting classifying one Test Array for class: " + className);
        for (double[] testInstance: testArray) { // for each test instance of A-class
            isClassA = false;
            isClassB = false;
            
            closestDistance = Double.MAX_VALUE;
            for (double[] trainInstanceA : trainArrayA) { // for each training instance of A-class
                checkForNearestNeighbor(trainInstanceA, testInstance);
            }
            tmpDistanceA = closestDistance;
            System.out.println("Now closestDistance for class A is calculated: " + Double.toString(tmpDistanceA));
            
            closestDistance = Double.MAX_VALUE;
            for (double[] trainInstanceB : trainArrayB) {
                checkForNearestNeighbor(trainInstanceB, testInstance);
            }
            tmpDistanceB = closestDistance;
            System.out.println("Now closestDistance for class B is calculated: " + Double.toString(tmpDistanceA));
            
            checkWhichClass(className, tmpDistanceA, tmpDistanceB);

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
        if (distA < distB) {
            isClassA = true;
            classACount++;
            if (className.equals("A")) {
                correctlyClassifiedA++;
                System.out.println("The instance correctly classified as class A! classACount: " + Integer.toString(classACount) + 
                                   ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA));
            }
            else {
                System.out.println("The instance INCORRECTLY classified as class A! It was from class B!");
            }
        }
        else if (distA > distB) {
            isClassB = true;
            classBCount++;
            if (className.equals("B")) {
                correctlyClassifiedB++;
                System.out.println("The instance correctly classified as class B! classBCount: " + Integer.toString(classBCount) + 
                                   ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB));
            }
            else {
                System.out.println("The instance INCORRECTLY classified as class B! It was from class A!");
            }
        }
        else {
            System.out.println("The instance CLASSIFIED as class UNKNOWN! It was from " + className);
        }
    }
}
                