import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class PegTarget {

	public final int leftContour = 0;
	public final int rightContour = 1;
	private PegContour[] targetContours;
	
	private boolean isEstablished;	// Target consists of at least one contour.
	private boolean isComplete;		// Target consists of both contours.
	
	// Vital Target Statistics determined from target contours
	private int leftEdge;
	private int rightEdge;
	private int topEdge;
	private int bottomEdge;
	private int maxWidth;
	private int maxHeight;
	private int maxArea;
	private double aspectRatio;
	private int centreX;
	
	
	/**
	 * 
	 * @param candidateCountours
	 */
	public PegTarget(ArrayList<MatOfPoint> candidateCountours) {
		isEstablished = false;;
		isComplete = false;
		resetStatistics();
		targetContours = parseContours(candidateCountours);
		if (targetContours[rightContour] != null) {
			// Target consists of TWO contours - So it is complete
			isEstablished = true;
			isComplete = true;
			establishStatistics(targetContours[leftContour],targetContours[rightContour]);
		} else if (targetContours[leftContour] != null) {
			// Target consists of ONLY ONE contour - Which is sort of ok but NOT complete
			// NOTE: In this case the left contour is also the right contour
			isEstablished = true;
			isComplete = false;
			establishStatistics(targetContours[leftContour],targetContours[leftContour]);
		} else {
			// Target consists of NO contours - We have NO valid target
			isEstablished = false;;
			isComplete = false;
			resetStatistics();
		}
	}
	
	private void resetStatistics() {
		leftEdge = 0;
		rightEdge = 0;
		topEdge = 0;
		bottomEdge = 0;
		maxWidth = 0;
		maxHeight = 0;
		maxArea = 0;
		aspectRatio = 0.0;
		centreX = 0;
	}
	
	private void establishStatistics(PegContour leftSide, PegContour rightSide) {
		resetStatistics();
		// Remember that Top Left Corner is (0,0) so...
		// Horizontal position uses X value increasing from left to tight
		// Vertical position uses Y value increasing from top to bottom
		Rect lRect = leftSide.getBoundingRectangle();
		Rect rRect = rightSide.getBoundingRectangle();
		leftEdge = lRect.x;
		rightEdge = rRect.x + rRect.width;
		topEdge = Math.min(lRect.y, rRect.y);
		bottomEdge = Math.max((lRect.y + lRect.height), (rRect.y + rRect.height));
		maxWidth = rightEdge - leftEdge;
		maxHeight = bottomEdge - topEdge;
		maxArea = maxWidth * maxHeight;
		aspectRatio = ((double) maxWidth) / ((double) maxHeight);
		centreX = leftEdge + (maxWidth/2);
	}

	public void dumpStatistics() {
		System.out.println("Dumping Target Statistics...");
		System.out.println(String.format("isEstablished= %s. isComplete= %s", isEstablished,isComplete ));
		System.out.println(String.format("leftEdge= %s. rightEdge= %s", leftEdge,rightEdge ));
		System.out.println(String.format("topEdge= %s. bottomEdge= %s", topEdge,bottomEdge ));
		System.out.println(String.format("maxWidth= %s. maxHeight= %s", maxWidth,maxHeight ));
		System.out.println(String.format("maxArea= %s. aspectRatio= %s", maxArea,aspectRatio ));
		System.out.println(String.format("centreX= %s. Distance= %s", centreX, estimateDistance() ));
	}
	
	public void drawOnImage(Mat imgSource) {
		if (isEstablished) {
			Imgproc.rectangle(imgSource, new Point(leftEdge, topEdge), new Point(rightEdge, bottomEdge), new Scalar(0, 255, 255));
		}
	}
	
	public double estimateDistance() {
		double targetHeightInMeters = (5.0 / 39.37);	// 5 inches / 39.37 inches per meter
		double targetWidthInMeters = (10.25 / 39.37);	// 10.25 inches / 39.37 inches per meter
		double axisHorizontalViewAngle = Math.toRadians(49.0);	// 49 degrees from published specs;
		double axisVerticalViewAngle = Math.toRadians(39.80);	// Estimated (49 degrees * 240/320) based on resolution change;
		double tanHorizontalAngle = Math.tan(axisHorizontalViewAngle);
		double tanVerticalAngle = Math.tan(axisVerticalViewAngle);
		double xResolution = 320.0;		// Resolution is 320x240 pixels
		double yResolution = 240.0;		// Resolution is 320x240 pixels
		double dist = 99.9;
		if (isEstablished) {
			dist = targetWidthInMeters * xResolution / (2.0 * maxWidth * tanHorizontalAngle);
			dist = targetWidthInMeters * xResolution / (maxWidth * tanHorizontalAngle);
			dist = targetHeightInMeters * yResolution / (2.0 * maxHeight * tanVerticalAngle);
			dist = targetHeightInMeters * yResolution / (maxHeight * tanVerticalAngle);
		}
		return dist;
	}

	/**
	 * Parse a raw list of a bunch of image contours to narrow down the list to those contours 
	 * that match the peg target specifications and then to further narrow down the list to 
	 * the best candidate for a left and right target contour.
	 * 
	 *  According to the Steamworks game, the peg target consists of two vertical contours 
	 *  in an outer bounding rectangle of width=10.25 inches and height=5 inches. There is a 
	 *  horizontal gap of 6.25 inches between the left and right contours. The target 
	 *  is 10.75 inches above the floor. The peg is in the center of the target.
	 * 
	 * 
	 * @param candidateCountours - a list of random image contours
	 * @return void
	 */
	private  PegContour[] parseContours (ArrayList<MatOfPoint> candidateCountours) {
		
		PegContour[] foundContours = new PegContour[2];
		
		// Pick the two best candidate contours from the list provided.
		// Just remember them for now; don't worry about left and right
	    for (MatOfPoint contour : candidateCountours) {
	    	if (PegContour.isaPegContour(contour)) {
	    		if (foundContours[leftContour] == null) {
	    			foundContours[leftContour] = new PegContour(contour);
	    		} else if (foundContours[rightContour] == null) {
	    			foundContours[rightContour] = new PegContour(contour);    			
	    		} else {
	    			// We have to choose between three valid candidate contours.
	    			// Keep the two contours with the largest area (size).
	    			PegContour newContour = new PegContour(contour);
	    			if (newContour.getArea() > foundContours[leftContour].getArea()) {
	    				// The new contour is bigger than the current left contour
	    				// so compare current left and right contours to see which one to keep
	    				if (foundContours[leftContour].getArea() > foundContours[rightContour].getArea()) {
	    					// Replace the right contour
	    					foundContours[rightContour] = newContour;
	    				} else {
	    					// Replace the left contour
	    					foundContours[leftContour] = newContour;
	    				}
	    			} else if (newContour.getArea() > foundContours[rightContour].getArea()) {
		    			// The new contour is bigger than the current right contour
		    			// so compare current left and right contours to see which one to keep
		    			if (foundContours[leftContour].getArea() > foundContours[rightContour].getArea()) {
		    				// Replace the right contour
		    				foundContours[rightContour] = newContour;
		    			} else {
		    				// Replace the left contour
		    				foundContours[leftContour] = newContour;
		    			}
		    		} // else current contours are both larger than the new contours so do nothing
	    		}
	    	}
	    } // end of the big for loop picking the best two candidates
	    
	    // Now we have narrowed down the list to 0, 1, or 2 contours.
	    // If we have 0 or 1 contours, the target is ok but incomplete so just return.
	    // If we have two contours, we have to figure out which one is left and which one is right;
	    if ((foundContours[leftContour] != null) && (foundContours[rightContour] != null)) {
	    	if (foundContours[leftContour].getLeftEdge() > foundContours[rightContour].getLeftEdge()) {
	    		PegContour tempContour = foundContours[leftContour];
	    		foundContours[leftContour] = foundContours[rightContour];
	    		foundContours[rightContour] = tempContour;
	    	}
	    }
		return foundContours;
	}

	/**
	 * @return Value of isEstablished - Target consists of at least one contour.
	 */
	public boolean isEstablished() {
		return isEstablished;
	}

	/**
	 * @return Value of  isComplete - Target consists of both contours.
	 */
	public boolean isComplete() {
		return isComplete;
	}
	
	public PegContour getLeftContour() {
		return targetContours[leftContour];
	}

	public PegContour getRightContour() {
		return targetContours[rightContour];
	}


}
