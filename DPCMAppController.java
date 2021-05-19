package bv_ws20;

import java.io.File;

import bv_ws20.RasterImage;
import bv_ws20.Filter.FilterType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class DPCMAppController {
	private static final String initialFileName = "test1.jpg";
	private static File fileOpenPath = new File(".");

	private static final Filter filter = new Filter();

	private float quantization;


	@FXML
	private Slider quantizationSlider;

	@FXML
	private Label quantizationLabel;

	@FXML
	private ComboBox<FilterType> filterSelection;

	@FXML
	private ImageView originalImageView;

	@FXML
	private ImageView predictionImageView;

	@FXML
	private ImageView reconstructedImageView;

	@FXML
	private Label messageLabel;

	@FXML
	private Label oEntropyLabel;

	@FXML
	private Label pEntropyLabel;

	@FXML
	private Label rEntropyLabel;

	@FXML
	private Label mseLabel;

	@FXML
	void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath); 
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if(selectedFile != null) {
			fileOpenPath = selectedFile.getParentFile();
			RasterImage img = new RasterImage(selectedFile);
			img.convertToGray();
			img.setToView(originalImageView);
			processImages();
			messageLabel.getScene().getWindow().sizeToScene();;
		}
	}

	@FXML
	void predictionChanged() {
		processImages();
	}

	@FXML
	void quantizationChanged() {
		quantization = (int) quantizationSlider.getValue();
		quantizationLabel.setText(String.format("%.1f", quantization));
		processImages();
	}

	@FXML
	public void initialize() {
		// set combo boxes items
		filterSelection.getItems().addAll(FilterType.values());
		filterSelection.setValue(FilterType.A);

		// initialize parameters
		quantizationSlider.setValue(1.0);
		quantizationChanged();
		predictionChanged();

		// load and process default image
		RasterImage img = new RasterImage(new File(initialFileName));
		img.convertToGray();
		img.setToView(originalImageView);
		processImages();

	}
	private void processImages() {
		if(originalImageView.getImage() == null)
			return; // no image: nothing to do

		long startTime = System.currentTimeMillis();

		RasterImage originalImg = new RasterImage(originalImageView); 
		RasterImage predictionImg = new RasterImage(originalImg.width, originalImg.height);
		RasterImage reconstructedImg = new RasterImage(originalImg.width, originalImg.height); 

		switch(filterSelection.getValue()) {
		case A:
			filter.applyPredictor("A", originalImg, predictionImg, reconstructedImg);
			break;
		case B:
			filter.applyPredictor("B", originalImg, predictionImg, reconstructedImg);
			break;
		case C:
			filter.applyPredictor("C", originalImg, predictionImg, reconstructedImg);
			break;
		case ABC:
			filter.applyPredictor("ABC", originalImg, predictionImg, reconstructedImg);
			break;
		case ADAPTIV:
			filter.applyPredictor("Adaptive", originalImg, predictionImg, reconstructedImg);
			break;
		}


		predictionImg.setToView(predictionImageView);
		reconstructedImg.setToView(reconstructedImageView);

		oEntropyLabel.setText(String.format("%.3f",  originalImg.getEntropy()));
		pEntropyLabel.setText(String.format("%.3f", predictionImg.getEntropy()));
		rEntropyLabel.setText(String.format("%.3f", reconstructedImg.getEntropy()));

		mseLabel.setText(String.format("%.1f", getMSE(originalImg, reconstructedImg))); 

		messageLabel.setText("Processing time: " + (System.currentTimeMillis() - startTime) + " ms");
	}

	public double getMSE(RasterImage originImg, RasterImage reconstructedImg) {

		int pixelTotal = originImg.width * originImg.height;	//number of pixels
		double mse =0.0; 	//return value
		int sum = 0;		//sum of errors

		for(int y = 0; y < originImg.height; y++) {
			for(int x = 0; x < originImg.width; x++) {
				int pos = y * originImg.width + x;
				int origPx = originImg.argb[pos] & 0xFF;	  		//origin pixel
				int reconPx = reconstructedImg.argb[pos] & 0xFF;	//reconstructed pixel
				int err = reconPx - origPx;  			// calculate Error, formula on slide 24 from 05-Statistische_Signalgroessen
				sum += (err * err);  //formula slide 25 from 05-Statistische_Signalgroessen
			}
		}

		mse = (double)(sum / pixelTotal) ; //formula slide 25 from 05-Statistische_Signalgroessen

		return mse;
	}
}
