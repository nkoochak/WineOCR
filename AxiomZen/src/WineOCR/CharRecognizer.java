package WineOCR;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

public class CharRecognizer {
	static File trainImageFolder;
	static File trainLable;
	static File trainSetFolder;

	static Map<Integer, ArrayList<TrainInstance>> trainsets = new HashMap<Integer, ArrayList<TrainInstance>>();
	static Map<Integer, Character> imageNametoLable = new HashMap<Integer, Character>();
	static Image image;
	static PixelImage pixelImage;

	public static void main(String[] args) {
		//1. Feature Extraction from image files:
		//(To be completed later)
//		CharRecognizer.ReadLablesFromXML();
//		CharRecognizer.TrainImagesToTrainSets(CharRecognizer.trainImageFolder);
//		CharRecognizer.WriteTrainSetsIntoFiles();
		
		//2. Testing the applicability of SVM on char recognition phase:
		startSVMClassification(args[0], args[1]);
	}

	static class TrainInstance{
		int[] featureVector; 
		int lable;
		
		TrainInstance(int[] featureVector,char lable){
			this.featureVector = featureVector;
			this.lable = lable;
		}
		
		String ToString(){
			String result="";
			for(int i=0;i<featureVector.length;i++){
				result+=featureVector[i]+" ";
			}
			result+=lable;
			return result;
		}
	}

	public static void ReadLablesFromXML(){
		try{
			trainLable = new File("Train Images\\char.xml");
			BufferedReader br = new BufferedReader(new FileReader(trainLable));
			String line;
			while ((line = br.readLine()) != null) {
				if(line.contains("<image file=")){
					imageNametoLable.put(Integer.parseInt(line.substring(line.substring(0,line.indexOf(".jpg")).lastIndexOf("/")+1,line.indexOf(".jpg"))), line.substring(line.indexOf("tag=")+5, line.indexOf("tag=")+6).charAt(0));
				}
			}
			br.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	public static void TrainImagesToTrainSets(File folder) {
		trainImageFolder = new File("Train Images\\char");
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				TrainImagesToTrainSets(fileEntry);
			} else {
				try{
					image = ImageIO.read(fileEntry);
					pixelImage = new PixelImage(image);
					pixelImage.toGrayScale(false);
				}
				catch (IOException e){
					e.printStackTrace();
				}
				if (image == null){
					System.err.println("Cannot find image file: " + fileEntry.getPath());
				}
				else{
					char lable = imageNametoLable.get(Integer.parseInt(fileEntry.getName().substring(0,fileEntry.getName().indexOf("."))));
					TrainInstance instance = new TrainInstance(pixelImage.pixels,lable);

					int featureSize = pixelImage.pixels.length;
					ArrayList<TrainInstance> list = trainsets.get(featureSize);
					if(list == null)
						list = new ArrayList<TrainInstance>();
					list.add(instance);
					trainsets.put(featureSize, list);
				}
			}
		}
	}

	public static void WriteTrainSetsIntoFiles(){
		try{
			trainSetFolder = new File("Train Sets");
			Set<Integer> sizes = trainsets.keySet();
			for (Iterator<Integer> i = sizes.iterator(); i.hasNext();) {
				Integer key = i.next();
				File trainfile = new File(trainSetFolder+"\\"+key+".txt");
				FileWriter fw = new FileWriter(trainfile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				for(TrainInstance instance : trainsets.get(key)){
					bw.write(instance.ToString());
					bw.newLine();
				}
				bw.close();
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public static double startSVMClassification(String trainFile, String testFile)
	{
		double pAccuracy = 0.0;
		String[] argv = new String[100];
		String[] argvP = new String[100];
		
		argv[0] = "-t";
		argv[1] = "0";
		argv[2] = trainFile;
		argv[3] = "..\\svm.model";

		argvP[0] = testFile;
		argvP[1] = "..\\svm.model";
		argvP[2] = "..\\svm.output";

		try {
			SVMTrain.main(argv);
			pAccuracy = SVMPredict.main(argvP);
			System.out.println("Prediction Accuracy is: " + pAccuracy);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (pAccuracy);
	}
}
