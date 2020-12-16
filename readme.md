## Table of Contents
1. [ General Info ](#info)
2. [ How to build SatdBailiff ](#build)
3. [ How to run SatdBailiff ](#run)
4. [ Results ](#results)
5. [ SatdBailiff Extension ](#extention)
6. [ Contributors ](#contributors)


<a name="info"></a>
## General Info  
#### Self-Admitted Technical Debt (SATD) Analyzer
This tool is intended to be used to mine SATD occurrences from
github repositories as a single or batch operation. 


#### What is SATD?

Self-Admitted Technical Debt is a candid form of technical debt in which the contributor of the debt self-documents the location of the debt. This admission is typically accompanied with a description of a knownor potential defect or a statement detailing what remaining work must be done.  Well-known and frequently used examples of SATD include comments beginning with TODO, FIXME, BUG, XXX, or HACK. SATD can also take other forms of more complex language void of any of the previously mentioned keywords.  Any comment  detailing  a not-quite-right implementation  present  in  the  surrounding  code  can  be  classified  as SATD.

#### Prerequisites
**The following versions are required to run the tool:**
* Java 1.8+ *

* MySql 5.4+

If building the tool from source:
* Maven 3


<sub>*If you are using openjdk then version 11 is required as javafx.util is missing from openjdk8 <sub>

#### Additional info
- Satd bailiff jar file v1.2 can be found [here](https://github.com/smilevo/SATDBailiff/releases/download/1.2/satd-analyzer-jar-with-all-dependencies.jar)
- A video tutorial on how to build and run the tool can be found [here](https://www.youtube.com/watch?v=yf0cvyvaY94&feature=youtu.be) 
- A docker image with all the required libraries, the tool as well as a complete environment is prepared and available [here](https://hub.docker.com/r/mihalbsh/satdbailiff). 
You can use that in case you dont have an environment with java,mysql etc

<a name="build"></a>
## Build SatdBailiff
This section has information on how to build the tool from the source code. If you plan to just run the 
prebuild jar file skip this section.

In order to build the jar file from source code:

1) Clone this Repo. 
2) In the project's root directory create a lib folder and place the satd classifier  [jar file](https://github.com/Tbabm/SATDDetector-Core/releases/download/v0.1/satd_detector.jar) *
3) In the project's root directory run `mvn clean package`. 
4) After the build has finished you should have the jar file with all the dependencies under the folder called target. The name of the build file should be called something like *satd-analyzer-jar-with-all-dependencies.jar*
5) That's it! This jar file can now be used to analyze java projects. 

<sub>* This project uses the implementation of another project (https://github.com/Tbabm/SATDDetector-Core) for SATD 
classification. A `.jar` of the linked project must be present in `lib/` in order for
this project to run. It should be noted, that the SATD classification model included
in that repository's released binaries differs from the model released with
this project's binaries. To use a different model in your own implementation,
follow the instructions in the aforementioned repository's readme,
and include the model files in `lib/models/`.<sub>
 

<a name="run"></a>
## Run SatdBailiff
To run satdBailiff you will need the jar file (either directly downloading the prebuild jar file or building it from source code).

#### Setting up the database
Before the tool can output any data, a mySQL server must be active to
receive the output data. The schema for the expected output can be found
in [sql/satd.sql](sql/satd.sql). You should create a database (named satd) then run this sql script to create all of the required tables.
After the script is executed you should have a database named satd with all of the required tables (Projects, Commits, SATD etc).

A `.properties` file is used to configure the
tool to connect to the database. The repository contains a
sample [.properties](mySQL.properties) file. The supplied 
`.properties` file should contain **all** the same fields. Extra fields will
be ignored. (Use your own mysql credentials and/or settings)

#### Select Projects 
The repositories of the projects to be analyzed must be included in a `CSV file`. The first value is the url of the github repository and the second value
is the hash of the commit where the mining ends (**Not required**. If no starting point given all of the commits are going to be analyzed from first commit to last). 

Example of csv

https://github.com/apache/log4j , 94eff9a041300970516ea866f8f0420d1cc75355

https://github.com/apache/tomcat , 


#### Running the .JAR
The tool has one functionality -- mining SATD occurrences as a single
or batch operation. The tool comes packaged with a Command Line Interface
for ease of use, so it can be run like so:

```
java -jar <file.jar> -r <repository_file> -d <database_properties_file>
```

The help menu output is as follows.

```
usage: satd-analyzer
 -a,--diff-algorithm <ALGORITHM>   the algorithm to use for diffing (Must
                                   be supported by JGit):
                                   - MYERS (default)
                                   - HISTOGRAM
 -d,--db-props <FILE>              .properties file containing database
                                   properties
 -e,--show-errors                  shows errors in output
 -h,--help                         display help menu
 -i,--ignore <WORDS>               a text file containing words to ignore.
                                   Comments containing any word in the
                                   text file will be ignored
 -l,--n_levenshtein <0.0-1.0>      the normalized levenshtein distance
                                   threshold which determines what
                                   similarity must be met to qualify SATD
                                   instances as changed
 -p,--password <PASSWORD>          password for Github authentication
 -r,--repos <FILE>                 new-line separated file containing git
                                   repositories
 -u,--username <USERNAME>          username for Github authentication
```

Example
```
java -jar satd-analyzer-jar-with-all-dependencies.jar -r maldonado_study.csv -d mySQL.properties
```

<a name="results"></a>
## Results
After the tool has finished running you should have a `reports` folder. Within it a csv file with the most important results as well as an html file with those results visualized.
In addition, all of the results are also saved in the `database`. The database has all of the information stored in it so if you need additional information you can run mysql queries to retrieve different types of data.

<a name="extention"></a>
## Tool Extension
The tool is extented to be able to detect `design` satd and mine refactorings on commits that a design satd is removed.
This way we can study the corellation between the satd text and the type of refactorings. If the satd was removed because a refactoring operation occured that fixed the issue described in the satd comment.

To classify the removed satd as design or no an azure binary classifier is used. The tool makes an API request with the removed satd comment text and 
gets a classification as a response. The [my_api_keyes.csv](my_api_keyes.csv) contains the credentials needed to make the api request.

Finally, the tool uses [refactoring miner](https://github.com/tsantalis/RefactoringMiner) to mine refactorings on commits that design satd were removed.

<a name="contributors"></a>
## Contributors
- [Ben Christians](https://github.com/bbchristians)
- [Mohamed Wiem Mkaouer](https://github.com/mkaouer)
- [Mihal Busho](https://github.com/michaelbusho)
- [Ahmed Alkhalid](https://github.com/ahalk1)
