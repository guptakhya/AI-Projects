import java.util.*;
import java.io.File;
import java.io.IOException;
import java.lang.*;


public class homework {
    public static void main(String args[]) throws IOException{
        
        String inputFileName = "input.txt";
        //
        FileReader readFile = new FileReader(inputFileName);
        
        readFile.runAlgo();
        
    }
}