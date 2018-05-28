package mariannelinhares.mnistandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import mariannelinhares.mnistandroid.models.Classification;
import mariannelinhares.mnistandroid.models.Classifier;
import mariannelinhares.mnistandroid.models.TensorFlowClassifier;

public class ImageActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ImageActivity.class.getSimpleName();

    private static final String ASSETS_FILE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp";

    private static final String LABELS_FILENAME = "labels.txt";
    private static final String LABELS_FILE_PATH = ASSETS_FILE_DIRECTORY + "/" + LABELS_FILENAME;

    private static final String RESNET_MODEL_FILENAME = "opt_trained_mobilenet.pb";
    private static final String RESNET_MODEL_FILE_PATH = ASSETS_FILE_DIRECTORY + "/" + RESNET_MODEL_FILENAME;

    private Classifier classifier;

    private ImageView imageViewSample;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initializeViews();
        loadModel();
    }

    private void initializeViews() {
        imageViewSample = (ImageView) findViewById(R.id.image_imageview_sample);
        imageViewSample.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_imageview_sample:
                readImage();
                break;
            default:
                break;
        }
    }


    private void loadModel() {
        File modelFilesDirectory = new File(ASSETS_FILE_DIRECTORY);
        if (!modelFilesDirectory.exists()) {
            if (!modelFilesDirectory.mkdir()) {
                finish();
                Toast.makeText(this, "Unable to resolve the model files at : " + modelFilesDirectory.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        final int pixelWidth = imageViewSample.getWidth();
        final int pixelHeight = imageViewSample.getHeight();
        Log.d(TAG, "This is the imageView's width : " + pixelWidth + " and height: " + pixelHeight);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //add 2 classifiers to our classifier arraylist
                    //the tensorflow classifier and the keras classifier
                    classifier = TensorFlowClassifier.create("ResNet50",
                            RESNET_MODEL_FILE_PATH, LABELS_FILE_PATH, pixelWidth, pixelHeight,
                            "input", "output", true);
                } catch (final Exception e) {
                    //if they aren't found, throw an error!
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();

    }

    private float[] getPixelData() {
        Bitmap bitmap = ((BitmapDrawable) imageViewSample.getDrawable()).getBitmap();
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Get MxN pixel data from bitmap
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] retPixels = new float[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            // Set 0 for white and 255 for black pixel
            int pix = pixels[i];
            int b = pix & 0xff;
            retPixels[i] = (float) ((0xff - b) / 255.0);
        }
        return retPixels;
    }

    private void readImage() {
        float[] pixels = getPixelData();
        final Classification classificationResult = classifier.recognize(pixels);
        String text = "";
        //if it can't classify, output a question mark
        if (classificationResult.getLabel() == null) {
            text += classifier.name() + ": ?\n";
        } else {
            //else output its name
            text += String.format("%s: %s, %f\n", classifier.name(), classificationResult.getLabel(),
                    classificationResult.getConf());
        }
        Log.d(TAG, text);
    }
}
