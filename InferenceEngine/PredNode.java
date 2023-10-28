import java.util.*;
import java.lang.*;
import java.io.IOException;


public class PredNode {
    private int numParams;
    private String completePredicateString;
    private String predName; //name of predicate
    private boolean isPos; // is the predicate positive
    private String[][] params; // int 0: variable (v)/constant(c), 1: value
    private HashSet<String> variableNames;
    
    
    public PredNode(){
        numParams = 0;
        completePredicateString = "";
        predName = "";
        isPos = true;
        variableNames = new HashSet<>();
        
    }
    
    public PredNode(String str){
        
        completePredicateString = str;
        int startInd = 0;
		if(str.charAt(0) == '~'){
            isPos = false;
            startInd = 1;
        }
        else{
            isPos = true;
        }
        int indOfParanthesis = str.indexOf("(");
        predName = str.substring(startInd, indOfParanthesis);
        //System.out.println("Predname: " + predName);
        //System.out.println("Is Positive " + isPos);
        String tmp = str.substring(indOfParanthesis+1, str.length()-1); //all but the paranthesis
        String[] listOfParams = tmp.split(",");
        numParams = listOfParams.length;
        params = new String[numParams][2];
        variableNames = new HashSet<>();
        
        for (int i=0; i<numParams;i++){
            
            if(listOfParams[i].charAt(0) >= 97 && listOfParams[i].charAt(0) <= 122){
                params[i][0] = "v";
            }
            else{
                params[i][0] = "c";
            }
            params[i][1] = listOfParams[i];
            
            //System.out.println("var or const " + params[i][0]);
            //System.out.println("Param Value " + params[i][1]);
        }
    }
    
    public String getCompletePredicateString(){
        return completePredicateString;
    }
    public String getPredName(){
        return predName;
    }
    public boolean getIsPos(){
        return isPos;
    }
    public String[][] getParamsList(){
        return params;
    }
    public int getNumParams(){
        return numParams;
    }
    public void setIsPos(boolean val){
        isPos = val;
    }
    public void setPredName(String val){
        predName = val;
    }
    public void setParamsList(String[][] val){
        params = new String[val.length][2];
        for(int i=0; i<val.length;i++){
            params[i] = val[i].clone();
        }
        
    }
    public void setNumParams(int val){
        numParams = val;
    }
    public void setCompletePredicateString(String val){
        completePredicateString = val;
    }
    public HashSet<String> getVariablesSet(){
        return variableNames;
    }
    public void setVarName(int i, String val){
        params[i][1] = val;
    }
    public void setVarType(int i, String val){
        params[i][0] = val;
    }
    public PredNode createCopy(){
        PredNode newPred = new PredNode();
        newPred.setNumParams(numParams);
        newPred.setParamsList(params);
        newPred.setPredName(predName);
        newPred.setIsPos(isPos);
        return newPred;
    }
    
    public HashMap<String, String> unifyAndSubs(PredNode p2){
        if(predName.equals(p2.getPredName()) && numParams == p2.getNumParams() && isPos != p2.getIsPos()){
            String[][] p2ParamsList = p2.getParamsList();
            //p2ParamsList = standardiseVars(p2ParamsList);
            HashMap<String,String> varMapping = new HashMap<>();
            
            if(canUnify(varMapping, p2ParamsList)){
                return varMapping;
            }
            else{
                return null;
            }
        }
        return null;
    }
    
    private String[][] standardiseVars(String[][] p2ParamsList){
        HashMap<String,String> map = new HashMap<>();
		String[] a = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
		List arr = Arrays.asList(a);
		HashSet<String> s1 = new HashSet<>(arr); //set of available letters for variables
		
		for(int i=0; i<params.length; i++){
			if(params[i][0].equals("v")){
               s1.remove(params[i][1]);
            } 
		}
		Iterator iter = s1.iterator();
		for(int i=0; i<p2ParamsList.length; i++){
			if(p2ParamsList[i][0].equals("v") && !s1.contains(p2ParamsList[i][1])){
				String mappedVal = map.get(p2ParamsList[i][1]);
				if(mappedVal == null){
					String newValue = (String)iter.next();
					map.put(p2ParamsList[i][1], newValue);
					p2ParamsList[i][1] = newValue;
					//System.out.println("Updated to 1: " + newValue);
					
				}
				else{
					p2ParamsList[i][1] = mappedVal;
					//System.out.println("Updated to 2: " + mappedVal);
				}
            } 
		}
        return p2ParamsList;
    }
    
    private boolean canUnify(HashMap<String, String> varMapping, String [][] p2ParamsList){
        for(int i=0; i<params.length; i++){
			if(params[i][0].equals("c") && p2ParamsList[i][0].equals("c")){
                if(!(params[i][1].equals(p2ParamsList[i][1]))){
                    return false;
                }
                else continue;
            } 
            else if(params[i][0].equals("v") && p2ParamsList[i][0].equals("c")){
                String val = varMapping.get(params[i][1]);
                if(val == null){
                    varMapping.put(params[i][1], p2ParamsList[i][1]);
                }
                else{
                    if(val.charAt(0) >= 65 && val.charAt(0)<=90 && (!val.equals(p2ParamsList[i][1]))){
                        return false;
                    }
                    else if(val.charAt(0) >= 97 && val.charAt(0)<=122){
                    	if(varMapping.get(val) != null && varMapping.get(val).charAt(0) <=90 && (!p2ParamsList[i][1].equals(varMapping.get(val)) && !val.equals(varMapping.get(val)))){
                    		return false;
                    	}
                    	else{
                    		varMapping.put(params[i][1], p2ParamsList[i][1]);
                    		varMapping.put(val, p2ParamsList[i][1]);
                    	}
                    }
                    continue;
                }
            }
            
            else if(params[i][0].equals("c") && p2ParamsList[i][0].equals("v")){
                String val = varMapping.get(p2ParamsList[i][1]);
                if(val == null){
                    varMapping.put(p2ParamsList[i][1], params[i][1]);
                }
                else{
                    if(val.charAt(0) >= 65 && val.charAt(0)<=90 && (!val.equals(params[i][1]))){
                        return false;
                    }
                    else if(val.charAt(0) >= 97 && val.charAt(0)<=122){
                    	String other = varMapping.get(val);
                    	if(other.charAt(0) <=90 && (!params[i][1].equals(other) && !val.equals(other))){
                    		return false;
                    	}
                    	else{
                    		varMapping.put(p2ParamsList[i][1], params[i][1]);
                            varMapping.put(other, params[i][1]);
                    	}
                    }
                    continue;
                }
            }
            else{
            	//System.out.println(params[i][1] + " "  + p2ParamsList[i][1]);
                String val1 = varMapping.get(params[i][1]);
                String val2 = varMapping.get(p2ParamsList[i][1]);
                
                if(val1 == null && val2 != null){
                	if(val2.charAt(0) <= 90){
                		varMapping.put(params[i][1], val2);
                	}
                	else if(!varMapping.get(val2).equals(params[i][1]) && !varMapping.get(val2).equals(val2)){
                		return false;
                	}
                }
                else if(val2 == null && val1 != null){
                	if(val1.charAt(0) <= 90){
                		varMapping.put(p2ParamsList[i][1], val1);
                	}
                	else if(!varMapping.get(val1).equals(p2ParamsList[i][1]) && !varMapping.get(val1).equals(val1)){
                		return false;
                	}
                }
                else if(val1 == null && val2 == null){
                	varMapping.put(params[i][1], params[i][1]);
                	varMapping.put(p2ParamsList[i][1], params[i][1]);
                }
                else{
                	if(val1.charAt(0) >= 65 && val1.charAt(0)<=90){
                		if(val2.charAt(0) >=65 && val2.charAt(0)<=90){
                			if(!val1.equals(val2)){
                				return false;
                			}
                		}
                		else{
                			String other = varMapping.get(val2);
                			if(other != null && other.charAt(0)<=90){
                				if(!other.equals(val1)){
	                				return false;
                				}
                				else{
                					varMapping.put(p2ParamsList[i][1], val1);
                                    varMapping.put(other, val1);
                				}
                			}
                		}
                	}
                	else{
                		if(val2.charAt(0) >=97 && val2.charAt(0)<=122){
                			String other1 = varMapping.get(val1);
                			String other2 = varMapping.get(val2);
                			if(other1.charAt(0)<=90 && other2.charAt(0) <=90 && !other1.equals(other2)){
                				return false;
                			}
                			else if(other1.charAt(0)<=90 && other2.charAt(0)>=97){
                				if(!other2.equals(p2ParamsList[i][1])){
                					return false;
                				}
                				else{
                					varMapping.put(p2ParamsList[i][1], other1);
	                				varMapping.put(val2, other1);
	                				varMapping.put(params[i][1], other1);
                				}
                				
                			}
                			else if(other2.charAt(0)<=90 && other1.charAt(0)>=97){
                				if(!other1.equals(params[i][1])){
                					return false;
                				}
                				else{
                					varMapping.put(p2ParamsList[i][1], other2);
	                				varMapping.put(val1, other2);
	                				varMapping.put(params[i][1], other2);
                				}
                			}
                			else continue;
                		}
                		else{
                			String other = varMapping.get(val1);
                			if(other != null && other.charAt(0)<=90){
                				if(!other.equals(val2)){
	                				return false;
                				}
                				else{
                					varMapping.put(params[i][1], val2);
                                    varMapping.put(other, val2);
                				}
                			}
                		}
                	}
                }
            }
		}
        return true;
    }
    
    
    public boolean samePreds(PredNode pred2){
        if(predName.equals(pred2.getPredName())){
            int numParams1 = numParams;
            int numParams2 = pred2.getNumParams();
            if(numParams1 == numParams2){
                String[][] params1 = new String[numParams1][2];
                String[][] params2 = new String[numParams2][2];
                params1 = params;
                params2 = pred2.getParamsList();
                for(int i=0; i<numParams1; i++){
                    if(!(params1[i][0].equals(params2[i][0])) || !(params1[i][1].equals(params2[i][1]))){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    
    public boolean similarPreds(PredNode pred2){
        if(predName.equals(pred2.getPredName())){
            int numParams1 = numParams;
            int numParams2 = pred2.getNumParams();
            if(numParams1 == numParams2){
                String[][] params1 = new String[numParams1][2];
                String[][] params2 = new String[numParams2][2];
                params1 = params;
                params2 = pred2.getParamsList();
                for(int i=0; i<numParams1; i++){
                    if(params1[i][0].equals("c") && !params1[i][1].equals(params2[i][1])){
                        return false;
                    }
                    else if(params1[i][0].equals("v") && params2[i][0].equals("c")){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    
    public boolean duplicatePreds(PredNode pred2){
        if(predName.equals(pred2.getPredName()) && isPos == pred2.getIsPos()){
            int numParams1 = numParams;
            int numParams2 = pred2.getNumParams();
            if(numParams1 == numParams2){
                String[][] params1 = params;
                String[][] params2 = pred2.getParamsList();
                for(int i=0; i<numParams1; i++){
                    if(!params1[i][0].equals(params2[i][0])){
                        return false;
                    }
                    else if(params1[i][0].equals("c") && !params2[i][1].equals(params1[i][1])){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
}