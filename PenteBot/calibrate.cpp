#include <iostream>
#include <unistd.h>
#include <string.h>
#include <string>
#include <stdlib.h>
#include <stdio.h>
#include <vector>
#include <fstream>
#include <sstream>
#include <stack>
#include <bits/stdc++.h>
#include <queue>
#include <algorithm>
#include <chrono>
#include <float.h>


using namespace std;
using namespace std::chrono;

class node{
    public:
    int row;
    int col;
    node* parent;
    //stack<int> eval;
    string boardVal;
    stack<string> playerCap;
    stack<vector<pair<int,int> > > captures;
    node(int i, int j, string boardValue){
        row = i;
        col = j;
        parent = NULL;
        boardVal = boardValue;
    }
    
    
};

int numCapture(node ***board, int x, int y, string me, string opp);
bool wouldWin(node ***board, int x, int y, string player);
string convertToOutputFormat(int x, int y);
int eval(node ***board, string orgMe, string player, string opp, int capBlack, int capWhite, int numC);
bool allowedMove(node ***board, int x, int y, int numWhite, int numBlack, string player);
void playGame(node ***board, int numBlack, int numWhite, int numVac, int capBlack, int capWhite, float timeLeft, string player, string opp, int startI, int startJ, int numC, int d);
void makeQueue(node ***board, queue<node *> &q, int numWhite, int numBlack, string player);
pair<int, int> updatePriorities(node ***board, int x, int y, string orgMe, string me, string opp, int capBlack, int capWhite, int numWhite, int numBlack);
pair<int, node*> runMinMax(node ***board, string playerOrg, string playerCurr, string opp, int alpha, int beta, int depth, int numW, int numB, int capW, int capB, int r, int c, int numCaps);
void undoMove(node ***board, int x, int y, string playerCurr, int &numW, int &numB, int &capW, int &capB, int numCaps);
void playMove(node ***board, int x, int y, string playerCurr, string opp, int &numW, int &numB, int &capW, int &capB, int &numCaps, bool &wins);
vector<int> openFoursAndThrees(node ***board, string player);
node* moveNearBy(node ***board, string player, int numW, int numB);

int main(){
    ofstream calib;
    calib.open("calibration.txt");
    for(int d = 1; d<=3; d++){
        auto start = high_resolution_clock::now();
        ifstream input;
        input.open("input.txt");

        string playerColour;
        getline(input, playerColour);
        string player;
        string opponent;
        if(playerColour == "BLACK"){
            player = "b";
            opponent = "w";
        }
        else{
            player = "w";
            opponent = "b";
        }
        float timeLeft;
        input >> timeLeft;
        //cout << "Time left " << timeLeft << endl;
        int capturedWhite;
        input >> capturedWhite;
        //cout << "Captured White " << capturedWhite << endl;
        string wasteComma;
        getline(input,wasteComma);
        string trim = wasteComma.substr(1);
        int capturedBlack;
        istringstream(trim) >> capturedBlack;
        //cout << "Captured Black " << capturedBlack << endl;

        string line;
        string **boardStrings;
        boardStrings = new string *[19];
        node ***board;
        board = new node **[19];
        int numVacant = 0;
        int numBlack = 0;
        int numWhite = 0;

        for (int i=0; i<19; i++) {
            getline(input,line);
            string *rows;
            rows = new string[19];
            node **rowNodes;
            rowNodes = new node *[19];
            for (int j=0; j<19; j++){
                rows[j] = line[j];
                node *newNode = new node(i,j, rows[j]);
                rowNodes[j] = newNode;
                if(rows[j] == "."){
                    numVacant++;
                }
                else if(rows[j] == "w"){
                    numWhite++;
                }
                else{
                    numBlack++;
                }
            }
            boardStrings[i] = rows;
            board[i] = rowNodes;
            //cout << board[i] << endl;
        }
        input.close();
        int maxEval = INT_MIN;
        int startI = -1;
        int startJ = -1;
        int numC = 0;
        for (int i=0; i<19; i++) {
            for (int j=0; j<19; j++){
                if(board[i][j]->boardVal == "."){
                    int numCaps = 0;
                    bool wins = false;
                    bool lose = false;
                    int nc = 0;
                    int val = INT_MIN;
                    playMove(board, i, j, opponent, player, numWhite, numBlack, capturedWhite, capturedBlack, nc, lose);
                    undoMove(board, i, j, opponent, numWhite, numBlack, capturedWhite, capturedBlack, nc);
                    if(lose == true){
                        val = INT_MAX-1;
                        //cout << "here at " << x << " " << y << endl; 
                    }
                    playMove(board, i, j, player, opponent, numWhite, numBlack, capturedWhite, capturedBlack, numCaps, wins);
                    if(wins == true){
                        val = INT_MAX;
                    }
                    else {
                        val = max(val, eval(board, player, player, opponent, capturedBlack, capturedWhite, numCaps));
                    }
                    //board[i][j]->eval.push(val);
                    if(val > maxEval){
                        maxEval = val;
                        startI = i;
                        startJ = j;
                        numC = numCaps;
                    }
                    undoMove(board, i, j, player, numWhite, numBlack, capturedWhite, capturedBlack, numCaps);
                }   
            }
        }
        queue<node*> q;
        makeQueue(board, q, numWhite, numBlack, player);
        //cout << "top " << q.front() << endl;
        //cout << "Start I " << startI << endl << "Start J " << startJ << endl;
        playGame(board, numBlack, numWhite, numVacant, capturedBlack, capturedWhite, timeLeft, player, opponent, startI, startJ, numC, d);
        
        auto stop = high_resolution_clock::now();
        auto duration = duration_cast<microseconds>(stop - start);
        auto ms = duration.count();
        calib << ms << endl;
    }
    calib.close();

    return 0;
}

//checks whether placing on a certain location leads to a capture
int numCapture(node ***board, int x, int y, string me, string opp){
    int caps = 0;
    vector<pair<int, int> > capDeets;
    if((x+1<19 && board[x+1][y]->boardVal == opp) && (x+2<19 && board[x+2][y]->boardVal == opp) && (x+3<19 && board[x+3][y]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x+1,y));
        capDeets.push_back(make_pair(x+2,y));
    }
    if((y+1<19 && board[x][y+1]->boardVal == opp) && (y+2<19 && board[x][y+2]->boardVal == opp) && (y+3<19 && board[x][y+3]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x,y+1));
        capDeets.push_back(make_pair(x,y+2));
    }
    if((y-1>=0 && board[x][y-1]->boardVal == opp) && (y-2>=0 && board[x][y-2]->boardVal == opp) && (y-3>=0 && board[x][y-3]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x,y-1));
        capDeets.push_back(make_pair(x,y-2));
    }
    if((x-1>=0 && board[x-1][y]->boardVal == opp) && (x-2>=0 && board[x-2][y]->boardVal == opp) && (x-3>=0 && board[x-3][y]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x-1,y));
        capDeets.push_back(make_pair(x-2,y));
    }
    if((x-1>=0 && y-1>=0 && board[x-1][y-1]->boardVal == opp) && (x-2>=0 && y-2>=0 && board[x-2][y-2]->boardVal == opp) && (x-3>=0 && y-3>=0 && board[x-3][y-3]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x-1,y-1));
        capDeets.push_back(make_pair(x-2,y-2));
    }
    if((x-1>=0 && y+1<19 && board[x-1][y+1]->boardVal == opp) && (x-2>=0 && y+2<19 && board[x-2][y+2]->boardVal == opp) && (x-3>=0 && y+3<19 && board[x-3][y+3]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x-1,y+1));
        capDeets.push_back(make_pair(x-2,y+2));
    }
    if((x+1<19 && y+1<19 && board[x+1][y+1]->boardVal == opp) && (x+2<19 && y+2<19 && board[x+2][y+2]->boardVal == opp) && (x+3<19 && y+3<19 && board[x+3][y+3]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x+1,y+1));
        capDeets.push_back(make_pair(x+2,y+2));
    }
    if((x+1<19 && y-1>=0 && board[x+1][y-1]->boardVal == opp) && (x+2<19 && y-2>=0 && board[x+2][y-2]->boardVal == opp) && (x+3<19 && y-3>=0 && board[x+3][y-3]->boardVal == me)){
        caps++;
        capDeets.push_back(make_pair(x+1,y-1));
        capDeets.push_back(make_pair(x+2,y-2));
    }
    if(caps > 0){
        board[x][y]->playerCap.push(opp);
        board[x][y]->captures.push(capDeets);
    }

    return caps*2;
}

bool wouldWin(node ***board, int x, int y, string player){
    
    int continuous = 0;
    int jMin = max(0, y-4);
    int jMax = min(18, y+4);
    int iMin = max(0, x-4);
    int iMax = min(18, x+4);
    for(int j=jMin; j<=jMax; j++){
        if(j == y){
            continuous++;
        }
        else{
            if(board[x][j]->boardVal != player){
                continuous = 0;
            }
            else continuous++;
        }
        
        if(continuous == 5){
            return true;
        }
    }
    continuous = 0;
    for(int i=iMin; i<=iMax; i++){
        if(i == x){
            continuous++;
        }
        else {
            if(board[i][y]->boardVal != player){
                continuous = 0;
            }
            else continuous++;
        }
        
        if(continuous == 5){
            return true;
        }
        
    }
    continuous = 0;
    for(int k=-4; k<5; k++){
        if(k == 0){
            continuous++;
        }
        else{
            if(x+k>=0 && x+k<19 && y+k>=0 && y+k<19){
                if(board[x+k][y+k]->boardVal != player){
                    continuous = 0;
                }
                else continuous++;
            }
        }
        
        if(continuous == 5){
            return true;
        }
    }
    
    continuous = 0;
    for(int k=-4; k<5; k++){
        if(k == 0){
            continuous++;
        }
        else{
            if(x+k>=0 && x+k<19 && y-k>=0 && y-k<19){
                if(board[x+k][y-k]->boardVal != player){
                    continuous = 0;
                }
                else continuous++;
            }
        }
        
        if(continuous == 5){
            return true;
        }
    }
    return false;
}


//evaluation function
int eval(node ***board, string orgMe, string player, string opp, int capBlack, int capWhite, int numC){
    int totalVal = 0;
    
    int capturePts = 0;
    
    if(orgMe == "w") {
        capturePts = capWhite - capBlack;
    }
    else{
        capturePts = capBlack - capWhite;
    }
    
    int openThreeDiff = 0;
    int openFourDiff = 0;
    int countOneToWinDiff = 0;
    if(orgMe == "w") {
        //capturePts = capWhite - capBlack;
        if(capBlack >= 10){
            return INT_MIN;
        }
        vector<int> openFoursAndThreePlayer = openFoursAndThrees(board,"w");
        vector<int> openFoursAndThreeOpp = openFoursAndThrees(board, "b");
        
        openFourDiff = openFoursAndThreePlayer[0] - openFoursAndThreeOpp[0];
        openThreeDiff = openFoursAndThreePlayer[1] - openFoursAndThreeOpp[1];
        countOneToWinDiff = openFoursAndThreePlayer[2] - openFoursAndThreeOpp[2];
    }
    else{
        //capturePts = capBlack - capWhite;
        
        if(capWhite >= 10){
            return INT_MIN;
        }
        vector<int> openFoursAndThreePlayer = openFoursAndThrees(board,"b");
        vector<int> openFoursAndThreeOpp = openFoursAndThrees(board, "w");
        
        openFourDiff = openFoursAndThreePlayer[0] - openFoursAndThreeOpp[0];
        openThreeDiff = openFoursAndThreePlayer[1] - openFoursAndThreeOpp[1];
        countOneToWinDiff = openFoursAndThreePlayer[2] - openFoursAndThreeOpp[2];
    }
    if(numC > 0){
        totalVal += capturePts*140;
    }
    
   
    totalVal += openFourDiff*170;
    totalVal += openThreeDiff*60;
    totalVal += countOneToWinDiff*20;
    
    return totalVal;
}


//convert to output format
string convertToOutputFormat(int x, int y){
    int num = 19-x;
    string cols[19] = {"A","B","C","D","E","F","G","H", "J","K","L","M","N","O","P","Q","R","S","T"};
    string finalString;
    ostringstream ss;
    ss << num;
    finalString += ss.str();
    finalString += cols[y];
    return finalString;
}


//checks if a given index i,j is an allowed move or not
bool allowedMove(node ***board, int x, int y, int numWhite, int numBlack, string player){
    if(numWhite == 1 && numBlack == 1 && player == "w"){
         if(x>=7 && x <= 11 && y>= 7 && y<=11){
             return false;
         }
    }
    if(board[x][y]->boardVal != "."){
        return false;
    }
    return true;
}

void playGame(node ***board, int numBlack, int numWhite, int numVac, int capBlack, int capWhite, float timeLeft, string player, string opp, int startI, int startJ, int numC, int d){
    /*
    ifstream caliber;
    caliber.open("calibration.txt");
    float d1 = 0.0;
    caliber >> d1;
    d1 = d1/1000000;
    float d2 = 0.0;
    caliber >> d2;
    d2 = d2/1000000;
    float d3 = 0.0;
    caliber >> d3;
    d3 = d3/1000000;
    caliber.close();
    int depth = 3;
    if(timeLeft < 3*d3){
        if(timeLeft < 2.5*d2){
            depth = 1;
        }
        else{
            depth = 2;
        }
    }
    */
    //cout << depth << endl;
    ofstream output;
    output.open("output.txt");
    if(numVac == 361){
        string fin = convertToOutputFormat(9,9);
        output << fin;
        output.close();
        return;
    }
    int a = INT_MIN;
    int b = INT_MAX;
    pair<int, node*> move = runMinMax(board, player, player, opp, a, b, d, numWhite, numBlack, capWhite, capBlack, startI, startJ, numC);
    /*if(move.first == 0){
        move.second = moveNearBy(board, player, numWhite, numBlack);
    }*/
    string fin = convertToOutputFormat(move.second->row, move.second->col);
    output << fin;
    output.close();
    return;
    
}


void makeQueue(node ***board, queue<node*> &q, int numWhite, int numBlack, string player){
    
    for(int i=0; i<19; i++){
        for(int j=0; j<19; j++){
            if(!allowedMove(board, i,j, numWhite, numBlack, player)){
                continue;
            }
            q.push(board[i][j]);
        }
    }
}


pair<int, node*> runMinMax(node ***board, string playerOrg, string playerCurr, string opp, int alpha, int beta, int depth, int numW, int numB, int capW, int capB, int r, int c, int numCaps){
    
    if(depth == 1){
        int val = eval(board, playerOrg, playerCurr, opp, capB, capW, numCaps);
        return make_pair(val, board[r][c]);
    }
    
    queue<node*> q;
    makeQueue(board, q, numW, numB, playerCurr);
    //MAX layer
    if(playerCurr == playerOrg){
        pair<int, node*> maxPair;
        maxPair.first = INT_MIN;
        while(!q.empty()){
            node* pr = q.front();
            q.pop();
            node *currNode = pr;
            int x = currNode->row;
            int y = currNode->col;
            int numCaps = 0;
            bool wins = false;
            bool lose = false;
            int nc = 0;
            pair<int, node*> scorePair = make_pair(INT_MIN, board[x][y]);
            pair<int, node*> tempPair = make_pair(INT_MIN, board[x][y]);
            playMove(board, x, y, opp, playerCurr, numW, numB, capW, capB, nc, lose);
            undoMove(board, x, y, opp, numW, numB, capW, capB, nc);
            if(lose == true){
                scorePair = make_pair(INT_MAX-1, board[x][y]);
                //cout << "here at " << x << " " << y << endl; 
            }
            playMove(board, x, y, playerCurr, opp, numW, numB, capW, capB, numCaps, wins);
            if(wins == true){
                scorePair = make_pair(INT_MAX, board[x][y]);
                //cout << "here at " << x << " " << y << endl; 
            }
            else {
                tempPair = runMinMax(board, playerOrg, opp, playerCurr, alpha, beta, depth-1, numW, numB, capW, capB, x, y, numCaps);
                if(scorePair.first < tempPair.first){
                    scorePair.first = tempPair.first;
                }
            }
            if(scorePair.first > maxPair.first){
                maxPair.first = scorePair.first;
                maxPair.second = scorePair.second;
                //cout << "Updated this at " << x << " " << y << endl;
            }
            undoMove(board, x, y, playerCurr, numW, numB, capW, capB, numCaps);
            
            alpha = max(alpha, maxPair.first);
            if (beta <= alpha) {
                break;
            }
        }
        
        return maxPair;
    }
    
    //MIN Layer
    else{
        pair<int, node*> maxPair;
        maxPair.first = INT_MAX;
        while(!q.empty()){
            node* pr = q.front();
            q.pop();
            node *currNode = pr;
            int x = currNode->row;
            int y = currNode->col;
            int numCaps = 0;
            bool wins = false;
            bool lose = false;
            int nc = 0;
            pair<int, node*> scorePair = make_pair(INT_MIN, board[x][y]);
            pair<int, node*> tempPair = make_pair(INT_MIN, board[x][y]);
            playMove(board, x, y, opp, playerCurr, numW, numB, capW, capB, nc, lose);
            undoMove(board, x, y, opp, numW, numB, capW, capB, nc);
            if(lose == true){
                scorePair = make_pair(INT_MIN+1, board[x][y]);
            }
            playMove(board, x, y, playerCurr, opp, numW, numB, capW, capB, numCaps, wins);
            if(wins == true){
                scorePair = make_pair(INT_MIN, board[x][y]);
            }
            else {
                tempPair = runMinMax(board, playerOrg, opp, playerCurr, alpha, beta, depth-1, numW, numB, capW, capB, x, y, numCaps);
                if(scorePair.first < tempPair.first){
                    scorePair.first = tempPair.first;
                }
            }
            if(scorePair.first < maxPair.first){
                maxPair.first = scorePair.first;
                maxPair.second = scorePair.second;
            }
            
            undoMove(board, x, y, playerCurr, numW, numB, capW, capB, numCaps);
            beta = min(beta, maxPair.first);
            if (beta <= alpha){
                break;
            }
        }
        
        return maxPair;
    }
}

//counts the number of open 4 connected in a row
vector<int> openFoursAndThrees(node ***board, string player){
    int countOpenFours = 0;
    int countOpenThrees = 0;
    int countOneToWin = 0;
    
    vector<int> ret;
    for(int i=0; i<19; i++){
        for(int j=0; j<19; j++){
            if(board[i][j]->boardVal == player){
                //horizontal check
                if (j < 15 && board[i][j+1]->boardVal == player && board[i][j+2]->boardVal == player && board[i][j+3]->boardVal == player && board[i][j+4]->boardVal == "."){
                    countOpenFours++;
                    if(j-1>=0 && board[i][j-1]->boardVal == "."){
                        countOpenFours += 30;
                    }
                    
                }
                if (j > 0 && j < 16 && board[i][j-1]->boardVal == "." && board[i][j+1]->boardVal == player && board[i][j+2]->boardVal == player && board[i][j+3]->boardVal == player){
                    countOpenFours++;
                    if(j+4<19 && board[i][j+4]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                
                //veritcle check
                if (i<15 && board[i+1][j]->boardVal == player && board[i+2][j]->boardVal == player && board[i+3][j]->boardVal == player && board[i+4][j]->boardVal == "."){
                    countOpenFours++;
                    if(i-1>=0 && board[i-1][j]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                if (i>0 && i<16 && board[i-1][j]->boardVal == "." && board[i+1][j]->boardVal == player && board[i+2][j]->boardVal == player && board[i+3][j]->boardVal == player){
                    countOpenFours++;
                    if(i+4<19 && board[i+4][j]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                
                //diagonal 1 check
                if (i<15 && j<15 && board[i+1][j+1]->boardVal == player && board[i+2][j+2]->boardVal == player && board[i+3][j+3]->boardVal == player && board[i+4][j+4]->boardVal == "."){
                    countOpenFours++;
                    if(i-1>=0 && j-1 >=0 && board[i-1][j-1]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                if (i>0 && i<16 && j>0 && j<16 && board[i-1][j-1]->boardVal == "." && board[i+1][j+1]->boardVal == player && board[i+2][j+2]->boardVal == player && board[i+3][j+3]->boardVal == player){
                    countOpenFours++;
                    if(i+4<19 && j+4<19 && board[i+4][j+4]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                
                //diagonal 2 check
                if (i>3 && j<15 && board[i-1][j+1]->boardVal == player && board[i-2][j+2]->boardVal == player && board[i-3][j+3]->boardVal == player && board[i-4][j+4]->boardVal == "."){
                    countOpenFours++;
                    if(i+1<19 && j-1>=0 && board[i+1][j-1]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                if (i<18 && i>2 && j>0 && j<17 && board[i+1][j-1]->boardVal == "." && board[i-1][j+1]->boardVal == player && board[i-2][j+2]->boardVal == player && board[i-3][j+3]->boardVal == player){
                    countOpenFours++;
                    if(i-4>=0 && j+4<19 && board[i-4][j+4]->boardVal == "."){
                        countOpenFours += 30;
                    }
                }
                
                //horizontal
                if (j<16 && board[i][j+1]->boardVal == player && board[i][j+2]->boardVal == player && board[i][j+3]->boardVal == "."){
                    countOpenThrees++;
                    if(j-1>=0 && board[i][j-1]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                    
                }
                if (j>0 && j<17 && board[i][j-1]->boardVal == "." && board[i][j+1]->boardVal == player && board[i][j+2]->boardVal == player){
                    countOpenThrees++;
                    if(j+3<19 && board[i][j+3]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                
                //veritcle check
                if (i<16 && board[i+1][j]->boardVal == player && board[i+2][j]->boardVal == player && board[i+3][j]->boardVal == "."){
                    countOpenThrees++;
                    if(i-1>=0 && board[i-1][j]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                if (i>0 && i<17 && board[i-1][j]->boardVal == "." && board[i+1][j]->boardVal == player && board[i+2][j]->boardVal == player){
                    countOpenThrees++;
                    if(i+3<19 && board[i+3][j]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                
                //diagonal 1 check
                if (i<16 && j<16 && board[i+1][j+1]->boardVal == player && board[i+2][j+2]->boardVal == player && board[i+3][j+3]->boardVal == "."){
                    countOpenThrees++;
                    if(i-1>=0 && j-1>=0 && board[i-1][j-1]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                if (i>0 && i<17 && j>0 && j<17 && board[i-1][j-1]->boardVal == "." && board[i+1][j+1]->boardVal == player && board[i+2][j+2]->boardVal == player){
                    countOpenThrees++;
                    if(i+3<19 && j+3<19 && board[i+3][j+3]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                
                //diagonal 2 check
                if (i>2 && j<16 && board[i-1][j+1]->boardVal == player && board[i-2][j+2]->boardVal == player && board[i-3][j+3]->boardVal == "."){
                    countOpenThrees++;
                    if(i+1<19 && j-1>=0 && board[i+1][j-1]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                if (i<18 && i>2 && j>0 && j<18 && board[i+1][j-1]->boardVal == "." && board[i-1][j+1]->boardVal == player && board[i-2][j+2]->boardVal == player){
                    countOpenThrees++;
                    if(i-3>=0 && j+3<19 && board[i-3][j+3]->boardVal == "."){
                        countOpenThrees += 6;
                    }
                }
                
                //horizontal check
                if (j < 15 && board[i][j+1]->boardVal == player && board[i][j+2]->boardVal == player && board[i][j+3]->boardVal == "." && board[i][j+4]->boardVal == player){
                    countOneToWin++;
                }
                if (j < 15 && board[i][j+1]->boardVal == player && board[i][j+2]->boardVal == "." && board[i][j+3]->boardVal == player && board[i][j+4]->boardVal == player){
                    countOneToWin++;
                }
                if (j < 15 && board[i][j+1]->boardVal == "." && board[i][j+2]->boardVal == player && board[i][j+3]->boardVal == player && board[i][j+4]->boardVal == player){
                    countOneToWin++;
                }
                
                //veritcle check
                if (i<15 && board[i+1][j]->boardVal == player && board[i+2][j]->boardVal == player && board[i+3][j]->boardVal == "." && board[i+4][j]->boardVal == player){
                    countOneToWin++;
                }
                if (i<15 && board[i+1][j]->boardVal == player && board[i+2][j]->boardVal == "." && board[i+3][j]->boardVal == player && board[i+4][j]->boardVal == player){
                    countOneToWin++;
                }
                if (i<15 && board[i+1][j]->boardVal == "." && board[i+2][j]->boardVal == player && board[i+3][j]->boardVal == player && board[i+4][j]->boardVal == player){
                    countOneToWin++;
                }
                
                //diagonal 1 check
                if (i<15 && j<15 && board[i+1][j+1]->boardVal == player && board[i+2][j+2]->boardVal == player && board[i+3][j+3]->boardVal == "." && board[i+4][j+4]->boardVal == player){
                    countOneToWin++;
                }
                if (i<15 && j<15 && board[i+1][j+1]->boardVal == player && board[i+2][j+2]->boardVal == "." && board[i+3][j+3]->boardVal == player && board[i+4][j+4]->boardVal == player){
                    countOneToWin++;
                }
                if (i<15 && j<15 && board[i+1][j+1]->boardVal == "." && board[i+2][j+2]->boardVal == player && board[i+3][j+3]->boardVal == player && board[i+4][j+4]->boardVal == player){
                    countOneToWin++;
                }
                
                //diagonal 2 check
                if (i>3 && j<15 && board[i-1][j+1]->boardVal == player && board[i-2][j+2]->boardVal == player && board[i-3][j+3]->boardVal == "." && board[i-4][j+4]->boardVal == player){
                    countOneToWin++;
                }
                if (i>3 && j<15 && board[i-1][j+1]->boardVal == player && board[i-2][j+2]->boardVal == "." && board[i-3][j+3]->boardVal == player && board[i-4][j+4]->boardVal == player){
                    countOneToWin++;
                }
                if (i>3 && j<15 && board[i-1][j+1]->boardVal == "." && board[i-2][j+2]->boardVal == player && board[i-3][j+3]->boardVal == player && board[i-4][j+4]->boardVal == player){
                    countOneToWin++;
                }
            }
            
            
        }
    }
    ret.push_back(countOpenFours);
    ret.push_back(countOpenThrees);
    ret.push_back(countOneToWin);
    return ret;
}


void playMove(node ***board, int x, int y, string playerCurr, string opp, int &numW, int &numB, int &capW, int &capB, int &numCaps, bool &wins){
    board[x][y]->boardVal = playerCurr;
    wins = wouldWin(board, x, y, playerCurr);
    if(wins == true){return;}
    numCaps = numCapture(board, x, y, playerCurr, opp);
    if(numCaps > 0){
        vector<pair<int,int> > capturedIndices = board[x][y]->captures.top();
        for(int i=0; i<capturedIndices.size(); i++){
            int st = capturedIndices[i].first;
            int en = capturedIndices[i].second;
            board[st][en]->boardVal = ".";
        }
    }
    if(playerCurr == "w"){
        numW++;
        capW += numCaps;
        if(capW >= 10){wins = true;}
        else{wins = false;}

    }
    else {
        numB++;
        capB += numCaps;
        if(capB >= 10){wins = true;}
        else{wins = false;}
    }
}


void undoMove(node ***board, int x, int y, string playerCurr, int &numW, int &numB, int &capW, int &capB, int numCaps){
    board[x][y]->boardVal = ".";
    
    if(numCaps > 0){
        string updateBackTo = board[x][y]->playerCap.top();
        board[x][y]->playerCap.pop();
        vector<pair<int,int> > capturedInd = board[x][y]->captures.top();
        board[x][y]->captures.pop();
        for(int i=0; i<capturedInd.size(); i++){
            int st = capturedInd[i].first;
            int en = capturedInd[i].second;
            board[st][en]->boardVal = updateBackTo;
        }
    }
    if(playerCurr == "w"){
        numW--;
        capW -= numCaps;

    }
    else {
        numB--;
        capB -= numCaps;
    }
}


node* moveNearBy(node ***board, string player, int numW, int numB){
    for(int i=1; i<=3; i++){
        for(int j=1; j<=3; j++){
            if(allowedMove(board, i+9, 9, numW, numB, player)){
                return board[i+9][9];
            }
            if(allowedMove(board, i+9, 9-j, numW, numB, player)){
                return board[i+9][9-j];
            }
            if(allowedMove(board, i+9, j+9, numW, numB, player)){
                return board[i+9][j+9];
            }
            if(allowedMove(board, 9, 9+j, numW, numB, player)){
                return board[9][9+j];
            }
            if(allowedMove(board, 9, 9-j, numW, numB, player)){
                return board[9][9-j];
            }
            if(allowedMove(board, 9-i, 9-j, numW, numB, player)){
                return board[9-i][9-j];
            }
            if(allowedMove(board, 9-i, 9+j, numW, numB, player)){
                return board[9-i][9+j];
            }
            
        }
    }
}