import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.*;
import java.io.IOException;


public class FileReader {
    
    private File inFile;
    private String queryLine;
    private int numLinesInput;
    private HashMap<String,ArrayList<Integer>> kb;
    private int counter;
    private PredNode queryPredNode;
    private HashMap<String, ArrayList<PredNode>> kbInfer;
    private HashMap<Integer, ArrayList<PredNode>> sentenceIndex;
    private Integer sentenceIndexTracker;
    
    public FileReader(String filename) throws IOException{
        
        inFile = new File(filename);
        try (Scanner in = new Scanner(inFile)){
            queryLine = in.nextLine();
            queryPredNode = new PredNode(queryLine);
            numLinesInput = in.nextInt();
            in.nextLine();
            
            sentenceIndexTracker = 1;
            kb = new HashMap<>();
            kbInfer = new HashMap<>();
            sentenceIndex = new HashMap<>();
            //create ouput file
            File op = new File("output.txt");
            File opFile = new File("SentenceIndOutput.txt");
            File opFile2 = new File("kbOutput2.txt");
            File opFile3 = new File("kbOutput.txt");
            File opFile4 = new File("logs.txt");

            //if o/p file exists, delete and create it again to clear it of any prev content
            if(!(opFile.createNewFile())){
                opFile.delete();
                opFile = new File("SentenceIndOutput.txt");
            }
            FileWriter writer = new FileWriter(opFile);
            if(!(opFile2.createNewFile())){
                opFile2.delete();
                opFile2 = new File("kbInferOutput.txt");
            }
            FileWriter writer2 = new FileWriter(opFile2);
            
            if(!(opFile3.createNewFile())){
                opFile3.delete();
                opFile3 = new File("kbOutput.txt");
            }
            FileWriter writer3 = new FileWriter(opFile3);
            
            if(!(opFile4.createNewFile())){
                opFile4.delete();
                opFile4 = new File("logs.txt");
            }
            FileWriter writer4 = new FileWriter(opFile4);
            
            for(int i=0; i<numLinesInput; i++){
                String line = in.nextLine();
                ConvertToCnf convertor = new ConvertToCnf(line, kb, counter, kbInfer, sentenceIndex, sentenceIndexTracker);
                sentenceIndexTracker = convertor.getSentIndCounter();
                counter = convertor.getCounter();
            }
            
            /*
            for(int i=0; i<kb.size(); i++){
                ArrayList<PredNode> pre = kb.get(i);
                
                for(int k=0; k<pre.size(); k++){
                    PredNode p = pre.get(k);
                    String name = p.getPredName();
                    if(!p.getIsPos()){
                        name = "~" + name;
                    }
                    writer.write(name);
                    writer.write("(");
                    String[][] param = p.getParamsList();
                    for(int j=0; j<param.length; j++){
                        writer.write(param[j][1]);
                        writer.write(",");
                    }
                    writer.write(")");
                    if(k<pre.size()-1){
                        writer.write(" | ");
                    }
                }
                
                writer.write("\n");
                //System.out.println("KB size " + kb.size());
                //System.out.println("KB line " + i + "'s last is " + t.get(t.size()-1).getPredNodesList().get(0).getPredName());
                //System.out.println(convertor.isPredicate());
            }
            writer.close();
            */
            
            for(Map.Entry<String, ArrayList<Integer>> entry: kb.entrySet()) {
                String pName = entry.getKey();
                writer3.write(pName);
                writer3.write("\n");
                ArrayList<Integer> pList = entry.getValue();
                for(Integer p: pList){
                    writer3.write(p.toString());
                    writer3.write("\n");
                }   
            }
            writer3.close();
            
            
            for(Map.Entry<Integer, ArrayList<PredNode>> entry: sentenceIndex.entrySet()) {
                Integer i = entry.getKey();
                writer.write(i.toString());
                writer.write(" : ");
                ArrayList<PredNode> pList = entry.getValue();
                for(PredNode p: pList){
                    String name = p.getPredName();
                    if(!p.getIsPos()){
                        name = "~" + name;
                    }
                    writer.write(name);
                    writer.write("(");
                    String[][] param = p.getParamsList();
                    for(int j=0; j<param.length; j++){
                        writer.write(param[j][1]);
                        writer.write(",");
                    }
                    writer.write(")");
                    writer.write(" | ");
                }
                writer.write("\n");
                
            }
            writer.close();
            
            
            for(Map.Entry<String, ArrayList<PredNode>> entry: kbInfer.entrySet()) {
                String pName = entry.getKey();
                ArrayList<PredNode> pList = entry.getValue();
                for(PredNode p : pList){
                    String name = pName;
                    if(!p.getIsPos()){
                        name = "~" + name;
                    }
                    writer2.write(name);
                    writer2.write("(");
                    String[][] param = p.getParamsList();
                    for(int j=0; j<param.length; j++){
                        writer2.write(param[j][1]);
                        writer2.write(",");
                    }
                    writer2.write(")");
                    writer2.write("\n");
                }
            }
            writer2.close();
            
            if(!(op.createNewFile())){
                op.delete();
                op = new File("output.txt");
            }
            FileWriter writeToOp = new FileWriter(op);
            
            //neagte the query
            ArrayList<PredNode> st = new ArrayList<>();
            queryPredNode.setIsPos(!queryPredNode.getIsPos());
            st.add(queryPredNode);
            boolean[] visited = new boolean[sentenceIndexTracker];
            int depth = 0;
            HashSet<PredNode> proven = new HashSet<>();
            //call Backward Chaining function
            boolean finalAns = backChaining(st, visited, depth, proven);
            writer4.close();
            if(finalAns){
                writeToOp.write("TRUE");
            }
            else writeToOp.write("FALSE");
            writeToOp.close();
            
            
            /*
            //TEST
            System.out.println("Test");
            
            */
        }
    }
    
    
    private boolean backChaining(ArrayList<PredNode> st, boolean[] visited, int depth, HashSet<PredNode> proven){
        if(st.size() > 0){
            PredNode query = st.remove(st.size()-1);
            String pName = query.getPredName();
            if(st.size() == 0){
                ArrayList<PredNode> concludedPredList = kbInfer.get(pName);
                if(concludedPredList != null){
                    for(PredNode p: concludedPredList){
                        if(p.unifyAndSubs(query) != null){
                            //System.out.println("Here3 " + p.getPredName());
                            String[][] a = p.getParamsList();
                            //System.out.println(a[0][0] + a[0][1]);
                            proven.add(query);
                            return true;
                        }
                    }
                }

            }
            
            //If Predicate doesn't exist in the query, return false
            if(!kb.keySet().contains(pName)){
                //System.out.println("F1");
                return false;
            }

            for(Integer ind : kb.get(pName)){
                if(!visited[ind]){
                    ArrayList<PredNode> sentence = sentenceIndex.get(ind);
                    for(PredNode p: sentence){
                        HashMap<String, String> subs = p.unifyAndSubs(query);
                        
                        if(subs != null){
                            //System.out.println(subs.toString());
                            ArrayList<PredNode> newSent = createNewSentence(sentence, p);
                            ArrayList<PredNode> copiedStack = new ArrayList<>();
                            for(PredNode pn: st){
                                PredNode newerNode = pn.createCopy();
                                boolean addToStack = true;
                                for(PredNode pInStack: copiedStack){
                                    if(pn.similarPreds(pInStack)){
                                        addToStack = false;
                                    }
                                }
                                if(addToStack){
                                    copiedStack.add(newerNode);
                                }
                            }
                            substitution(subs, newSent);
                            /*
                            System.out.println("New Sentence ");
                            for(PredNode aa : newSent){
                                System.out.print(aa.getPredName() + "(");
                                String[][] la = aa.getParamsList();
                                for(int l=0; l<la.length; l++){
                                    System.out.print(la[l][1] + ",");
                                }
                                System.out.println(")");
                            }
                            */
                            substitution(subs, copiedStack);
                            for(PredNode pd: newSent){
                                boolean addToStack = true;
                                for(PredNode pInStack: copiedStack){
                                    if(pd.samePreds(pInStack)){
                                        addToStack = false;
                                    }
                                }
                                if(addToStack){
                                    copiedStack.add(pd);
                                }
                                
                            }
                            boolean[] copyVisited = Arrays.copyOf(visited, visited.length);
                            for(PredNode pnd : sentence){
                                String[][] params = pnd.getParamsList();
                                boolean bre = false;
                                for(int k=0; k<params.length; k++){
                                    if(params[k][0].equals("v") && sentence.size() > 1){
                                        copyVisited[ind] = true;
                                        bre = true;
                                        break;
                                    }
                                }
                                if(bre){
                                    break;
                                }
                            }
                            
                            //System.out.println("Copied Stack Size " + copiedStack.size());
                            //writer4.write("Copied Stack Size ");
                            //writer4.write(copiedStack.size());
                            //writer4.write("\n");
                            /*
                            for(int i=0; i<copiedStack.size(); i++){
                                //writer4.write(copiedStack.get(i).getPredName());
                                //writer4.write("(");
                                
                                System.out.print(copiedStack.get(i).getPredName() + "(");
                                String[][] la = copiedStack.get(i).getParamsList();
                                for(int l=0; l<la.length; l++){
                                    //writer4.write(la[l][1] + ",");
                                    System.out.print(la[l][1] + ",");
                                }
                                System.out.println(")");
                                //writer4.write(")");
                                //writer4.write("\n");
                            }
                            */
                            if(backChaining(copiedStack,copyVisited,depth+1, proven)){
                                //System.out.println("Here " + query.getPredName());
                                /*
                                String[][] a = query.getParamsList();
                                for(int b = 0; b<a.length; b++){
                                    if(a[b][0].equals("v")){
                                        if(checkInProven(proven, query)){
                                            System.out.println("Returning true inner");
                                            return true;
                                        }
                                        System.out.println("(" + a[b][1] + ")");
                                        System.out.println("Returning false");
                                        return false;
                                    }
                                }
                                System.out.println(a[0][0] + " " + a[0][1]);
                                */
                                //System.out.println("Returning true");
                                proven.add(query);
                                return true;
                            }
                        }
                    }
                }
            }
            /*
            System.out.println("F2");
            for(int i=0; i<st.size(); i++){
                System.out.print(st.get(i).getPredName() + "(");
                String[][] la = st.get(i).getParamsList();
                for(int l=0; l<la.length; l++){
                    //writer4.write(la[l][1] + ",");
                    System.out.print(la[l][1] + ",");
                }
                System.out.println(")");
            }
            */
            return false;
        }
        
        //System.out.println("Here1");
        return true;
    }
    
    
    public ArrayList<PredNode> createNewSentence(ArrayList<PredNode> sent, PredNode p){
        ArrayList<PredNode> res = new ArrayList<>();
        for(PredNode pred : sent){
            if (pred == p){
                continue;
            }
            PredNode newPred = pred.createCopy();
            res.add(newPred);
        }
        return res;
    }
    
    public void substitution(HashMap<String,String> subs, ArrayList<PredNode> sentence){
        for(PredNode p: sentence){
            String[][] params = p.getParamsList();
            for(int i=0; i<params.length; i++){
                String newVal = subs.get(params[i][1]);
                if(params[i][0].equals("v") && newVal != null && newVal.charAt(0) <= 90){
                    p.setVarType(i, "c");
                    p.setVarName(i, newVal);
                    //System.out.println("Updated for: " + p.getPredName() + " to: " + p.getParamsList()[i][1]);
                }
                if(params[i][0].equals("v") && newVal != null && newVal.charAt(0) >= 97){
                    p.setVarType(i, "v");
                    p.setVarName(i, newVal);
                    //System.out.println("Updated for: " + p.getPredName() + " to: " + p.getParamsList()[i][1]);
                }
            }
        }
    }
    
    
    public boolean checkInProven(HashSet<PredNode> proven, PredNode a){
        Iterator<PredNode> it = proven.iterator();
        while(it.hasNext()){
            PredNode p = it.next();
            if(p.getPredName().equals(a.getPredName()) && p.getIsPos() == a.getIsPos()){
                String[][] pParams = p.getParamsList();
                String[][] aParams = a.getParamsList();
                for(int i=0; i<aParams.length; i++){
                    if(aParams[i][0].equals("c") && !aParams[i][1].equals(pParams[i][1])){
                        return false;
                    }
                }
                return true;
            }
                
        }
        return false;
    }
    
}
