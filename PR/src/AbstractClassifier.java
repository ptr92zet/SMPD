import Jama.Matrix;
import java.util.ArrayList;
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
public abstract class AbstractClassifier implements Classifier{

    protected double trainPercentage;
    protected AbstractFeatureSelector selector;

    protected int[] bestFeaturesIndexes;
    protected int[][] allRowIndexes; // 0-trainingA, 1-trainingB, 2-testA, 3-testB
    
    protected Matrix trainMatrixA, trainMatrixB;
    protected Matrix testMatrixA, testMatrixB;
    protected double[][] trainArrayA, trainArrayB;
    protected double[][] testArrayA, testArrayB;

    protected int classACount, classBCount;
    protected int correctlyClassifiedA, correctlyClassifiedB;
    protected int incorrectlyClassifiedA, incorrectlyClassifiedB;
    protected int unknownA, unknownB;
    
    private boolean isDataSetTrained;
    
    public AbstractClassifier(AbstractFeatureSelector selectorInProgram) {
        this.selector = selectorInProgram;
        this.bestFeaturesIndexes = selector.getFeatureWinnersFLD();
        this.allRowIndexes = new int[4][1];
        this.isDataSetTrained = false;
        this.resetClassificationCounters();
    }

    // FROM INTERFACE
    @Override
    public void generateTrainingAndTestSets(double trainRatio){
        this.trainPercentage = trainRatio;
        Matrix matrixA = selector.classMatrixes.get(0).copy();
        Matrix matrixB = selector.classMatrixes.get(1).copy();
        
        int matrixASize = matrixA.transpose().getRowDimension();
        int matrixBSize = matrixB.transpose().getRowDimension();
        int trainMatrixASize = (int)(matrixASize * trainPercentage); // x % from first class instances
        int trainMatrixBSize = (int)(matrixBSize * trainPercentage); // x % from second class instances
        int testMatrixASize = matrixASize - trainMatrixASize;
        int testMatrixBSize = matrixBSize - trainMatrixBSize;       
        
        int colDimA = matrixA.transpose().getColumnDimension();
        int colDimB = matrixB.transpose().getColumnDimension();
        
        int[] allColumnsIndicesA = new int[colDimA];
        int[] allColumnsIndicesB = new int[colDimB];
        
        for (int i=0; i<colDimA; i++) {
            allColumnsIndicesA[i] = i;
        }
        for (int i=0; i<colDimB; i++) {
            allColumnsIndicesB[i] = i;
        }
        
        this.trainMatrixA = matrixA.transpose().getMatrix(0, trainMatrixASize-1, allColumnsIndicesA); 
        this.trainMatrixB = matrixB.transpose().getMatrix(0, trainMatrixBSize-1, allColumnsIndicesB);
        this.testMatrixA = matrixA.transpose().getMatrix(trainMatrixASize, matrixASize-1, allColumnsIndicesA);
        this.testMatrixB = matrixB.transpose().getMatrix(trainMatrixBSize, matrixBSize-1, allColumnsIndicesB);
        
        System.out.println("MatrixA Size: " + matrixASize + ", TrainArrayA Size: " + trainMatrixASize);
        System.out.println("MatrixB Size: " + matrixBSize + ", TrainArrayB Size: " + trainMatrixBSize);
        System.out.println("TestArrayA Size: " + testMatrixASize);
        System.out.println("TestArrayB Size: " + testMatrixBSize);
        
        isDataSetTrained = true;
    }

    @Override
    public void getDerivedFeaturesFromSelector() {
        getAllRowIndexes();
        getArraysBestFeaturesOnly();
    }

    @Override
    public void resetClassificationCounters() {
        this.classACount = 0;
        this.classBCount = 0;
        this.correctlyClassifiedA = 0;
        this.correctlyClassifiedB = 0;
        this.incorrectlyClassifiedA = 0;
        this.incorrectlyClassifiedB = 0;
        this.unknownA = 0;
        this.unknownB = 0;
    }

    @Override
    public boolean isDataSetTrained() {
        return this.isDataSetTrained;
    }

    @Override
    public double countDistance(double[] trainInstance, double[] testInstance) {
        double dist = 0;        
        for (int i=0; i<trainInstance.length; i++) { // for each feature within the given dimension
            dist += (testInstance[i]- trainInstance[i])*(testInstance[i]- trainInstance[i]);
        }
        return Math.sqrt(dist); // distance between current test and training instance
    }

    // OWN ABSTRACT METHODS
    protected abstract void classifyOneTestArray(double[][] array, String className);
   ///protected abstract void checkWhichClass(String className, double distA, double distB);
    //protected abstract void checkWhichClass(String className, ArrayList<Double> closestDistances);

    // OWN HELPERS
    private void getAllRowIndexes() {
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
}