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
    
    
    public NNClassifier(int trainRatio, AbstractFeatureSelector selector)
    {
        super.generateTrainingAndTestSets(trainRatio, selector);
        
    }
    
    @Override
    public void classify() {

    }
}
