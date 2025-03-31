DROP TABLE IF EXISTS Commits;
DROP TABLE IF EXISTS SATD;
DROP TABLE IF EXISTS SATDInFile;
DROP TABLE IF EXISTS Projects;

CREATE TABLE IF NOT EXISTS Projects (
	p_id INTEGER NOT NULL,
    p_name TEXT NOT NULL UNIQUE,
    p_url TEXT NOT NULL UNIQUE,
    PRIMARY KEY (p_id)
);

CREATE TABLE IF NOT EXISTS SATDInFile (
	f_id INTEGER,
    f_comment TEXT,
    f_comment_type TEXT,
    f_path TEXT,
    start_line INT,
    end_line INT,
    containing_class TEXT,
    containing_method TEXT,
    method_declaration TEXT,
    method_body TEXT,
    "type" TEXT,
    PRIMARY KEY (f_id)
);

CREATE TABLE IF NOT EXISTS Commits(
	commit_hash TEXT,
    p_id INT,
    author_name TEXT,
    author_email TEXT,
    author_date TEXT,
    committer_name TEXT,
    committer_email TEXT,
    commit_date TEXT,
    PRIMARY KEY (p_id, commit_hash),
    FOREIGN KEY (p_id) REFERENCES Projects(p_id)
);

CREATE TABLE IF NOT EXISTS SATD (
	satd_id INTEGER,
    satd_instance_id INT, -- Not a key value, used only to associate SATD Instances
    parent_instance_id INT,
    p_id INT,
	first_commit TEXT,
    second_commit TEXT,
    first_file INT,
    second_file INT,
    resolution TEXT,
    PRIMARY KEY (satd_id),
    FOREIGN KEY (p_id) REFERENCES Projects(p_id),
    FOREIGN KEY (p_id, first_commit) REFERENCES Commits(p_id, commit_hash),
    FOREIGN KEY (p_id, second_commit) REFERENCES Commits(p_id, commit_hash),
    FOREIGN KEY (first_file) REFERENCES SATDInFile(f_id),
    FOREIGN KEY (second_file) REFERENCES SATDInFile(f_id)
); 

CREATE TABLE IF NOT EXISTS RefactoringsRmv (
    refactoringID INTEGER NOT NULL,
    commit_hash TEXT NOT NULL,
    projectID INTEGER,
    type TEXT,
    description TEXT,
    PRIMARY KEY (refactoringID),
    UNIQUE (refactoringID)
);

CREATE INDEX IF NOT EXISTS commit_hash_idx ON RefactoringsRmv (commit_hash);

CREATE INDEX IF NOT EXISTS commit_hash_projectID_idx ON RefactoringsRmv (commit_hash, projectID);


CREATE TABLE IF NOT EXISTS AfterRefactoring (
    afterID INTEGER NOT NULL,
    refID INTEGER NOT NULL,
    filePath TEXT,
    startLine INTEGER,
    endLine INTEGER,
    startColumn INTEGER,
    endColumn INTEGER,
    description TEXT,
    codeElement TEXT,
    PRIMARY KEY (afterID),
    FOREIGN KEY (refID) REFERENCES RefactoringsRmv (refactoringID) 
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idRefactorings_idx ON AfterRefactoring (refID);


CREATE TABLE IF NOT EXISTS  BeforeRefactoring (
  "beforeID" INTEGER NOT NULL,
  "refactoringID" INTEGER NOT NULL,
  "filePath" mediumtext,
  "startLine" INTEGER,
  "endLine" INTEGER,
  "startColumn" INTEGER,
  "endColumn" INTEGER,
  "description" mediumtext,
  "codeElement" mediumtext,
  PRIMARY KEY ("beforeID")
);
