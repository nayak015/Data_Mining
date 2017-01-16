//package classification2;


import java.util.*;



import java.io.*;


public class regression {

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
	
	static Map<Integer, Point> MStrainlistIDs = new HashMap<Integer, Point>();
	static Map<Integer, Point> MSvallistIDs = new HashMap<Integer, Point>();
	static Map<Integer, Double> MS_XiT_Xi = new HashMap<Integer, Double>();
	//static Map<Integer, HashMap<Integer, Double>> MS_XiT_Ximinus  = new HashMap<Integer, HashMap<Integer, Double>>();
	static Map<Integer, Double> XiT_Xi = new HashMap<Integer, Double>();
	
	static Map<Integer, HashMap<Integer, Double>> MS_feature_to_doc_to_value  = new HashMap<Integer, HashMap<Integer, Double>>();
	
	static int MAXF1_lambda_index = 0;
	static double MAXF1_sofar = 0.0;
	
	static Map<Integer, Point> MS_X = new HashMap<Integer, Point>();// X used for Model selection
	
	static double[] lambda_array = {0.01, 0.05, 0.1, 0.5, 1.0, 10.0};//6 
	static int lambda_array_size = 6;
	
	static int[] MS_testpts_class = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // contains TP+FN value for each class_no(given by index_no)
	static int[] testpts_class ={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // contains TP+FN value for each class_no(given by index_no)
	static int total_testpts = 0;
	static int MS_total_testpts = 0;
	
	
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
    	String MS_train_file = new String(args[8]);
    	String MS_test_file = new String(args[9]);
    	
	/*
	String path = "/home/nayak015/Desktop/project3/project3-files/";    
	String input_file = "20newsgroups_word.ijv";
    	String input_rlabel_file = "20newsgroups.rlabel";
    	String train_file = "20newsgroups.train";
    	String test_file = "20newsgroups.test";
    	String class_file = "20newsgroups.class";
    	String features_label_file = "20newsgroups_word.clabel";
    	String feature_representation_option = "tf"; // binary, tf , idtf
    	String output_file = "output_file";
    	String MS_train_file = "20newsgroups_ridge.train";
    	String MS_test_file = "20newsgroups_ridge.val";
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
				
				MS_feature_to_doc_to_value.put(new Integer(j), null);
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
						MS_feature_to_doc_to_value.put(new Integer(j), null);
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
    	
    	
    	System.out.println("Creating MS_trainlistIDs...");
    	
    	System.out.println("Reading the ridge_train file...");
    	my_point = null; // resetting
    	my_binaryclass = null; //resetting
    	s = null;
    	try {
			s = new BufferedReader(new FileReader(MS_train_file));
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
				MStrainlistIDs.put(new Integer(i), my_point);
				l.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Reading the ridge_val file...");
    	
    	MS_total_testpts = 0;
    	my_point = null; // resetting
    	my_binaryclass = null; //resetting
    	s = null;
    	try {
			s = new BufferedReader(new FileReader(MS_test_file));
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
				MSvallistIDs.put(new Integer(i), my_point);
				MS_total_testpts++;
				MS_testpts_class[classindex]++;
				l.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Reading clabel file...");
    	
    	my_point = null; // resetting
    	my_binaryclass = null; //resetting
    	s = null;
    	try {
			s = new BufferedReader(new FileReader(features_label_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	line = null;
	    int line_no = 0;
    	try {
			while ((line = s.readLine())!= null){
				line_no++;
				Scanner l = new Scanner(line);
				l.useDelimiter(" ");
				String featurename = l.next();
				feature_to_label.put(new Integer(line_no), featurename);			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	System.out.println("Starting Model Selection now...");
    	
    	System.out.println("Creating y for each class in Model Selection now...");
    	
    	MS_y_generation();
    	
    	
    	compute_MS_XiT_Xi();
    	
    	
    	
    	
    	int best_lambda_index = 0;
    	
    	
    	for (int lambda_index = 0; lambda_index< lambda_array_size; lambda_index++){
    		double lambda_val = lambda_array[lambda_index];
    		System.out.println("Starting with lambda = " + lambda_val+ " now...");
    		
    		
    		// remember to reset things if you need to
    		
    		// for each class obtain w
    		MS_get_w(lambda_index);
    		
    		
    		//loop over MSvallist
    		loop_over_MSvallist(lambda_index);
    		
    		//compute best F1 score
    		MS_performance_analysis(lambda_index);
    		
    		
    	}// end of lambda loop
    	
    	System.out.println("End of MODEL Selection...");
    	System.out.println("Best lambda: " + lambda_array[MAXF1_lambda_index]);
    	best_lambda_index = MAXF1_lambda_index;
    	
    	System.out.println("Training Binary classifiers with train-file now...");
    	
    	y_generation();
    	
    	
    	compute_XiT_Xi();
    	
    	System.out.println("Training the w for Binary classifiers with train-file now...");
    	
    	get_w(best_lambda_index);
    	
    	//loop over testlist
		loop_over_testlist(best_lambda_index);
		
		//compute F1 score
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
	
	
	
	
	public static void MS_performance_analysis(int lambda_index){
		
		double MAXF1_AVG = 0.0;
		Iterator iter = classno_to_binaryclass.entrySet().iterator();
		Integer classid = null;
		Binary_classifier classifier_ref = null;
		while(iter.hasNext()){// every binary class
			double MAXF1 = 0.0;
			Map.Entry<Integer, Binary_classifier> classpair = (Map.Entry)iter.next();
			classid = classpair.getKey();
			classifier_ref = classpair.getValue();
			
			classifier_ref.MS_SORTED_testpt_to_ps = classifier_ref.sortByValues();
			
			Set set2 = classifier_ref.MS_SORTED_testpt_to_ps.entrySet();
			
			Iterator iterator2 = set2.iterator();
			
			int index = MS_total_testpts-1;
			
		    while(iterator2.hasNext()) {
			   Map.Entry me2 = (Map.Entry)iterator2.next();
			   //System.out.print(me2.getKey() + ": ");
			   //System.out.println(me2.getValue());
			   Map<Integer, Integer> mymap = classifier_ref.MS_sortedpointMap.get(lambda_index);
			   if( mymap == null){
				   classifier_ref.MS_sortedpointMap.put(new Integer(lambda_index), new HashMap<Integer, Integer>());
				}
			   classifier_ref.MS_sortedpointMap.get(lambda_index).put(new Integer(index), new Integer((int)me2.getKey())); // might throw error
			   //System.out.println("Check!");
			   //System.out.print((int)me2.getKey() + ": ");
			   //System.out.println(classifier_ref.sortedpointMap.get(index));
			   index--;
		    }
		    
		    for(int L=0; L<MS_total_testpts; L++){
		    	
		    	double TP_FP_sum = (double)L;
		    	
		    	double TP_FN_sum = (double)MS_testpts_class[classifier_ref.class_number];
		    	
		    	int TP = 0;
		    	
		    	for (int count = 0; count <L; count++){
		    		Integer docid = classifier_ref.MS_sortedpointMap.get(lambda_index).get(count);
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
		System.out.println("For Lambda: " + lambda_array[lambda_index] + " MAXF1_AVG: " + MAXF1_AVG);
		if(MAXF1_AVG > MAXF1_sofar){
			MAXF1_sofar = MAXF1_AVG;
			MAXF1_lambda_index = lambda_index;
		}
	}
	
	
	
	
	
	
	
	
	
	public static void loop_over_MSvallist(int lambda_index){
		Iterator it = MSvallistIDs.entrySet().iterator();
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
				// compute dot-product
				double dot_product = MSdot_product(point_ref, classifier_ref, lambda_index);
				classifier_ref.testpt_to_ps.put(new Integer(point_ref.ID) , new Double(dot_product));
				point_ref.class_to_score.put(new Integer(classid.intValue()), new Double(dot_product));
				
				if(dot_product > max_diff){
					max_diff = dot_product;
					Map<Integer, Double> mypred = null;
					point_ref.MS_prediction_score.put(new Integer(lambda_index), 0.0);
					point_ref.MS_prediction_score.put(lambda_index,new Double(max_diff));
					point_ref.MS_class_prediction.put(new Integer(lambda_index), 0);
					point_ref.MS_class_prediction.put(lambda_index,new Integer(classid.intValue()));
				}
			}
		}
		
		
	}
	
	
	
	
	
	
	public static void loop_over_testlist(int lambda_index){
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
				// compute dot-product
				double dot_product = dot_product(point_ref, classifier_ref);
				classifier_ref.testpt_to_ps.put(new Integer(point_ref.ID) , new Double(dot_product));
				point_ref.class_to_score.put(new Integer(classid.intValue()), new Double(dot_product));
				
				if(dot_product > max_diff){
					max_diff = dot_product;
					point_ref.prediction_score = max_diff;
					point_ref.class_prediction = classid.intValue();
				}
			}
			test_allocation.put(new Integer(docid.intValue()), point_ref.class_prediction);
		}
	}
	
	
	
	
	
	public static double dot_product(Point my_point, Binary_classifier my_classifier){
		
		double sum_pos = 0.0;
		double product = 0.0;
		//calculate +ive and point cosine similarity
		Iterator it = my_point.normalised.entrySet().iterator();
		Integer feature = null;
		Double freq = null;
		while(it.hasNext()){
			Map.Entry<Integer, Double> featurepair = (Map.Entry)it.next();
			feature = featurepair.getKey();
			freq = featurepair.getValue();
			Double check = my_classifier.w.get(feature);
			if(check != null){ // check by switching to .intValue
				product = ((double)freq.doubleValue()) * ((double)check.doubleValue());
				sum_pos = sum_pos + product;
			}
		}
		return sum_pos;
		
	}
	
	
	
	
	
	public static double MSdot_product(Point my_point, Binary_classifier my_classifier, int lambda_index){
		
		double sum_pos = 0.0;
		double product = 0.0;
		//calculate +ive and point cosine similarity
		Iterator it = my_point.normalised.entrySet().iterator();
		Integer feature = null;
		Double freq = null;
		while(it.hasNext()){
			Map.Entry<Integer, Double> featurepair = (Map.Entry)it.next();
			feature = featurepair.getKey();
			freq = featurepair.getValue();
			Map<Integer, Double> check = my_classifier.lambda_to_w.get(lambda_index);
			if(check.containsKey(feature.intValue())){ // check by switching to .intValue
				product = ((double)freq.doubleValue()) * ((double)check.get(feature.intValue()).doubleValue());
				sum_pos = sum_pos + product;
			}
		}
		
		return sum_pos;
		
	}
	
	
	
	public static void compute_MS_XiT_Xi(){	//also populates MS_feature_todoc_to_value
		
		for(Integer i : feature_to_label.keySet()){
			for(Integer docid : MStrainlistIDs.keySet()){
				if(docID_to_point.get(docid).normalised.containsKey(i)){
					if(MS_feature_to_doc_to_value.get(i) == null){
						MS_feature_to_doc_to_value.put(i, new HashMap<Integer, Double>());
						MS_feature_to_doc_to_value.get(i).put(new Integer(docid.intValue()), new Double(docID_to_point.get(docid).normalised.get(i).doubleValue()));
					}
					else{
						MS_feature_to_doc_to_value.get(i).put(new Integer(docid.intValue()), new Double(docID_to_point.get(docid).normalised.get(i).doubleValue()));
					}
				}
			}
		}
		
		for(Integer i : MS_feature_to_doc_to_value.keySet()){
			double sum = 0.0;
			if(MS_feature_to_doc_to_value.get(i) != null){
				for(Integer docid : MS_feature_to_doc_to_value.get(i).keySet()){
					sum += Math.pow(MS_feature_to_doc_to_value.get(i).get(docid).doubleValue(),2);
				}
			}
			MS_XiT_Xi.put(new Integer(i.intValue()), new Double(sum));
		}
		
		
		//populate MS_XiT_Ximinus
		/*
		for(Integer I : MS_feature_to_doc_to_value.keySet()){
			
			for(Integer j : MS_feature_to_doc_to_value.keySet()){
				if (I.intValue() != j.intValue()){
					System.out.println("I: " + I + " J :" +j);
					double sum = 0.0;
					for(Integer docid_I : MS_feature_to_doc_to_value.get(I).keySet()){
						Double check = MS_feature_to_doc_to_value.get(I).get(docid_I);
						if(check != null){
							if(MS_feature_to_doc_to_value.get(j) != null){
								Double check2 = MS_feature_to_doc_to_value.get(j).get(docid_I);
								if(check2 != null){
									sum += (MS_feature_to_doc_to_value.get(j).get(docid_I).doubleValue())*(MS_feature_to_doc_to_value.get(I).get(docid_I).doubleValue());
								}
							}
						}
					}
					MS_XiT_Ximinus.put(new Integer(I.intValue()), new HashMap<Integer, Double>());
					MS_XiT_Ximinus.get(I).put(new Integer(j.intValue()), new Double(sum));
				}	
					
			}	
		}*/
			
		
		
		
	}
	
	
	
	
	public static void compute_XiT_Xi(){	//also populates MS_feature_todoc_to_value
		
		for(Integer i : feature_to_label.keySet()){
			for(Integer docid : trainlistIDs.keySet()){
				if(docID_to_point.get(docid).normalised.containsKey(i)){
					if(MS_feature_to_doc_to_value.get(i) == null){
						MS_feature_to_doc_to_value.put(i, new HashMap<Integer, Double>());
						MS_feature_to_doc_to_value.get(i).put(new Integer(docid.intValue()), new Double(docID_to_point.get(docid).normalised.get(i).doubleValue()));
					}
					else{
						MS_feature_to_doc_to_value.get(i).put(new Integer(docid.intValue()), new Double(docID_to_point.get(docid).normalised.get(i).doubleValue()));
					}
				}
			}
		}
		
		for(Integer i : MS_feature_to_doc_to_value.keySet()){
			double sum = 0.0;
			if(MS_feature_to_doc_to_value.get(i) != null){
				for(Integer docid : MS_feature_to_doc_to_value.get(i).keySet()){
					sum += Math.pow(MS_feature_to_doc_to_value.get(i).get(docid).doubleValue(),2);
				}
			}
			XiT_Xi.put(new Integer(i.intValue()), new Double(sum));
		}
		
		
		//populate MS_XiT_Ximinus
		/*
		for(Integer I : MS_feature_to_doc_to_value.keySet()){
			
			for(Integer j : MS_feature_to_doc_to_value.keySet()){
				if (I.intValue() != j.intValue()){
					System.out.println("I: " + I + " J :" +j);
					double sum = 0.0;
					for(Integer docid_I : MS_feature_to_doc_to_value.get(I).keySet()){
						Double check = MS_feature_to_doc_to_value.get(I).get(docid_I);
						if(check != null){
							if(MS_feature_to_doc_to_value.get(j) != null){
								Double check2 = MS_feature_to_doc_to_value.get(j).get(docid_I);
								if(check2 != null){
									sum += (MS_feature_to_doc_to_value.get(j).get(docid_I).doubleValue())*(MS_feature_to_doc_to_value.get(I).get(docid_I).doubleValue());
								}
							}
						}
					}
					MS_XiT_Ximinus.put(new Integer(I.intValue()), new HashMap<Integer, Double>());
					MS_XiT_Ximinus.get(I).put(new Integer(j.intValue()), new Double(sum));
				}	
					
			}	
		}*/
			
		
		
		
	}
	
	
	
	
	public static void MS_get_w(int lambda_index){
		double lambda = lambda_array[lambda_index];
		Iterator iter = classno_to_binaryclass.entrySet().iterator();
		Integer classid = null;
		Binary_classifier classifier_ref = null;
		while(iter.hasNext()){// every binary class
			Map.Entry<Integer, Binary_classifier> classpair = (Map.Entry)iter.next();
			classid = classpair.getKey();
			classifier_ref = classpair.getValue();
			for(Integer i : feature_to_label.keySet()){
				double random = new Random().nextDouble();
				Map<Integer, Double> mymap = classifier_ref.lambda_to_w.get(lambda_index);
				if( mymap == null){
					classifier_ref.lambda_to_w.put(new Integer(lambda_index), new HashMap<Integer, Double>());
				}
				classifier_ref.lambda_to_w.get(lambda_index).put(new Integer(i.intValue()), new Double(random));
			}// initialized w for one binary class with 0 to 1 random double.
			
			// add stuff here to compute the correct w.
			
		}
		
		/* TO BE IMPLEMENTED TO GET THE CORRECT Ws.
		double tolerance = 2.0;
		double prev_error = 10;
		while(tolerance > 0.001){
			double new_error = 0;
			
			for(Integer i : feature_to_label.keySet()){
				double deno = MS_XiT_Xi.get(i).doubleValue() * lambda;
				
				
			
			
			}
			
			//compute new_error using wi
			
			tolerance = prev_error - new_error;
			prev_error = new_error;
		}
		*/
		
		
		
		
	}
	
	
	
	
	
	
	
	public static void get_w(int lambda_index){
		double lambda = lambda_array[lambda_index];
		Iterator iter = classno_to_binaryclass.entrySet().iterator();
		Integer classid = null;
		Binary_classifier classifier_ref = null;
		while(iter.hasNext()){// every binary class
			Map.Entry<Integer, Binary_classifier> classpair = (Map.Entry)iter.next();
			classid = classpair.getKey();
			classifier_ref = classpair.getValue();
			for(Integer i : feature_to_label.keySet()){
				double random = new Random().nextDouble();
				classifier_ref.w.put(new Integer(i.intValue()), new Double(random));
			}// initialized w for one binary class with 0 to 1 random double.
			
			// add stuff here to compute the correct w.
			
		}
		
		/* TO BE IMPLEMENTED TO GET THE CORRECT Ws.
		double tolerance = 2.0;
		double prev_error = 10;
		while(tolerance > 0.001){
			double new_error = 0;
			
			for(Integer i : feature_to_label.keySet()){
				double deno = MS_XiT_Xi.get(i).doubleValue() * lambda;
				
				
			
			
			}
			
			//compute new_error using wi
			
			tolerance = prev_error - new_error;
			prev_error = new_error;
		}
		*/
		
		
		
		
	}
	
	
	
	

	public static void MS_y_generation(){
		
		Binary_classifier my_class = null;
		for(Integer key : classno_to_binaryclass.keySet()){
			my_class = classno_to_binaryclass.get(key);
			
			for(Integer key1 : MStrainlistIDs.keySet()){
				
				if (MStrainlistIDs.get(key1).class_number == my_class.class_number){// target class
					my_class.MS_y.put(new Integer(key1.intValue()), 1);
				}
				else{
					//my_class.MS_y.put(new Integer(key1.intValue()), 0); // non target class
				}
			}
		}
	}
	
	
	
	public static void y_generation(){
		
		Binary_classifier my_class = null;
		for(Integer key : classno_to_binaryclass.keySet()){
			my_class = classno_to_binaryclass.get(key);
			
			for(Integer key1 : trainlistIDs.keySet()){
				
				if (trainlistIDs.get(key1).class_number == my_class.class_number){// target class
					my_class.y.put(new Integer(key1.intValue()), 1);
				}
				else{
					//my_class.MS_y.put(new Integer(key1.intValue()), 0); // non target class
				}
			}
		}
	}
	
	
	
	
	public static void train_classifiers(){
		
	}
	
	
	public static void test_classifiers(){
		
		
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
		
		Map<Integer, Double> MS_prediction_score = new HashMap<Integer, Double>();
		Map<Integer, Integer> MS_class_prediction = new HashMap<Integer, Integer>();
		
		Map<Integer, Double> class_to_score = new HashMap<Integer, Double>();
		//constructor
		
		
		
	}
	
	
	public static class Binary_classifier{
		
		Map<Integer, Integer> MS_y = new HashMap<Integer, Integer>();
		static Map<Integer, Double> MS_XiT_y = new HashMap<Integer, Double>();
		
		Map<Integer, Integer> y = new HashMap<Integer, Integer>();
		
		Map<Integer, Double> w = new HashMap<Integer, Double>();
		Map<Integer, Map<Integer, Double>> lambda_to_w = new HashMap<Integer, Map<Integer, Double>>();
		
		
		Map<Integer, Double> testpt_to_ps = new HashMap<Integer, Double>();

		Map<Integer, Integer> sortedpointMap = new HashMap<Integer, Integer>();
		Map<Integer, Map<Integer, Integer>> MS_sortedpointMap = new HashMap<Integer, Map<Integer, Integer>>();
		
		Map<Integer, Double> SORTED_testpt_to_ps;
		Map<Integer, Map<Integer, Double>> MS_SORTED_testpt_to_ps;
		
		int class_number; //ID for class
		String class_name; 
		int total_pos_trains_IDs = 0;
		int total_neg_trains_IDs = 0;
		int TP, FN, FP, TN;
		
		// CONSTRUCTOR
		
		
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
