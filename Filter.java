package bv_ws20;

import bv_ws20.RasterImage;

public class Filter {

	public enum FilterType {
		A("A (Horizontal)"), B("B (Vertical)"), C("C (Diagonal)"), ABC("A+B-C"), ADAPTIV("Adaptiv");

		private final String name;

		private FilterType(String s) {
			name = s;
		}

		public String toString() {
			return this.name;
		}
	};

	int pos;
	int A,B,C; // these are used to save A,B,C values for A+B-C
	int P = 0 ; //predication value
	int X = 0 ; 
	int error = 0;
	int predPixel = 0;
	int recPixel = 0;
	
	public void applyPredictor(String type, RasterImage src, RasterImage pred, RasterImage rec) {
	
	for(int y = 0; y<src.height; y++){
		for(int x = 0; x<src.width;x++){	
			pos = y*src.width+x;
			
			P = (src.argb[pos]) & 0xff; // current pixel value
			
			//choosing type of predictor
			switch(type) {
			
			case "A":  //horizontal, x must be > 0 to prevent out of bound
				if(x>0)		X= (src.argb[(pos-1)] & 0xff);  // X will take the left pixel's value
				else  		X = 128; 						
				break;
			
			case "B":  	//vertical, y must be > 0 to prevent out of bound
				if(y>0)  	X= (src.argb[(pos - src.width)] & 0xff);  // X will take the value of the pixel above it
				else  		X = 128;
				break;
				
			case "C": 	//diagonal x, and y must be > 0 to prevent out of bound
				if (y > 0 & x > 0)	X = (src.argb[(pos - src.width) - 1] & 0xff); // X will take the value of the pixel in the left corner above
				else  				X = 128;
				break;
				
			case "ABC": //A+B-C  horizontal + vertical - diagonal
				if(x>0)			A = (src.argb[(pos-1)] & 0xff); 				else A = 128;
				if(y>0) 		B = (src.argb[(pos - src.width)] & 0xff); 		else B = 128;
				if(y>0 & x>0) 	C = (src.argb[(pos - src.width) - 1] & 0xff); 	else C = 128;
				X = A + B - C;
				break;
			
			case "Adaptive": 
				// adaptive
				if(x>0)			A = (src.argb[(pos-1)] & 0xff); 				else A = 128;
				if(y>0) 		B = (src.argb[(pos - src.width)] & 0xff); 		else B = 128;
				if(y>0 & x>0) 	C = (src.argb[(pos - src.width) - 1] & 0xff); 	else C = 128;
				//if horizontal - diagonal < vertical - diagonal, P = horizontal, else P = vertical
				if (Math.abs(A - C) < Math.abs(B - C)) X = B; //compare total values of subtractions (i.e. -123 --> 123)
				else X = A;
				break;
			}
			
			//calculate error
			error = P - X;
			
			predPixel = limit(error + 128); //instruction in Aufgabenstellung slide
			recPixel = limit(error + X);    // slide 12 from 07-Grundlagen-der-Bildkompression
			
			pred.argb[pos] = 0xff000000 | (predPixel << 16) | (predPixel << 8) | predPixel;
			rec.argb[pos] = 0xff000000 | (recPixel << 16) | (recPixel << 8) | recPixel;
		}
	}
}

	public void quantization() {
		
		
	}
	
	public int limit(int n) {
		return (n > 255) ? 255 : (n < 0 ? 0 : n);
	}

	public double limit(double n) {
		return (n > 255) ? 255 : (n < 0 ? 0 : n);
	}
}
