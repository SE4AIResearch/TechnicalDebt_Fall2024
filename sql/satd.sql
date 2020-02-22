DROP TABLE IF EXISTS satd.CommitMetaData, satd.Commits, satd.SATD, satd.SATDInFile, satd.Tags, satd.Projects;

CREATE TABLE IF NOT EXISTS satd.Projects (
	p_id INT AUTO_INCREMENT,
    p_name VARCHAR(255) UNIQUE,
    p_url VARCHAR(255) UNIQUE,
    PRIMARY KEY (p_id)
);

CREATE TABLE IF NOT EXISTS satd.Tags (
	t_id INT AUTO_INCREMENT,
	tag VARCHAR(255),
    p_id INT,
    PRIMARY KEY (t_id),
    FOREIGN KEY (p_id) REFERENCES satd.Projects(p_id)
);

CREATE TABLE IF NOT EXISTS satd.SATDInFile (
	f_id INT AUTO_INCREMENT,
    f_comment BLOB,
    f_path VARCHAR(256),
    start_line INT,
    end_line INT,
    PRIMARY KEY (f_id)
);

CREATE TABLE IF NOT EXISTS satd.SATD (
	satd_id INT,
	first_tag_id INT,
    second_tag_id INT,
    first_file INT,
    second_file INT,
    resolution VARCHAR(64),
    PRIMARY KEY (satd_id),
    FOREIGN KEY (first_tag_id) REFERENCES satd.Tags(t_id),
    FOREIGN KEY (second_tag_id) REFERENCES satd.Tags(t_id),
    FOREIGN KEY (first_file) REFERENCES satd.SATDInFile(f_id),
    FOREIGN KEY (second_file) REFERENCES satd.SATDInFile(f_id)
);

CREATE TABLE IF NOT EXISTS satd.CommitMetaData(
	commit_hash varchar(256),
    # TODO add more metadata
    PRIMARY KEY (commit_hash)
);

CREATE TABLE IF NOT EXISTS satd.Commits (
	satd_id INT,
    commit_hash varchar(256),
    commit_type ENUM('BEFORE', 'BETWEEN', 'AFTER'),
    FOREIGN KEY (satd_id) REFERENCES satd.SATD(satd_id),
    FOREIGN KEY (commit_hash) REFERENCES satd.CommitMetaData(commit_hash)
);  