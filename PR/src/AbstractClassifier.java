
import Jama.Matrix;
import java.util.ArrayList;

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
    protected boolean useSFS;
    protected AbstractFeatureSelector selector;
    protected int[] bestFeaturesIndexes;
    protected Matrix matrixA;
    protected Matrix matrixB;
    
    public boolean isDataSetTrained = false;
    
    protected Matrix trainMatrixA;
    protected Matrix trainMatrixB;
    protected Matrix testMatrixA;
    protected Matrix testMatrixB;
    
    @Override
    public void generateTrainingAndTestSets(double trainRatio, AbstractFeatureSelector selectorInProgram){
        this.trainPercentage = trainRatio; 
        this.selector = selectorInProgram;
        this.matrixA = selector.classMatrixes.get(0).copy();
        this.matrixB = selector.classMatrixes.get(1).copy();
        this.bestFeaturesIndexes = selector.getFeatureWinnersFLD();
        
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
        
        this.isDataSetTrained = true;
    }
    

}
