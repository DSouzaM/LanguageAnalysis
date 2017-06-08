import MySQLdb
import time
import re
from kmeans import compute

# Creates database connnection
def get_db():
  return MySQLdb.connect(user="root",passwd="password",db="main")


# SQL query string to select all repositories
def get_repos():
  return """SELECT NAME, ID, README FROM repositories LIMIT 1000"""

def clean_string(string):
  pat = re.compile("[^\x00-\x7F]+", re.UNICODE)
  return pat.sub("", string)



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
for name, repo_id, readme in cursor:

  repo_count += 1
  repos.append({
    "name" : name,
    "id" : repo_id
  })
  readmes.append(clean_string(readme))

print "Input cleaned in {0:.2f} seconds.".format(time.time() - start_time)

vectorizer, feature_vectors, kmeans, cluster_results = compute(readmes)