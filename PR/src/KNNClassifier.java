
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class KNNClassifier extends AbstractClassifier {

    private int k;
    //private double tmpDistanceA = 0, tmpDistanceB = 0;
    //private ArrayList<Double> closestDistancesA;
    //private ArrayList<Double> closestDistancesB;
    private ArrayList<Double> closestDistances;
    private int swapForSecondClass;
    
    public KNNClassifier(double trainRatio, AbstractFeatureSelector selectorInProgram, int k) {
        super(selectorInProgram);
        this.k = k;
        this.swapForSecondClass = 0;
        //initializeClosestDistancesA();
        //initializeClosestDistancesB();
        initializeClosestDistances();
    }

    @Override
    public void classify() {
        int instanceCount, correctCount;
        resetClassificationCounters();
        
        if (this.bestFeaturesIndexes != null) {
            getDerivedFeaturesFromSelector();
            classifyOneTestArray(testArrayA, "A");
            classifyOneTestArray(testArrayB, "B");
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
    
    @Override
    protected void classifyOneTestArray(double[][] testArray, String className) {
        double tmpDist = 0.0;
        double currMaxDist = 0.0;

        System.out.println("--> Starting function classifyOneTestArray for class: " + className + "\n");

        for (double[] testInstance: testArray) { // for each test instance of current class
            System.out.println("Classifying sample from " + className + ": " + Arrays.toString(testInstance));
            
            //initializeClosestDistancesA();
            initializeClosestDistances();
            for (double[] trainInstanceA : trainArrayA) { // for each training instance of A-class
                //System.out.println("Calculating distance to the sample from class A: " + Arrays.toString(trainInstanceA));
                tmpDist = countDistance(trainInstanceA, testInstance);
                currMaxDist = Collections.max(closestDistances);
                if (tmpDist < currMaxDist) {
                    closestDistances.remove(currMaxDist);
                    closestDistances.add(tmpDist);
                }
            }
            //System.out.println("Now closestDistancesA to the samples for class A are calculated: " + Arrays.toString(closestDistancesA.toArray()));
            System.out.println("Now closestDistances to the samples for class A are calculated: " + Arrays.toString(closestDistances.toArray()));

            //initializeClosestDistancesB();
            initializeClosestDistances();
            for (double[] trainInstanceB : trainArrayB) { // for each training instance of B-class
                //System.out.println("Calculating distance to the sample from class B: " + Arrays.toString(trainInstanceB));
                tmpDist = countDistance(trainInstanceB, testInstance);
                currMaxDist = Collections.max(closestDistances);
                if (tmpDist < currMaxDist) {
                    closestDistances.remove(currMaxDist);
                    closestDistances.add(tmpDist);
                    swapForSecondClass++;
                }
            }

           // System.out.println("Now closestDistancesB to the samples for class B are calculated: " + Arrays.toString(closestDistancesB.toArray()));
            System.out.println("Now closestDistances to the samples for class B are calculated: " + Arrays.toString(closestDistances.toArray()));            
            checkWhichClass(className);
            System.out.println("");
        }
    }

    //@Override
    protected void checkWhichClass(String className) {
        switch (className) {
            case "A":
                classACount++;
                if (swapForSecondClass <= (k/2)) { // odd number division by 2 = equals k/2 - 0.5 !
                    correctlyClassifiedA++;
                    System.out.println("Correctly classified as class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA));
                                       //", unknownA: " + Integer.toString(unknownA));
                }
                else {
                    incorrectlyClassifiedA++;
                    System.out.println("INCORRECTLY classified as class B! It was from class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA));
                                       //", unknownA: " + Integer.toString(unknownA));
                }
                break;
            case "B":
                classBCount++;
                if (swapForSecondClass > (k/2)) {
                    correctlyClassifiedB++;
                    System.out.println("Correctly classified as class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB));
                }
                else {
                    incorrectlyClassifiedB++;
                    System.out.println("INCORRECTLY classified as class A! It was from class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB));
                }
                break;
        }
    }
    
//    private void initializeClosestDistancesA() {
//        closestDistancesA = new ArrayList<Double>(k);
//        for (int i=0; i<k; i++) {
//            closestDistancesA.add(Double.MAX_VALUE);
//        }
//    }
//    
//    private void initializeClosestDistancesB() {
//        closestDistancesB = new ArrayList<Double>(k);
//        for (int i=0; i<k; i++) {
//            closestDistancesB.add(Double.MAX_VALUE);
//        }
//    }
    
    private void initializeClosestDistances() {
        closestDistances = new ArrayList<Double>(k);
        for (int i=0; i<k; i++) {
            closestDistances.add(Double.MAX_VALUE);
        }
        swapForSecondClass = 0;
    }
}
