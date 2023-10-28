import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.*;
import java.io.IOException;


public class Node {
    private int elevation;
    private int confirmedPathCost; //total computed path distance so far
    private int netPathCost; // will be used to compute the priority of the node, includes heuristic distance
    private Integer[] coordinates;
    private Node parent;
    private boolean isBlocker;
    private int momentumWhenVisited;
    
    public Node(Integer[] coord, int ele){
        coordinates = coord;
        elevation = ele;
        confirmedPathCost = 0;
        netPathCost = 0;
        parent = null;
        momentumWhenVisited = 0;
    }
    
    public void setParent(Node par){
        parent = par;
    }
    
    public void setNetPathCost(int pc){
        netPathCost = pc;
    }
    
    public void setConfirmedPathCost(int cpc){
        confirmedPathCost = cpc;
    }
    
    public void setMomentumWhenVisited(int M){
        momentumWhenVisited = M;
    }
    
    public Integer[] getCoordinates(){
        return coordinates;
    }
    
    public int getElevation(){
        return elevation;
    }
    
    public int getConfirmedPathCost(){
        return confirmedPathCost;
    }
    
    public int getNetPathCost(){
        return netPathCost;
    }
    
    public Node getParent(){
        return parent;
    }
    
    public int getMomentumWhenVisited(){
        return momentumWhenVisited;
    }
}