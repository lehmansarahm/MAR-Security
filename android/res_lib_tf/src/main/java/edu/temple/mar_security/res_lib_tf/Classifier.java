package edu.temple.mar_security.res_lib_tf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import edu.temple.mar_security.res_lib.utils.Constants;

/** A classifier specialized to label images using TensorFlow Lite. */
public abstract class Classifier {

    /** The runtime device type used for executing classification. */
    public enum Device {
        CPU,
        GPU
    }

    /** Number of results to show in the UI. */
    private static final int MAX_RESULTS = 3;

    /** The loaded TensorFlow Lite model. */
    private MappedByteBuffer tfliteModel;

    /** Image size along the x axis. */
    private final int imageSizeX;

    /** Image size along the y axis. */
    private final int imageSizeY;

    /** Optional GPU delegate for acceleration. */
    // TODO: Declare a GPU delegate
    // private GpuDelegate gpuDelegate = null;

    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    // TODO: Declare a TFLite interpreter
    protected Interpreter tflite;

    /** Options for configuring the Interpreter. */
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    /** Labels corresponding to the output of the vision model. */
    private List<String> labels;

    /** Input image TensorBuffer. */
    private TensorImage inputImageBuffer;

    /** Output probability TensorBuffer. */
    private final TensorBuffer outputProbabilityBuffer;

    /** Processer to apply post processing of the output probability. */
    private final TensorProcessor probabilityProcessor;

    protected static String MODEL_FILENAME;
    protected static String LABEL_FILENAME;

    /**
     * Creates a classifier with the provided configuration.
     *
     * @param activity The current Activity.
     * @param modelFilename The name + ext of the TF model file
     * @param labelFilename The name + ext of the labels file
     * @param device The device to use for classification.
     * @param numThreads The number of threads to use for classification.
     * @return A classifier with the desired configuration.
     */
    public static Classifier create(Activity activity, String modelFilename, String labelFilename,
                                    Classifier.Device device, int numThreads) throws IOException {
        return new ClassifierFloatMobileNet(activity, modelFilename, labelFilename, device, numThreads);
    }

    /** Initializes a {@code Classifier}. */
    protected Classifier(Activity activity, String modelFilename, String labelFilename,
                         Device device, int numThreads) throws IOException {

        MODEL_FILENAME = modelFilename;
        LABEL_FILENAME = labelFilename;

        tfliteModel = FileUtil.loadMappedFile(activity, getModelFilename());
        switch (device) {
            case GPU:
                // TODO: Create a GPU delegate instance and add it to the interpreter options
                // gpuDelegate = new GpuDelegate();
                // tfliteOptions.addDelegate(gpuDelegate);
                break;
            case CPU:
                break;
        }

        tfliteOptions.setNumThreads(numThreads);

        // TODO: Create a TFLite interpreter instance
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        // Loads labels out from the label file.
        labels = FileUtil.loadLabels(activity, getLabelFilename());

        // Reads type and shape of input and output tensors, respectively.
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[1];
        imageSizeX = imageShape[2];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

        // Creates the input tensor.
        inputImageBuffer = new TensorImage(imageDataType);

        // Creates the output tensor and its processor.
        outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

        // Creates the post processor for the output probability.
        probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

        Log.d(Constants.LOG_TAG, "Created a Tensorflow Lite Image Classifier.");
    }

    /** Runs inference and returns the classification results. */
    public List<RecognitionResult> recognizeImage(final Bitmap bitmap, int sensorOrientation) {
        // Logs this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("loadImage");
        long startTimeForLoadImage = SystemClock.uptimeMillis();
        inputImageBuffer = loadImage(bitmap, sensorOrientation);
        long endTimeForLoadImage = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.v(Constants.LOG_TAG, "Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage));

        // Runs the inference call.
        Trace.beginSection("runInference");
        long startTimeForReference = SystemClock.uptimeMillis();
        // TODO: Run TFLite inference
        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());
        long endTimeForReference = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.v(Constants.LOG_TAG, "Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

        // Gets the map of label and probability.
        // TODO: Use TensorLabel from TFLite Support Library to associate the probabilities
        //       with category labels
        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(outputProbabilityBuffer))
                        .getMapWithFloatValue();
        Trace.endSection();

        // Gets top-k results.
        return getTopKProbability(labeledProbability);
    }

    /** Closes the interpreter and model to release resources. */
    public void close() {
        if (tflite != null) {
            // TODO: Close the interpreter
            tflite.close();
            tflite = null;
        }

        // TODO: Close the GPU delegate
        /* if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        } */

        tfliteModel = null;
    }

    /** Get the image size along the x axis. */
    public int getImageSizeX() {
        return imageSizeX;
    }

    /** Get the image size along the y axis. */
    public int getImageSizeY() {
        return imageSizeY;
    }

    /** Loads input image, and applies preprocessing. */
    private TensorImage loadImage(final Bitmap bitmap, int sensorOrientation) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int numRoration = sensorOrientation / 90;
        // TODO(b/143564309): Fuse ops inside ImageProcessor.
        // TODO: Define an ImageProcessor from TFLite Support Library to do preprocessing
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeMethod.NEAREST_NEIGHBOR))
                        .add(new Rot90Op(numRoration))
                        .add(getPreprocessNormalizeOp())
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    /** Gets the top-k results. */
    private static List<RecognitionResult> getTopKProbability(Map<String, Float> labelProb) {
        // Find the best classifications.
        PriorityQueue<RecognitionResult> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<RecognitionResult>() {
                            @Override
                            public int compare(RecognitionResult lhs, RecognitionResult rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (Map.Entry<String, Float> entry : labelProb.entrySet()) {
            pq.add(new RecognitionResult("" + entry.getKey(), entry.getKey(), entry.getValue(), null));
        }

        final ArrayList<RecognitionResult> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    /** Gets the name of the model file stored in Assets. */
    protected abstract String getModelFilename();

    /** Gets the name of the label file stored in Assets. */
    protected abstract String getLabelFilename();

    /** Gets the TensorOperator to nomalize the input image in preprocessing. */
    protected abstract TensorOperator getPreprocessNormalizeOp();

    /**
     * Gets the TensorOperator to dequantize the output probability in post processing.
     *
     * <p>For quantized model, we need de-quantize the prediction with NormalizeOp (as they are all
     * essentially linear transformation). For float model, de-quantize is not required. But to
     * uniform the API, de-quantize is added to float model too. Mean and std are set to 0.0f and
     * 1.0f, respectively.
     */
    protected abstract TensorOperator getPostprocessNormalizeOp();
}
