
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
    protected Matrix matrixA;
    protected Matrix matrixB;
    
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
        
        int matrixASize = matrixA.getRowDimension();
        int matrixBSize = matrixB.getRowDimension();
        int trainMatrixASize = (int)(matrixASize * trainPercentage);
        int trainMatrixBSize = (int)(matrixBSize * trainPercentage);
        int testMatrixASize = matrixASize - trainMatrixASize;
        int testMatrixBSize = matrixBSize - trainMatrixBSize;       
        
        int colDimA = matrixA.getColumnDimension();
        int colDimB = matrixB.getColumnDimension();
        
        int[] allColumnsIndicesA = new int[colDimA];
        int[] allColumnsIndicesB = new int[colDimB];
        
        for (int i=0; i<colDimA; i++) {
            allColumnsIndicesA[i] = i;
        }
        for (int i=0; i<colDimB; i++) {
            allColumnsIndicesB[i] = i;
        }
        
        trainMatrixA = matrixA.getMatrix(0, trainMatrixASize-1, allColumnsIndicesA);
        trainMatrixB = matrixB.getMatrix(0, trainMatrixBSize-1, allColumnsIndicesB);
        testMatrixA = matrixA.getMatrix(trainMatrixASize, matrixASize-1, allColumnsIndicesA);
        testMatrixB = matrixB.getMatrix(trainMatrixBSize, matrixBSize-1, allColumnsIndicesB);
    }
    

}
