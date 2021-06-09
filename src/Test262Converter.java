import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.File;

public class Test262Converter {
    // Definitions of assertions
    final static String assertFunction = "function assert(param) { return Boolean(param)}";
    final static String assertSameValueFunction = "assert.sameValue = function (actual, expected, message) { return actual === expected; }";
    final static String assertNotSameValueFunction = "assert.notSameValue = function (actual, expected, message) { return actual !== expected; }";
    final static String assertThrowsFunction = "assert.throws = function (error, func, message) { try{ func(); return false; } catch(e){ return e instanceof error;}}";
    // takes one argument: the file within the same directory as this Java file that you want to convert
    public static void main(String[] args) throws IOException {
        // list of file names in the directory of this Java program
        String[] listOfFiles = new File(System.getProperty("user.dir")).list();
        ArrayList<String> newArrayListOfFiles = new ArrayList<String>();
        // foreach loop that forms an array list of all of the JavaScript files in the directory
        for (String path : listOfFiles){
            if (path.matches(".*(.js).*")){
                newArrayListOfFiles.add(path);
            }
        }
        // for loop that converts every JavaScript (.js) file in the directory
        for(String file : newArrayListOfFiles){
            System.out.println(file);
            adaptFile(file);
        }
        System.out.println("Done");
        System.out.println("Number of Files Processed: " + newArrayListOfFiles.size());
        //adaptFile(args[0]);
    }
    // method that converts a JavaScript file into the relevant format
    private static void adaptFile(String pathForFile) throws IOException {
        // the first four booleans keep track of what assertions need to be added later as method definitions
        boolean sameValue = false;
        boolean notSameValue = false;
        boolean throwsJS = false;
        boolean assertJS = false;
        // the next four booleans keep track of what assertion methods have already been declared.
        // important for avoiding an infinite spiral of adding assertion methods every
        // time this program is used
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
            // if an assertion method is called, it must be converted
            // The first condition means "this string has assert.sameValue, assert.throws, assert.notSameValue, or assert("
            if (currentLine.matches(".*(assert.sameValue|assert.throws|assert.notSameValue|assert[(])(.*)")
                    // the second condition means "this string does not start with function assert, assert.sameValue =,
                    // assert.notSameValue =, or assert.throws =
                    // this second condition is used to ensure that this if conditional only evaluates to true
                    // for lines in which an assertion is used
                && !currentLine.matches("(function assert|assert.sameValue =|assert.notSameValue =|assert.throws =).*")
                    // the third conditional means "this string does not have var __result as a substring of it"
                && !currentLine.matches(".*var __result.*")){
                // the next four if conditionals check to see which assertion was used. We'll keep track of these
                // to know which assertions we need to define.
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
                // string replacement on the assertion call line that uses the var __reusult = assert.method format
                currentLine = currentLine.replaceAll(".*(assert.sameValue|assert.throws|assert.notSameValue|assert[(])(.*)", "var __result" + numberOfChanges + " = $1" + "$2");
                // increments index variable that checks how many assertion calls there are
                numberOfChanges++;
                // list to keep track of what expectation variables need to be appended to the end of the file
                expectations.add(true);
                // adds the modified line into the new file that will override the current one
                newFile.add(currentLine);
                continue;
            }
            // the four else if conditionals check to see if an assertion method has already been defined.
            // For each one that has already been defined, they will not be defined again
            // to avoid an infinite spiral of adding the definitions if the program is called
            // repeatedly on the same files
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
            // adds the unmodified line to the new file. Only happens if there is no assertion call on this line.
            newFile.add(currentLine);
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
        // the next four if conditionals will add the relevant assertion method definitions, but
        // two criterions must be met. 1. An assertion call of that method type was used
        // 2. the assertion method was not already defined at the top of the file which it would be
        // if the program was used on a particular .js file at least once
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