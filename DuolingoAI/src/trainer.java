import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Adam;

public class SimpleAITraining {
    public static void main(String[] args) throws Exception {
        int batchSize = 64;
        int outputNum = 10; // 10 chiffres (0-9)
        int epochs = 5;     // Nombre de passes sur les données

        // Charger les données MNIST
        DataSetIterator trainData = new MnistDataSetIterator(batchSize, true, 123);
        DataSetIterator testData = new MnistDataSetIterator(batchSize, false, 123);

        // Définition du modèle
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123) // Pour la reproductibilité
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001)) // Optimiseur Adam
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(28 * 28) // Entrée = 28x28 pixels
                        .nOut(128)    // Neurones cachés
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(128)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        // Création du réseau de neurones
        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        // Entraînement du modèle
        System.out.println("Entraînement en cours...");
        for (int i = 0; i < epochs; i++) {
            model.fit(trainData);
            System.out.println("Epoch " + (i + 1) + " terminé.");
        }

        // Évaluation du modèle
        System.out.println("Évaluation...");
        double totalAccuracy = 0;
        int totalSamples = 0;
        while (testData.hasNext()) {
            DataSet batch = testData.next();
            INDArray output = model.output(batch.getFeatures());
            totalAccuracy += model.score();
            totalSamples++;
        }
        System.out.println("Précision moyenne : " + (totalAccuracy / totalSamples));
    }
}
