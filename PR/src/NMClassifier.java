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
public class NMClassifier extends AbstractClassifier {
    
    private double closestDistanceA, closestDistanceB;
    
    public NMClassifier(AbstractFeatureSelector selectorInProgram) {
        super(selectorInProgram);
        this.closestDistanceA = 0.0;
        this.closestDistanceB = 0.0;        
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
        double[] meanVectorA = createMeanVector(trainArrayA);
        double[] meanVectorB = createMeanVector(trainArrayB);
        System.out.println("--> Starting function classifyOneTestArray for class: " + className + "\n");
        
        for (double[] testInstance: testArray) { // for each test instance of current class
            System.out.println("Classifying sample from " + className + ": " + Arrays.toString(testInstance));
            
            closestDistanceA = countDistance(meanVectorA, testInstance);
            System.out.println("Now closestDistance to the mean for class A is calculated: " + Double.toString(closestDistanceA));
            
            closestDistanceB = countDistance(meanVectorB, testInstance);
            System.out.println("Now closestDistance to the mean for class B is calculated: " + Double.toString(closestDistanceA));
            
            checkWhichClass(className, closestDistanceA, closestDistanceB);
            System.out.println("");
        }
    }
    
    protected void checkWhichClass(String className, double distA, double distB) {
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
    
    private double[] createMeanVector(double[][] array) {
        int rowCount = array.length;
        System.out.println(rowCount);
        int dimension = selector.getSelectedDimension();
        double[] sums = new double[dimension];
        double[] meanVector = new double[dimension];

        
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < rowCount; j++) {
                sums[i] += array[j][i];
            }
        }
        
        for (int i = 0; i < dimension; i++) {
            meanVector[i] = sums[i] / rowCount;
        }
        return meanVector;
    }
    
}
