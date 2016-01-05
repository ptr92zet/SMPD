
import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Piotr
 */
public class FeatureSelectorSfsImpl extends AbstractFeatureSelector {
    private int tempSelectedDimension = 0;
    private int[] featureMatrixRowIndexes;
    private int featureMatrixRowDim;
    private int[][] featureMatrixColIndexes;
    private long startTime, stopTime;
    private long stepCounter = 0;

    @Override
    public void readDataSetFromFile() {
        isDataSetParsed = false;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Datasets - plain text files", "txt");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String line;
            double[] values;

            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                while ((line = reader.readLine()) != null) {
                    String className = line.split(",")[0].split(" ")[0];
                    String classFeatures = line.substring(line.indexOf(",") + 1);

                    values = getDoubleValues(classFeatures.split(","));
                    featureCount = values.length;

                    Tuple<String, double[]> tuple = new Tuple<String, double[]>(className, values);

                    if (objectsCount.size() <= 0) {
                        objectsCount.put(className, 1);
                    }
                    else if (objectsCount.containsKey(className)) {
                        int countForClass = objectsCount.get(className) + 1;
                        objectsCount.put(className, countForClass);
                    }
                    else {
                        objectsCount.put(className, 1);
                    }

                    features.add(tuple);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            inputDataFileName = fileChooser.getSelectedFile().getName();
            System.out.println("End of readDataSet. " + objectsCount.toString());
            isDataSetRead = true;

        }
        else if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION)
            { }
    }

    @Override
    public void createClassMatrixes() {
        for (String className : objectsCount.keySet()) {
            int noOfInstances = objectsCount.get(className);
            int i = 0;
            double[][] dataRows = new double[noOfInstances][];
            Iterator it = features.iterator();
            
            while (it.hasNext() && i < noOfInstances) {
                Tuple tuple = (Tuple) it.next();
                String classNameFromEntry = (String) tuple.getKey();
                
                if (classNameFromEntry.equals(className)) {
                    double[] row = (double[]) tuple.getValue();
                    dataRows[i] = row;
                    i++;
                }
            }
            
            Matrix classMatrix = new Matrix(dataRows);
            classMatrixes.add(classMatrix.transpose()); // instances are rows and features are columns - so transposing
            System.out.println("End of createClassMatrixes");
            isDataSetParsed = true;
        }
    }

    @Override
    public void selectFeatures() {
        startTime = System.nanoTime();
        stepCounter = 0;
        System.out.println("[" + (new Date().toString()) + "] I'm in function: selectFeatures");
        System.out.println("COMPUTATION START TIME: " + new Date().toString());

        try {
            tempSelectedDimension = 1;
            findBestFeatureIn1D();
            int[] chosenFeatures = getFeatureWinnersFLD();

            for (tempSelectedDimension = 2;
                tempSelectedDimension <= selectedDimension;
                tempSelectedDimension++) {

                goThroughChosenFeatures(chosenFeatures);
                chosenFeatures = getFeatureWinnersFLD();
            }

            System.out.println("Winners: " + Arrays.toString(chosenFeatures));

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        stopTime = System.nanoTime() - startTime;
        System.out.println("COMPUTATION STOP TIME: " + new Date().toString());
        System.out.println("ELAPSED TIME: " + (stopTime / 1000000000.0) + " s");
    }

    private void findBestFeatureIn1D() {
        goThroughChosenFeatures(null);
    }

    private void goThroughChosenFeatures(int[] chosenFeatures) {
        resetFieldsAfterChangingDimension();
        if (chosenFeatures == null) {
            featureMatrixRowIndexes = new int[1];
        } else {
            copyFeatureIndexesToRowIndexes(chosenFeatures);
        }

        int lastRowIndex = featureMatrixRowIndexes.length - 1;

        for (featureMatrixRowIndexes[lastRowIndex] = 0;
                featureMatrixRowIndexes[lastRowIndex] < featureMatrixRowDim;
                featureMatrixRowIndexes[lastRowIndex]++) {

            if (chosenFeatures != null && checkForFeatureIndexRepeats(chosenFeatures)) {
                continue;
            }

            analyzeFeaturesForFLD(tempSelectedDimension);
        }
    }

    private void resetFieldsAfterChangingDimension() {
        featureMatrixRowIndexes = new int[tempSelectedDimension];
        featureMatrixRowDim = classMatrixes.get(0).getRowDimension();
        featureMatrixColIndexes = compareAndGetColDimensions(classMatrixes.get(0), classMatrixes.get(1));
        bestFeatureFLD = 0;
    }

    private void copyFeatureIndexesToRowIndexes(int[] chosenFeatures) {
        System.arraycopy(chosenFeatures, 0,
                featureMatrixRowIndexes, 0, chosenFeatures.length);
    }

    private boolean checkForFeatureIndexRepeats(int[] chosenFeatures) {
        boolean omitFeature = false;
        int lastRowIndex = featureMatrixRowIndexes.length - 1;
        for (int feature : chosenFeatures) {
            if (featureMatrixRowIndexes[lastRowIndex] == feature) {
                omitFeature = true;
            }
        }
        return omitFeature;
    }

    private void analyzeFeaturesForFLD(int dimension) {
        Matrix matrixA = classMatrixes.get(0);
        Matrix matrixB = classMatrixes.get(1);

        Matrix currentXMatrixA = matrixA.getMatrix(featureMatrixRowIndexes, featureMatrixColIndexes[0]);
        Matrix currentXMatrixB = matrixB.getMatrix(featureMatrixRowIndexes, featureMatrixColIndexes[1]);

        Tuple<Double, double[]> tupleA;
        Tuple<Double, double[]> tupleB;

        tupleA = computeDetAndMeanMatrix(currentXMatrixA);
        tupleB = computeDetAndMeanMatrix(currentXMatrixB);

        double[] meanVectorA = tupleA.getValue();
        double[] meanVectorB = tupleB.getValue();

        double[] diffArray = new double[dimension];

        for (int i = 0; i < dimension; i++) {

            diffArray[i] = meanVectorA[i] - meanVectorB[i];
            diffArray[i] = diffArray[i] * diffArray[i];
        }

        double sumOfSquaresOfDiffs = 0;
        for (double diff : diffArray) {
            sumOfSquaresOfDiffs += diff;
        }

        double tmp = Math.sqrt(sumOfSquaresOfDiffs) / (tupleA.getKey() + tupleB.getKey());
        if (tmp > bestFeatureFLD) {
            bestFeatureFLD = tmp;
            featureWinnersFLD = featureMatrixRowIndexes.clone();
        }
    }

    private double[] getDoubleValues(String[] featuresVals) {
        double[] values = new double[featuresVals.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Double.parseDouble(featuresVals[i]);
        }
        return values;
    }

    private int[][] compareAndGetColDimensions(Matrix matrixA, Matrix matrixB) {
        int colDimA = matrixA.getColumnDimension();
        int colDimB = matrixB.getColumnDimension();

        int[] allColumnsIndicesA = new int[colDimA];
        int[] allColumnsIndicesB = new int[colDimB];

        for (int i = 0; i < colDimA; i++) {
            allColumnsIndicesA[i] = i;
        }
        for (int i = 0; i < colDimB; i++) {
            allColumnsIndicesB[i] = i;
        }
        int[][] colDimensions = new int[2][1];
        colDimensions[0] = allColumnsIndicesA;
        colDimensions[1] = allColumnsIndicesB;

        return colDimensions;
    }

    private Tuple<Double, double[]> computeDetAndMeanMatrix(Matrix currentXMatrix) {
        System.out.println("[" + (new Date().toString()) + "] I'm in function: computeDetAndMeanMatrix, step: " + ++stepCounter);
        Matrix meanMatrix = null;
        Matrix diffMatrix = null;
        Matrix sMatrix = null;
        double[] meanVector;

        meanVector = createMeanVector(currentXMatrix);
        meanMatrix = createMeanMatrix(meanVector, currentXMatrix.getColumnDimension());
        diffMatrix = currentXMatrix.minus(meanMatrix);
        sMatrix = diffMatrix.times(diffMatrix.transpose());
        Tuple<Double, double[]> tuple = new Tuple<Double, double[]>(sMatrix.det(), meanVector);

        return tuple;
    }

    private double[] createMeanVector(Matrix currentXMatrix) {
        double[] rowSums = new double[tempSelectedDimension];
        double[][] currentXArray = currentXMatrix.getArray();
        double[] currentMeanVector = new double[tempSelectedDimension];
        int colDim = currentXMatrix.getColumnDimension();

        for (int i = 0; i < colDim; i++) {
            for (int j = 0; j < tempSelectedDimension; j++) {
                rowSums[j] += currentXArray[j][i];
            }
        }

        for (int i = 0; i < tempSelectedDimension; i++) {
            currentMeanVector[i] = rowSums[i] / colDim;
        }
        return currentMeanVector;
    }

    private Matrix createMeanMatrix(double[] meanVector, int colDim) {
        double[][] meanArray = new double[tempSelectedDimension][colDim];
        for (int i = 0; i < colDim; i++) {
            for (int j = 0; j < tempSelectedDimension; j++) {
                meanArray[j][i] = meanVector[j];
            }
        }
        return new Matrix(meanArray);
    }
}
