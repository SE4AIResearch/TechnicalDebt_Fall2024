package edu.rit.se.git;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

/**
 * Initializes a Git Repository. This includes cloning it locally and
 * locating unknown commits within it.
 */
public class RepositoryInitializer {

    @Getter
    @NonNull
    private String repoDir;
    @NonNull
    private String gitURI;

    @NonNull
    private String gitUsername = "u";
    @NonNull
    private String gitPassword = "p";

    private Git repoRef = null;
    private Boolean gitDidInit = false;

    public RepositoryInitializer(String uri, String baseName) {
        this(uri, baseName, "u", "p");
    }

    public RepositoryInitializer(String uri, String baseName, String gitUsername, String gitPassword) {
        String homePath = System.getProperty("user.home");
        File reposBaseDir = new File(homePath, ".technical_debt/repos");
        if (!reposBaseDir.exists()) {
            boolean created = reposBaseDir.mkdirs();
            if (created) {
                System.out.println("Created repos directory at: " + reposBaseDir.getAbsolutePath());
            }
        }
        this.repoDir = new File(reposBaseDir, baseName).getAbsolutePath();
        this.gitURI = uri;
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
    }

    public boolean initRepo() {
        final File newGitRepo = new File(this.repoDir);
        if (newGitRepo.exists()) {
            this.cleanRepo();
        }
        newGitRepo.mkdirs();
        try {
            this.repoRef = Git.cloneRepository()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.gitUsername, this.gitPassword))
                    .setURI(this.gitURI)
                    .setDirectory(newGitRepo)
                    .setCloneAllBranches(false)
                    .call();
            this.repoRef.getRepository().getConfig().setString("remote", "origin", "url", this.gitURI);
            this.repoRef.getRepository().getConfig().save();
            this.gitDidInit = true;
        } catch (GitAPIException e) {
            System.err.println("\nGit API error in git init: " + e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println("\nIOException when setting remote in new repo.");
        }
        return this.gitDidInit;
    }

    public RepositoryCommitReference getMostRecentCommit(String head) {
        final RevWalk revWalk = new RevWalk(this.repoRef.getRepository());
        try {
            return new RepositoryCommitReference(
                    this.repoRef,
                    GitUtil.getRepoNameFromGithubURI(this.gitURI),
                    this.gitURI,
                    revWalk.parseCommit(this.repoRef.getRepository().resolve(
                            head != null ? head : Constants.HEAD))
            );
        } catch (IOException e) {
            System.err.println("\nCould not parse the supplied diff for the repository: " + head);
        }
        return null;
    }

    public void cleanRepo() {
        if (this.repoRef != null) {
            this.repoRef.getRepository().close();
        }
        File repo = new File(this.repoDir);
        try {
            FileUtils.deleteDirectory(repo);
        } catch (IOException e) {
            System.err.println("\nError deleting git repo");
        }
    }

    public boolean didInitialize() {
        return this.gitDidInit;
    }
}
