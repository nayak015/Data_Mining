//Author: Ankit Nayak


import java.util.*;

//import whatup.itemsetnode;

import java.io.*;

public class hcrminer {

	static int minsup;
	static double minconf;
	static int hfrange;
	static int maxleafsize;
	
	static int no_of_frequent_itemsets;
	static int no_of_high_conf_rules;
	
	static Set<Integer> list_of_tids;
	static int total_transactions;
	static Map<Integer, Vector <Integer>> dataset = new HashMap<>();
	static Vector<Integer> fullitemset = new Vector<Integer>();
	static Vector<Vector<itemsetnode>> largeitemset=new Vector<Vector<itemsetnode>>();
	static Vector<candidateelement> candidate=new Vector<candidateelement>();
	
	static Map<Vector<Integer>, Integer> largeitemset_hash = new HashMap<>();
	static Map<itemsetnode, Integer> lalala = new HashMap<>();
	
	static Vector<Vector<Integer>> subset = new Vector<Vector<Integer>>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// fill in the parameters
		//minsup minconf inputfile outputfile hfrange maxleafsize
		
		minsup = Integer.parseInt(args[0]);
		minconf = Double.parseDouble(args[1]);
		String input_file = new String(args[2]);
		String output_file = new String(args[3]);
		hfrange = Integer.parseInt(args[4]);
		maxleafsize = Integer.parseInt(args[5]);
		
	    
		
		//populate the dataset
		BufferedReader s = null;
		try {
			s = new BufferedReader(new FileReader(input_file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Vector<Integer> dataset_itemlist = null;
		String line = null;
		try {
			while ((line = s.readLine())!= null){
				Scanner l = new Scanner(line);
			    int list_no = l.nextInt();
				int object = l.nextInt();
				dataset_itemlist = dataset.get(list_no);
				if(dataset_itemlist == null) {
					dataset_itemlist = new Vector<>();
				}
				dataset_itemlist.add(object);
				dataset.put(list_no, dataset_itemlist);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(m);
		//System.out.println(m.keySet());
		list_of_tids = dataset.keySet();
		//Access the items in a particular tid = 1 like this 
		total_transactions = Collections.max(dataset.keySet());
		
		
		//done populating the dataset
		no_of_high_conf_rules=0;
		
		//start time
	    Date d=new Date();
	    long s1=d.getTime();
		
		
		
		//generate F_1 itemsets
		Vector<itemsetnode> F_1;
		F_1 = gen_frequent_1_itemsets();
		
		no_of_frequent_itemsets = F_1.size();
		largeitemset.addElement(F_1);
		
		//apriori process to generate the frequent itemsets
		int k = 1;
		
		candidateelement cande;

	    System.out.println();
	    System.out.println("Algorithm apriori starting now.....");
	    System.out.println();
	    
	    while (true)
	    {
	      k++; 
	      cande=new candidateelement(); // level
	      cande.candlist= createcandidate(k); //generate k+1 itemset candidates
	     
	      //printing candidate itemsets
	      /*Iterator<itemsetnode> iter = cande.candlist.iterator();
	      while(iter.hasNext())
	    	  System.out.println("C"+k+"("+k+"-candidate-itemset): "+iter.next().itemset);*/
	      //System.out.println("C"+k+"("+k+"-candidate-itemset): "+cande.candlist);
	      
	      //System.out.println("end condition: " + cande.candlist.isEmpty());
	      if (cande.candlist.isEmpty())
	    	break; // if not new k+1 itemset generated, stop

	      cande.htroot = null;
	      cande.htroot = createHashTree(cande.candlist, k);
	      
	      //testing
	      /*
	      System.out.println(cande.htroot.children);
	      System.out.println(cande.htroot.children.get(1).data_list.size());
	      //System.out.println(cande.htroot.children.get(1).data_list);
	      System.out.println(cande.htroot.children.get(1).data_list.get(1).itemset);
	      int sum=0;
	      for(int q=0; q<10;q++){
	    	  System.out.println("size " + q + " : " + cande.htroot.children.get(q).data_list.size());
	    	  sum +=cande.htroot.children.get(q).data_list.size();
	      }
	      System.out.println(cande.candlist.size());
	      System.out.println(sum);*/
	      
	      // For each transaction in the dataset
	      //traverse the tree
	      for(int i = 0; i<=total_transactions; i++){
	    	  //System.out.println("transaction "+ i + " " + dataset.get(i));
	    	  if (!(k>dataset.get(i).size())){ // only if the transaction size is bigger than or equal to k
	    	  tree_traversal(dataset.get(i), cande.htroot, 0, k, null);
	    	  /*itemsetnode p=null;
		      Iterator<itemsetnode> iter = cande.candlist.iterator();
		      while(iter.hasNext()){
		    	  p = iter.next();
		    	  System.out.println("C"+k+"("+k+"-candidate-itemset): "+p.itemset+ " support: "+ p.support);
		      }*/
	    	  }
	      }
	      
	      // adding the frequent candidate itemsets to largeitemset.
	      Vector<itemsetnode> next_FK = new Vector<itemsetnode>();
	      itemsetnode t=null;
	      Iterator<itemsetnode> iter = cande.candlist.iterator();
	      while(iter.hasNext()){
	    	  t = iter.next();
	    	  if (t.support>=minsup){
	    		  next_FK.add(t);
	    	  }
	      }
	      
	      largeitemset.addElement(next_FK);
	      no_of_frequent_itemsets += next_FK.size();
	    }// end of appriori freq itemset gen

		
	    //printing frequent itemsets
	    PrintWriter writer;
	    if(minsup<=20){
	    try {
			writer = new PrintWriter(output_file, "UTF-8");
			System.out.println("writing to the file now");
			Iterator<Vector<itemsetnode>> freq = largeitemset.iterator();
		    while (freq.hasNext()){
		    	
		    	Iterator<itemsetnode> freq_item = freq.next().iterator();
		    	itemsetnode q;
		    	while(freq_item.hasNext()){// printing in the correct format.
		    		q = freq_item.next();
		    		String S1 = "";
		    		
		    		for(int i=0; i<q.itemset.size();i++){
						String S2 = new String(q.itemset.get(i).toString());
		    			S1 +=S2;
		    			S1 += " ";
		    		}
		    		writer.println(S1 + "| {} | " + q.support + " | " + "-1");
		    	}
		    }
		    writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }
	   // later confidence stuff 
		// confidence calculation and rule generation
	    else{ // only is minsup more than 20, else too many rules.
			try {
				writer = new PrintWriter(output_file, "UTF-8");
				System.out.println("writing to the file now");
				Iterator<Vector<itemsetnode>> freq = largeitemset.iterator();
			    while (freq.hasNext()){
			    	
			    	Iterator<itemsetnode> freq_item = freq.next().iterator();
			    	itemsetnode q;
			    	while(freq_item.hasNext()){// printing in the correct format.
			    		q = freq_item.next();
			    		String S1 = "";
			    		for(int i=0; i<q.itemset.size();i++){
			    			String S2 = new String(q.itemset.get(i).toString());
							S1 +=S2;
							S1 += " ";
			    		}
			    		writer.println(S1 + "| {} | " + q.support + " | " + "-1");
			    	}
			    }
			    
			    
				System.out.println("Rule Generation process starting now...");
				
				createhashmap();
				
				
				Iterator<Vector<itemsetnode>> freq1 = largeitemset.iterator();
				Vector<itemsetnode> FK_1 = freq1.next(); // ignore the FK_1
			    while (freq1.hasNext()){
			    	Iterator<itemsetnode> FK_k = freq1.next().iterator();
			    	itemsetnode q;
			    	while(FK_k.hasNext()){
			    		q = FK_k.next();
			    		itemsetnode.rulesetnode New_cand;
			    		Vector<itemsetnode.rulesetnode> New_cand_list = new Vector<itemsetnode.rulesetnode>();
			    		for (int i = 0; i<q.itemset.size(); i++){
			    			Vector<Integer> lhs = new Vector<Integer>(q.itemset);
			    			Vector<Integer> rhs = new Vector<Integer>();
			    			rhs.add(q.itemset.get(i));
			    			lhs.remove(q.itemset.get(i));
			    			New_cand = q.new rulesetnode(lhs,rhs);
			    			if (New_cand.confidence > minconf){
			    				New_cand_list.addElement(New_cand);
			    			}
			    			// else ignore
			    		}
			    		q.high_conf_rules.addElement(New_cand_list);
			    		//Now we have H1.
			    		//System.out.println("------------------------------------------------Now we have H1");
			    		
			    		Vector<itemsetnode.rulesetnode> New_cand_list1 = new Vector<itemsetnode.rulesetnode>();
			    		itemsetnode.rulesetnode New_cand_k=null;
			    		int rule_k = q.itemset.size();
			    		int rule_m = 1;//size of rule consequent initially
			    		Vector<itemsetnode.rulesetnode> candi_list= null;
			    		while(rule_k>rule_m+1){
			    			
			    			New_cand_list1.removeAllElements();
			    			//generate new candidates
			    			
			    			candi_list = createcandidaterules(rule_m, q);
			    			Iterator<itemsetnode.rulesetnode> iterat = candi_list.iterator();
			    			while(iterat.hasNext()){
			    				New_cand_k = iterat.next();
			    				if(New_cand_k.confidence>minconf){
			    					New_cand_list.addElement(New_cand_k);
			    				}
			    			}
			    			rule_m++;
			    			q.high_conf_rules.addElement(New_cand_list1);
			    			
			    		}
			    		//print all the rules and confidence
			    		
						//System.out.println("Rules writing to the file now");
						Iterator<Vector<itemsetnode.rulesetnode>> rules = q.high_conf_rules.iterator();
					    while (rules.hasNext()){
					    	Iterator<itemsetnode.rulesetnode> rule_item = rules.next().iterator();
					    	itemsetnode.rulesetnode q_rule;
					    	while(rule_item.hasNext()){// printing in the correct format.
								no_of_high_conf_rules += 1; 
					    		q_rule = rule_item.next();
					    		String S1 = "";
					    		for(int i=0; i<q_rule.lhs.size();i++){
					    			String S2 = new String(q_rule.lhs.get(i).toString());
									S1 +=S2;
									S1 += " ";
					    		}
					    		S1 += " | ";
					    		
					    		for(int i=0; i<q_rule.rhs.size();i++){
					    			String S2 = new String(q_rule.rhs.get(i).toString());
									S1 +=S2;
									S1 += " ";
					    		}
					    		
					    		writer.println(S1 + " | " + q.support + " | " + q_rule.confidence);
					    	}
					    }
					    
			    		
			    		
			    		
			    	}
			    }
			    writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
		
		
		
		//end time
	    d=new Date();
	    long s2=d.getTime();
	    System.out.println();
	    System.out.println("Execution time is: "+((s2-s1)/(double)1000) + " seconds.");
	    
		// output the frequent itemsets
		System.out.println("Total frequent itemsets: " + no_of_frequent_itemsets);
		System.out.println("Total high confidence rules: " + no_of_high_conf_rules);
		
		//confidence to generate the rules
		
		
	}
	
	
	static void createhashmap(){
		Iterator<Vector<itemsetnode>> freq = largeitemset.iterator();
	    while (freq.hasNext()){
	    	Iterator<itemsetnode> freq_item = freq.next().iterator();
	    	itemsetnode q;
	    	while(freq_item.hasNext()){
	    		q = freq_item.next();
	    		largeitemset_hash.put(q.itemset, q.support);
	    	}
	    }
	}
	
	
	
	static class candidateelement{
		Node htroot;
		Vector<itemsetnode> candlist; // not sure if vector of integers or itemsetnode
	}
	
	// itemsetnode has the vector of items and the support count.
	static class itemsetnode{

		Vector<Integer> itemset = null;
		int support;
		
		Vector<Vector<rulesetnode>> high_conf_rules = new Vector<Vector<rulesetnode>>();

		public itemsetnode(Vector<Integer> itemset1,int i1)
		{
			itemset=itemset1;
			support=i1;
		}

		public itemsetnode(Vector<Integer> itemset1){
			itemset=itemset1;
			support=0;
		}
		
		public itemsetnode(){
			support=0;
		}
		
		public class rulesetnode{
			Vector<Integer> lhs = null;
			Vector<Integer> rhs = null;
			double confidence;
			
			public rulesetnode(Vector<Integer> lhs_g, Vector<Integer> rhs_g)
			{
				lhs = lhs_g;
				rhs = rhs_g;
				confidence = (double)itemsetnode.this.support/((double)largeitemset_hash.get(lhs));
				//System.out.println("itemsetnode.this.support:  " + itemsetnode.this.support);
				//System.out.println("(int)largeitemset_hash.get(lhs):  " + (int)largeitemset_hash.get(lhs));
				
				//System.out.println("---------------------------------------------------------------------------- confidence= " + confidence + "  itemset lhs = " + lhs + "  itemset rhs = " + rhs);
			}
			
			public rulesetnode(){
				lhs = new Vector<Integer>();
				rhs = new Vector<Integer>();
			}
			
		}
		
	}
	
	//generate a vector of frequent 1 itemset.
	static Vector<itemsetnode> gen_frequent_1_itemsets(){
		Vector<itemsetnode> return_set = new Vector<itemsetnode>();
		for (int i = 0; i <= total_transactions; i++){
			Vector<Integer> x = new Vector<Integer>();
			x = dataset.get(i);
			for (int j = 0; j < x.size(); j++){
				if(!fullitemset.contains(x.get(j))){
					fullitemset.add(x.get(j));
				}
			}	
		}// created the fullitemset. created a copy items.
		
		//support count for candidate 1 itemset
		int count;
		
		for (int i = 0; i<fullitemset.size(); i++){
			count = 0;
			int y = fullitemset.elementAt(i);
			for (int j = 0; j <= total_transactions; j++){
				if(dataset.get(j).contains(y)){
					count++;
				}
			}
			//System.out.println("element " + y + " support: "+count);
			//pruning. 
			if (count >= minsup){
				Vector<Integer> a = new Vector<Integer>();
				a.add(y);
				itemsetnode w = new itemsetnode(a,count);
				return_set.add(w);
				lalala.put(w, count);
				//System.out.println("frequent item "+ w.itemset +" support: "+w.support);
			}
		}
		return return_set;
	} // end of gen_frequent_1_itemsets()
	
	// returns a vector of itemsetnodes using Fk-1, Fk-1 matching approach
	static Vector<itemsetnode> createcandidate(int k){
		Vector<itemsetnode> return_set = new Vector<itemsetnode>();

		//Vector<Vector<Integer>> subset;
		
		
		int elements_in_Fk_prev = largeitemset.get(k-2).size();
		//System.out.println("--createcandidate():  elements_in_Fk_prev: " + elements_in_Fk_prev);
		Vector<Integer> cand1 = new Vector<Integer>();
		Vector<Integer> cand2 = new Vector<Integer>();
		boolean present = true;
		
		for (int i=0; i < elements_in_Fk_prev; i++){
			cand1 = largeitemset.get(k-2).elementAt(i).itemset;
			//System.out.println("--createcandidate():  cand1: " + cand1);
			for (int j=i+1; j < elements_in_Fk_prev; j++){
				cand2 = largeitemset.get(k-2).elementAt(j).itemset;
				//System.out.println("--createcandidate():  cand2: " + cand2);
				// check k-1 if equal then create new itemsetnode
				//Vector<Integer> x = new Vector<Integer>(cand1.subList(0, cand1.size()-1));
				//Vector<Integer> y = new Vector<Integer>(cand2.subList(0, cand2.size()-1));
				Vector<Integer> z = new Vector<Integer>();
				if (cand1.subList(0, cand1.size()-1).equals(cand2.subList(0, cand2.size()-1))){
					z.addAll(cand1);
					z.add(cand2.lastElement());
					//System.out.println("--createcandidate():  z: " + z);
					//z_superset.addElement((Set<Integer>) z);
					//here only generate the subsets of k-1 and check in large itemset if it exists. if all subsets exsist, then add to return set
					if (k>2){
						getSubsets(z, k-1);// generate k-1 size subsets
						//Vector<Vector<Integer>> subset_copy = subset;
						Iterator<Vector<Integer>> itr = subset.iterator();
						Vector<Integer> p = null;
						boolean so_far_present = true;
						while(itr.hasNext() && so_far_present){
							p = itr.next();
							//System.out.println("--createcandidate():  p: " + p);
							itemsetnode r = null;
							Iterator<itemsetnode> it_r = largeitemset.get(k-2).iterator();
							boolean lala =true;
							while(it_r.hasNext() && lala){
								r = it_r.next();
								//System.out.println("--createcandidate():  r.itemset: " + r.itemset + "   p : " + p);
								if (r.itemset.containsAll(p)){
									present = true;
									//System.out.println("--createcandidate():  candidate present in Fk_prev: " + p);
									lala = false;
								}
							}
							if(!present)	so_far_present = false;
						}
						if (so_far_present){
							//System.out.println("--createcandidate():  Adding new candidate now!!  candidate <z>: " + z);
							itemsetnode new_itemset = new itemsetnode(z);
							return_set.addElement(new_itemset);
						}
						//present = true;
					}
					else{ // just add
						itemsetnode new_itemset = new itemsetnode(z);
						return_set.addElement(new_itemset);
					}
				}
			}
		}
		//System.out.println("return set: " + return_set);
		return return_set;		
	}
	
	
	
	public static Node addChild(Node parent, itemsetnode data_item, int child_no) {
		Node node = new Node(parent);
		node.depth = parent.depth+1;
		node.setData(data_item);
		parent.getChildren().set(child_no, node);
		parent.node_type = 2;
		node.data_list_map.put(data_item.itemset, 0);
		return node;
	}
	
	
	
	 static class Node {
		int node_type=1; //bucket (1) or Intermediate_node (2)
	    ArrayList<Node> children = new ArrayList<Node>();// array of children
	    Node parent;
	    Vector<itemsetnode> data_list = new Vector<itemsetnode>();// only store in terminal nodes or leaves
	    Map<Vector<Integer>, Integer> data_list_map = new HashMap<>();
	    //public int no_of_elements;
	    //public int ID;
	    int depth;
	    
	    public Node(){
	    	node_type = 2;// Intermediate Leaf
	    	data_list = new Vector<itemsetnode>();
	    	depth = 0;
	    	for (int i=0; i<hfrange; i++)	children.add(null);
	    }
	    
	    public Node(Node parent) {
	    	
	    	this.parent = parent;
	    	for (int i=0; i<hfrange; i++)	children.add(null);
	    }
	    
	    /*public Node(Vector<itemsetnode> data) {
	        this.data_list = data;
	        for (int i=0; i<hfrange; i++)	children.add(null);
	    }*/

	    public Node(Vector<itemsetnode> data, Node parent) {
	        this.data_list = data;
	        this.parent = parent;
	        if(parent == null)	this.depth = 0;
	        else	this.depth = parent.depth+1;
	        for (int i=0; i<hfrange; i++)	children.add(null);
	    }

	    public List<Node> getChildren() {
	        return children;
	    }
	    
	    public Node getParent() {
	    	return parent;
		}

	    public void setParent(Node parent) {// dont need this
	        //parent.addChild(this);
	        this.parent = parent;
	    }
	    
	    public Vector<itemsetnode> getData() {
	        return this.data_list;
	    }

	    public void setData(itemsetnode data) {
	    	this.data_list.addElement(data);
	    }

	    public boolean isRoot() {
	        return (this.parent == null);
	    }

	    public boolean isLeaf() {
	        if(this.children.size() == 0) 
	            return true;
	        else 
	            return false;
	    }

	    public void removeParent() {
	        this.parent = null;
	    }
	    
	    
	 }
	
	
	// returns the root of the hash tree.
	public static Node createHashTree(Vector<itemsetnode> candidate_list, int k){
		//System.out.println("In createHashTree");
		Node root_node = new Node(null, null);
		itemsetnode a_candidate;
		Iterator<itemsetnode> itr_cand = candidate_list.iterator();
		int hash_index = 0;
		Node child_node = null;
		while(itr_cand.hasNext()){
			a_candidate = itr_cand.next();
			Iterator<Integer> itr_in_cand = a_candidate.itemset.iterator();
			hash_index = itr_in_cand.next() % hfrange; // hash function
			// think about how to add the nodes.
			/*System.out.println("Adding itemset : " + a_candidate.itemset);
			System.out.println("Hash Index : " + hash_index);
			System.out.println("size : " + root_node.children.size());*/
			if (root_node.getChildren().get(hash_index) == null){// create the node child instance 1st time
				child_node = addChild(root_node, a_candidate, hash_index);
			}
			else{
				child_node = root_node.getChildren().get(hash_index);
				//System.out.println("createHT(): ==========: node type: " + child_node.node_type);
				while(child_node.node_type == 2){ // while the child_node is a Intermediate Leaf keep going down.
					//System.out.println("createHT(): Hit a IL Depth:" + child_node.depth);
					hash_index = itr_in_cand.next() % hfrange;// hash on the next element.
					if (child_node.getChildren().get(hash_index) == null){// create the node child instance 1st time
						Node tmp = addChild(child_node, a_candidate, hash_index);
						child_node = tmp;
					}
					else{
						child_node = child_node.getChildren().get(hash_index);
					}
				}
				// we have reached a leaf
				child_node.data_list.addElement(a_candidate);
				child_node.data_list_map.put(a_candidate.itemset, 0);
				//System.out.println("createHT(): -----------------Adding element---- Depth:" + child_node.depth+ " " + a_candidate.itemset);
				//check if maxLeaf or depth/ the itemset has another element to hash on
				if(child_node.data_list.size() > maxleafsize && child_node.depth < a_candidate.itemset.size()){
					turn_leaf_into_intermedite_node(child_node);
				}
			}
			
			//root_node.getChildren().get(hash_index).data_list.addElement(a_candidate);

		}
		return root_node;
	}
	
	
	public static void turn_leaf_into_intermedite_node(Node current_node){
    	//Node current_node = this;
		//System.out.println("IN turn_leaf_into_intermedite_node. DEPTH: " + current_node.depth);
		int depth_of_currentnode = current_node.depth;
		Iterator<itemsetnode> iter = current_node.data_list.iterator();
		itemsetnode a_candidate;

		while(iter.hasNext()){
			a_candidate = iter.next();
			//System.out.println("turn_leaf_into_inter_node-- hashing on : " + a_candidate.itemset.get(depth_of_currentnode));
			
			int hash_index = a_candidate.itemset.get(depth_of_currentnode) % hfrange;
			if (current_node.getChildren().get(hash_index) == null){// create the node child instance 1st time
				addChild(current_node, a_candidate, hash_index);
			}
			else{
				current_node.getChildren().get(hash_index).data_list.addElement(a_candidate);
				current_node.getChildren().get(hash_index).data_list_map.put(a_candidate.itemset, 0);
			}
		}
		current_node.node_type = 2;
		current_node.data_list = null;
		current_node.data_list_map = null;
		
	}
	
	//all item transactions traverse the candidate hashtree and updates their corresponding support count
	public static void tree_traversal(Vector<Integer> transaction, Node htnode, int recursion_depth, int k, Vector<Integer> fixed_set_index){
		//System.out.println("In tree traversal transaction: " + transaction);
		//System.out.println("In tree traversal r depth: " + recursion_depth);
		if (fixed_set_index == null){// first time
			fixed_set_index = new Vector<Integer>();
		}
		
		//base case: if the htnode is a bucket, then check  
		if (htnode.node_type == 1){
			Vector<Integer> check_set = new Vector<Integer>();
			for (int i=0; i<fixed_set_index.size(); i++){
				check_set.add(transaction.get(fixed_set_index.get(i)));
			}
			//System.out.println("1-------------------------check_set:  " +check_set);
			if (check_set.size() < k){
			Vector<Integer> loose_set = new Vector<Integer>(transaction.subList(fixed_set_index.lastElement()+1, transaction.size()));
			getSubsets(loose_set, k - check_set.size());
			Vector<Vector<Integer>> loose_sub = new Vector<Vector<Integer>>(subset);
			//System.out.println("1-------------------------loose_set:  " +loose_set);
			//System.out.println("1-------------------------loose_sub:  " +loose_sub);
			for (int j=0; j<loose_sub.size(); j++){
				int count=0;
				Iterator<Integer> it = loose_sub.get(j).iterator();
				while(it.hasNext()){
					Integer a = it.next();
					check_set.add(a);
					count++;
				}
					//check_set // got out first possible candidate from transaction
				//}  

					//System.out.println("222-------------------------check_set:  " +check_set);
					if(htnode.data_list_map.containsKey(check_set)){
						htnode.data_list_map.put(check_set, htnode.data_list_map.get(check_set) +1);
						//System.out.println("Updated the support value for check_set :" + check_set);
						itemsetnode candidate_element;
						Iterator<itemsetnode> iter = htnode.data_list.iterator();
						while(iter.hasNext()){
							candidate_element = (itemsetnode) iter.next();
							//System.out.println("-------------------------check_set:  " +check_set+ "candidate_element.itemset: " + candidate_element.itemset);
							if(check_set.equals(candidate_element.itemset)){
								//System.out.println("-------------------------Incrementing itemset support " + candidate_element.itemset);
								candidate_element.support++;
								break;
							}
						}
						/*itemsetnode candidate_element;
						Iterator<itemsetnode> iter = htnode.data_list.iterator();
						while(iter.hasNext()){
							candidate_element = (itemsetnode) iter.next();
							System.out.println("-------------------------check_set:  " +check_set+ "candidate_element.itemset: " + candidate_element.itemset);
							if(check_set.equals(candidate_element.itemset)){
								System.out.println("-------------------------Incrementing itemset support " + candidate_element.itemset);
								candidate_element.support++;
								break;
							}
						}*/
						}
					for(int n=0; n<count;n++)
						check_set.remove(check_set.size()-1);
					}
				}
			
			//}//end of if check_set<k
			else if (check_set.size()==k){
				//System.out.println("222-------------------------check_set:  " +check_set);
				if(htnode.data_list_map.containsKey(check_set)){
					htnode.data_list_map.put(check_set, htnode.data_list_map.get(check_set) +1);
					//System.out.println("Updated the support value for check_set :" + check_set);
					
					itemsetnode candidate_element;
					Iterator<itemsetnode> iter = htnode.data_list.iterator();
					while(iter.hasNext()){
						candidate_element = (itemsetnode) iter.next();
						//System.out.println("-------------------------check_set:  " +check_set+ "candidate_element.itemset: " + candidate_element.itemset);
						if(check_set.equals(candidate_element.itemset)){
							//System.out.println("-------------------------Incrementing itemset support " + candidate_element.itemset);
							candidate_element.support++;
							break;
						}
					
					}			
				/*itemsetnode candidate_element;
				Iterator<itemsetnode> iter = htnode.data_list.iterator();
				while(iter.hasNext()){
					candidate_element = (itemsetnode) iter.next();
					System.out.println("-------------------------check_set:  " +check_set+ "candidate_element.itemset: " + candidate_element.itemset);
					if(check_set.equals(candidate_element.itemset)){
						System.out.println("-------------------------Incrementing itemset support " + candidate_element.itemset);
						candidate_element.support++;
						break;
					}
				}*/
				
				}
			
			}
			return;
		}
		else{//recursive call
			for (int b = recursion_depth; b<(transaction.size() - k + 2) ;b++){
				if (htnode.children.get(transaction.elementAt(b) % hfrange)!= null){
					if (fixed_set_index.isEmpty()){
					fixed_set_index.add(b);
					//System.out.println(" k " +k+"     transaction.elementAt(b) : " + transaction.elementAt(b) + " fixed_Set_index " + fixed_set_index);
					tree_traversal(transaction, htnode.children.get(transaction.elementAt(b) % hfrange), recursion_depth+1, k, fixed_set_index);
					fixed_set_index.remove(fixed_set_index.size()-1);
					}
					else if (fixed_set_index.lastElement()<b){
						fixed_set_index.add(b);
						//System.out.println(" k " +k+"     transaction.elementAt(b) : " + transaction.elementAt(b) + " fixed_Set_index " + fixed_set_index);
						tree_traversal(transaction, htnode.children.get(transaction.elementAt(b) % hfrange), recursion_depth+1, k, fixed_set_index);
						fixed_set_index.remove(fixed_set_index.size()-1);
					}
				}
				
			}
			
		}
		
	}
	
	// function to generate subsets of size k
	static void genSubsets(Vector<Integer> array, boolean[] used, int startIndex, int currentSize, int k ){
		
		if(currentSize==k){
			Vector<Integer> a = new Vector<Integer>();
			for(int i=0; i<array.size(); i++){
				if (used[i]){
					a.add(array.get(i));
				}
			}
			subset.addElement(a);
			return;
		}
		if(startIndex==array.size())
			return;
		
		used[startIndex] = true;
		genSubsets(array, used, startIndex+1, currentSize + 1 , k );
		
		used[startIndex] = false;
		genSubsets(array, used, startIndex+1, currentSize , k );
	}
	
	static void getSubsets(Vector<Integer> array, int k){
		subset.removeAllElements();//resetting subset
		boolean[] used = new boolean[array.size()];
		genSubsets(array, used, 0, 0 , k);
	}

	
	
	
	
	static Vector<itemsetnode.rulesetnode> createcandidaterules(int rule_m, itemsetnode q){
		Vector<itemsetnode.rulesetnode> return_set = new Vector<itemsetnode.rulesetnode>();
		
		int elements_in_Fk_prev = q.high_conf_rules.get(rule_m-1).size();
		//System.out.println("--createcandidate():  elements_in_Fk_prev: " + elements_in_Fk_prev);
		Vector<Integer> cand1 = new Vector<Integer>();
		Vector<Integer> cand2 = new Vector<Integer>();
		Vector<Integer> cand1_lhs = new Vector<Integer>();
		boolean present = true;
		
		for (int i=0; i < elements_in_Fk_prev; i++){
			cand1 = q.high_conf_rules.get(rule_m-1).elementAt(i).rhs;
			cand1_lhs = q.high_conf_rules.get(rule_m-1).elementAt(i).lhs;
			//System.out.println("--createcandidaterule():  cand1: " + cand1);
			for (int j=i+1; j < elements_in_Fk_prev; j++){
				cand2 = q.high_conf_rules.get(rule_m-1).elementAt(j).rhs;
				//System.out.println("--createcandidaterule():  cand2: " + cand2);
				
				Vector<Integer> z = new Vector<Integer>();
				if (cand1.subList(0, cand1.size()-1).equals(cand2.subList(0, cand2.size()-1))){
					z.addAll(cand1);
					z.add(cand2.lastElement());
					//System.out.println("--createcandidaterule():  z: " + z);
					//here only generate the subsets of k-1 and check in large itemset if it exists. if all subsets exsist, then add to return set
					if (rule_m>=2){
						getSubsets(z, rule_m);// generate k-1 size subsets
						//Vector<Vector<Integer>> subset_copy = subset;
						Iterator<Vector<Integer>> itr = subset.iterator();
						Vector<Integer> p = null;
						boolean so_far_present = true;
						Vector<Integer> checklhs = null;
						while(itr.hasNext() && so_far_present){
							p = itr.next();
							//System.out.println("--createcandidate():  p: " + p);
							itemsetnode.rulesetnode r = null;
							checklhs = new Vector<Integer>();
							Iterator<Integer> it_r = q.itemset.iterator();
							Integer y = null;
							while(it_r.hasNext()){
								y = it_r.next();
								//System.out.println("--createcandidate():  r.itemset: " + r.itemset + "   p : " + p);
								if (!p.contains(y)){
									checklhs.add(y);
								}
							}
							double conf = ((double)q.support/(double)largeitemset_hash.get(checklhs));
							if(conf<minconf)	so_far_present = false;
						}
						if (so_far_present){
							//System.out.println("--createcandidate():  Adding new candidate now!!  candidate <z>: " + z);
							itemsetnode.rulesetnode new_itemset = q.new rulesetnode(checklhs,z);
							return_set.addElement(new_itemset);
						}
					}
					else{ // just add
						Integer l = new Integer(cand2.lastElement());
						cand1_lhs.remove(l);
						itemsetnode.rulesetnode new_itemset = q.new rulesetnode(cand1_lhs,z);
						return_set.addElement(new_itemset);
					}
				}
			}
		}
		//System.out.println("return set: " + return_set);
		return return_set;		
	}
	
	
	
	
	
	
	
	
	
	
	
}	

