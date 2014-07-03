/**
 * ALMA MATER STUDIORUM BOLOGNA - Universita' degli Studi di Bologna
 * PROGETTO DI Simulazione di sistemi Mod2
 * Anno Accademico 2013/2014
 *
 * Classe L'ambiente della simulazione
 * 
 * @author Baschieri Daniele, daniele.baschieri@studio.unibo.it
 * @author Liu Tong, tong.liu2@studio.unibo.it
 * @version  0.8
 */


package people;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.logging.Logger;
import java.util.*;


public class ENV  extends Environment{
	
    /*
     * ====================================================
     *  		classes
     * ====================================================
     * */
	
	public class Couple{
		int a;
		int b;
		boolean finished = false;
		public Couple(int a,int b){
			this.a=a;
			this.b=b;
		}
		public String toString(){
			return "["+a+","+b+"]";
		}
	}
	
	public class Agent{
		String myString;
		int myConfidence;

		public Agent(String s,int c){
			this.myString=s;
			this.myConfidence=c;
		}
		public String toString(){
			return "[myString: "+myString+", myConfidence: "+myConfidence+"]";
		}
	}
	
    /*
     * ====================================================
     *  		Variables
     * ====================================================
     * */
	
	int nStep=3;
	int step=0;
	int qoL=3;
	int nAgents;
	String realString="0000000000";
	int maxConfidence=9;
	int minConfidence=8;
	int readyAgent = 0;
	
	ArrayList<Couple> coupleList;
	ArrayList<Agent> agentList;
	

    // common literals
	public static Literal communicate;
	Literal myString[];
	Literal confidence[];
	public final String hd = new String("hammingDistance");
	public final String hde = new String("hammingDistanceEnv");
	public final String us = new String("updateString");
	public final String cc = new String("concludeCommunication");
	public final String ir = new String("informReady");
	

    static Logger logger = Logger.getLogger(ENV.class.getName());
    
    /*
     * ====================================================
     *  		initiation
     * ====================================================
     * */

    @Override
    public void init(String[] args) {
    	
    	logger.info("STARTing SIMULATION: \n num of agents "+args[0]);
		nAgents=Integer.parseInt(args[0]);
		realString=args[1];
		logger.info("realString "+realString);
		agentList = new ArrayList<Agent>();
		
        initPercepts();
        printAgentsPossession();
        generateCoupleList();
		startACycle();

    }
    
	
    /** creates the agents percepts  */
    void initPercepts() {
		myString=new Literal[nAgents];
		confidence=new Literal[nAgents];
		
		for(int i=1;i<=nAgents;i++){
			String aS = generateString();
			int aC = generateConfidence();
			myString[i-1]= Literal.parseLiteral("receiveString(\""+aS+"\")");
			addPercept("dummy"+i,myString[i-1]);
			
			confidence[i-1]= Literal.parseLiteral("receiveConfidence("+aC+")");
			addPercept("dummy"+i,confidence[i-1]);
			
			//a copy of mental states
			Agent tmpAgent = new Agent(aS,aC);
			agentList.add(tmpAgent);
     
		}
    }
    
    /*
     * ====================================================
     *  		ACTIONS
     * ====================================================
     * */
	
    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("["+ag+"] doing: "+action);
        
        boolean result = true;
		if (action.getFunctor().equals(hd)) {
        	/*Calculating hammingDistance */
			int distance=getHummingDistance(action.getTerm(0).toString(),action.getTerm(1).toString());
        	logger.info("Calculating HammingDistance !!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1)+" D="+distance);
			
			addPercept(ag,Literal.parseLiteral("distance("+distance+")"));
        }
		if (action.getFunctor().equals(hde)) {
        	/*Calculating hammingDistance */
			int distance=getHummingDistance(action.getTerm(0).toString().replaceAll("\"",""),realString);
        	logger.info("Calculating HammingDistanceENV !!! term1: "+action.getTerm(0)+" term2:"+realString+" D="+distance);
			
			double punteggio=Math.random()*realString.length();
			logger.info("Punteggio "+punteggio+" / "+distance);
			if(punteggio>distance){
				logger.info("Successo");
				addPercept(ag,Literal.parseLiteral("result(success)"));
			}else{
				logger.info("Failure");
				addPercept(ag,Literal.parseLiteral("result(failure)"));
			}
        }
		if (action.getFunctor().equals(us)) {
			String newString=getQoLString(action.getTerm(0).toString(),action.getTerm(1).toString());
			updateEnvCopiedStringByAgentName(ag,newString);
			logger.info("Calculating updateString !!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1)+" "+newString);
			addPercept(ag,Literal.parseLiteral("communicateUpdate("+newString+")"));
		}
		if (action.getFunctor().equals(cc)) {
			//a conversation is finished 
			Couple p = getCoupleByAgentName(ag);
			if(!p.finished){
				addPercept("dummy"+p.a,Literal.parseLiteral("reset(belief)"));
				addPercept("dummy"+p.b,Literal.parseLiteral("reset(belief)"));
				p.finished = true;
			}
		}
		if (action.getFunctor().equals(ir)) {
			//an agent cleaned his belief
			incrementReadyAgent();
		}
        return result;
    }   
    

	
    /*
     * ====================================================
     *  		simulation cycle
     * ====================================================
     * */
    
    void startACycle(){
    	for(int i=0;i<coupleList.size();i++)
		{
    		Couple p = coupleList.get(i);
    		communicate= Literal.parseLiteral("communicate(\"dummy"+p.a+"\" , \"dummy"+p.b+"\")");
			logger.info("ENV Starts a conversation: "+step+"/"+nStep+" ["+p.a+" => "+p.b+"]");
			addPercept("dummy"+p.a,communicate);
		}
    }
	
    //when a agent ends a conversation
	synchronized void incrementReadyAgent(){
		readyAgent++;
		logger.info("number of ready agents: "+readyAgent+"/"+nAgents);
		if(readyAgent == nAgents){
			if(step <nStep){
			
				logger.info("ENV Starts Group Conversation: "+step+"/"+nStep);
				step++;
				logger.info("goin to restart chat cyclte ");
				readyAgent = 0;
				
				clearAllPercepts();
				generateCoupleList();
				startACycle();
				
			}
			printAgentsPossession();
		}
	}

	//update string result
	void updateEnvCopiedStringByAgentName(String dummy,String newString){
		int dummyIndex = Integer.parseInt(dummy.replaceAll("dummy", ""));
		Agent a = agentList.get(dummyIndex-1);
		a.myString = newString;
	}
	
    /*
     * ====================================================
     *  		utilities
     * ====================================================
     * */
	
	Couple getCoupleByAgentName(String dummy){
		Couple p;
		for(int i = 0 ; i< coupleList.size(); i++){
			p=coupleList.get(i);
			if(dummy.equals("dummy"+p.a) || dummy.equals("dummy"+p.b)){
				logger.info("trovato");
				return p;
			}
			
		}
		logger.info("nn trovato");
			return null;
		
	}
	
	public int getHummingDistance(String s1,String s2){
		
		if(s1.length()!=s2.length()){
			System.out.println("ERRORE: #001 STRINGHE DIVERSE "+s1+":"+s1.length()+" "+s2+":"+s2.length());
		}
		int dist=0;
		for(int i=0;i<s1.length();i++){
			if(s1.charAt(i)!=s2.charAt(i))
				dist++;
		}
		return dist;
	}
	/**
		s1: the main String of the Leader
		s2: the String of the follower
	*/
	public String getQoLString(String s1,String s2){
		String s="";
		if(s1.length()!=s2.length()){
			System.out.println("ERRORE: #002 STRINGHE DIVERSE "+s1+":"+s1.length()+" "+s2+":"+s2.length());
		}
		int dist=0;
		for(int i=0;i<s1.length();i++){
			if((s1.charAt(i)!=s2.charAt(i))&(dist<=qoL)){
				dist++;
				s+=""+s1.charAt(i);
			}else{
				s+=""+s2.charAt(i);
			}
		}
		return s;		
	}
	
	void printAgentsPossession(){
		logger.info("===============  START AGENTE REPORT ==============");
		for(int i = 0 ; i< agentList.size(); i++){
			Agent a = agentList.get(i);
			logger.info("DUMMY"+i+": myString: "+a.myString+", myConfidence: "+a.myConfidence);
		}
		logger.info("===============  END AGENTE REPORT ==============");
	}
	
    /*
     * ====================================================
     *  		generate methods
     * ====================================================
     * */
	
	void generateCoupleList(){
		ArrayList<Integer> scoupleList = new ArrayList<Integer>();
		for(int i=1;i<=nAgents;i++)
		{
			scoupleList.add(i);
		}                                       
		Collections.shuffle(scoupleList);
		coupleList = new ArrayList<Couple>();
		for(int i=0;i<(nAgents-1);i+=2)
		{
			coupleList.add(new Couple(scoupleList.get(i),scoupleList.get(i+1)));
			logger.info(""+coupleList.get(coupleList.size()-1));
		}
	}
    
    int generateConfidence(){
		return (int)(Math.round(Math.random()*(maxConfidence-minConfidence))+minConfidence);
	}

    String generateString(){
		String s="";
		for(int i=0;i<10;i++){
			if(Math.random()>0.5){
				s+="0";
			}else{
				s+="1";
			}
		}
		return s;
	}

}
