import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.*;
import java.io.IOException;


public class FileReader {
    
    private File inFile;
    private String algo;
    private int stamina;
    private int startingX;
    private int startingY;
    private int W;
    private int H;
    private int numLodges;
    private int[][] lodgeLocs;
    private int[][] map;
    private Node[][] visited;
    
    public FileReader(String filename) throws IOException{
        
        inFile = new File(filename);
        try (Scanner in = new Scanner(inFile)){
            algo = in.nextLine();
            W = in.nextInt();
            H = in.nextInt();
            in.nextLine();
            startingX = in.nextInt(); //actually col index
            startingY = in.nextInt(); // actually row index
            in.nextLine();
            stamina = in.nextInt();
            in.nextLine();
            numLodges = in.nextInt();
            in.nextLine();

            lodgeLocs = new int[numLodges][2];
            for(int i=0; i<numLodges; i++){
                lodgeLocs[i][0] = in.nextInt();
                lodgeLocs[i][1] = in.nextInt();

                in.nextLine();
            }

            map = new int[H][W];
            for(int j=0; j<H; j++){
                for(int k=0; k<W; k++){
                    map[j][k] = in.nextInt();
                }
                
                if(in.hasNextLine()){
                    in.nextLine();
                }
            }
            
            visited = new Node[H][W];
        }
       
        
    }
    
    
    public void runAlgo(){
        try{
            //create ouput file
            File opFile = new File("output.txt");

            //if o/p file exists, delete and create it again to clear it of any prev content
            if(!(opFile.createNewFile())){
                opFile.delete();
                opFile = new File("output.txt");
            }

            FileWriter writer = new FileWriter(opFile);
            
            if(startingY < 0 || startingX < 0 || map[startingY][startingX] < 0 || stamina < 0 ){
                writer.write("Invalid Input");
            }
            else{
                if(algo.equals("BFS")){
                    for(int i=0; i<numLodges; i++){
                        visited = new Node[H][W]; //resetting visited array for each iteration 
                        writer.write(runBFS(lodgeLocs[i][0], lodgeLocs[i][1]));
                        if(i<numLodges-1){
                            writer.write("\n");
                        }
                    }
                }
                else if(algo.equals("UCS")){
                    for(int i=0; i<numLodges; i++){
                        visited = new Node[H][W];
                        writer.write(runUCS(lodgeLocs[i][0], lodgeLocs[i][1]));
                        if(i<numLodges-1){
                            writer.write("\n");
                        }
                    }
                }
                else{
                    for(int i=0; i<numLodges; i++){
                        visited = new Node[H][W];
                        writer.write(runAStar(lodgeLocs[i][0], lodgeLocs[i][1]));
                        if(i<numLodges-1){
                            writer.write("\n");
                        }
                    }
                }
            }
            
            writer.close();
        }
        
        catch(Exception e) {
            System.err.println(e);
        }
        
    }
    
    //run BFS for the lodge
    private String runBFS(int lodgeCol, int lodgeRow){
        
        Node rootNode = createRoot();
        Queue<Node> q = new LinkedList<>(); 
        q.add(rootNode);
        HashMap<String, Node> inQ = new HashMap<>();
        String rootString = startingY + "." + startingX;
        inQ.put(rootString, rootNode);
        
        while(!q.isEmpty()){
            
            Node currNode = q.poll();
            Integer[] top = currNode.getCoordinates();
            inQ.remove(top[0] + "." + top[1]);
            if(top[0] == lodgeRow && top[1] == lodgeCol){
                String strFinal = getPath(currNode, "");
                return strFinal + top[1] + "," + top[0];
            }
            visited[top[0]][top[1]] = currNode;
            
            addValidNeighbours(q, top[0], top[1], currNode, 0, lodgeRow, lodgeCol, inQ);
        }
        
        return "FAIL";
    }
    
    //run UCS for the lodge
    private String runUCS(int lodgeCol, int lodgeRow){
        
        Node rootNode = createRoot();
        
        PriorityQueue<Node> priQ = new PriorityQueue<Node>(new PathComparator()); 
        priQ.add(rootNode);
        HashMap<String, Node> inQ = new HashMap<>();
        String rootString = startingY + "." + startingX;
        inQ.put(rootString, rootNode);
        
        while(!priQ.isEmpty()){
            
            Node currNode = priQ.poll();
            Integer[] top = currNode.getCoordinates();
            inQ.remove(top[0] + "." + top[1]);
            if(top[0] == lodgeRow && top[1] == lodgeCol){
                String strFinal = getPath(currNode, "");
                return strFinal + top[1] + "," + top[0];
            }
            visited[top[0]][top[1]] = currNode;
            
            addValidNeighbours(priQ, top[0], top[1], currNode, 0, lodgeRow, lodgeCol, inQ);
        }
        
        return "FAIL";
    }
    
    
    //run A* for the lodge
    private String runAStar(int lodgeCol, int lodgeRow){
        
        Node rootNode = createRoot();
        PriorityQueue<Node> priQ = new PriorityQueue<Node>(new PathComparator()); 
        priQ.add(rootNode);
        HashMap<String, Node> inQ = new HashMap<>();
        String rootString = startingY + "." + startingX;
        inQ.put(rootString, rootNode);
        
        while(!priQ.isEmpty()){
            
            Node currNode = priQ.poll();
            
            Integer[] top = currNode.getCoordinates();
            inQ.remove(top[0] + "." + top[1]);
            
            if(top[0] == lodgeRow && top[1] == lodgeCol){
                String strFinal = getPath(currNode, "");
                return strFinal + top[1] + "," + top[0];
            }
            Node visNode = visited[top[0]][top[1]];
            
            Node prev = currNode.getParent();
            int currEle = Math.abs(currNode.getElevation());
            int prevEle = 0;
            if(prev != null){
                prevEle = Math.abs(prev.getElevation());
            }
            
            int momentum = Math.max(0, prevEle - currEle);
            if(visNode != null && visNode.getConfirmedPathCost() <= currNode.getConfirmedPathCost() && visNode.getMomentumWhenVisited() > momentum){
                continue;
            }
            visited[top[0]][top[1]] = currNode;
            addValidNeighbours(priQ, top[0], top[1], currNode, momentum, lodgeRow, lodgeCol, inQ);
            
        }
        
        return "FAIL";
    }
    
    //check if a move is valid
    private boolean isMoveAllowed(int currX, int currY, int newX, int newY, int M){
        
        if(map[newX][newY] < 0){
            if(Math.abs(map[newX][newY]) > Math.abs(map[currX][currY])){
                return false;
            }
        }
        else{
            if(algo.equals("A*")){
                if(map[newX][newY] - Math.abs(map[currX][currY]) > stamina + M){
                    return false;
                }
            }
            else {
                if(map[newX][newY] - Math.abs(map[currX][currY]) > stamina){
                    return false;
                }
            }
        }
        return true;
    }
    
    //method to create the rootNode
    private Node createRoot(){
        //startingY is the rowVal and startingX is colVal 
        Integer[] root = new Integer[2];
        root[0] = startingY;
        root[1] = startingX;
        
        Node rootNode = new Node(root, map[startingY][startingX]);
        return rootNode;
    }
    
    //for a given node, finds the neighbours (i ranges from i-1 to i+1 and j from j-1 to j+1)
    //checks if going to that neighbour is an allowed move
    //checks if that neighbour wasa previously visited but should be revisited
    //and adds them in the queue
    //q1 - queue to add neighbours to
    //currX, currY - x,y coordinates respectively of the current node
    //par - the current node aka the parent node to these neighbours
    //M - momentum
    //destX, destY - coordinates for the lodge
    private void addValidNeighbours(Queue<Node> q1, int currX, int currY, Node par, int M, 
                                    int destX, int destY, HashMap<String,Node> inQueue){
        
        for(int i= Math.max(0,currX-1); i<=Math.min(H-1,currX+1); i++){
            for(int j= Math.max(0,currY-1); j<=Math.min(W-1,currY+1); j++){
                if(i != currX || j != currY){ //to exclude current Node
                    int M_new = M;
                    int newEle = Math.abs(map[i][j]);
                    int currEle = Math.abs(par.getElevation());
                    if(newEle - currEle <= 0){
                        M_new = 0;
                    }
                    if(isMoveAllowed(currX,currY,i,j,M_new)){
                        Integer[] node = new Integer[2];
                        node[0] = i;
                        node[1] = j;
                        Node neighbour = new Node(node, newEle);
                        neighbour.setParent(par);
                        if(!algo.equals("BFS")){
                            int nodePathCost = par.getConfirmedPathCost();
                            if(i == currX || j == currY){
                                nodePathCost += 10;
                            }
                            else{
                                nodePathCost += 14;
                            }
                            int elevationCost = 0;
                            int heuristicCost = 0;
                            if(algo.equals("A*")){
                                elevationCost = newEle - currEle <= M_new ? 0 : Math.max(0, newEle - currEle - M_new);
                                heuristicCost = getHeuristicValue(destX, destY, i, j);
                                
                            }
                            int confirmCost = nodePathCost + elevationCost;
                            int estimatedPathCost = confirmCost + heuristicCost;

                            if(shouldAddToQueue(i, j, inQueue, confirmCost, estimatedPathCost, par)){
                                neighbour.setConfirmedPathCost(nodePathCost + elevationCost);
                                neighbour.setNetPathCost(estimatedPathCost);
                                neighbour.setMomentumWhenVisited(M_new);
                                q1.add(neighbour);
                                String key = i + "." + j;
                                inQueue.put(key,neighbour);
                            }
                        }
                        else{
                            if(visited[i][j] == null){
                                q1.add(neighbour);
                            }                            
                        }
                        
                    }
                }
            }
        }
    }
    
    //heurtistic function
    //Takes the distance euclidean distance between 2 points
    //eqn - sqrt((y2-y1)^2 + (x2-x1)^2)
    private int getHeuristicValue(int lodgeX, int lodgeY, int nodeX, int nodeY){
        double distYSq = Math.pow((lodgeY - nodeY),2);
        double distXSq = Math.pow((lodgeX - nodeX),2);
        return (int)Math.sqrt(distYSq + distXSq);
    }    
    
    //get path for the output, recursive method
    private String getPath(Node curr, String conString){
        Integer[] currCoords = curr.getCoordinates();
        if(curr.getParent() == null){
            return conString;
        }
        else{
            Node parent = curr.getParent();
            conString += getPath(parent, conString);
            conString += parent.getCoordinates()[1] + "," + parent.getCoordinates()[0] + " ";
            //System.out.println(conString);
            return conString;
        }
    }
    
    
    private boolean shouldAddToQueue(int i, int j, HashMap<String, Node> inQu, 
                                     int comparisonPathCost, int estNewCost, Node curr){
        if(visited[i][j] == null){
            String key = i + "." + j;
            if(inQu.get(key) == null){
                return true;
            }
            else if(inQu.get(key).getConfirmedPathCost() > comparisonPathCost){
                return true;
            }
            else if(algo.equals("A*") && inQu.get(key).getMomentumWhenVisited() < (Math.abs(curr.getElevation()) - Math.abs(inQu.get(key).getElevation()))){
                return true;
            }
        }
        else if(visited[i][j].getNetPathCost() > estNewCost){
            return true;
        }
        else if(visited[i][j].getConfirmedPathCost() <= comparisonPathCost && visited[i][j].getMomentumWhenVisited() < (Math.abs(curr.getElevation()) - Math.abs(visited[i][j].getElevation()))){
            return true;
        }
        return false;
    }
    
    /*
    
    public int getNumLodges(){
        return numLodges;
    }
    
    public int getStamina(){
        return stamina;
    }
    
    public String algo(){
        return algo;
    }
    
    public void printMap(){
        for(int i=0; i<H; i++){
            //System.out.println(map[i].toString());
            for(int j=0; j<W; j++){
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    public void printLodgeLocs(){
        for(int i=0; i<numLodges; i++){
            //System.out.println(map[i].toString());
            for(int j=0; j<2; j++){
                System.out.print(lodgeLocs[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    */
}


