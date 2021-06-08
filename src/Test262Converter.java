import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Test262Converter {
    // boolean flags used to keep track of what assertion methods need to be added
    static boolean sameValue = false;
    static boolean notSameValue = false;
    static boolean throwsJS = false;
    static boolean assertJS = false;
    // takes one argument: the file within the same directory as this Java file that you want to convert
    public static void main(String[] args) throws IOException {
        // int to keep track of the index variable for the expect and result variables
        int numberOfChanges = 1;
        // list to keep track of what expect values need to be appended to the end of the file
        ArrayList<Boolean> expectations = new ArrayList<Boolean>();
        // the new file that will replace the old one
        ArrayList<String> newFile = new ArrayList<String>();
        // the file in its current state
        List<String> currentFile = getFile(args[0]);
        // foreach loop that looks at every line in the file
        for (String currentLine : currentFile){
            // replaces any assert method with var __resultx = assert.method, where x is an index variable
            if (currentLine.matches(".*(assert.sameValue|assert.throws|assert.notSameValue|assert[(])(.*)")){
                if (currentLine.matches(".*(assert.sameValue).*")){
                    sameValue = true;
                }
                if (currentLine.matches(".*(assert.notSameValue).*")){
                    notSameValue = true;
                }
                if (currentLine.matches(".*(assert.throws).*")){
                    throwsJS = true;
                }
                if (currentLine.matches(".*(assert[(]).*")){
                    assertJS = true;
                }
                currentLine = currentLine.replaceAll(".*(assert.sameValue|assert.throws|assert.notSameValue|assert[(])(.*)", "var __result" + numberOfChanges + " = $1" + "$2");
                numberOfChanges++;
                expectations.add(true);
                newFile.add(currentLine);
            }
            else {
                newFile.add(currentLine);
            }
        }
        // saves current progress
        Files.write(Paths.get(args[0]), newFile);
        currentFile = getFile(args[0]);
        //newFile = new ArrayList<String>();
        // for loop that appends expectation variables to the end of the file
        for (int i = 0; i < expectations.size(); i++){
            currentFile.add("var __expect" + (i + 1) + " = " + expectations.get(i) + ";");
        }
        Files.write(Paths.get(args[0]), currentFile);
        newFile = (ArrayList<String>) getFile(args[0]);
        LinkedList<String> newFileWithAssertions = new LinkedList<String>();
        newFileWithAssertions.add("function assert(param) { return Boolean(param)}");
        // these blocks of code add the relevant assert methods
        if (sameValue){
            newFileWithAssertions.add("assert.sameValue = function (actual, expected, message) { return actual === expected; }");
        }
        if (notSameValue){
            newFileWithAssertions.add("assert.notSameValue = function (actual, expected, message) { return actual !== expected; }");
        }
        if (throwsJS){
            newFileWithAssertions.add("assert.throws = function (error, func, message) { try{ func(); return false; } catch(e){ return e instanceof error;}}");
        }
        newFile.addAll(0, newFileWithAssertions);
        // saves progress
        Files.write(Paths.get(args[0]), newFile);
    }
    // helper method that reads a file given its path
    private static List<String> getFile(String path) throws IOException {
        return Files.readAllLines(Paths.get(path));
    }
}