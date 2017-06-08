from kmeans import compute
import time
import csv


def print_results(kmeans, vectorizer, cluster_languages):
  keywords = vectorizer.get_feature_names()

  cluster_centers = kmeans.cluster_centers_

  # for each cluster, generate the indices of the top words in descending order
  top_keyword_idxs = cluster_centers.argsort()[:, ::-1]

  for cluster_idx in range(len(top_keyword_idxs)):
    cluster_keyword_idxs = top_keyword_idxs[cluster_idx]
    languages = cluster_languages[cluster_idx].items()
    languages.sort(key=lambda x: x[1], reverse=True)

    print "Cluster {0}:".format(cluster_idx)
    print languages
    for idx in cluster_keyword_idxs[:10]:
      print keywords[idx],
    print



# Reads in a sample of StackOverflow data (from Kaggle dataset: https://www.kaggle.com/stackoverflow/stacksample)
# Assumes data located in ./res/
# Runs k-means algorithm on question bodies in an attempt to cluster by keywords

LANGUAGES = ['ruby', 'c', 'shell', 'c#', 'javascript', 'c++', 'scala', 'php', 'python', 'java'] # set of top languages to look up
SAMPLE_SIZE = 2000 # questions to use for each language

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

vectorizer, feature_vectors, kmeans, cluster_results = compute(question_bodies, n_clusters=12, max_features=3000, max_df=0.1, min_df=3)


cluster_languages = []
for i in range(kmeans.cluster_centers_.shape[0]): # initialize an empty dict for each cluster
  cluster_languages.append({})

# compute language counts for each cluster
for i in range(len(kmeans.labels_)):
	lang = questions[i]["tag"]
	cluster_id = kmeans.labels_[i]
	if lang not in cluster_languages[cluster_id]:
		cluster_languages[cluster_id][lang] = 0
	cluster_languages[cluster_id][lang] += 1

print_results(kmeans, vectorizer, cluster_languages)