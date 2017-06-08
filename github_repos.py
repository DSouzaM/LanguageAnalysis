import MySQLdb
import time
import re
from kmeans import compute


# Creates database connnection
def get_db():
  return MySQLdb.connect(user="root",passwd="password",db="main")


# SQL query string to select all repositories
def get_repos():
  return """SELECT r.NAME, r.ID, r.README, l.NAME
  FROM repositories r
  INNER JOIN repository_languages rl on r.ID = rl.REPOSITORY_ID
  INNER JOIN languages l on rl.LANGUAGE_ID = l.ID
  WHERE rl.IS_PRIMARY=1
  ORDER BY RAND()
  LIMIT 10000"""

def clean_string(string):
  pat = re.compile("[^\x00-\x7F]+", re.UNICODE)
  return pat.sub("", string)

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


# Grab repository data
print "Fetching repository data..."
db = get_db()
cursor = db.cursor()
cursor.execute(get_repos())

repo_count = 0
start_time = time.time()

repos = []
readmes = []

# Initial processing
print "Cleaning input..."
for name, repo_id, readme, lang in cursor:

  repo_count += 1
  repos.append({
    "name" : name,
    "id" : repo_id,
    "language" : lang
  })
  readmes.append(clean_string(readme))

print "Input cleaned in {0:.2f} seconds.".format(time.time() - start_time)



vectorizer, feature_vectors, kmeans, cluster_results = compute(readmes, n_clusters=12, max_features=3000, max_df=0.1)

cluster_languages = []
for i in range(kmeans.cluster_centers_.shape[0]): # initialize an empty dict for each cluster
  cluster_languages.append({})

# compute language counts for each cluster
for i in range(len(kmeans.labels_)):
	lang = repos[i]["language"]
	cluster_id = kmeans.labels_[i]
	if lang not in cluster_languages[cluster_id]:
		cluster_languages[cluster_id][lang] = 0
	cluster_languages[cluster_id][lang] += 1

print_results(kmeans, vectorizer, cluster_languages)