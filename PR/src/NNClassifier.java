import java.util.Arrays;

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
    
    private double closestDistance;
    private double tmpDistanceA, tmpDistanceB;
    
    public NNClassifier(AbstractFeatureSelector selectorInProgram)
    {
        super(selectorInProgram);
        this.closestDistance = 0.0;
        this.tmpDistanceA = 0.0;
        this.tmpDistanceB = 0.0;
    }

    // FROM ABSTRACT CLASS
    @Override
    protected void classifyOneTestArray(double[][] testArray, String className) {
        double tmpDist;

        System.out.println("--> Starting function classifyOneTestArray for class: " + className + "\n");

        for (double[] testInstance: testArray) { // for each test instance of current class
            System.out.println("Classifying sample from " + className + ": " + Arrays.toString(testInstance));

            closestDistance = Double.MAX_VALUE;
            for (double[] trainInstanceA : trainArrayA) { // for each training instance of A-class
                tmpDist = countDistance(trainInstanceA, testInstance);
                if (tmpDist < closestDistance) {
                    closestDistance = tmpDist;
                }
            }
            tmpDistanceA = closestDistance;
            System.out.println("Now closestDistance to the samples for class A is calculated: " + Double.toString(tmpDistanceA));

            closestDistance = Double.MAX_VALUE;
            for (double[] trainInstanceB : trainArrayB) { // for each training instance of B-class
                //System.out.println("Calculating distance to the sample from class B: " + Arrays.toString(trainInstanceB));
                tmpDist = countDistance(trainInstanceB, testInstance);
                if (tmpDist < closestDistance) {
                    closestDistance = tmpDist;
                }
            }
            tmpDistanceB = closestDistance;
            System.out.println("Now closestDistance to the samples for class B is calculated: " + Double.toString(tmpDistanceB));

            determineClassForSample(className);
            System.out.println("");
        }
    }

    @Override
    protected void determineClassForSample(String className) {
        determineClassForSample(className, tmpDistanceA, tmpDistanceB);
    }

    private void determineClassForSample(String className, double distA, double distB) {
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
}