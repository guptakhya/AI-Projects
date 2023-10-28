import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.*;
import java.io.IOException;

public class ConvertToCnf{
    private HashMap<String,ArrayList<Integer>> kbCnf;
    private HashMap<String,ArrayList<PredNode>> kbConclude;
    private String toConvert;
    private int varCount;
    private HashMap<Integer,ArrayList<PredNode>> sentenceInd;
    private Integer sentIndCounter;
    
    public ConvertToCnf(String inp, HashMap<String,ArrayList<Integer>> kb, int counter, HashMap<String, ArrayList<PredNode>> kbInfer, HashMap<Integer,ArrayList<PredNode>> sentenceIndex, Integer indCounter){
        //Scanner 
        varCount = counter;
        kbCnf = kb;
        kbConclude = kbInfer;
        toConvert = inp;
        sentenceInd = sentenceIndex;
        sentIndCounter = indCounter;
        convertToClauses(toConvert);

    }
    
    private void convertToClauses(String sImp){
        
        Scanner scImp = new Scanner(sImp);
        boolean hasImplication = false;
        ClauseNode clauseAfterImp = null;
        String prev = scImp.next();
        ArrayList<String> tmpList = new ArrayList<>();
        tmpList.add(prev);
        ArrayList<ClauseNode> clausesSeparatedByOR = new ArrayList<>();
        while(scImp.hasNext()){
            String word = scImp.next();
            if(word.equals("&")){
                //String addToList = scImp.next();
                tmpList.add(scImp.next());
            }
            else if(word.equals("|")){
                ClauseNode clNode = new ClauseNode(tmpList);
                clausesSeparatedByOR.add(clNode);
                tmpList = new ArrayList<>();
                tmpList.add(scImp.next());
            }
            else{ 
                if(word.equals("=>")){
                    hasImplication = true;
                    break;
                    
                }
                
            }
        }
        ClauseNode clNode = new ClauseNode(tmpList);
        //System.out.println(tmpList);
        //System.out.println(clNode.getPredsList().);
        clausesSeparatedByOR.add(clNode);
        
        if(hasImplication){
            tmpList = new ArrayList<>();
            tmpList.add(scImp.next());

            clauseAfterImp = new ClauseNode(tmpList);
            
            processImplicationKb(clausesSeparatedByOR, clauseAfterImp);
        }
        
        
        //System.out.println("Num Clasuses in List " + clausesSeparatedByOR.size());
        //System.out.println("Has Implication? " + hasImplication);
        //if(hasImplication){
        //    System.out.println("Clause after implication " + clauseAfterImp.getPredNodesList().get(0).getPredName());
            //clausesSeparatedByOR.add(clauseAfterImp);
        //} 
        else {
            processNonImplicationSetence(clausesSeparatedByOR);
            //kbCnf.add(clausesSeparatedByOR);
        }
    }

    
    private void processImplicationKb(ArrayList<ClauseNode> beforeImpClauses, ClauseNode afterImpClause){
        for(int i=0; i<beforeImpClauses.size(); i++){
            negate(beforeImpClauses.get(i));
            ArrayList<ClauseNode> dividedList = new ArrayList<>();
            dividedList.add(beforeImpClauses.get(i));
            //dividedList.add(afterImpClause);
            removeRedundancy(dividedList);
            cleanClauses(dividedList);
            if(dividedList.size() != 0){
                //kbCnf.add(dividedList);
                ArrayList<PredNode> appendToKbPreds = new ArrayList<>();
                for(ClauseNode k : dividedList){
                    appendToKbPreds.addAll(k.getPredNodesList());
                    //System.out.println(appendToKbPreds.size());
                }
                PredNode afterImpp = afterImpClause.getPredNodesList().get(0).createCopy();
                appendToKbPreds.add(afterImpp);
                //updateVars(appendToKbPreds);
                addToKbMap(appendToKbPreds);
                //System.out.println(kbCnf.size());
                //kbCnf.add(appendToKbPreds);
            }
        }
        
    }

    private void negate(ClauseNode clause){
        ArrayList<PredNode> predLst = clause.getPredNodesList();
        for(PredNode p : predLst){
            p.setIsPos(!p.getIsPos());
        }
    }
    
    
    private void processNonImplicationSetence(ArrayList<ClauseNode> clauses){
        
        ArrayList<PredNode> preds = clauses.get(0).getPredNodesList();
        
        if(clauses.size() == 1 && preds.size() == 1){
            String predName = preds.get(0).getPredName();
            ArrayList<PredNode> predList = kbConclude.get(predName);
            if(predList == null){
                predList = new ArrayList<>();
            }
            predList.add(preds.get(0));
            kbConclude.put(predName, predList);
        }
        //else{
        
            Queue<ArrayList<PredNode>> q = new LinkedList<>();
            for(PredNode p : preds){
                ArrayList<PredNode> individualClause = new ArrayList<>();
                individualClause.add(p);
                q.add(individualClause);
            }

            for(int i=1; i<clauses.size(); i++){
                ClauseNode currClause = clauses.get(i);
                preds = currClause.getPredNodesList();
                int currQSize = q.size();
                for(int j = 0; j < currQSize; j++){
                    ArrayList<PredNode> appendTo = q.poll();
                    ArrayList<PredNode> predsOfLaterClauses = currClause.getPredNodesList();
                    for(PredNode pn : predsOfLaterClauses){
                        ArrayList<PredNode> exsistingPredsInClause = new ArrayList<>();
                        exsistingPredsInClause = (ArrayList)appendTo.clone();
                        exsistingPredsInClause.add(pn);
                        q.add(exsistingPredsInClause);
                    }
                }
            }

            while(!q.isEmpty()){
                ClauseNode addToKb = new ClauseNode(q.poll(),1);
                ArrayList<ClauseNode> listOfClause = new ArrayList<>();
                listOfClause.add(addToKb);
                removeRedundancy(listOfClause);
                cleanClauses(listOfClause);
                if(listOfClause.size() != 0){
                    //kbCnf.add(listOfClause);
                    ArrayList<PredNode> appendToKbPreds = new ArrayList<>();
                    for(ClauseNode k : listOfClause){
                        ArrayList<PredNode> arrPreds = k.getPredNodesList();
                        appendToKbPreds.addAll(arrPreds);
                    }
                    
                    addToKbMap(appendToKbPreds);
                    //kbCnf.add(appendToKbPreds);
                }
            }
        
        
    }
    
    public boolean isPredicate(String toCheck){
        for(int i=0; i<toCheck.length(); i++){
            if(toCheck.charAt(i) == '('){
                return true;
            }
        }
        return false;
    }
    
    public boolean isVar(String toCheck){
        if(toCheck.charAt(0) >= 97 && toCheck.charAt(0) <= 122){
            return true;
        }
        return false;
    }
    
    public boolean isConst(String toCheck){
        if(toCheck.charAt(0) >= 65 && toCheck.charAt(0) <= 90){
            return true;
        }
        return false;
    }
    
    public void removeRedundancy(ArrayList<ClauseNode> clList){
        ArrayList<PredNode> preds = new ArrayList<>();
        HashMap<PredNode, ClauseNode> mapper = new HashMap<>();
        for(int i=0; i<clList.size(); i++){
            ArrayList<PredNode> temp = clList.get(i).getPredNodesList();
            for(PredNode p : temp){
                mapper.put(p,clList.get(i));
                preds.add(p);
            }
        }
        HashSet<Integer> skipThem = new HashSet<>();
        for(int i=0; i<preds.size(); i++){
            boolean remove = false;
            
            if(skipThem.contains(i)){
                //System.out.println("Entered Here");
                continue;
            }
            PredNode currNode = preds.get(i);
            
            for(int j=i+1; j<preds.size();j++){
                if(skipThem.contains(j)){
                    //System.out.println("Entered Here too");
                    continue;
                }
                PredNode innerLoopNode = preds.get(j);
                if(currNode.samePreds(innerLoopNode)){
                    //System.out.println("CurrNode in remove Red: " + currNode.getPredName());
                    //System.out.println("j Node in remove Red: " + innerLoopNode.getPredName());
                    if(currNode.getIsPos() != innerLoopNode.getIsPos()){
                        remove = true;
                    }
                    ClauseNode removeFrom = mapper.get(innerLoopNode);
                    //System.out.println("remove 1");
                    removeFrom.removePredFromClause(innerLoopNode);
                    //System.out.println("remove 2");
                    //System.out.println("Array Size: " + removeFrom.getPredNodesList().size());
                    skipThem.add(j);
                    
                }
            }
            if(remove){
                ClauseNode removeFromCurr = mapper.get(currNode);
                //System.out.println("remove 3");
                removeFromCurr.removePredFromClause(currNode);
                //System.out.println("remove 4");
                //System.out.println("Array Size: " + removeFromCurr.getPredNodesList().size());
            }
        }
    }
    
    
    
    public void cleanClauses(ArrayList<ClauseNode> clauseListToClean){
        //System.out.println("Cleaning clauses");
        for(int i=0; i<clauseListToClean.size(); i++){
            ClauseNode cl = clauseListToClean.get(i);
            if(cl.getPredNodesList().size() == 0){
                //System.out.println("Tried Cleaning");
                clauseListToClean.remove(cl);
                i--;
                //System.out.println("Cleaning Successful");
            }
        }
    }
    
    private void updateVars(ArrayList<PredNode> arr){
        HashMap<String, String> mapper = new HashMap<>();
        ArrayList<PredNode> newParams = new ArrayList<>();
        for(PredNode p: arr){
            String[][] params = p.getParamsList();
            //System.out.println("Size : " + params.length);
            for(int i=0; i<params.length; i++){
                if(params[i][0].equals("v")){
                    String val = mapper.get(params[i][1]);
                    if(val == null){
                        String newVar = "v"+varCount;
                        varCount++;
                        mapper.put(params[i][1],newVar);
                        p.setVarName(i,newVar);
                    }
                    else{
                        p.setVarName(i,val);
                    }
                }
            }
        }
    }
    
    public int getCounter(){
        return varCount;
    }
    
    private void addToKbMap(ArrayList<PredNode> pList){
        if(isDuplicateSentence(pList)){
            return;
        }
        updateVars(pList);
        HashSet<String> addedAlready = new HashSet<>();
        sentenceInd.put(sentIndCounter, pList);
        for(PredNode p : pList){
            String pName = p.getPredName();
            if(addedAlready.contains(pName)){
                continue;
            }
            
            ArrayList<Integer> mappedList = kbCnf.get(pName);
            if(mappedList == null){
                mappedList = new ArrayList<>();
            }
            mappedList.add(sentIndCounter);
            addedAlready.add(pName);
            kbCnf.put(pName, mappedList);
            
        }
        sentIndCounter++;
    }
    
    public int getSentIndCounter(){
        return sentIndCounter;
    }
    
    public boolean isDuplicateSentence(ArrayList<PredNode> predList){
        HashMap<Integer, Integer> sentenceCount = new HashMap<>();
        for(PredNode p: predList){
            ArrayList<Integer> sentIndices = kbCnf.get(p.getPredName());
            if(sentIndices == null){
                continue;
            }
            for(Integer i : sentIndices){
                ArrayList<PredNode> sentencePreds = sentenceInd.get(i);
                for(PredNode pnd: sentencePreds){
                    if(p.duplicatePreds(pnd)){
                        Integer count = sentenceCount.get(i);
                        if(count == null){
                            count = 0;
                        }
                        if(count+1 == predList.size()){
                            if(predList.size() >= sentencePreds.size()){
                                return true;
                            }
                            else{
                                //replace exsiting sentence
                                updateVars(predList);
                                sentenceInd.put(i, predList);
                                return true;
                            }
                        }
                        sentenceCount.put(i,count+1);
                        break;
                    }
                }
                
            }
        }
        return false;
    }
}

