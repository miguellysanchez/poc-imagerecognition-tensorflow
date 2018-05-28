package mariannelinhares.mnistandroid.models;


//Provides access to an application's raw asset files;
import android.content.res.AssetManager;
import android.util.Log;
//Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
import java.io.BufferedReader;
//for erros
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
//An InputStreamReader is a bridge from byte streams to character streams:
// //It reads bytes and decodes them into characters using a specified charset.
// //The charset that it uses may be specified by name or may be given explicitly, or the platform's default charset may be accepted.
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
//made by google, used as the window between android and tensorflow native C++
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Changed from https://github.com/MindorksOpenSource/AndroidTensorFlowMNISTExample/blob/master
 * /app/src/main/java/com/mindorks/tensorflowexample/TensorFlowImageClassifier.java
 * Created by marianne-linhares on 20/04/17.
 */

//lets create this classifer
public class TensorFlowClassifier implements Classifier {

    // Only returns if at least this confidence
    //must be a classification percetnage greater than this
    private static final float THRESHOLD = 0.1f;
    private static final String TAG = TensorFlowClassifier.class.getSimpleName();

    private TensorFlowInferenceInterface tensorFlowInterface;

    private String name;
    private String inputName;
    private String outputName;
    private int inputSizeA;
    private int inputSizeB;
    private boolean feedKeepProb;

    private List<String> labels;
    private float[] output;
    private String[] outputNames;

    //given a saved drawn model, lets read all the classification labels that are
    //stored and write them to our in memory labels list
    private static List<String> readLabels(File labelFile) throws IOException {
        FileInputStream labelFileInputStream = new FileInputStream(labelFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelFileInputStream));

        String line;
        List<String> labels = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }

        br.close();
        return labels;
    }


   //given a model, its label file, and its metadata
    //fill out a classifier object with all the necessary
    //metadata including output prediction
    public static TensorFlowClassifier create(String name, String modelFilePath,
                                              String labelFilePath, int inputSizeA, int inputSizeB,
                                              String inputName, String outputName,
                                              boolean feedKeepProb) throws IOException {

        File modelFile = new File(modelFilePath);
        File labelFile = new File(labelFilePath);
        if(!modelFile.exists() || !labelFile.exists()){
            return null;
        }

        //intialize a classifier
        TensorFlowClassifier tensorFlowClassifier = new TensorFlowClassifier();

        //store its name, input and output labels
        tensorFlowClassifier.name = name;

        tensorFlowClassifier.inputName = inputName;
        tensorFlowClassifier.outputName = outputName;

        //read labels for label file
        tensorFlowClassifier.labels = readLabels(labelFile);

        //set its model path and where the raw asset files are
        FileInputStream modelInputStream = new FileInputStream(modelFile);
        tensorFlowClassifier.tensorFlowInterface = new TensorFlowInferenceInterface(modelInputStream);
        int numClasses = 10;

        //how big is the input?
        tensorFlowClassifier.inputSizeA = inputSizeA;
        tensorFlowClassifier.inputSizeB = inputSizeB;

        // Pre-allocate buffer.
        tensorFlowClassifier.outputNames = new String[] { outputName };

        tensorFlowClassifier.outputName = outputName;
        tensorFlowClassifier.output = new float[numClasses];

        tensorFlowClassifier.feedKeepProb = feedKeepProb;

        return tensorFlowClassifier;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public Classification recognize(final float[] pixels) {

        //using the interface
        //give it the input name, raw pixels from the drawing,
        //input size
        tensorFlowInterface.feed(inputName, pixels, 1, inputSizeA, inputSizeB, 1);

        //probabilities
        if (feedKeepProb) {
            Log.d(TAG, "FEED KEEP PROB");
            tensorFlowInterface.feed("keep_prob", new float[] { 1 });
        }
        //get the possible outputs
        tensorFlowInterface.run(outputNames);

        //get the output
        tensorFlowInterface.fetch(outputName, output);

        // Find the best classification
        //for each output prediction
        //if its above the threshold for accuracy we predefined
        //write it out to the view
        Classification ans = new Classification();
        for (int i = 0; i < output.length; ++i) {
            System.out.println(output[i]);
            System.out.println(labels.get(i));
            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {
                ans.update(output[i], labels.get(i));
            }
        }

        return ans;
    }
}
