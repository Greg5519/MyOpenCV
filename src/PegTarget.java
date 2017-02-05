import java.util.ArrayList;

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
	
	/**
	 * 
	 * @param candidateCountours
	 */
	public PegTarget(ArrayList<MatOfPoint> candidateCountours) {
		isEstablished = false;;
		isComplete = false;
		targetContours = parseContours(candidateCountours);
		if (targetContours[leftContour] != null) {isEstablished = true;}
		if (targetContours[rightContour] != null) {isComplete = true;}
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
