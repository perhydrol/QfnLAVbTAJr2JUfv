package gitlet;

import java.io.IOException;
import java.util.*;

public class Command {

    static void init() {
        Repository.initDirFile();
        StagingArea stagingArea = null;
        try {
            stagingArea = new StagingArea();
            Commit commit = new Commit("initial commit", "", stagingArea.getTrackedFiles(), null);
            Branch branch = new Branch("master", commit.getSha());
            Head head = new Head(commit.getSha(), branch.getCurrentBranchName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void add(String filePath) {
        if (!Base.stringToFile(filePath).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        StagingArea stagingArea = StagingArea.fromFile();
        try {
            stagingArea.add(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Commit changes
    static void commit(String message) {
        if (message == null || message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        StagingArea stagingArea = StagingArea.fromFile();
        Head head = Head.fromFile();
        Commit commit = head.getCommit();
        Commit newCommit = new Commit(message, commit.getSha(), stagingArea.getTrackedFiles(), stagingArea.getRemovalFiles());
        try {
            head.setCommitSHA(newCommit.getSha());
            Branch branch = Branch.fromFile();
            branch.setCurrentCommit(newCommit.getSha());
            stagingArea.cleanStagingArea();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Show commit logs
    static void log() {
        Head head = Head.fromFile();
        Commit commit = head.getCommit();
        String parent = commit.getSha();
        while (true) {
            Commit current = Commit.fromFile(parent);
            current.printCommitLog();
            if (current.getParentsSha().isEmpty()) {
                break;
            } else {
                parent = current.getParentsSha().get(0);
            }
        }
    }

    // Show global commit logs
    static void globalLog() {
        TreeMap<String, String> branchCommit = Branch.fromFile().getBranches();
        List<String> visited = new ArrayList<>();
        for (String branchName : branchCommit.keySet()) {
            String parent = branchCommit.get(branchName);
            while (true) {
                Commit current = Commit.fromFile(parent);
                if (!visited.contains(current.getSha())) {
                    current.printCommitLog();
                    visited.add(current.getSha());
                }
                if (current.getParentsSha().isEmpty()) {
                    break;
                } else {
                    parent = current.getParentsSha().get(0);
                }
            }
        }
    }

    // Find commits by message
    static void find(String commitMessage) {
        TreeMap<String, String> branchCommit = Branch.fromFile().getBranches();
        List<String> visited = new ArrayList<>();
        boolean successful = false;
        for (String branchName : branchCommit.keySet()) {
            String parent = branchCommit.get(branchName);
            while (true) {
                Commit current = Commit.fromFile(parent);
                if (!visited.contains(current.getSha())) {
                    if (current.getMessage().equals(commitMessage)) {
                        System.out.println(current.getSha());
                        successful = true;
                    }
                    visited.add(current.getSha());
                }
                if (current.getParentsSha().isEmpty()) {
                    break;
                } else {
                    parent = current.getParentsSha().get(0);
                }
            }
        }
        if (!successful) {
            System.out.println("Found no commit with that message.");
        }
    }

    // Show the status of the repository
    static void status() {
        Branch branch = Branch.fromFile();
        StagingArea stagingArea = StagingArea.fromFile();
        Head head = Head.fromFile();
        Commit commit = head.getCommit();
        System.out.println("=== Branches ===");
        for (String branchName : branch.getBranches().keySet()) {
            if (branchName.equals(branch.getCurrentBranchName())) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }

        System.out.println("\n=== Staged Files ===");
        for (String file : stagingArea.getTrackedFiles().keySet()) {
            if (commit.fileChanged(Base.stringToFile(file))) {
                System.out.println(file);
            }
        }

        System.out.println("\n=== Removed Files ===");
        for (String file : stagingArea.getRemovalFiles()) {
            System.out.println(file);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String file : stagingArea.getChangedFiles().keySet()) {
            System.out.println(file);
        }

        System.out.println("\n=== Untracked Files ===");
        List<String> allFiles = Utils.plainFilenamesIn(Repository.CWD.toString());
        allFiles.removeAll(stagingArea.getRemovalFiles());
        allFiles.removeAll(stagingArea.getTrackedFiles().keySet());
        allFiles.removeAll(stagingArea.getChangedFiles().keySet());
        allFiles.removeAll(commit.getTrackedFiles().keySet());
        for (String s : allFiles) {
            System.out.println(s);
        }
    }

    public static void handleBranchCheckout(Branch branch, Head head, String branchName) {
        if (branchName.equals(branch.getCurrentBranchName())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        if (!branch.isExited(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        Commit targetCommit = branch.getBranchCommit(branchName);
        branch.getCurrentCommit().rest(targetCommit);
        try {
            head.setCommitSHA(targetCommit.getSha());
            branch.checkout(branchName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void recoverFile(Commit curCommit, String filePath) {
        String fileSHA = curCommit.getFileSha(filePath);
        if (fileSHA != null) {
            Blob fileBlob = Blob.fromFile(fileSHA);
            fileBlob.recovery();
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void recoverFileFromCommit(String commitSHA, String filePath) {
        Commit targetCommit = Commit.fromFile(commitSHA);
        if (targetCommit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        recoverFile(targetCommit, filePath);
    }

    // Create a new branch
    static void branch(String branchName) {
        Head head = Head.fromFile();
        Branch branch = Branch.fromFile();
        if (branch.isExited(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            branch.newBranch(branchName, head.getCommit().getSha());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Remove a branch
    static void rmBranch(String branchName) {
        Branch branch = Branch.fromFile();
        try {
            branch.rmBranch(branchName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Reset to a specific commit
    static void reset(String commitSHA) {
        Commit commit = Commit.fromFile(commitSHA);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Head head = Head.fromFile();
        Commit curCommit = head.getCommit();
        curCommit.rest(commit);
        try {
            head.setCommitSHA(commitSHA);
            StagingArea stagingArea = StagingArea.fromFile();
            stagingArea.cleanStagingArea();
            Branch branch = Branch.fromFile();
            branch.setCurrentCommit(commitSHA);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Remove a file from the working directory and staging area
    static void rm(String filePath) {
        StagingArea stagingArea = StagingArea.fromFile();
        try {
            stagingArea.rm(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Merge a branch into the current branch
    static void merge(String branchName) {
        Head head = Head.fromFile();
        Branch branch = Branch.fromFile();
        Commit current = branch.getCurrentCommit();
        Commit target = branch.getBranchCommit(branchName);
        try {
            Branch.merge(current.getSha(), target.getSha(), branch.getCurrentBranchName(), branchName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
