import java.io.File;
import java.net.URL;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

//
// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
//
class DetectFaceDemo {
  public void run() {
    System.out.println("\nRunning DetectFaceDemo");
    //File inFile = new File("Resources/lena.png");
    File inFile = new File("Resources/SteveGrad.jpg");
    String inPath = inFile.getAbsolutePath();
    //File outFile = new File("Resources/faceDetection.png");
    File outFile = new File("Resources/faceDetection.jpg");
    String outPath = outFile.getAbsolutePath();
    File xmlFile = new File("Resources/lbpcascade_frontalface.xml");
    String xmlPath = xmlFile.getAbsolutePath();

    System.out.println(String.format("Reading %s", inPath));



    // Create a face detector from the cascade file in the resources
    // directory.
    CascadeClassifier faceDetector = new CascadeClassifier(xmlPath);
    Mat image = Imgcodecs.imread(inPath);

    //Stuff
    //Imgproc.rectangle(image, new Point(10, 10), new Point(60, 60), new Scalar(0, 255, 0));
    //System.out.println(String.format("Writing %s", outPath));
    //Imgcodecs.imwrite(outPath, image);
    //Stuff
    
    
    // Detect faces in the image.
    // MatOfRect is a special container class for Rect.
    MatOfRect faceDetections = new MatOfRect();
    faceDetector.detectMultiScale(image, faceDetections);

    System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

    // Draw a bounding box around each face.
    for (Rect rect : faceDetections.toArray()) {
        Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
    }

    // Save the visualized detection.
    System.out.println(String.format("Writing %s", outPath));
    Imgcodecs.imwrite(outPath, image);
  }
}

public class Hello
{
   public static void main( String[] args )
   {
      System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
      Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
      System.out.println( "mat = " + mat.dump() );
      new DetectFaceDemo().run();
   }
   
   
}