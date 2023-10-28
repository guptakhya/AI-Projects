import java.util.*;
import java.lang.*;
import java.io.IOException;


public class ClauseNode {
    private ArrayList<PredNode> predNodesList; //total computed path distance so far
    
    
    public ClauseNode(ArrayList<String> listOfPreds){
        
        predNodesList = new ArrayList<>();
        for(String a : listOfPreds){
            if(!a.equals("")){
                //System.out.println(a);
                PredNode pNode = new PredNode(a);
                //System.out.println("Yes");
                //System.out.println("pNode.name " + pNode.getPredName());
                predNodesList.add(pNode);
                //System.out.println("Yes please");
            }
            
        }
    }
    
    public ClauseNode(PredNode pred){
        predNodesList = new ArrayList<>();
        predNodesList.add(pred);
    }
    
    public ClauseNode(ArrayList<PredNode> preds, int i){
        predNodesList = new ArrayList<>();
        predNodesList = preds;
    }
    
    public ArrayList<PredNode> getPredNodesList(){
        return predNodesList;
    }
    
    public void removePredFromClause(PredNode pred){
        predNodesList.remove(pred);
    }
}