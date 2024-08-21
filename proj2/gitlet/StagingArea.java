package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages the staging area for Gitlet. This includes tracking changes to files
 * that are staged for addition or removal and generating commits based on these changes.
 */
public class StagingArea {
    private Index stagingArea;
    private Tree tree;
    private String perCommit;
    private Head head;
    private Branch branch;
    // 1: changed; -1:removed;
    private HashMap<String, Integer> trackedFiles;

    /**
     * Initializes the staging area by loading the current state from the repository.
     */
    public StagingArea() {
        this.head = Head.fromFile();
        this.branch = head.getCurrentBranch();
        this.stagingArea = new Index();
        this.tree = head.getCurrentTree();
        this.perCommit = head.getCurrentCommitSHA();
        try {
            this.trackedFiles = Utils.readObject(Repository.IS_FILE_CHANGED, HashMap.class);
        } catch (IllegalArgumentException e) {
            this.trackedFiles = new HashMap<>();
        }
        String temp = Utils.readContentsAsString(Repository.IS_FILE_CHANGED);
    }

    /**
     * Prints the status of the staging area, including staged, removed, and not staged files.
     */
    public void printStaging() {
        Deque<String> staged = new ArrayDeque<>();
        List<String> removed = new ArrayList<>();
        Set<String> notStaged = new HashSet<>(stagingArea.getEntry().keySet());
        Set<String> tracked = new HashSet<>(stagingArea.getEntry().keySet());
        for (String s : trackedFiles.keySet()) {
            int ans = trackedFiles.get(s);
            if (ans > 0) {
                staged.addFirst(s);
                notStaged.remove(s);
            } else if (ans < 0) {
                removed.add(s);
                notStaged.remove(s);
            }
        }
        System.out.println("\n=== Staged Files ===");
        for (String s : staged) {
            System.out.println(s);
        }
        System.out.println("\n=== Removed Files ===");
        for (String s : removed) {
            System.out.println(Repository.toRelativePath(s));
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String s : notStaged) {
            String sha;
            try {
                sha = Repository.getFileSHA(s);
            } catch (RuntimeException e) {
                // System.out.println(s + "(deleted)");
                continue;
            }
            if (!sha.equals(stagingArea.getFileSHA(s))) {
                System.out.println(s + "(modified)");
            }
        }
        System.out.println("\n=== Untracked Files ===");
        HashSet<String> allFiles = Repository.getAllFilesInSubdirectories(Repository.CWD.toString());
        allFiles.removeAll(tracked);
        for (String s : allFiles) {
            System.out.println(s);
        }
    }

    /**
     * Adds a file to the staging area if it has been modified since the last commit.
     *
     * @param filePath The path of the file to be added.
     * @return true if the file was added to the staging area, false if it was identical to the current commit's version.
     */
    public boolean add(String filePath) {
        if (!Repository.StringToFile(filePath).exists()) {
            System.out.println("File does not exist.");
            return false;
        }
        String curSHA = Repository.getFileSHA(filePath);
        String preSHA = this.tree.getFileSHA(filePath);
        this.stagingArea.put(filePath);
        stagingArea.saveIndex();
        if (preSHA == null || !preSHA.equals(curSHA)) {
            trackedFiles.put(filePath, 1);
            saveChanged();
            return true;
        } else {
            trackedFiles.put(filePath, 0);
            saveChanged();
        }
        return false;
    }

    /**
     * Saves the current state of tracked files to the repository.
     */
    public void saveChanged() {
        if (!Repository.IS_FILE_CHANGED.exists()) {
            try {
                Repository.IS_FILE_CHANGED.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeObject(Repository.IS_FILE_CHANGED, this.trackedFiles);
    }

    /**
     * Adds a file to the staging area using a File object.
     *
     * @param filePath The file to be added.
     * @return true if the file was added to the staging area, false otherwise.
     */
    public boolean add(File filePath) {
        String relativeFilePath = Repository.toRelativePath(filePath.toString());
        return add(relativeFilePath);
    }

    /**
     * Removes a file from the staging area or the working directory.
     *
     * @param filePath The path of the file to be removed.
     * @return true if the file was successfully removed, false if it was neither staged nor tracked.
     */
    public boolean rm(String filePath) {
        return rm(Repository.StringToFile(filePath));
    }

    /**
     * Removes a file from the staging area or the working directory using a File object.
     *
     * @param filePath The file to be removed.
     * @return true if the file was successfully removed, false otherwise.
     */
    public boolean rm(File filePath) {
        String preSHA = this.tree.getFileSHA(filePath);
        String relativeFilePath = Repository.toRelativePath(filePath.toString());
        if (preSHA != null || stagingArea.getFileSHA(relativeFilePath) != null) {
            stagingArea.remove(relativeFilePath);
            if (filePath.exists() && preSHA != null) {
                Utils.restrictedDelete(filePath);
            }
            trackedFiles.put(relativeFilePath, -1);
            stagingArea.saveIndex();
            saveChanged();
            return true;
        } else {
            System.out.println("No reason to remove the file.");
            return false;
        }
    }

    /**
     * Generates a new commit with the specified message, based on the current staging area.
     *
     * @param m The commit message.
     * @return The sha-1 hash of the newly created commit.
     */
    public String genNewCommit(String m) {
        if (m.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return null;
        }
        if (trackedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return null;
        }
        Tree newTree = new Tree(Repository.CWD.toString(), stagingArea.getEntry());
        newTree.saveTree();
        this.tree = newTree;
        Commit newCommit = new Commit(m, newTree.getSHA(), perCommit);
        newCommit.saveCommit();
        head.setCurrentCommitSHA(newCommit);
        branch.newCommit(newCommit.getSHA());
        branch.saveBranch();
        this.perCommit = newCommit.getSHA();
        trackedFiles.clear();
        saveChanged();
        return newCommit.getSHA();
    }

    public boolean isTracked(File file) {
        return isTracked(file.toString());
    }

    public boolean isTracked(String file) {
        file = Repository.toRelativePath(file);
        if (trackedFiles.get(file) != null) {
            return true;
        }
        return false;
    }
}
