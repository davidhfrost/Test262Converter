import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.io.File;

public class Test262Converter {
    // Definitions of assertions. Important that these are variables and not magic literals for the purpose of not
    // running into problems when the converter is called multiple times
    static String assertFunction = "function assert(param) { return Boolean(param)}";
    static String assertSameValueFunction = "assert.sameValue = function (actual, expected, message) { return actual === expected; }";
    static String assertNotSameValueFunction = "assert.notSameValue = function (actual, expected, message) { return actual !== expected; }";
    static String assertThrowsFunction = "assert.throws = function (error, func, message) { try{ func(); return false; } catch(e){ return e instanceof error;}}";
    // takes one argument: the file within the same directory as this Java file that you want to convert
    public static void main(String[] args) throws IOException {
        String[] listOfFiles = new File(System.getProperty("user.dir")).list();
        ArrayList<String> newArrayListOfFiles = new ArrayList<String>();
        for (String path : listOfFiles){
            if (path.matches(".*(.js).*")){
                newArrayListOfFiles.add(path);
            }
        }
        //listOfFiles = (String[])Arrays.stream(listOfFiles).filter(x -> x.matches(".*.js$")).toArray();
        for(String file : newArrayListOfFiles){
            System.out.println(file);
            adaptFile(file);
        }
        System.out.println("Done");
        System.out.println(newArrayListOfFiles.size());
        //adaptFile(args[0]);
    }
    private static void adaptFile(String pathForFile) throws IOException {
        boolean sameValue = false;
        boolean notSameValue = false;
        boolean throwsJS = false;
        boolean assertJS = false;
        boolean sameValueFound = false;
        boolean notSameValueFound = false;
        boolean throwsFound = false;
        boolean assertFound = false;
        // int to keep track of the index variable for the expect and result variables
        int numberOfChanges = 1;
        // list to keep track of what expect values need to be appended to the end of the file
        ArrayList<Boolean> expectations = new ArrayList<Boolean>();
        // the new file that will replace the old one
        ArrayList<String> newFile = new ArrayList<String>();
        // the file in its current state
        List<String> currentFile = getFile(pathForFile);
        // foreach loop that looks at every line in the file
        for (String currentLine : currentFile){
            // replaces any assert method with var __resultx = assert.method, where x is an index variable
            if (currentLine.matches(".*(assert.sameValue|assert.throws|assert.notSameValue|assert[(])(.*)")
                && !currentLine.matches("(function assert|assert.sameValue =|assert.notSameValue =|assert.throws =).*")
                && !currentLine.matches(".*var __result.*")){
                //&& !currentLine.matches("(" + assertFunction +"|" + assertNotSameValueFunction
                //    + "|" + assertSameValueFunction + "|" + assertThrowsFunction + ").*")){
                //&& !currentLine.matches(".*(assert.sameValue = function| assert.notSameValue = function|assert.throws = function).*")
                //&& !currentLine.matches("function assert.*")){
                //&& !currentLine.matches(".*("+assertFunction + "|" + assertSameValueFunction + "|" + assertNotSameValueFunction + "|" + assertThrowsFunction + ").*")){
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
                continue;
            }
            else if (currentLine.matches("assert.throws =.*")){
                throwsFound = true;
            }
            else if (currentLine.matches("assert.sameValue =.*")){
                sameValueFound = true;
            }
            else if (currentLine.matches("assert.notSameValue =.*")){
                notSameValueFound = true;
            }
            else if (currentLine.matches("function assert.*")){
                assertFound = true;
            }
            newFile.add(currentLine);
            /*
            else {
                newFile.add(currentLine);
            }*/
        }
        // saves current progress
        Files.write(Paths.get(pathForFile), newFile);
        currentFile = getFile(pathForFile);
        //newFile = new ArrayList<String>();
        // for loop that appends expectation variables to the end of the file
        for (int i = 0; i < expectations.size(); i++){
            currentFile.add("var __expect" + (i + 1) + " = " + expectations.get(i) + ";");
        }
        Files.write(Paths.get(pathForFile), currentFile);
        newFile = (ArrayList<String>) getFile(pathForFile);
        LinkedList<String> newFileWithAssertions = new LinkedList<String>();
        if (!assertFound) {
            newFileWithAssertions.add(assertFunction);
        }
        // these blocks of code add the relevant assert methods
        if (sameValue && !sameValueFound){
            newFileWithAssertions.add(assertSameValueFunction);
        }
        if (notSameValue && !notSameValueFound){
            newFileWithAssertions.add(assertNotSameValueFunction);
        }
        if (throwsJS && !throwsFound){
            newFileWithAssertions.add(assertThrowsFunction);
        }
        newFile.addAll(0, newFileWithAssertions);
        // saves progress
        Files.write(Paths.get(pathForFile), newFile);
    }
    // helper method that reads a file given its path
    private static List<String> getFile(String path) throws IOException {
        return Files.readAllLines(Paths.get(path));
    }
}