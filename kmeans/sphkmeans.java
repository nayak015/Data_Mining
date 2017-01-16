


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.Collections;
import java.util.*;
import java.io.*;

public class sphkmeans {
	
	static int k; // number of clusters
	static int trials; // number of trials. default is 20
	static int dimen_no;
	static Map<Integer, Map <Integer, Integer>> documents = new HashMap<>(); // newid : freq_vec
	static List<Integer> document_id_list;
	static Map<Integer, String> class_labels = new HashMap<>(); // newid : class_label	
	static Map<String, Vector<Integer>> labels_to_points = new HashMap<>(); // class_label : newid	
	static List<String> labels_id_list = new ArrayList<String>();
	
	static Map<Integer, Integer> article; // = new HashMap<>(); // dimen# : freq#
	static Map<Integer, Double> magnitude = new HashMap<>();									
	static Map<Integer, Double> cluster_mag;// = new HashMap<>();										//need to renew
	static Map<Integer, Double> point_cluster_sim;// = new HashMap<>();								//need to renew
	static Map<Integer, Vector <Integer>> clusters;// = new HashMap<>(); // cluster_no : newid   	 	//need to renew
	static Map<Integer, Double> clusters_objective_func;// = new HashMap<>(); // cluster_no : obj_func	//need to renew
	static Map<Integer, Integer> point_to_cluster;// = new HashMap<>(); //datapoint : cluster_no		//need to renew
	static double best_objective_func = 0.0;
	
	static Map<Integer, Integer> best_point_to_cluster; //datapoint : cluster_no
	static Map<Integer, Vector <Integer>> best_clusters; // cluster_no : newid
	
	
	
	public static void main(String[] args){
		//System.out.println("ANKYMONKEY at work");
		
		//start time
	    Date d=new Date();
	    long s1=d.getTime();
		
		
		// fill in the parameters
		//input-file	classfile	k	trials	outputfile
		
		String input_file = new String(args[0]);
		String classfile = new String(args[1]);
		k = Integer.parseInt(args[2]);
		trials = Integer.parseInt(args[3]);
		String output_file = new String(args[4]);
		
	    
		int [] seed_list = {1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39};
		
		
		//populate the dataset
		BufferedReader s = null;
		//String input_file = "char3.csv";
		//String classfile = "reuters21578.class";
		//String output_file = "output_clustering.txt";
		
		// assuming total #dimen is 6870 - lines in bag.clabel  
		
		try {
			s = new BufferedReader(new FileReader(input_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		article = null;
		//Vector<Double> dataset_itemlist_norm = null;
		//boolean first_newid = false;
		int highest_dimen = 0;
		String line = null;
		try {
			while ((line = s.readLine())!= null){
				Scanner l = new Scanner(line);
				l.useDelimiter(",");
				int newid = l.nextInt();
				int dimen = l.nextInt();
				int freq = l.nextInt();
				
				if(highest_dimen<dimen)	highest_dimen = dimen;
				
				article = documents.get(newid);
				if(article == null) {
					//first_newid = true;
					article = new HashMap<>();
					magnitude.put(newid, 0.0);
				}
				article.put(dimen, freq);
				Double sum1 = magnitude.get(newid);
				Double freq2 = Math.pow(freq, 2);
				magnitude.put(newid, freq2+sum1);
				documents.put(newid, article);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dimen_no = highest_dimen +1;
		System.out.println("dimen: " + dimen_no );
		document_id_list = new ArrayList<Integer>(documents.keySet());
		
		
		//populate clabel
		s = null;
		Vector<Integer> dataset_itemlist = null;
		try {
                        s = new BufferedReader(new FileReader(classfile));
                } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                line = null;
                try {
                        while ((line = s.readLine())!= null){
                                Scanner l = new Scanner(line);
                                l.useDelimiter(",");
                                int newid = l.nextInt();
                                String label = l.next();
								class_labels.put(newid, label);
								dataset_itemlist = labels_to_points.get(label);
								if(dataset_itemlist == null) {
									dataset_itemlist = new Vector<>();
									dataset_itemlist.add(newid);
									labels_to_points.put(label, dataset_itemlist);
									labels_id_list.add(label);
								}
								else{
									labels_to_points.get(label).add(newid);
								}
                        }
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
		// end of populating clabel
			
		for (int i = 0; i<labels_to_points.size();i++){
				System.out.print("	" + labels_id_list.get(i));
			}
		
		for (int tr = 0; tr<trials; tr++){
			
			//resetting
			//magnitude = new HashMap<>();										//need to renew
			cluster_mag = new HashMap<>();										//need to renew
			point_cluster_sim = new HashMap<>();								//need to renew
			clusters = new HashMap<>(); // cluster_no : newid   	 	//need to renew
			clusters_objective_func = new HashMap<>(); // cluster_no : obj_func	//need to renew
			point_to_cluster = new HashMap<>(); //datapoint :
			
			
			int seed = seed_list[tr];
			Random rand = new Random(seed); // setting seed
			int upperbound = document_id_list.size()-1;
			int lowerbound = 0;
			int random_integer;
			List<Integer> initial_centroids = new ArrayList<Integer>();// contains the newid of the intial centroids
			for (int i = k; i>0; i--){
				random_integer = rand.nextInt(upperbound-lowerbound) + lowerbound;
				initial_centroids.add(document_id_list.get(random_integer));
			}
			
			System.out.println(initial_centroids);
			Map<Integer, Vector<Double>> cluster_centroids = new HashMap<>();// cluster: centroid
			Vector<Integer> newl = null;
			for (int i = 0; i<k; i++){
				newl = new Vector<Integer>();
				newl.addElement(initial_centroids.get(i));
				clusters.put(i, newl);
				point_to_cluster.put(initial_centroids.get(i), i);
				point_cluster_sim.put(initial_centroids.get(i), 1.0);
				
			}// initialize the cluster MAP with intial clusters.
			
			int count = 0;
			boolean centroids_didnt_change = false;
			// kmeans
			while(!centroids_didnt_change){
				System.out.println("Starting K-means iteration");
				
				// calculate the new centroids
				System.out.println("Calculating new centroids");
				for(int j=0; j<k; j++){ //can restructure this to improve efficiency
					int totalInCluster = clusters.get(j).size();
					//System.out.println("cluster size: " + totalInCluster);
					Vector<Double> centroid = new Vector<Double>(); //make this a map for efficiency
					Vector<Integer> itemsInCluster = clusters.get(j);
					for (int i =0; i<dimen_no;i++){	
						double sum = 0.0;
						Iterator<Integer> it = itemsInCluster.iterator();
						while(it.hasNext()){
							Integer datapoint = it.next(); // this is a newid
							Map<Integer, Integer> freq = null;
							freq = documents.get(datapoint);
							Integer dime = freq.get(i);
							if(dime == null){
								sum += 0.0;
							}
							else{
								sum += freq.get(i);
							}
						}
						if (totalInCluster>0){
							double average = sum/totalInCluster;
							centroid.addElement(average);
						}
						else {
							//System.out.println("Empty Cluster!!");
							centroid.addElement(0.0);
						}
					}
					
					cluster_centroids.put(j,centroid); // update the cluster's centroid
					
					update_cluster_mag(j, centroid);
				}
				
				
				//assign all points to centroids
				System.out.println("assign all points to centroids");
				centroids_didnt_change = true;
				int points = 0;
				
				for (Integer dataset_point : documents.keySet()){
					double maxSim = 0.0;
					Integer best_centroid = null;
					for (Integer each_centroid : cluster_centroids.keySet()){
						double new_sim = cosineSim(documents.get(dataset_point), cluster_centroids.get(each_centroid), magnitude.get(dataset_point), each_centroid);
						if (new_sim > maxSim){
							maxSim = new_sim;
							best_centroid = each_centroid;
						}
					}
					Integer check = point_to_cluster.get(dataset_point);
					if (check == null){
						point_to_cluster.put(dataset_point, best_centroid);
						clusters.get(best_centroid).add(dataset_point);
						point_cluster_sim.put(dataset_point, maxSim);
						centroids_didnt_change = false;
						points++;
					}
					else{
						if(!(point_to_cluster.get(dataset_point).equals(best_centroid))){
							// points changed
							points++;
							centroids_didnt_change = false;
							Integer prev_cluster = point_to_cluster.get(dataset_point);
							clusters.get(prev_cluster).remove(dataset_point);
							
							point_to_cluster.put(dataset_point, best_centroid);
							clusters.get(best_centroid).add(dataset_point);
							point_cluster_sim.put(dataset_point, maxSim);
						}
					}
					
				}
				//System.out.println(clusters.get(0));
				count++;		
				System.out.println("K means iteration: " + count + " points changed: " + points);
			}// k-means
			
			
			
			System.out.println("DONEEEEEEE with kmeans");	
			
			
			//calculate total objective function
			double objective_func = 0.0;
			for (Integer clusterid : clusters.keySet()){
				for (Integer point : clusters.get(clusterid)){
					if(point_cluster_sim.get(point)!= null)
						objective_func += point_cluster_sim.get(point);
				}
				
			}
			
			System.out.println("objective function: " + objective_func);
			if(best_objective_func<objective_func){
				best_objective_func = objective_func;
				// add logic to save the state
				best_point_to_cluster=point_to_cluster;
				best_clusters=clusters;
			}
			
		}// trials
		
		
		//calculate entropy and purity here
		
		// print the matrix
		for (int i = 0; i<labels_to_points.size();i++){
			System.out.print("	" + labels_id_list.get(i));
		}
		System.out.println();
		
		double purity_maxi = 0.0;
		double purity_total = 0.0;
		
		Vector<Vector<Integer>> entropy_matrix = new Vector<>();
		for (int j = 0; j<k; j++){ 
			System.out.print("C"+j + " :	");
			Vector<Integer> row = new Vector<Integer>();
			
			for (int i = 0; i<labels_to_points.size(); i++){
				int counter = 0;
				for (int m = 0; m<best_clusters.get(j).size(); m++){
					if (labels_to_points.get(labels_id_list.get(i)).contains(best_clusters.get(j).get(m)))	counter++;
				}
				System.out.print(counter + "	");
				row.addElement(new Integer(counter));
				if(counter>=purity_maxi)	purity_maxi = counter;
			}
			entropy_matrix.addElement(row);
			purity_total += purity_maxi;
			purity_maxi = 0.0; 
			System.out.println();
		}
		
		System.out.println("Best Purity: " + purity_total/(double)documents.size());
		
		//calculating entropy
		//Vector<Double> cluster_entropy = new Vector<Double>();
		double total_entropy = 0.0;
		for (int i=0;i<entropy_matrix.size();i++){
			Vector<Integer> row = entropy_matrix.elementAt(i);
			double e_j = 0.0;
			for(int j =0; j<row.size();j++){
				if((int)row.elementAt(j) == 0)	e_j += 0.0;
				else{
					double p_ij = (double)row.elementAt(j)/(double)best_clusters.get(i).size();
					e_j += p_ij * (Math.log(p_ij)/Math.log(2.0));
				}
			}
			//cluster_entropy.addElement(new Double(e_j));
			double nume = best_clusters.get(i).size() * e_j;
			total_entropy += nume/documents.size();
		}
		total_entropy = -1.0*total_entropy;
		System.out.println("Best Entropy: " + total_entropy);
		
		//add stuff about writing to outputfile
		
		
		
		System.out.println("writing to the file now");
		
		PrintWriter writer;
		try{
			writer = new PrintWriter(output_file, "UTF-8");
			System.out.println("writing to the file now");
			for (Map.Entry<Integer,Integer> entry : best_point_to_cluster.entrySet()) {
				  Integer key = entry.getKey();
				  Integer value = entry.getValue();
				  writer.println(key + "," + value);
			}
			writer.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//end time
	    d=new Date();
	    long s2=d.getTime();
	    System.out.println();
	    System.out.println("Execution time is: "+((s2-s1)/(double)1000) + " seconds.");
		
	}// main
	
	
	static void update_cluster_mag(Integer cluster_no, Vector<Double> vec2){
		if (vec2.size()==0){//empty cluster
			cluster_mag.put(cluster_no,0.0);
		}
		else{
			double mag2 = 0.0;
			for (int i = 0; i < dimen_no; i++) //docVector1 and docVector2 must be of same length
			{
				mag2 += Math.pow(vec2.get(i), 2); //(b^2)
			}
			cluster_mag.put(cluster_no,mag2);
		}
	}
	
	static double cosineSim(Map<Integer, Integer> vec1, Vector<Double> vec2, Double vec1mag, int cluster_no){
		double dotProduct = 0.0;
        double magnitude1 = Math.pow(vec1mag, 0.5);
        double magnitude2 = cluster_mag.get(cluster_no);
        double cosineSimilarity = 0.0;
        
        for(Integer dimension : vec1.keySet()){
        	dotProduct += vec1.get(dimension) * vec2.elementAt(dimension); 
        }
        
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
		
	}
	
}//class kmeans
