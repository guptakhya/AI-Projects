import java.util.*;


//comparator class used to enter the nodes in the priority queue
//in increasing order of netPathCost
public class PathComparator implements Comparator<Node>{
    
    public int compare(Node n1, Node n2) {
        if(n1.getNetPathCost() < n2.getNetPathCost()){
            return -1;
        }
        else if(n1.getNetPathCost() > n2.getNetPathCost()){
            return 1;
        }
        return 0;
    }
}