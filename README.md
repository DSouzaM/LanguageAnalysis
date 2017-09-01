# LanguageAnalysis

I wanted to build something in Scala, but didn't know what people typically used Scala for. GitHub tags repositories by the primary language, so I had the idea to try and cluster programming languages based on keywords in repositories' README's - in Scala. This project could be divided into two core components: extraction and analysis. It's a bit of a mess, because I originally was working in Scala and decided to switch to Python when I started working on the analysis component.

#### Extraction
1. I created a [Scala wrapper](https://github.com/DSouzaM/github-api-wrapper) for the GitHub API.
2. Using [Slick](http://slick.lightbend.com/), I wrote an incremental extraction system to fetch results from the API.

#### Analysis
1. I made a simple Tokenizer that used the [Java Wiktionary Library](https://dkpro.github.io/dkpro-jwktl/) to filter words by part of speech.
2. I implemented the [TextRank](https://web.eecs.umich.edu/~mihalcea/papers/mihalcea.emnlp04.pdf) algorithm to extract keywords. This algorithm proved to be much slower than [tf-idf](https://en.wikipedia.org/wiki/Tf%E2%80%93idf), so I ended up not using it.
3. At this point, I decided to switch to Python to take advantage of the libraries it already had. I used [nltk](http://www.nltk.org/) for tokenizing, and [scikit-learn](http://scikit-learn.org/) to vectorize repositories with tf-idf and then cluster using k-means. I played around with the parameters to improve the results from clustering. Below is a sample, listing the top ten stems for each cluster along with the number of repositories for each language in each cluster. Some clusters were a lot more defined than others (e.g. the last cluster is clearly about machine learning).
![Clustering results on GitHub repository data](https://github.com/DSouzaM/LanguageAnalysis/blob/master/examples/github-example.PNG)
