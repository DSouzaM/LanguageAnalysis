# LanguageAnalysis

##### Required libraries (add the jars to `lib` folder and SBT does the rest):
1. [GitHub API Wrapper][1] (included)
2. [JWKTL][2], a library to interface with the Wiktionary dump ([download][3])
3. [Oracle Berkeley DB Java Edition][4], a dependency for JWKTL ([download][5])
4. [bzip2][6], a dependency for JWKTL ([download][7])


##### Wiktionary dump setup:
1. Download `enwiktionary-latest-pages-articles.xml.bz2` from the [Wiktionary dumps][8] (it's ~600MB compressed)
2. Extract it to a `res` folder (it's ~6GB decompressed)
3. Run `com.dsouzam.language_analysis.Initialize`
  - This will parse the xml into a Berkeley DB at `res/wiktionary` so it can be queried by JWKTL. This step can take a while.
4. (optional) Delete `enwiktionary-latest-pages-articles.xml` from the `res` folder

[1]: https://github.com/DSouzaM/github-api-wrapper
[2]: https://dkpro.github.io/dkpro-jwktl/documentation/getting-started/
[3]: https://search.maven.org/#search|ga|1|a%3A%22dkpro-jwktl%22
[4]: http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/overview/index-093405.html
[5]: http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/downloads/index.html
[6]: http://www.kohsuke.org/bzip2/
[7]: http://www.kohsuke.org/bzip2/bzip2.jar
[8]: https://dumps.wikimedia.org/enwiktionary/latest/
