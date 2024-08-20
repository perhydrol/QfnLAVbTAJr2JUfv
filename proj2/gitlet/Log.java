package gitlet;

import java.util.LinkedList;
import java.util.List;

public class Log {
    private String currentCommitSHA;
    private Head currentHead;

    public Log(String currentCommitSHA, Head currentHead) {
        this.currentCommitSHA = currentCommitSHA;
        this.currentHead = currentHead;
    }

    private void printCommit(Commit curCommit) {
        System.out.println("===");
        List<String> parents = curCommit.getParentSHAs();
        System.out.println("commit " + curCommit.getSHA());
        if (parents.size() > 1) {
            System.out.println("Merge: " + parents.get(0).substring(0, 8) + " " + parents.get(1).substring(0, 8));
        }
        System.out.println(Repository.timeFormat(curCommit.getTime()));
        System.out.println(curCommit.getMessage());
        System.out.println("");
    }

    public void printLog() {
        LinkedList<String> parentList = new LinkedList<>();
        parentList.addLast(currentCommitSHA);
        while (!parentList.isEmpty()) {
            String cur = parentList.pop();
            if (cur == null || cur.isEmpty()) {
                break;
            }
            Commit curCommit = Commit.fromFile(cur);
            printCommit(curCommit);
            if (!cur.equals(currentHead.getCurrentBranch().getSTAR_COMMIT_SHA())) {
                parentList.addLast(curCommit.getParentSHAs().get(0));
            }
        }
    }

    public void globalLog() {
        String commitContent = Utils.readContentsAsString(Repository.COMMIT_LOG);
        String[] eachLine = commitContent.split("\\r?\\n");
        for (String s : eachLine) {
            String[] line = s.split(";");
            Commit temp = Commit.fromFile(line[0]);
            printCommit(temp);
        }
    }

    public void find(String message) {
        String commitContent = Utils.readContentsAsString(Repository.COMMIT_LOG);
        String[] eachLine = commitContent.split("\\r?\\n");
        boolean flag = false;
        for (String s : eachLine) {
            String[] line = s.split(";");
            if (line[1].equals(message)) {
                System.out.println(line[0]);
                flag = true;
            }
        }
        if (!flag) {
            System.out.println("Found no commit with that message.");
        }
    }
}
