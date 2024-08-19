package gitlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */

    /**
     * Appends a line of text to the commit log file.
     *
     * @param content The content to be appended.
     */
    private static void appendLineToCommitLog(String content) {
        try (FileWriter writer = new FileWriter(Repository.COMMIT_LOG, true)) { // true表示追加模式
            writer.write(content + System.lineSeparator()); // 写入内容并添加换行符
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }


    /**
     * Initializes the Gitlet repository by creating necessary directories and files.
     * This includes the main Gitlet directory, commit log, and other metadata files.
     */
    private static void initDirFile() {
        Repository.GITLET_DIR.mkdir();
        try {
            Repository.HEAD.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Repository.INDEX.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Repository.LOGS_DIR.mkdir();
        Repository.REFS_DIR.mkdir();
        Repository.HEADS_DIR.mkdir();
        try {
            Repository.HEAD_LOG.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Repository.OBJECTS_DIR.mkdir();
        try {
            Repository.IS_FILE_CHANGED.createNewFile();
            Utils.writeContents(Repository.IS_FILE_CHANGED, "false");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Repository.COMMIT_LOG.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the Gitlet system with an initial commit and default branch.
     * Creates an empty tree, initial commit, and master branch.
     */
    private static void initClass() {
        Tree emptyTree = new Tree(null);
        emptyTree.saveTree();
        Commit initCommit = new Commit("initial commit", emptyTree.getSHA(), "");
        initCommit.saveCommit();
        Branch master = new Branch("master", initCommit.getSHA());
        master.saveBranch();
        Head head = new Head(initCommit.getSHA(), master.getName());
        Index index = new Index();
        head.saveHEAD();
    }

    public static void main(String[] args) {
        args=new String[]{"rm","gitlet-design.md"};
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                if (Repository.GITLET_DIR.exists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    return;
                } else {
                    initDirFile();
                    initClass();
                }
                break;
            case "add":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                String file = args[1];
                if (Repository.GITLET_DIR.exists()) {
                    StagingArea stagingArea = new StagingArea();
                    stagingArea.add(file);
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                String message = args[1];
                if (Repository.GITLET_DIR.exists()) {
                    StagingArea stagingArea = new StagingArea();
                    String SHA = stagingArea.genNewCommit(message);
                    appendLineToCommitLog(SHA + ";" + message);
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "log":
                if (Repository.GITLET_DIR.exists()) {
                    Head head = Head.fromFile();
                    Log log = new Log(head.getCurrentCommitSHA(), head);
                    log.printLog();
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "global-log":
                if (Repository.GITLET_DIR.exists()) {
                    Head head = Head.fromFile();
                    Log log = new Log(head.getCurrentCommitSHA(), head);
                    log.globalLog();
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "find":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                String m = args[1];
                // String m = "test";
                if (Repository.GITLET_DIR.exists()) {
                    Head head = Head.fromFile();
                    Log log = new Log(head.getCurrentCommitSHA(), head);
                    log.find(m);
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "status":
                if (Repository.GITLET_DIR.exists()) {
                    Head head = Head.fromFile();
                    String curBranchName = head.getCurrentBranch().getName();
                    System.out.println("=== Branches ===");
                    List<String> branches = Utils.plainFilenamesIn(Repository.HEADS_DIR);
                    if (branches != null) {
                        for (String s : branches) {
                            if (s.equals(curBranchName)) {
                                System.out.println("*" + curBranchName);
                            } else {
                                System.out.println(s);
                            }
                        }
                    }
                    StagingArea stagingArea = new StagingArea();
                    stagingArea.printStaging();
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "checkout":
                if (Repository.GITLET_DIR.exists()) {
                    Head head = Head.fromFile();
                    Branch curBranch = head.getCurrentBranch();
                    Tree curTree = head.getCurrentTree();
                    String filePath;
                    switch (args.length) {
                        case 3:
                            filePath = args[2];
                            String fileSHA = curTree.getFileSHA(filePath);
                            if (fileSHA != null) {
                                Blob fileBlob = Blob.fromFile(fileSHA);
                                fileBlob.recovery();
                            } else {
                                System.out.println("File does not exist in that commit.");
                            }
                            break;
                        case 4:
                            String commitSHA = args[1];
                            Commit targetCommit = Commit.fromFileShortSHA(commitSHA);
                            if (targetCommit == null) {
                                System.out.println("No commit with that id exists.");
                                System.exit(0);
                            }
                            filePath = args[3];
                            targetCommit.recoveryFile(filePath);
                            break;
                        case 2:
                            String s = args[1];
                            if (s.equals(curBranch.getName())) {
                                System.out.println("No need to checkout the current branch.");
                                System.exit(0);
                            }
                            try {
                                Branch branch = Branch.fromFile(s);
                                Tree.checkout(curTree, branch.getTree());
                            } catch (RuntimeException e) {
                                System.out.println("No such branch exists.");
                                System.exit(0);
                            }
                            break;
                        default:
                            System.out.println("Incorrect operands.");
                            break;
                    }
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "branch":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                if (Repository.GITLET_DIR.exists()) {
                    String s = args[1];
                    Head head = Head.fromFile();
                    try {
                        Branch branch = Branch.fromFile(s);
                        System.out.println("A branch with that name already exists.");
                        System.exit(0);
                    } catch (RuntimeException e) {
                        Branch newBranch = new Branch(s, head.getCurrentCommitSHA());
                        newBranch.saveBranch();
                    }
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "rm-branch":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                if (Repository.GITLET_DIR.exists()) {
                    String s = args[1];
                    Head head = Head.fromFile();
                    if (s.equals(head.getCurrentBranch().getName())) {
                        System.out.println("Cannot remove the current branch.");
                        System.exit(0);
                    }
                    File branchFile = Utils.join(Repository.HEADS_DIR, s);
                    if (branchFile.exists()) {
                        Utils.restrictedDelete(branchFile);
                    } else {
                        System.out.println("A branch with that name does not exist.");
                        System.exit(0);
                    }
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "reset":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                if (Repository.GITLET_DIR.exists()) {
                    Head head = Head.fromFile();
                    String curCommitSHA = head.getCurrentCommitSHA();
                    Commit curCommit = Commit.fromFile(curCommitSHA);
                    String s = args[1];
                    Commit targetCommit = Commit.fromFileShortSHA(s);
                    if (targetCommit == null) {
                        System.out.println("No commit with that id exists.");
                        System.exit(0);
                    } else {
                        Tree curTree = Tree.fromFile(curCommit.getTreeSHA());
                        Tree targetTree = Tree.fromFile(targetCommit.getTreeSHA());
                        boolean ans = Tree.ifReset(curTree, targetTree);
                        if (ans) {
                            Tree.checkout(curTree, targetTree);
                        }
                        head.setCurrentCommitSHA(targetCommit.getSHA());
                        Branch branch = head.getCurrentBranch();
                        branch.moveCurrentCommit(targetCommit.getSHA());
                        branch.saveBranch();
                    }
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            case "rm":
                if (Repository.GITLET_DIR.exists()) {
                    String filePath = args[1];
                    StagingArea stagingArea = new StagingArea();
                    stagingArea.rm(filePath);
                } else {
                    System.out.println("Not in an initialized Gitlet directory.");
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
