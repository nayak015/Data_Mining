__author__ = 'ankitnayak'

import os
import re
import sys
import csv
import string
import operator
from bs4 import BeautifulSoup
from datetime import datetime


def init_document():
	document = { 'topics' : [], 'newid' : [], 'body' : [], 'freq_vec': dict([]), 'freq_vec_ngram': dict([]) }
	return document

def populate_document_contents(document,article, count_topic):
	for topic in article.topics.children:
		document['topics'].append(topic.text.encode('ascii', 'ignore'))
		if count_topic.get(topic.text.encode('ascii', 'ignore'), False) :
			count_topic[topic.text.encode('ascii', 'ignore')] = count_topic[topic.text.encode('ascii', 'ignore')] + 1;
		else:
			count_topic[topic.text.encode('ascii', 'ignore')] = 1 ;

def tokenize(text):
	# encode unicode to string
	ascii = text.encode('ascii', 'ignore')
	#replacing nonalphanumeric with space
	ascii = re.sub('[^0-9a-zA-Z]+', ' ', ascii)
	# remove digits
	no_digits = ascii.translate(None, string.digits)
	# tokenize
	tokens = no_digits.split();
	return tokens;

def populate_body(document, article):
	text = article.find('text');
	body = text.body;
	if body != None:
		document['body'] = tokenize(body.text);
	#for x in document['body']:
		#if (document['body_words'].get(x), False):
			#document['body_words'][x] = document['body_words'][x] +1;
		#else:
			#document['body_words'][x] = 1;
			

def generate_document(text, newid, count_topic):
	document = init_document()
	document["newid"].append(newid);
	populate_document_contents(document, text, count_topic)
	populate_body(document, text); 	
	return document

def generate_tree(text):
	return BeautifulSoup(text, "html.parser")

def parse_documents():
	count_topic = {};
	documents = [];
	top_20_documents = [];
	top_20_topics = [];
    # generate well-formatted document set for each file
	for file in os.listdir('reuters21578'):
        # open 'reut2-XXX.sgm' file from /data directory
		if (file.endswith(".sgm")):
			print "processing file: ", file;
			data = open(os.path.join(os.getcwd(), "reuters21578", file), 'r')
			text = data.read()
			data.close()
			tree = generate_tree(text.lower()); 
			# separate segments & generate documents
			count = 0;
			for reuter in tree.find_all("reuters"):
				#DEBUGGING
				#print reuter
				newid = int(reuter.get("newid"))
				#FUTURE check if it only has one topic here len(topics)==1
				if len(reuter.topics) == 1:
					document = generate_document(reuter, newid, count_topic)
					documents.append(document)
			print "Finished extracting information from file:", file
			
	# here we have extracted the single topic body, newid and topic
	#print len(sorted(count_topic))
	#print sorted(count_topic)
	
	sorted_count_topic = sorted(count_topic.items(), key=operator.itemgetter(1))
	count = 20;
	for x in range(1,21):
		top_20_topics.append(sorted_count_topic[-x][0])
	print top_20_topics
	print "length of top 20 topics:", len(top_20_topics)
	
	for x in documents:
		if(x["topics"][0] in top_20_topics):
			top_20_documents.append(x);

	return top_20_documents;


def create_bag_clabel(feature_labels_copy):
	f = open("bag.clabel", 'wt')
	try:
		writer = csv.writer(f)
		for x in feature_labels_copy:
			writer.writerow([x,]);
	finally:
		f.close()

	#print open("bag.clabel", 'rt').read();

def create_ngram_clabel(n,feature_labels_copy):
	filename = "char"+str(n)+".clabel"
	f = open(filename, 'wt')
	try:
		writer = csv.writer(f)
		for x in feature_labels_copy:
			writer.writerow([x,]);
	finally:
		f.close();

def main(argv):
	tstart = datetime.now();
    # generate list of top 20 topics document objects for feature selection
	print('Generating document objects. This will take some time')
	documents = parse_documents()
	'''
	for x in documents:
		print "newid:  ", x['newid'];
		print "topics:  ", x['topics'];
		print "body:  ", x['body'];
	'''
	print "Total number of documents with top 20 single topics: ",  len(documents);
	
	#print "Body" , documents[0]['newid']
	#print documents[0]['body']
	
	
	feature_labels = [];
	freq_vector_bagofwords = {};
	for x in documents:
		
		for token in x['body']:
			if(token not in freq_vector_bagofwords):
				freq_vector_bagofwords[token] = 0;
				#feature_labels.append(token);	

	#print freq_vector_bagofwords;

	for x in documents:
		x['freq_vec'] = freq_vector_bagofwords.copy();
		for token in x['body']:
			x['freq_vec'][token] = x['freq_vec'][token] + 1
			
	#print documents[0]['freq_vec'];
	
	for x in documents:
		for token in x['body']:
			freq_vector_bagofwords[token] = freq_vector_bagofwords[token] +1;
			
	#print freq_vector_bagofwords;
	
	#for x in list(freq_vector_bagofwords.keys()):
		#for y in documents:
			#if x in y['body']:
				#freq_vector_bagofwords[x] = freq_vector_bagofwords[x] +1
	
	
	for x in documents:
		#print ("length of doc vector before:", len(x['freq_vec']))
		for token in list(freq_vector_bagofwords.keys()):
			if (freq_vector_bagofwords[token] < 5):
				del x['freq_vec'][token]
	
			
	
	for token in list(freq_vector_bagofwords.keys()):
			if (freq_vector_bagofwords[token] >= 5):
				feature_labels.append(token)
		
		#print ("length of doc vector after:", len(x['freq_vec']))
		
		
	
	classfile = open("reuters21578.class", 'wt')	
	try:
		writer = csv.writer(classfile)
		for x in documents:
			if(len(x['body']) !=0):
				writer.writerow([x['newid'][0], x['topics'][0]]);
	finally:
		classfile.close()
	
	print("size of dimen: ")
	print len(feature_labels);
	
	#test
	#print documents[0]
	
	#print feature_labels;

	create_bag_clabel(feature_labels);
	
	#create_bag_csv
	f = open("bag.csv", 'wt')
	try:
		writer = csv.writer(f)
		for x in documents:
			for y in feature_labels:
				if x['freq_vec'][y] is not 0:
				#print(y)
					dimension_no = feature_labels.index(y);
					writer.writerow([x['newid'][0], dimension_no, x['freq_vec'][y]]);
	finally:
		f.close()
		
	
	#n-grams

	for n in [3, 5, 7]:
		print "Starting " + str(n) + "-grams" 	
		feature_labels = [];
		mapfeature_labels = {};
		freq_vector_ngrams = {};

		for x in documents:
			if(len(x['body']) !=0):
				x['freq_vec_ngram'] = {}
				body_string = "";
				for word in x['body']:
					if word in x['freq_vec']:
						body_string += str(word)
						body_string += str(" ")
				body_string = body_string[:-1]
				#print body_string
				body_list = [body_string[i:i+n] for i in range(len(body_string)-n+1)]
				
				#print body_list;
				
				for token in body_list:
					if token in x['freq_vec_ngram']:
						x['freq_vec_ngram'][token] = x["freq_vec_ngram"][token] + 1
					else:
						x['freq_vec_ngram'][token] = 1
						if token not in freq_vector_ngrams:
							freq_vector_ngrams[token] = 0;
							mapfeature_labels[token] = len(feature_labels)
							feature_labels.append(token);
							
				
			#print "DOne here-------------------------"
		
		create_ngram_clabel(n, feature_labels);
		
		filename = "char"+str(n)+".csv"
		
		print "printing to " , filename
		f_ngram = open(filename, 'wt')
		try:
			writer = csv.writer(f_ngram)
			for x in documents:
				for y in list(x['freq_vec_ngram'].keys()):
					dimension_no = mapfeature_labels[y]
					writer.writerow([x['newid'][0], dimension_no, x['freq_vec_ngram'][y]]);
		finally:
			f.close()
	

	tend = datetime.now()
	
	print "Total time: " , (tend - tstart).seconds , "secs"

if __name__ == "__main__":
    main(sys.argv[1:])
