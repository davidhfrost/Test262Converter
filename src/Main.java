import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {
    static boolean sameValue = false;
    static boolean notSameValue = false;
    static boolean throwsJS = false;
    static boolean assertJS = false;
    // takes one argument: the file within the same directory as this Java file that you want to convert
    public static void main(String[] args) throws IOException {
        int numberOfChanges = 1;
        ArrayList<Boolean> expectations = new ArrayList<Boolean>();
        ArrayList<String> newFile = new ArrayList<String>();
        List<String> currentFile = getFile(args[0]);
        for (String currentLine : currentFile){
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
        Files.write(Paths.get(args[0]), newFile);
        currentFile = getFile(args[0]);
        //newFile = new ArrayList<String>();
        for (int i = 0; i < expectations.size(); i++){
            currentFile.add("var __expect" + (i + 1) + " = " + expectations.get(i) + ";");
        }
        Files.write(Paths.get(args[0]), currentFile);
        newFile = (ArrayList<String>) getFile(args[0]);
        LinkedList<String> newFileWithAssertions = new LinkedList<String>();
        newFileWithAssertions.add("function assert() {");
        newFileWithAssertions.add("}");
        if (sameValue){
            newFileWithAssertions.add("assert.sameValue = function (actual, expected, message) { return actual === expected; }");
        }
        if (notSameValue){
            newFileWithAssertions.add("assert.notSameValue = function (actual, expected, message) { return actual !== expected; }");
        }
        newFile.addAll(0, newFileWithAssertions);
        Files.write(Paths.get(args[0]), newFile);
    }
    private static List<String> getFile(String path) throws IOException {
        return Files.readAllLines(Paths.get(path));
    }
}