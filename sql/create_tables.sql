CREATE TABLE IF NOT EXISTS Projects (
	p_id INTEGER NOT NULL,
    p_name TEXT(255) NOT NULL UNIQUE,
    p_url TEXT(255) NOT NULL UNIQUE,
    PRIMARY KEY (p_id)
);

CREATE TABLE IF NOT EXISTS SATDInFile (
	f_id INTEGER,
    f_comment TEXT(4096),
    f_comment_type TEXT(32),
    f_path TEXT(512),
    start_line INT,
    end_line INT,
    containing_class TEXT(512),
    containing_method TEXT(512),
    method_declaration TEXT,
    method_body TEXT,
    `type` TEXT(45),
    PRIMARY KEY (f_id)
);

CREATE TABLE IF NOT EXISTS Commits(
	commit_hash TEXT(256),
    p_id INT,
    author_name TEXT(256),
    author_email TEXT(256),
    author_date TEXT,
    committer_name TEXT(256),
    committer_email TEXT(256),
    commit_date TEXT,
    PRIMARY KEY (p_id, commit_hash)
    FOREIGN KEY (p_id) REFERENCES Projects(p_id)
);

CREATE TABLE IF NOT EXISTS SATD (
	satd_id INTEGER,
    satd_instance_id INT, 
    parent_instance_id INT,
    p_id INT,
	first_commit TEXT(256),
    second_commit TEXT(256),
    first_file INT,
    second_file INT,
    resolution TEXT(64),
    PRIMARY KEY (satd_id),
    FOREIGN KEY (p_id) REFERENCES Projects(p_id),
    FOREIGN KEY (first_commit) REFERENCES Commits(commit_hash),
    FOREIGN KEY (second_commit) REFERENCES Commits(commit_hash),
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


CREATE TABLE IF NOT EXISTS BeforeRefactoring (
  `beforeID` INTEGER PRIMARY KEY AUTOINCREMENT,
  `refactoringID` INTEGER NOT NULL,
  `filePath` TEXT,
  `startLine` INTEGER,
  `endLine` INTEGER,
  `startColumn` INTEGER,
  `endColumn` INTEGER,
  `description` TEXT,
  `codeElement` TEXT,
  FOREIGN KEY (`refactoringID`) REFERENCES RefactoringsRmv(`refactoringID`) ON DELETE CASCADE
);
