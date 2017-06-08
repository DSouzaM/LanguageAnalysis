from kmeans import compute
import time
import csv

# Reads in a sample of StackOverflow data (from Kaggle dataset: https://www.kaggle.com/stackoverflow/stacksample)
# Assumes data located in ./res/
# Runs k-means algorithm on question bodies in an attempt to cluster by keywords

LANGUAGES = ['ruby', 'c', 'shell', 'c#', 'javascript', 'c++', 'scala', 'php', 'python', 'java'] # set of top languages to look up
SAMPLE_SIZE = 100 # questions to use for each language

language_questions = {} # maintain a dict from question id to language; if id in dict, it has one of desired languages as tag

print "Parsing tags to find relevant questions..."

with open("res/Tags.csv", "r") as f:
  csvfile = csv.reader(f)
  for question_id, tag in csvfile:
    if tag in LANGUAGES:
      language_questions[question_id] = tag


# Iterate through questions csv, grab SAMPLE_SIZE questions per language
print "Parsing questions..."
start_time = time.time()
questions_remaining = {}
for language in LANGUAGES:
	questions_remaining[language] = SAMPLE_SIZE


questions = [] # list of question dicts with id, language info
question_bodies = [] # list of question bodies

with open("res/Questions.csv", "r") as f:
	csvfile = csv.reader(f)
	for row in csvfile:
		q_id = row[0]
		if q_id in language_questions and questions_remaining[language_questions[q_id]] > 0: # this question has a tag we want
			body = row[6]
			lang = language_questions[q_id]
			questions.append({
				  "id" : q_id,
				  "tag": lang
				})
			question_bodies.append(body)

			questions_remaining[lang] -= 1

			if sum(questions_remaining.values()) == 0:
				break

print "Done in {0:.2f} seconds.".format(time.time() - start_time)

vectorizer, feature_vectors, kmeans, cluster_results = compute(question_bodies)
