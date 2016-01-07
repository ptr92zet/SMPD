
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    private double tmpDistanceA = 0, tmpDistanceB = 0;
    private ArrayList<Double> closestDistancesA;
    private ArrayList<Double> closestDistancesB;
    
    public KNNClassifier(double trainRatio, AbstractFeatureSelector selectorInProgram, int k) {
        super(selectorInProgram);
        this.k = k;
        this.closestDistancesA = new ArrayList<Double>(k);
        this.closestDistancesB = new ArrayList<Double>(k);
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
            
            closestDistancesA = new ArrayList<Double>(k);
            for (double[] trainInstanceA : trainArrayA) { // for each training instance of A-class
                //System.out.println("Calculating distance to the sample from class A: " + Arrays.toString(trainInstanceA));
                tmpDist = countDistance(trainInstanceA, testInstance);
                currMaxDist = Collections.max(closestDistancesA);
                if (tmpDist < currMaxDist) {
                    closestDistancesA.remove(currMaxDist);
                    closestDistancesA.add(tmpDist);
                }
            }
            System.out.println("Now closestDistances to the samples for class A are calculated: " + Arrays.toString(closestDistancesA.toArray()));

            closestDistancesB = new ArrayList<Double>(k);
            for (double[] trainInstanceB : trainArrayB) { // for each training instance of B-class
                //System.out.println("Calculating distance to the sample from class B: " + Arrays.toString(trainInstanceB));
                tmpDist = countDistance(trainInstanceB, testInstance);
                currMaxDist = Collections.max(closestDistancesB);
                if (tmpDist < currMaxDist) {
                    closestDistancesB.remove(currMaxDist);
                    closestDistancesB.add(tmpDist);
                }
            }

            System.out.println("Now closestDistances to the samples for class B are calculated: " + Arrays.toString(closestDistancesB.toArray()));
            
            checkWhichClass(className, closestDistancesA, closestDistancesB);
            System.out.println("");
        }
    }

    //@Override
    protected void checkWhichClass(String className, ArrayList<Double> closestDistancesA, ArrayList<Double> closestDistancesB) {
        switch (className) {
            case "A":
                classACount++;
                if (distA < distB) {
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
    
    private double findMaxInClosestDistances() {
        return Collections.max(closestDistances);
    }
    
}
