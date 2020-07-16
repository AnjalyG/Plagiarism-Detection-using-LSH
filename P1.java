import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class P1 {

	public static void main(String[] args) { 
		P1 obj = new P1();
		String path = "/home/mwang2/test/coen281/";
		System.out.println("Enter File Name");
		Scanner scanner = new Scanner(System.in);
		String fName = scanner.nextLine();
		if(fName != null) {
			// List<String> fileNames = obj.getTestFileNames(fName);
            List<String> fileNames = obj.formatFileName(fName);
			List<String>  mainDataList = obj.getTextFromDocs(path,fileNames);
			int[][] shinglesMatrix = obj.createShingle(mainDataList, 9);
			int noOfShingles = shinglesMatrix.length;
			if(noOfShingles > 0) {
				ArrayList<ArrayList<Integer>> randPerms = obj.createRandomPermutations(noOfShingles);
				int[][] signatureMatrix = obj.minHash(randPerms,shinglesMatrix,15);
				int bandSize = obj.findingSCurve(signatureMatrix);
                HashMap<String,String> similarPairs = obj.localitySensitiveHashing(signatureMatrix, bandSize);
                obj.printOutput(randPerms,shinglesMatrix,signatureMatrix,similarPairs);
			}else {
				System.out.println("Invalid File, please use another!");
			}
		}else {
			System.out.println("Invalid File, please use another!");
		}
	 }


    /*Formating the input to extract the file names*/
    private List<String> formatFileName(String filename){
        List<String> data = new ArrayList<String>();
        String[] fNames = filename.split(",|\\s+");
		for(String f: fNames) {
		    data.add(f);
		}
        return data;
    }

    //Function to read and return data from all the files
	private List<String> getTextFromDocs(String path, List<String> fileNames){
		List<String>  mainDataList = new ArrayList<String>();
		if(fileNames !=null && fileNames.size()>=1) {
			for(String fileName:fileNames) {
				String data = getText(path+fileName);
				if(data!=null) {
					mainDataList.add(data.toLowerCase());
				}
			}
		}
		return mainDataList;
	}

    //Helper function to read data from a file
	private String getText(String filename) {
		if(!new File(filename).exists()) return null;
		StringBuilder  strBuilder = new StringBuilder(); 
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	strBuilder.append(line);
		    }
		    br.close();
		    return strBuilder.toString();
		} catch (FileNotFoundException e) {
            System.out.println("File not found exception");
		} catch (IOException e) {
            System.out.println("IO exception");
		}
		return null;
	}

    //Junk Function--Initially used to retrive file names
	/*private List<String> getTestFileNames(String filename) {
		
		if(!new File(filename).exists()) return null;
		List<String> data = new ArrayList<String>(); 
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] fNames = line.split(",|\\s+");
		    	for(String f: fNames) {
		    		data.add(f);

		    	}
		    	 
		    }
		   
		    br.close();
		    return data;
		} catch (FileNotFoundException e) {
			
			//e.printStackTrace();
		} catch (IOException e) {
			
			//e.printStackTrace();
		}
		
		return null;
	}*/

    //Function to create shingles
	private int[][] createShingle(List<String> text, int k) {
		Set<String> shingles = new HashSet<String>();
		for(int i = 0; i<text.size();i++) {
			for(int j = 0; j<text.get(i).length()-k ;j++) {
				shingles.add(text.get(i).toString().substring(j, j+k));
			}
		}
		int[][] shinglesMatrix  = new int[shingles.size()][text.size()];
        //initializing shingles matrix
		for (int i = 0;i<shingles.size();i++) {
			for(int j =0;j<text.size();j++) {
				shinglesMatrix[i][j] = 0;
			}
		}
        //marking if the value is present or not
		int m = 0;
		for (String s : shingles)  {
			for(int n =0;n<text.size();n++) {
				if(text.get(n).contains(s)) {
					shinglesMatrix[m][n] = 1;
				}
			}
			m++;
		}
		return shinglesMatrix;
	}

    //Helper function to createRandomPermutations
	private ArrayList<ArrayList<Integer>> createRandomPermutations(int shingleSize) {
		ArrayList<ArrayList<Integer>> randPerms = new ArrayList<ArrayList<Integer>>();
		for(int r = 0;r<15;r++) {;
			randPerms.add(generateRandomPermutation(shingleSize));
		}
		return randPerms;
	}

    //Helper function to generate random permutation
	private ArrayList<Integer> generateRandomPermutation(int len) {
		ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=1; i<=len; i++) {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);
		return list;
	}

    //junk function to check jaccard similarity from shingles matrix
	/*private void checkSimilarity(int[][] shinglesMatrix){
		for(int i = 0;i<shinglesMatrix[0].length;i++) {
			for(int j = i+1;j<shinglesMatrix[0].length;j++) {
				int union=0,intersection=0;
				for(int k =0;k<shinglesMatrix.length;k++) {
					if(shinglesMatrix[k][i] == shinglesMatrix[k][j] && shinglesMatrix[k][i] != 0) {
						intersection++;
					}
					if(shinglesMatrix[k][i] != 0 || shinglesMatrix[k][j] != 0 ) {
						union++;
					}
				}
				float sim = 0;
				if(intersection!=0) {
					sim = ((float)intersection/union)*100;
				}else {
					sim = 0;
				}
				if(sim > 0) {
					System.out.println("Similarity between document "+i+" and document "+j+" = "+sim+"%");
				}
			}
		}
	}*/
	
    //Function to generate minHash from random permutation and shingles matrix
	private int[][] minHash(ArrayList<ArrayList<Integer>> randPerms, int[][] shinglesMatrix,int permNo) {
		int[][] minHashMat = new int[permNo][shinglesMatrix[0].length];
		for(int i=0; i<permNo;i++) {
			for(int j =0;j<shinglesMatrix[0].length;j++) {
				for(int k=0;k<shinglesMatrix.length;k++) {
					if(shinglesMatrix[k][j]==1) {
						minHashMat[i][j] = randPerms.get(i).get(k);
						break;
					}
				}
			}
		}
		return minHashMat;
	}
	
    //Function to tune b and r values
	private int findingSCurve(int[][] signatureMatrix) {
		
		int perfectb = 0,backUpb = 0;
		double[] previous = {1,-1};
		double[] simThershold = {0.2,0.8};
		for(int j=1;j<signatureMatrix.length;j++) {
			if(signatureMatrix.length%j==0 ) {
				int b = j;
				int r = signatureMatrix.length/b;
				double[] ans = new double[simThershold.length];
				System.out.println("b = "+b+" r = "+r);
				for(int i =0; i<simThershold.length;i++) {
					ans[i] = 1 - Math.pow((1-Math.pow(simThershold[i],r)),b);
					System.out.print(ans[i]+" ");
				}
				if(ans[0] <= previous[0] && ans[1] >= previous[1]) {
					backUpb = b;
					if(ans[0] <= 0.003 && ans[1] >= 0.997) {
						System.out.println("*I am here*");
						perfectb = b;
						previous[0] = ans[0];
						previous[1] = ans[0];
						backUpb = b;
						
					}
				}
			}
		}
		if(perfectb >0) {
			System.out.println("returning original");
			return perfectb;
		}
		else
			return backUpb;
				
			
	}
	
    //Function to generate LSH
	private HashMap<String,String> localitySensitiveHashing(int[][] signatureMatrix,int b) {
		HashMap<String,String> similarPairs= new HashMap<String,String>();
		int r = signatureMatrix.length/b;
		for(int k = 0;k<b;k++) {
			for(int j=0;j<signatureMatrix[0].length;j++) {
				String colJ = "";
				for(int i =k*r;i<(k*r)+r;i++)
					colJ += signatureMatrix[i][j];
				for(int l = j+1;l<signatureMatrix[0].length;l++) {
					String colL = "";
					for(int ii =k*r;ii<(k*r)+r;ii++)
						colL += signatureMatrix[ii][l];
					if (colJ.equals(colL)) {
						int[] pair1 =new int[] {j,l};
                        Arrays.sort(pair1);
                        String key = pair1[0]+" "+pair1[1];
                        if(similarPairs.containsKey(key) )continue;
                        similarPairs.put(key,key);
					}
				}
			}
		}
		return similarPairs;
	}
	
    //Helper function to print all the required outputs
	private void printOutput(ArrayList<ArrayList<Integer>> permutations, int[][] shingleMatrix,int[][] signatureMatrix,HashMap<String,String> similarPairs) {
		System.out.println("permutations -> permutations.size() "+permutations.size()+" permutations.get(0).size() "+permutations.get(0).size());
		System.out.println("shingleMatrix -> shingleMatrix.length "+shingleMatrix.length+" shingleMatrix[0].length "+shingleMatrix[0].length);
		System.out.println("RANDOM PERMUTATIONS \t\t\t\t\t\t\t\t\t SHINGLES MATRIX");
		int rowController = 0, permColController = 0, shingleColController =0;
		while(rowController < shingleMatrix.length ) {
			permColController = 0; shingleColController =0;
			while(permColController < permutations.size()) {
				int length = (int)(Math.log10(permutations.get(permColController).get(rowController))+1);
				if(length==4)
					System.out.print(permutations.get(permColController).get(rowController)+" ");
				else if(length==3)
					System.out.print(permutations.get(permColController).get(rowController)+"  ");
				else if(length==2)
					System.out.print(permutations.get(permColController).get(rowController)+"   ");
				else
					System.out.print(permutations.get(permColController).get(rowController)+"    ");
				permColController++;
			}
			System.out.print("\t");
			while(shingleColController < shingleMatrix[0].length) {
				System.out.print(shingleMatrix[rowController][shingleColController]+" ");
				shingleColController++;
			}
			System.out.println();
			rowController++;
		}
		System.out.println("\nTHE SIGNATURE MATRIX\n");
		for(int k =0;k<signatureMatrix.length;k++) {
			for(int l = 0;l<signatureMatrix[0].length;l++) {
				int length = (int)(Math.log10(signatureMatrix[k][l])+1);
				if(length==4)
					System.out.print(signatureMatrix[k][l]+" ");
				else if(length==3)
					System.out.print(signatureMatrix[k][l]+"  ");
				else if(length==2)
					System.out.print(signatureMatrix[k][l]+"   ");
				else
					System.out.print(signatureMatrix[k][l]+"    ");
			}
			System.out.println();
		}

        // Printing out the similar pair
        if(!similarPairs.isEmpty()) {
		    Iterator iter = similarPairs.values().iterator();
			while (iter.hasNext()) {
				String temp = (String) iter.next();
			    String[] t = temp.split(" ");
				System.out.println("The file "+Integer.parseInt(t[0])+" and "+Integer.parseInt(t[1])+" are similar");
			}
		}else
		    System.out.println("There are no similar files");

		
	}
}
