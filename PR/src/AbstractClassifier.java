import Jama.Matrix;

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

    protected AbstractFeatureSelector selector;

    protected double trainPercentage;
    protected Matrix trainMatrixA, trainMatrixB;
    protected Matrix testMatrixA, testMatrixB;
    protected double[][] trainArrayA, trainArrayB;
    protected double[][] testArrayA, testArrayB;

    protected int[] bestFeaturesIndexes;
    protected int[][] allRowIndexes; // 0-trainingA, 1-trainingB, 2-testA, 3-testB

    protected int classACount, classBCount;
    protected int correctlyClassifiedA, correctlyClassifiedB;
    protected int incorrectlyClassifiedA, incorrectlyClassifiedB;
    protected int unknownA, unknownB;

    protected boolean isDataSetTrained;
    protected boolean isClassified;

    protected String classificationResults;
    protected String trainingAndTestSetsSizes;

    public AbstractClassifier(AbstractFeatureSelector selectorInProgram) {
        this.selector = selectorInProgram;
        this.bestFeaturesIndexes = selector.getFeatureWinnersFLD();
        this.allRowIndexes = new int[4][1];
        this.isDataSetTrained = false;
        this.isClassified = false;
        this.classificationResults = "";
        this.trainingAndTestSetsSizes = "";
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

        isDataSetTrained = true;

        prepareDataSetSizesInfo();
    }

    @Override
    public void getDerivedFeaturesFromSelector() {
        getAllRowIndexes();
        getArraysBestFeaturesOnly();
    }

    @Override
    public void classify() {
        resetClassificationCounters();
        getDerivedFeaturesFromSelector();
        classifyOneTestArray(testArrayA, "A");
        classifyOneTestArray(testArrayB, "B");
        isClassified = true;
        prepareClassificationInfo();
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
        this.isClassified = false;
    }

    @Override
    public boolean isDataSetTrained() {
        return this.isDataSetTrained;
    }

    @Override
    public boolean isClassified() {
        return this.isClassified;
    }

    @Override
    public double countDistance(double[] trainInstance, double[] testInstance) {
        double dist = 0;        
        for (int i=0; i<trainInstance.length; i++) { // for each feature within the given dimension
            dist += (testInstance[i]- trainInstance[i])*(testInstance[i]- trainInstance[i]);
        }
        return Math.sqrt(dist); // distance between current test and training instance (or mean)
    }

    @Override
    public String getTrainingAndTestSetsSizes() {
        return this.trainingAndTestSetsSizes;
    }

    @Override
    public String getClassificationResults() {
        return this.classificationResults;
    }

    // OWN ABSTRACT METHODS
    protected abstract void classifyOneTestArray(double[][] array, String className);
    protected abstract void determineClassForSample(String className);

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
        trainArrayA = trainMatrixA.getMatrix(allRowIndexes[0], bestFeaturesIndexes).getArrayCopy();
        trainArrayB = trainMatrixB.getMatrix(allRowIndexes[1], bestFeaturesIndexes).getArrayCopy();
        testArrayA = testMatrixA.getMatrix(allRowIndexes[2], bestFeaturesIndexes).getArrayCopy();
        testArrayB = testMatrixB.getMatrix(allRowIndexes[3], bestFeaturesIndexes).getArrayCopy();
    }

    private void prepareDataSetSizesInfo() {
        StringBuilder sizesInfo = new StringBuilder("<html>");
        if (isDataSetTrained) {
            int matrixASize = selector.classMatrixes.get(0).copy().transpose().getRowDimension();
            int matrixBSize = selector.classMatrixes.get(1).copy().transpose().getRowDimension();
            int trainSetASize = (int)(matrixASize * this.trainPercentage); // x % from first class instances
            int trainSetBSize = (int)(matrixBSize * this.trainPercentage); // x % from second class instances
            int testSetASize = matrixASize - trainSetASize;
            int testSetBSize = matrixBSize - trainSetBSize;
            sizesInfo.append("Matrix A size: ").append(matrixASize).append("<br>");
            sizesInfo.append("Matrix B Size: ").append(matrixBSize).append("<br>");
            sizesInfo.append("Training set A size: ").append(trainSetASize).append(", ");
            sizesInfo.append("Test set A size: ").append(testSetASize).append("<br>");
            sizesInfo.append("Training set B size: ").append(trainSetBSize).append(", ");
            sizesInfo.append("Test set B size: ").append(testSetBSize).append("<br>");
            sizesInfo.append("</html>");
        }
        else {
            sizesInfo.append("Data sets are not trained!");
        }
        trainingAndTestSetsSizes = sizesInfo.toString();
    }

    private void prepareClassificationInfo() {
        StringBuilder resultsInfo = new StringBuilder("<html>");
        if (isClassified) {
            int samplesCount = classACount+classBCount;
            int correctCount = correctlyClassifiedA + correctlyClassifiedB;
            int incorrectCount = incorrectlyClassifiedA + incorrectlyClassifiedB;
            int unknownCount = unknownA+unknownB;
            double percentage = ((double)correctCount/(double)samplesCount)*100.0;
            resultsInfo.append("END!").append("<br><br>");
            resultsInfo.append("Used classifier type: ").append(this.getClass().getSimpleName()).append("<br>");
            resultsInfo.append("All samples to classify was: ").append(samplesCount).append("<br>");
            resultsInfo.append("Correctly classified samples: ").append(correctCount).append("<br>");
            resultsInfo.append("Badly classified samples: ").append(incorrectCount).append("<br>");
            resultsInfo.append("Unknown samples: ").append(unknownCount).append("<br>");
            resultsInfo.append("PERCENTAGE: ").append(percentage).append("%<br>");
            resultsInfo.append("</html>");
        }
        else {
            resultsInfo.append("Classification not performed!");
        }
        classificationResults = resultsInfo.toString();
    }
}