package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Branch implements Serializable {
    private class Node implements Serializable {
        String commitSha;
        String branchName;
        String endCommitSha;

        Node(String commitSha, String branchName) {
            this.endCommitSha = commitSha;
            this.commitSha = commitSha;
            this.branchName = branchName;
        }
    }

    private TreeMap<String, Node> branches;
    private Node current;

    public Branch(String branchName, String commitSha) throws IOException {
        Node node = new Node(commitSha, branchName);
        branches = new TreeMap<>();
        branches.put(branchName, node);
        current = node;
        saveBranch();
    }

    public boolean isExited(String branchName) {
        return branches.containsKey(branchName);
    }

    public void newBranch(String branchName, String commitSha) throws IOException {
        Node node = new Node(commitSha, branchName);
        branches.put(branchName, node);
        saveBranch();
    }

    public void checkout(String branchName) throws IOException {
        if (!branches.containsKey(branchName)) {
            throw new RuntimeException("Branch: no the branch call the" + branchName);
        }
        current = branches.get(branchName);
        saveBranch();
    }

    public String getCurrentBranchName() {
        return current.branchName;
    }

    public TreeMap<String, String> getBranches() {
        TreeMap<String, String> branchCommit = new TreeMap<>();
        for (String name : branches.keySet()) {
            branchCommit.put(name, branches.get(name).commitSha);
        }
        return branchCommit;
    }

    public TreeMap<String, String> getBranchesEndCommit() {
        TreeMap<String, String> branchCommit = new TreeMap<>();
        for (String name : branches.keySet()) {
            branchCommit.put(name, branches.get(name).endCommitSha);
        }
        return branchCommit;
    }

    public Commit getCurrentCommit() {
        return Commit.fromFile(current.commitSha);
    }

    public Commit getBranchCommit(String branchName) {
        if (!branches.containsKey(branchName)) {
            throw new RuntimeException("Branch: no the branch call the" + branchName);
        }
        return Commit.fromFile(branches.get(branchName).commitSha);
    }

    public void setCurrentCommit(String commitSha) throws IOException {
        current.commitSha = commitSha;
        saveBranch();
    }

    public void setCurrentCommit(Commit commit) throws IOException {
        setCurrentCommit(commit.getSha());
    }

    public void addCommit(String commitSha) throws IOException {
        current.commitSha = commitSha;
        current.endCommitSha = commitSha;
        saveBranch();
    }

    public void addCommit(Commit commit) throws IOException {
        addCommit(commit.getSha());
    }

    public void rmBranch(String branchName) throws IOException {
        if (isExited(branchName)) {
            if (getCurrentBranchName().equals(branchName)) {
                System.out.println("Cannot remove the current branch.");
                return;
            }
            branches.remove(branchName);
            saveBranch();
        } else {
            System.out.println("A branch with that name does not exist.");
            return;
        }
    }

    public static void merge(String currentSha, String targetSha, String currentBranchName, String targetBranchName) throws IOException {
        Commit currentCommit = Commit.fromFile(currentSha);
        Commit targetCommit = Commit.fromFile(targetSha);
        Commit splitPointCommit = Commit.getSplitPoint(currentCommit, targetCommit);
        HashMap<String, String> splitFiles = splitPointCommit.getTrackedFiles();
        HashMap<String, String> curFiles = currentCommit.getTrackedFiles();
        HashMap<String, String> targetFiles = targetCommit.getTrackedFiles();
        StagingArea stagingArea = StagingArea.fromFile();
        if (splitPointCommit.getSha().equals(targetCommit.getSha())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitPointCommit.getSha().equals(currentCommit.getSha())) {
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Set<String> items = new HashSet<>(curFiles.keySet());
        items.addAll(targetFiles.keySet());
        List<String> filesInDir = Utils.plainFilenamesIn(Repository.CWD);
        if (filesInDir != null) {
            items.addAll(filesInDir);
        }
        for (String item : items) {
            String tarSHA = targetFiles.getOrDefault(item, "");
            String curSHA = curFiles.getOrDefault(item, "");
            String splitSHA = splitFiles.getOrDefault(item, "");
            // 文件在给定分支中自分裂点起修改，但在当前分支中未修改
            boolean isModifiedInGivenBranchOnly = !tarSHA.equals(splitSHA)
                    && curSHA.equals(splitSHA);
            // 文件在当前分支中自分裂点起修改，但在给定分支中未修改
            boolean isModifiedInCurrentBranchOnly = !curSHA.equals(splitSHA)
                    && tarSHA.equals(splitSHA);
            // 文件在当前分支和给定分支中以相同方式修改或删除
            boolean isModifiedInSameWayInBothBranches = curSHA.equals(tarSHA);
            // 文件在分裂点不存在，仅在当前分支存在
            boolean isFileOnlyInCurrentBranch = splitSHA.isEmpty() && !curSHA.isEmpty() && tarSHA.isEmpty();
            // 文件在分裂点不存在，仅在给定分支存在
            boolean isFileOnlyInGivenBranch = splitSHA.isEmpty() && curSHA.isEmpty() && !tarSHA.isEmpty();
            // 文件在分裂点存在，在当前分支未修改，在给定分支缺失
            boolean isUnmodifiedInCurrentAndAbsentInGiven = !splitSHA.isEmpty()
                    && splitSHA.equals(curSHA)
                    && tarSHA.isEmpty();
            // 文件在分裂点存在，在给定分支未修改，在当前分支缺失
            boolean isUnmodifiedInGivenAndAbsentInCurrent = !splitSHA.isEmpty()
                    && curSHA.isEmpty()
                    && tarSHA.equals(splitSHA);
            // 文件在当前分支和给定分支中以不同方式修改（发生冲突）
            boolean isConflict = !curSHA.equals(splitSHA) && !tarSHA.equals(splitSHA) && !curSHA.equals(tarSHA);
            boolean willChange = isModifiedInGivenBranchOnly
                    || isFileOnlyInGivenBranch || isUnmodifiedInCurrentAndAbsentInGiven;
            // 未跟踪文件
            boolean unTrackedFile = !(stagingArea.getTrackedFiles().containsKey(item) || currentCommit.fileExits(item))
                    && Base.stringToFile(item).exists();
            if (unTrackedFile) {
                willChange = willChange && !Base.getFileSHA(item).equals(tarSHA);
            }
            // 存在未跟踪文件且会被合并覆盖或删除
            boolean isUntrackedFileInTheWay = unTrackedFile && willChange;
            if (isModifiedInCurrentBranchOnly
                    || isModifiedInSameWayInBothBranches || isFileOnlyInCurrentBranch || isUnmodifiedInGivenAndAbsentInCurrent) {
                continue;
            } else if (isUntrackedFileInTheWay) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            } else if (isConflict) {
                System.out.println("Encountered a merge conflict.");
                handleMergeConflict(item, curSHA, tarSHA);
            } else if (!tarSHA.isEmpty() && (isModifiedInGivenBranchOnly || isFileOnlyInGivenBranch)) {
                Blob file = Blob.fromFile(tarSHA);
                file.recovery();
                stagingArea.add(file.getFilePath());
            } else if (isUnmodifiedInCurrentAndAbsentInGiven) {
                stagingArea.rm(item);
            }
        }
        Command.commit("Merged " + targetBranchName + " into " + currentBranchName + ".");
    }

    private static void handleMergeConflict(String item, String curSHA, String tarSHA) {
        Blob curBlob = null;
        Blob tarBlob = null;
        if (curSHA != null && !curSHA.isEmpty()) {
            curBlob = Blob.fromFile(curSHA);
        }
        if (tarSHA != null && !tarSHA.isEmpty()) {
            tarBlob = Blob.fromFile(tarSHA);
        }
        String curCode = curBlob != null ? curBlob.getCode() : "";
        String tarCode = tarBlob != null ? tarBlob.getCode() : "";
        String contact = "<<<<<<< HEAD\n" + curCode + "=======\n" + tarCode + ">>>>>>>\n";
        File file = Base.stringToFile(item);
        Utils.writeContents(file, contact);
    }

    private void saveBranch() throws IOException {
        if (!Repository.BRANCH.exists()) {
            Repository.BRANCH.createNewFile();
        }
        Utils.writeObject(Repository.BRANCH, this);
    }

    public static Branch fromFile() {
        return Utils.readObject(Repository.BRANCH, Branch.class);
    }
}
