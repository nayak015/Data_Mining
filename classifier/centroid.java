//package classification;


import java.util.*;
import java.io.*;


public class centroid {

	static Map<String, Integer> classname_to_number = new HashMap<String,Integer>();
	static Map<Integer, String> class_map_string = new HashMap<Integer, String>();
	static Map<Integer, Point> docID_to_point = new HashMap<Integer, Point>();
	static Map<Point, Integer> point_to_classno = new HashMap<Point, Integer>();
	static Map<Integer, Integer> ID_to_classno = new HashMap<Integer, Integer>();
	static Map<Integer, String> feature_to_label = new HashMap<Integer, String>();
	static Map<Integer, Integer> test_allocation = new HashMap<Integer, Integer>();
	static Map<Integer, Binary_classifier> classno_to_binaryclass = new HashMap<Integer, Binary_classifier>();
	static Map<Integer, Integer> feature_to_dococcurance = new HashMap<Integer, Integer>();
	static Map<Integer, Double> idf_feature_to_dococcurance = new HashMap<Integer, Double>();
	
	static Map<Integer, Point> trainlistIDs = new HashMap<Integer, Point>();
	static Map<Integer, Point> testlistIDs = new HashMap<Integer, Point>();
	
	static int[] testpts_class ={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // contains TP+FN value for each class_no(given by index_no)
	static int total_testpts = 0;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("last project, lets go!");
		
		//start time
	    Date d=new Date();
	    long s1=d.getTime();
	    
		
	    // fill in the parameters
	    /*
	    	input-file 
	    	input-rlabel-file 
	    	train-file 
	    	test-file 
	    	class-file 
	    	features-label-file 
	    	feature-representation-option 
	    	output-file 
	    	[options]
	 	// just uncomment this for final
    	*/
	String input_file = new String(args[0]);
    	String input_rlabel_file = new String(args[1]);
    	String train_file = new String(args[2]);
    	String test_file = new String(args[3]);
    	String class_file = new String(args[4]);
    	String features_label_file = new String(args[5]);
    	String feature_representation_option = new String(args[6]);
    	String output_file = new String(args[7]);
    	
	/*
	    String path = "/home/nayak015/Desktop/project3/project3-files/";
	    String input_file = "20newsgroups_char.ijv";
    	String input_rlabel_file = "20newsgroups.rlabel";
    	String train_file = "20newsgroups.train";
    	String test_file = "20newsgroups.test";
    	String class_file = "20newsgroups.class";
    	String features_label_file = "20newsgroups_char.clabel";
    	String feature_representation_option = "idtf"; // binary, tf , idtf
    	String output_file = "output_file";
	*/	
		
    	// reading from the rlabel file
    	System.out.println("Reading the rlabel...");
    	BufferedReader s = null;
    	try {
			s = new BufferedReader(new FileReader(input_rlabel_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String line = null;
	    Point my_point;
	    Binary_classifier my_binaryclass;
	    int class_index = 0;
	    int number_of_points = 0;
    	try {
			while ((line = s.readLine())!= null){
				Scanner l = new Scanner(line);
				l.useDelimiter(" ");
				int id = l.nextInt();
				String classname = l.next();
				int rlabelid = l.nextInt();
				
				number_of_points++;
				my_point = new Point();
				my_point.ID = id;
				my_point.class_name = classname;
				my_point.rlabel_ID = rlabelid;
				
				Integer value = classname_to_number.get(classname);
				if (value == null){// new classname
					class_map_string.put(new Integer(class_index), classname);
					classname_to_number.put(classname, new Integer(class_index));
					my_binaryclass = new Binary_classifier();
					my_binaryclass.class_name = classname;
					my_binaryclass.class_number = class_index;
					classno_to_binaryclass.put(new Integer(class_index), my_binaryclass);
					class_index++;
				}
				my_point.class_number = classname_to_number.get(classname).intValue();
				docID_to_point.put(new Integer(id), my_point);
				point_to_classno.put(my_point, classname_to_number.get(classname));
				l.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Finished reading the rlabel...");
    	//done reading from rlabel file
    	
    	System.out.println("Total number of points: " + number_of_points);
    	
    	
    	
    	//read trainlist and testlist
    	System.out.println("Reading the trainlist file...");
    	my_point = null; // resetting
    	my_binaryclass = null; //resetting
    	s = null;
    	try {
			s = new BufferedReader(new FileReader(train_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	line = null;
	    
    	try {
			while ((line = s.readLine())!= null){
				Scanner l = new Scanner(line);
				l.useDelimiter(" ");
				int i = l.nextInt();
				my_point = docID_to_point.get(i);
				trainlistIDs.put(new Integer(i), my_point);
				l.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Reading the testlist file...");
    	my_point = null; // resetting
    	my_binaryclass = null; //resetting
    	s = null;
    	try {
			s = new BufferedReader(new FileReader(test_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	line = null;
	    
    	try {
			while ((line = s.readLine())!= null){
				Scanner l = new Scanner(line);
				l.useDelimiter(" ");
				int i = l.nextInt();
				my_point = docID_to_point.get(i);
				int classindex = my_point.class_number;
				testlistIDs.put(new Integer(i), my_point);
				testpts_class[classindex]++;
				total_testpts++;
				l.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Finished reading train, test files...");
    	
    	//end of reading trainlist, testlist
    	
    	
    	
    	// reading .ijv file and creating points
    	System.out.println("Reading the .ijv file...");
    	my_point = null; // resetting
    	my_binaryclass = null; //resetting
    	Point check_point = null;
    	s = null;
    	try {
			s = new BufferedReader(new FileReader(input_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	line = null;
	    
    	try {
			while ((line = s.readLine())!= null){
				Scanner l = new Scanner(line);
				l.useDelimiter(" ");
				int i = l.nextInt();
				int j = l.nextInt();
				int v = l.nextInt();
				
				my_point = docID_to_point.get(i);
				if(feature_representation_option.equals("tf") || feature_representation_option.equals("idtf")){
					my_point.Term_freq.put(new Integer(j), new Double(v));
					double mag = my_point.magnitude_sq;
					my_point.magnitude_sq = mag + Math.pow(v, 2);
				}
				else if(feature_representation_option.equals("binary")){
					my_point.Term_freq.put(new Integer(j), 1.0);
					double mag = my_point.magnitude_sq;
					my_point.magnitude_sq = mag + 1.0;
				}
				
				check_point = trainlistIDs.get(i);
				if(check_point != null){
					Integer feature_value = feature_to_dococcurance.get(j);
					if(feature_value == null){
						feature_to_dococcurance.put(new Integer(j), 1);
					}
					else{
						int value_update = feature_value.intValue();
						value_update += 1;
						feature_to_dococcurance.put(j, value_update);
					}
					check_point = null;
				}
				
				
				l.close();		
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	System.out.println("Finished reading the .ijv file...");
    	//end of reading .ijv file
	    my_point = docID_to_point.get(5);
    	System.out.println("? id 1, classnumber: "+ my_point.class_number);
    	System.out.println("? id 1, feature: "+ feature_to_dococcurance.get(7));
    	
    	//initialize IDF
    	if(feature_representation_option.equals("idtf")){
    		System.out.println("Updating idf values for trainset features...");
			populate_idtf(number_of_points);
		}
    	
    	//normalizing each point
    	System.out.println("Normalizing points...");
    	normalize(feature_representation_option);
    	
    	//check if normalisation worked  ---- worked
    	/*double sum = 0.0;
    	for (Double value: my_point.normalised.values()){
    		sum += Math.pow(value, 2);
    	}
    	System.out.println("sum: " + sum);
    	System.out.println("mag: " + Math.sqrt(sum));*/
    	
    	System.out.println("Training Binary classifiers now...");
    	
    	train_classifiers();
    	
    	System.out.println("Finished Training Binary classifiers...");
    	
    	//check if normalisation worked for centroids ---- worked
    	/*my_binaryclass = classno_to_binaryclass.get(1);
    	double sum = 0.0;
    	for (Double value: my_binaryclass.neg_cent.values()){
    		sum += Math.pow(value, 2);
    	}
    	System.out.println("sum: " + sum);
    	System.out.println("mag: " + Math.sqrt(sum));*/
    	
    	System.out.println("Starting testing Binary classifiers...");
  
    	test_classifiers();
    	
    	System.out.println("Finished testing...");
    	
    	
    	System.out.println("Performance analysis starting...");
    	//loop over each classifiers
    	
    	performance_analysis();
    	
    	
    	
    	
    	
    	System.out.println("Performance analysis finished...");
    	
    	
    	System.out.println("Writing to output file...");
    	print_to_output(output_file);
    	
    	
    	
		//end time
	    d=new Date();
	    long s2=d.getTime();
	    System.out.println();
	    System.out.println("Execution time is: "+((s2-s1)/(double)1000) + " seconds.");
	}

	
	public static void train_classifiers(){
		Iterator it = trainlistIDs.entrySet().iterator();
		Integer docid = null;
		Point point_ref = null;
		while(it.hasNext()){
			Map.Entry<Integer, Point> pair = (Map.Entry)it.next();
			docid = pair.getKey();
			point_ref = pair.getValue();
			
			int correct_classno = point_ref.class_number;
			Binary_classifier class_alloc = null;
			
			for(Integer key : classno_to_binaryclass.keySet()){
				if(key.intValue() == correct_classno){
					class_alloc = classno_to_binaryclass.get(correct_classno);
					class_alloc.pos_train_IDs.put(docid, point_ref);
					class_alloc.total_pos_trains_IDs++;
				}
				else{
					class_alloc = classno_to_binaryclass.get(key.intValue());
					class_alloc.neg_train_IDs.put(docid, point_ref);
					class_alloc.total_neg_trains_IDs++;
				}
			}
		}
		// updated all the pos and neg train_ids of all classes
		
		System.out.println("Calculating +ive and -ive centroids for all classifiers...");
		
		Binary_classifier my_class = null;
		for(Integer key : classno_to_binaryclass.keySet()){
			my_class = classno_to_binaryclass.get(key);
			System.out.println("Computing +ive centroids for classifier : " + my_class.class_name);
			my_class.pos_compute_centroids();
			System.out.println("Normalizing +ive centroids for classifier : " + my_class.class_name);
			my_class.normalize_pos_cent();
			
			System.out.println("Computing -ive centroids for classifier : " + my_class.class_name);
			my_class.neg_compute_centroids();
			System.out.println("Normalizing -ive centroids for classifier : " + my_class.class_name);
			my_class.normalize_neg_cent();
		}
		
		
	}
	
	
	public static void test_classifiers(){
		Iterator it = testlistIDs.entrySet().iterator();
		Integer docid = null;
		Point point_ref = null;
		while(it.hasNext()){//every test point
			Map.Entry<Integer, Point> pair = (Map.Entry)it.next();
			docid = pair.getKey();
			point_ref = pair.getValue();
			
			
			double max_diff = 0.0;
			Iterator iter = classno_to_binaryclass.entrySet().iterator();
			Integer classid = null;
			Binary_classifier classifier_ref = null;
			while(iter.hasNext()){// every binary class
				Map.Entry<Integer, Binary_classifier> classpair = (Map.Entry)iter.next();
				classid = classpair.getKey();
				classifier_ref = classpair.getValue();
				
				double similarity_difference = cosine_similarity_difference(point_ref,classifier_ref);
				classifier_ref.testpt_to_ps.put(new Integer(point_ref.ID) , new Double(similarity_difference));
				point_ref.class_to_score.put(new Integer(classid.intValue()), new Double(similarity_difference));
				
				if(similarity_difference > max_diff){
					max_diff = similarity_difference;
					point_ref.prediction_score = max_diff;
					point_ref.class_prediction = classid.intValue();
				}
			}
			test_allocation.put(new Integer(docid.intValue()), point_ref.class_prediction);
			
		}
		
		
	}
	
	public static double cosine_similarity_difference(Point my_point, Binary_classifier my_classifier){
		double difference = 0.0;
		double sum_pos = 0.0;
		double sum_neg = 0.0;
		double product = 0.0;
		//calculate +ive and point cosine similarity
		Iterator it = my_point.normalised.entrySet().iterator();
		Integer feature = null;
		Double freq = null;
		while(it.hasNext()){
			Map.Entry<Integer, Double> featurepair = (Map.Entry)it.next();
			feature = featurepair.getKey();
			freq = featurepair.getValue();
			
			if(my_classifier.pos_cent.containsKey(feature.intValue())){ // check by switching to .intValue
				product = ((double)freq.doubleValue()) * ((double)my_classifier.pos_cent.get(feature.intValue()).doubleValue());
				sum_pos = sum_pos + product;
			}
		}
		
		
		//calculate -ive and point cosine similarity
		Iterator iter = my_point.normalised.entrySet().iterator();
		feature = null;
		freq = null;
		while(iter.hasNext()){
			Map.Entry<Integer, Double> featurepair = (Map.Entry)iter.next();
			feature = featurepair.getKey();
			freq = featurepair.getValue();
			
			if(my_classifier.neg_cent.containsKey(feature.intValue())){ // check by switching to .intValue
				product = ((double)freq.doubleValue()) * ((double)my_classifier.neg_cent.get(feature.intValue()).doubleValue());
				sum_neg = sum_neg + product;
			}
		}
		
		//compute difference
		difference = sum_pos - sum_neg;
		
		return difference;
	}
	
	
	public static void performance_analysis(){
		double MAXF1_AVG = 0.0;
		Iterator iter = classno_to_binaryclass.entrySet().iterator();
		Integer classid = null;
		Binary_classifier classifier_ref = null;
		while(iter.hasNext()){// every binary class
			double MAXF1 = 0.0;
			Map.Entry<Integer, Binary_classifier> classpair = (Map.Entry)iter.next();
			classid = classpair.getKey();
			classifier_ref = classpair.getValue();
			
			classifier_ref.SORTED_testpt_to_ps = classifier_ref.sortByValues();
			
			Set set2 = classifier_ref.SORTED_testpt_to_ps.entrySet();
			
			Iterator iterator2 = set2.iterator();
			
			int index = total_testpts-1;
			
		    while(iterator2.hasNext()) {
			   Map.Entry me2 = (Map.Entry)iterator2.next();
			   //System.out.print(me2.getKey() + ": ");
			   //System.out.println(me2.getValue());
			   classifier_ref.sortedpointMap.put(new Integer(index), new Integer((int)me2.getKey()));
			   //System.out.println("Check!");
			   //System.out.print((int)me2.getKey() + ": ");
			   //System.out.println(classifier_ref.sortedpointMap.get(index));
			   index--;
		    }
		    
		    for(int L=0; L<total_testpts; L++){
		    	
		    	double TP_FP_sum = (double)L;
		    	
		    	double TP_FN_sum = (double)testpts_class[classifier_ref.class_number];
		    	
		    	int TP = 0;
		    	
		    	for (int count = 0; count <L; count++){
		    		Integer docid = classifier_ref.sortedpointMap.get(count);
		    		Point my_point = docID_to_point.get(docid);
		    		if(my_point.class_number == classifier_ref.class_number)	TP++;
		    	}
		    	
		    	double prec = (double)TP/TP_FP_sum;
		    	
		    	double rec = (double)TP/TP_FN_sum;
		    	
		    	double F1 = (2*prec*rec)/(prec+rec);
		    	
		    	if(F1>MAXF1){
		    		MAXF1 = F1;
		    	}
		    	
		    }
		    
		    MAXF1_AVG += MAXF1;
		    System.out.println("MAXF1: " + classifier_ref.class_name + " " + MAXF1);
		    
		}
		MAXF1_AVG = MAXF1_AVG/20.0;
		System.out.println("MAXF1_AVG: " + MAXF1_AVG);
		
		
	}
	
	
	
	
	
	public static void populate_idtf(int N){
		Iterator it = feature_to_dococcurance.entrySet().iterator();
		Integer feature = null;
		Integer occurance = null;
		while(it.hasNext()){
			Map.Entry<Integer, Integer> pair = (Map.Entry)it.next();
			feature = pair.getKey();
			occurance = pair.getValue();
			double new_value = 0.0;
			new_value = new_value + (double)occurance.intValue();
			double log_value = ((double)N)/new_value; 
			double final_value = Math.log(log_value)/Math.log(2);
			idf_feature_to_dococcurance.put(new Integer(feature.intValue()), new Double(final_value));
		}
	}
	
	
	public static void normalize(String feature_representation_option_copy){
		Iterator it = docID_to_point.entrySet().iterator();
		Integer docid = null;
		Point update_point = null;
		while(it.hasNext()){
			Map.Entry<Integer, Point> pair = (Map.Entry)it.next();
			docid = pair.getKey();
			update_point = pair.getValue();
			
			Iterator iter = update_point.Term_freq.entrySet().iterator();
			Integer feature = null;
			Double tf_val = null;
			Double freq = null;
			
			if(!feature_representation_option_copy.equals("idtf")){
				double deno = Math.sqrt(update_point.magnitude_sq);		
				while(iter.hasNext()){
					Map.Entry<Integer, Double> featurepair = (Map.Entry)iter.next();
					feature = featurepair.getKey();
					freq = featurepair.getValue();
					double new_value = freq/deno;
					update_point.normalised.put(feature, new Double(new_value));
				}
			}
			else{//case of idtf
				double mag_sq = 0.0;
				while(iter.hasNext()){//update new mag
					freq = null;
					Map.Entry<Integer, Double> featurepair = (Map.Entry)iter.next();
					feature = featurepair.getKey();
					tf_val = featurepair.getValue();
					freq = idf_feature_to_dococcurance.get(feature.intValue());
					if(freq ==null){
						freq = new Double(0.0);
					}
					double new_val = tf_val.doubleValue() * freq.doubleValue();
					mag_sq = mag_sq + Math.pow(new_val, 2);
					update_point.Term_freq.put(feature, new_val);
				}
				Iterator iter2 = update_point.Term_freq.entrySet().iterator();
				feature = null;
				double deno = Math.sqrt(mag_sq);		
				while(iter2.hasNext()){
					freq = null;
					Map.Entry<Integer, Double> featurepair = (Map.Entry)iter2.next();
					feature = featurepair.getKey();
					freq = featurepair.getValue();
					double new_value = freq.doubleValue()/deno;
					update_point.normalised.put(feature, new Double(new_value));
				}
			}
			
		}
	}
	
	public static class Point{
		Map<Integer, Double> Term_freq = new HashMap<Integer, Double>();
		
		int class_number;
		double magnitude_sq = 0.0;
		int ID;
		int rlabel_ID;
		String class_name;
		
		Map<Integer, Double> normalised = new HashMap<Integer, Double>();;
		double prediction_score;
		int class_prediction;
		
		Map<Integer, Double> class_to_score = new HashMap<Integer, Double>();
		//constructor
		
		
		
	}
	
	
	public static class Binary_classifier{
		Map<Integer, Double> pos_cent = new HashMap<Integer, Double>();;
		Map<Integer, Double> neg_cent = new HashMap<Integer, Double>();;
		
		Map<Integer, Point> pos_train_IDs = new HashMap<Integer, Point>();;
		Map<Integer, Point> neg_train_IDs = new HashMap<Integer, Point>();;
		
		Map<Integer, Double> testpt_to_ps = new HashMap<Integer, Double>();

		Map<Integer, Integer> sortedpointMap = new HashMap<Integer, Integer>();
		
		Map<Integer, Double> SORTED_testpt_to_ps;
		
		int class_number; //ID for class
		String class_name; 
		int total_pos_trains_IDs = 0;
		int total_neg_trains_IDs = 0;
		int TP, FN, FP, TN;
		
		// CONSTRUCTOR
		
		
		public void pos_compute_centroids(){
			
			for(Point my_point : this.pos_train_IDs.values()){
				
				for(Map.Entry<Integer, Double> entry : my_point.normalised.entrySet() ){
					Double item_in_cent = null;
					Integer feature = entry.getKey();
					Double freq = entry.getValue();
					
					item_in_cent = this.pos_cent.get(feature.intValue());
					if(item_in_cent == null){
						this.pos_cent.put(new Integer(feature.intValue()), new Double(freq.doubleValue()));
					}
					else{
						double val = item_in_cent.doubleValue();
						val = val + freq.doubleValue();
						this.pos_cent.put(feature.intValue(), val);
					}
				}
				
			}
			
			for (Integer feature : this.pos_cent.keySet()){
				Double val = this.pos_cent.get(feature.intValue());
				double new_val = ((double)val.doubleValue())/((double)this.total_pos_trains_IDs);
				this.pos_cent.put(feature, new Double(new_val));
			}
			
			System.out.println("Finished computing +ive centroids for classifier : " + this.class_name);
			
		}
		
		public void neg_compute_centroids(){
			
			for(Point my_point : this.neg_train_IDs.values()){
				
				for(Map.Entry<Integer, Double> entry : my_point.normalised.entrySet() ){
					Double item_in_cent = null;
					Integer feature = entry.getKey();
					Double freq = entry.getValue();
					
					item_in_cent = this.neg_cent.get(feature.intValue());
					if(item_in_cent == null){
						this.neg_cent.put(new Integer(feature.intValue()), new Double(freq.doubleValue()));
					}
					else{
						double val = item_in_cent.doubleValue();
						val = val + freq.doubleValue();
						this.neg_cent.put(feature.intValue(), val);
					}
				}
				
			}
			
			for (Integer feature : this.neg_cent.keySet()){
				Double val = this.neg_cent.get(feature.intValue());
				double new_val = ((double)val.doubleValue())/((double)this.total_neg_trains_IDs);
				this.neg_cent.put(feature, new Double(new_val));
			}
			
			System.out.println("Finished computing -ive centroids for classifier : " + this.class_name);
			
		}
		
		public void normalize_pos_cent(){
			Iterator iter = this.pos_cent.entrySet().iterator();
			Integer feature = null;
			Double freq = null;
			double tf_val = 0.0;
			double mag_sq = 0.0;
			while(iter.hasNext()){//update new mag
				freq = null;
				Map.Entry<Integer, Double> featurepair = (Map.Entry)iter.next();
				feature = featurepair.getKey();
				tf_val = featurepair.getValue().doubleValue();
				mag_sq = mag_sq + Math.pow(tf_val, 2);
			}
			Iterator iter2 = this.pos_cent.entrySet().iterator();
			feature = null;
			double deno = Math.sqrt(mag_sq);		
			while(iter2.hasNext()){
				freq = null;
				Map.Entry<Integer, Double> featurepair = (Map.Entry)iter2.next();
				feature = featurepair.getKey();
				freq = featurepair.getValue();
				double new_value = freq.doubleValue()/deno;
				this.pos_cent.put(feature, new Double(new_value));
			}
		}
		
		public void normalize_neg_cent(){
			Iterator iter = this.neg_cent.entrySet().iterator();
			Integer feature = null;
			Double freq = null;
			double tf_val = 0.0;
			double mag_sq = 0.0;
			while(iter.hasNext()){//update new mag
				freq = null;
				Map.Entry<Integer, Double> featurepair = (Map.Entry)iter.next();
				feature = featurepair.getKey();
				tf_val = featurepair.getValue().doubleValue();
				mag_sq = mag_sq + Math.pow(tf_val, 2);
			}
			Iterator iter2 = this.neg_cent.entrySet().iterator();
			feature = null;
			double deno = Math.sqrt(mag_sq);		
			while(iter2.hasNext()){
				freq = null;
				Map.Entry<Integer, Double> featurepair = (Map.Entry)iter2.next();
				feature = featurepair.getKey();
				freq = featurepair.getValue();
				double new_value = freq.doubleValue()/deno;
				this.neg_cent.put(feature, new Double(new_value));
			}
		}
		
		public HashMap sortByValues(){
			List list = new LinkedList(this.testpt_to_ps.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue());
	            }
	       });
	       
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
		      
		       
		}
		
	}
	
	public static void print_to_output(String output_file_path){
		PrintWriter writer;
		try{
			writer = new PrintWriter(output_file_path, "UTF-8");
			for (Map.Entry<Integer,Integer> entry : test_allocation.entrySet()) {
				  Integer docid = entry.getKey();
				  Integer classno = entry.getValue();
				  writer.println(docID_to_point.get(docid).rlabel_ID + ", " + class_map_string.get(classno.intValue()));
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
		
	}
			
}
