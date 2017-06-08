from nltk.tokenize import word_tokenize
from nltk.stem.porter import PorterStemmer
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy
import time


class StemTokenizer(object):
  def __init__(self):
    self.stemmer = PorterStemmer()
  def __call__(self, string):
    return [self.stemmer.stem(token) for token in word_tokenize(string) if is_alpha(token) and len(token) > 3]
    

class AlphaTokenizer(object):
  def __call__(self, string):  
    return [token for token in word_tokenize(string) if is_alpha(token) and len(token) > 3]


# Checks that a word is ascii - stemmer expects ascii input
def is_ascii(word): 
  return all(ord(c) < 128 for c in word)


# Checks that a word consists only of alphanumeric characters
def is_alpha(word):
  return all(ord(c) >= 97 and ord(c) <= 122 for c in word)



def generate_features(documents, vectorizer):
  start_time = time.time()
  print"Generating tf-idf feature vectors for documents..."

  feature_vectors = vectorizer.fit_transform(documents)
  print "Feature vectors generated in {0:.2f} seconds.".format(time.time() - start_time)
  return feature_vectors


def cluster(feature_vectors, kmeans):
  start_time = time.time()
  print "Performing k-means clustering on feature vectors..."

  cluster_results = kmeans.fit_transform(feature_vectors)
  print "Clustering completed in {0:.2f} seconds.".format(time.time() - start_time)
  return cluster_results



def compute(documents, n_clusters=8, max_features=300, max_df=0.8, min_df=2):
  # Feature vector generation
  vectorizer = TfidfVectorizer(stop_words="english", tokenizer=StemTokenizer(), max_features=max_features, max_df=max_df, min_df=min_df)
  feature_vectors = generate_features(documents, vectorizer)

  # K-means clustering
  kmeans = KMeans(n_clusters=n_clusters)
  cluster_results = cluster(feature_vectors, kmeans)
  print "Silhouette score for data: {0:.4f}".format(silhouette_score(feature_vectors, kmeans.labels_))
  return (vectorizer, feature_vectors, kmeans, cluster_results) 
