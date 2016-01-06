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

    public KNNClassifier(double trainRatio, AbstractFeatureSelector selectorInProgram) {
        super(trainRatio, selectorInProgram);
    }

    @Override
    public void classify() {

    }
    
    @Override
    protected void classifyOneTestArray(double[][] array, String className) {

    }

    @Override
    protected void checkWhichClass(String className, double distA, double distB) {

    }
    
}
