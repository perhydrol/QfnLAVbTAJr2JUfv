package gitlet;

import java.io.IOException;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * 只储存绝对路径
 *
 * @author Hydrogen
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        if (firstArg.equals("init")) {
            Command.init();
            return;
        } else if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (firstArg) {
            case "add":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.add(args[1]);
                break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.commit(args[1]);
                break;
            case "log":
                Command.log();
                break;
            case "global-log":
                Command.globalLog();
                break;
            case "find":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.find(args[1]);
                break;
            case "status":
                Command.status();
                break;
            case "checkout":
                Head head = Head.fromFile();
                Branch branch = Branch.fromFile();
                Commit curCommit = branch.getCurrentCommit();
                switch (args.length) {
                    case 2: // 迁出分支
                        Command.handleBranchCheckout(branch, head, args[1]);
                        break;
                    case 3: // 恢复文件
                        Command.recoverFile(curCommit, args[2]);
                        break;
                    case 4: // 从特定commit恢复文件
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            return;
                        }
                        Command.recoverFileFromCommit(args[1], args[3]);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        break;
                }
                break;
            case "branch":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.branch(args[1]);
                break;
            case "rm-branch":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.rmBranch(args[1]);
                break;
            case "reset":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.reset(args[1]);
                break;
            case "rm":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.rm(args[1]);
                break;
            case "merge":
                if (args.length < 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Command.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
