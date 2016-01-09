import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

    private final int k;
    private ArrayList<Double> closestDistances;
    private int swapForSecondClass;

    public KNNClassifier(double trainRatio, AbstractFeatureSelector selectorInProgram, int k) {
        super(selectorInProgram);
        this.k = k;
        this.swapForSecondClass = 0;
        initializeClosestDistances();
    }

    @Override
    protected void classifyOneTestArray(double[][] testArray, String className) {
        double tmpDist;
        double currMaxDist;

        System.out.println("--> Starting function classifyOneTestArray for class: " + className + "\n");

        for (double[] testInstance: testArray) { // for each test instance of current class
            System.out.println("Classifying sample from " + className + ": " + Arrays.toString(testInstance));

            initializeClosestDistances();
            for (double[] trainInstanceA : trainArrayA) { // for each training instance of A-class
                tmpDist = countDistance(trainInstanceA, testInstance);
                currMaxDist = Collections.max(closestDistances);
                if (tmpDist < currMaxDist) {
                    closestDistances.remove(currMaxDist);
                    closestDistances.add(tmpDist);
                }
            }
            System.out.println("Now closestDistances to the samples for class A are calculated: " + Arrays.toString(closestDistances.toArray()));

            for (double[] trainInstanceB : trainArrayB) { // for each training instance of B-class
                tmpDist = countDistance(trainInstanceB, testInstance);
                currMaxDist = Collections.max(closestDistances);
                if (tmpDist < currMaxDist) {
                    closestDistances.remove(currMaxDist);
                    closestDistances.add(tmpDist);
                    swapForSecondClass++;
                }
            }
            System.out.println("Now closestDistances to the samples for class B are calculated: " + Arrays.toString(closestDistances.toArray()));
            
            determineClassForSample(className);
            System.out.println("");
        }
    }

    @Override
    protected void determineClassForSample(String className) {
        switch (className) {
            case "A":
                classACount++;
                if (swapForSecondClass <= (k/2)) { // odd number division by 2 = equals k/2 - 0.5 !
                    correctlyClassifiedA++;
                    System.out.println("Correctly classified as class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA) +
                                       ", unknownA: " + Integer.toString(unknownA));
                }
                else {
                    incorrectlyClassifiedA++;
                    System.out.println("INCORRECTLY classified as class B! It was from class A! classACount: " + Integer.toString(classACount) + 
                                       ", correctlyClassifiedA: " + Integer.toString(correctlyClassifiedA) +
                                       ", incorrectlyA: " + Integer.toString(incorrectlyClassifiedA) +
                                       ", unknownA: " + Integer.toString(unknownA));
                }
                break;
            case "B":
                classBCount++;
                if (swapForSecondClass > (k/2)) {
                    correctlyClassifiedB++;
                    System.out.println("Correctly classified as class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB) +
                                       ", unknownB: " + Integer.toString(unknownB));
                }
                else {
                    incorrectlyClassifiedB++;
                    System.out.println("INCORRECTLY classified as class A! It was from class B! classBCount: " + Integer.toString(classBCount) + 
                                       ", correctlyClassifiedB: " + Integer.toString(correctlyClassifiedB) +
                                       ", incorrectlyB: " + Integer.toString(incorrectlyClassifiedB) +
                                       ", unknownB: " + Integer.toString(unknownB));
                }
                break;
        }
    }

    private void initializeClosestDistances() {
        closestDistances = new ArrayList<>(k);
        for (int i=0; i<k; i++) {
            closestDistances.add(Double.MAX_VALUE);
        }
        swapForSecondClass = 0;
    }
}
