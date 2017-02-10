import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;



public class PegDetection {
	
	private static File inFile, outFile;
	private static String inPath, outPath;

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
	    System.out.println("\nRunning Peg Detection Pipeline");
	    inFile = new File("Resources/Peg_300_Angle30L_Feb08.jpg");
	    inPath = inFile.getAbsolutePath();
	    outFile = new File("Resources/pegDetectionOutput.jpg");
	    outPath = outFile.getAbsolutePath();
	    
	    System.out.println(String.format("Reading %s", inPath));
	    Mat imgSource = Imgcodecs.imread(inPath);
	    VisionPipeline pipeline = new VisionPipeline();
        pipeline.process(imgSource);
        
        //Mat imgSink = pipeline.hslThresholdOutput();
        
        //ArrayList<MatOfPoint> contourDetections = pipeline.findContoursOutput();  // NOTE!!! Should use filtered Mat!
        ArrayList<MatOfPoint> contourDetections = pipeline.filterContoursOutput();
	    System.out.println(String.format("Detected %s contours", contourDetections.size()));
	    
	    PegTarget pegTarget = new PegTarget(contourDetections);
	    if (pegTarget.isEstablished()) {
	    	System.out.println("Target is Established.");
	    	Rect r = pegTarget.getLeftContour().getBoundingRectangle();
		    System.out.println(String.format("Point: X=%s Y=%s Width=%s Height=%s", r.x,r.y, r.width, r.height));
	        Imgproc.rectangle(imgSource, new Point(r.x, r.y), new Point(r.x + r.width, r.y +r.height), new Scalar(0, 255, 0));
		    if (pegTarget.isComplete()) {
		    	System.out.println("Target is COMPLETE.");
		    	r = pegTarget.getRightContour().getBoundingRectangle();
			    System.out.println(String.format("Point: X=%s Y=%s Width=%s Height=%s", r.x,r.y, r.width, r.height));
		        Imgproc.rectangle(imgSource, new Point(r.x, r.y), new Point(r.x + r.width, r.y +r.height), new Scalar(0, 0, 255));
		    } 
	    } else {
	    	System.out.println("Target is NOT Established.");
	    }
	    pegTarget.dumpStatistics();
	    pegTarget.drawOnImage(imgSource);
	    
	    // Draw a bounding box around each face.
	    //for (MatOfPoint contour : contourDetections) {
	    	//Rect r = Imgproc.boundingRect(contour);
		    //System.out.println(String.format("Point: X=%s Y=%s Width=%s Height=%s", r.x,r.y, r.width, r.height));
	        //Imgproc.rectangle(imgSource, new Point(r.x, r.y), new Point(r.x + r.width, r.y +r.height), new Scalar(0, 255, 0));
	    //}

	    
	    System.out.println(String.format("Writing %s", outPath));
	    Imgcodecs.imwrite(outPath, imgSource);


	}

}
