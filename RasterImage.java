package bv_ws20;

import java.io.File;
import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class RasterImage {
	
	private static final int gray  = 0xffa0a0a0;

	public int[] argb;	// pixels represented as ARGB values in scanline order
	public int width;	// image width in pixels
	public int height;	// image height in pixels
	
	public RasterImage(int width, int height) {
		// creates an empty RasterImage of given size
		this.width = width;
		this.height = height;
		argb = new int[width * height];
		Arrays.fill(argb, gray);
	}
	
	public RasterImage(RasterImage image) {
		// copy constuctor
		this.width = image.width;
		this.height = image.height;
		argb = image.argb.clone();
	}
	
	public RasterImage(File file) {
		// creates an RasterImage by reading the given file
		Image image = null;
		if(file != null && file.exists()) {
			image = new Image(file.toURI().toString());
		}
		if(image != null && image.getPixelReader() != null) {
			width = (int)image.getWidth();
			height = (int)image.getHeight();
			argb = new int[width * height];
			image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
		} else {
			// file reading failed: create an empty RasterImage
			this.width = 256;
			this.height = 256;
			argb = new int[width * height];
			Arrays.fill(argb, gray);
		}
	}
	
	public RasterImage(ImageView imageView) {
		// creates a RasterImage from that what is shown in the given ImageView
		Image image = imageView.getImage();
		width = (int)image.getWidth();
		height = (int)image.getHeight();
		argb = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
	}
	
	public void setToView(ImageView imageView) {
		// sets the current argb pixels to be shown in the given ImageView
		if(argb != null) {
			WritableImage wr = new WritableImage(width, height);
			PixelWriter pw = wr.getPixelWriter();
			pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
			imageView.setImage(wr);
		}
	}
	
	
	// image point operations to be added here

	public void convertToGray() {
		// TODO: convert the image to grayscale
		int r,b,g,pos,grey;
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					pos = y*width + x;

					r = (argb[pos] >> 16) & 0xff;
					g = (argb[pos] >>  8) & 0xff;
					b =  argb[pos]        & 0xff;
					
					//grey = (int)(0.299*r + 0.587*g + 0.114*b);
					//however, above formula causes issues with the histogram depiction later on
					grey = (r+g+b)/3;

					argb[pos] = (0xFF<<24) | (grey<<16) | (grey<<8) | grey;
				}
			}
	}
	public double[] histogram() { //code from Ex.5
		double[] histogram = new double[256];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int currentValue = argb[y * width + x] & 0xff;
				histogram[currentValue] ++;
			}
		}
		return histogram;
	}
	
	public double getEntropy() { //code from Ex. 5
		int pixelTotal = height*width; 
		double[] histogram = histogram();
		double probability = 0; 
		double entropy = 0;
		for (int i =0; i < histogram.length; i++) {
			probability = histogram[i] / (double)pixelTotal; 
			if (probability > 0) {
			// entropy formula: H = H + (-p(i)*log_2(p(i))) 
			entropy += -probability * Math.log(probability) / Math.log(2);
			}
		}
		return entropy;
	}
	
}

