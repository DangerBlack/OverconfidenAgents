/**
 * ALMA MATER STUDIORUM BOLOGNA - Universita' degli Studi di Bologna
 * PROGETTO DI Simulazione di sistemi Mod2
 * Anno Accademico 2013/2014
 *
 * Classe L'ambiente della simulazione
 * 
 * @author Baschieri Daniele, daniele.baschieri@studio.unibo.it
 * @author Liu Tong, tong.liu2@studio.unibo.it
 * @version  0.9
 */

package people;

import jason.asSyntax.*;
import jason.environment.Environment;
import java.util.logging.Logger;
import java.util.*;
import java.io.*;

public class ENV  extends Environment{
	

    /*
     * ====================================================
     *  		Variables
     * ====================================================
     * */
	
	//System parameters
	int nStep=1000;
	int step=0;
	int qoL=5;
	int maxConfidence=4;//4 7 9
	int minConfidence=4;
	boolean debugMode = false;
	/**
	*TRUE if only qoL different bit from the beginning are modified between the two string
	*FALSE if qoL random bit are modified between the two string	
	*/
	boolean shouldBlindCopy=true; //true is better
	
	
	//parameters that will be changed by system
	int nAgents;
	String realString="";
	int readyAgent = 0;
	

	ArrayList<Couple> coupleList;
	ArrayList<Agent> agentList;
	

    // common literals
	public static Literal communicate;
	Literal myString[];
	Literal confidence[];
	public final String hd = new String("hammingDistance");
	public final String hde = new String("hammingDistanceEnv");
	public final String gns = new String("getNewString");
	public final String cc = new String("concludeCommunication");
	public final String ir = new String("informReady");
	public final String pj = new String("printj");

    static Logger logger = Logger.getLogger(ENV.class.getName());
    public SaveTrace save;
	
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
	public class SaveTrace{
		String path="";
		String fileName="";
		PrintWriter output;
		public SaveTrace(){
			try{
			int n=0;
			File f;
			do{
				fileName="trace-ags"+nAgents+"-steps"+nStep+"-cf"+maxConfidence+"-qol"+qoL+"-"+n+".txt";			
				f=new File(path+fileName);
				n++;
			}while(f.exists());
			
			output=new PrintWriter(f);
			}catch(FileNotFoundException e){
				logger.info("Error #005 Unable to save file");
			}
		}
		public void close(){
			output.close();
		}
		/** it PRINT the Average Correct Bit
		*/
		public void print(double ACB){
			output.println(ACB);
		}
	}
    
    /*
     * ====================================================
     *  		initiation
     * ====================================================
     * */

    @Override
    public void init(String[] args) {
    	
    	logger.info("STARTing SIMULATION: \n num of agents "+args[0]);
		nAgents=Integer.parseInt(args[0]);
		

		realString=generateString();
		agentList = new ArrayList<Agent>();
		
		
        initPercepts();
		save=new SaveTrace();
        printAgentsPossession();
        generateCoupleList();
        //generateCoupleList2();
		startACycle();

    }
    
	
    //add initial string and confidence
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
        boolean result = true;
		if (action.getFunctor().equals(hd)) {
			
        	//h distance between 2 agents
			int distance=gethammingDistance(action.getTerm(0).toString(),action.getTerm(1).toString());
			
			//inform student agent the interest distance
			addPercept(ag,Literal.parseLiteral("interestDistance("+distance+")"));
			
        }
		if (action.getFunctor().equals(hde)) {
			
        	//h distance between agent and environment and probability of successful learning
			int distance=gethammingDistance(action.getTerm(0).toString().replaceAll("\"",""),realString);
        	
			double punteggio=Math.random()*realString.length();
			
			if(punteggio>distance){
				addPercept(ag,Literal.parseLiteral("result(success)"));
			}else{
				addPercept(ag,Literal.parseLiteral("result(failure)"));
			}
        }
		if (action.getFunctor().equals(gns)) {
			
			//get new string from strings of 2 agents
			String newString=getQoLString(action.getTerm(0).toString(),action.getTerm(1).toString());
			
			//inform agent the new string
			addPercept(ag,Literal.parseLiteral("newKnowledge("+newString+")"));
			
			//environment update belief copy
			updateEnvCopiedStringByAgentName(ag,newString);
				
		}
		if (action.getFunctor().equals(cc)) {
			
			//environment receives conclude conversation signal
			Couple p = getCoupleByAgentName(ag);
			if(!p.finished){              
				addPercept("dummy"+p.a,Literal.parseLiteral("reset(belief)"));
				addPercept("dummy"+p.b,Literal.parseLiteral("reset(belief)"));
				p.finished = true;
			}
			
		}
		if (action.getFunctor().equals(ir)) {
			
			//environment receives signal that an agent has already cleaned the his belief
			incrementReadyAgent();
			
		}
		if (action.getFunctor().equals(pj)) {
			if(debugMode)
			logger.info("Agent"+ag+":"+action.getTerm(0).toString());
		}
        return result;
    }   
    

	
    /*
     * ====================================================
     *  		simulation cycle
     * ====================================================
     * */
    
    void startACycle(){
    	
    	//add "communicate" belief to trigger communication activities 
    	for(int i=0;i<coupleList.size();i++)
		{
    		Couple p = coupleList.get(i);
    		communicate= Literal.parseLiteral("communicate(\"dummy"+p.a+"\" , \"dummy"+p.b+"\")");
			
    		addPercept("dummy"+p.a,communicate);
    		
    		if(debugMode)
    		logger.info("ENV Starts a conversation: "+step+"/"+nStep+" ["+p.a+" => "+p.b+"]");
			
		}
    	
    }
	
    //when a agent ends a conversation
	synchronized void incrementReadyAgent(){
		readyAgent++;
		
		if(debugMode)
		logger.info("number of ready agents: "+readyAgent+"/"+nAgents);
		
		//when all agents are ready
		if(readyAgent == coupleList.size()*2){
			
			//logger.info("all agents have reseted their believes: "+readyAgent+"/"+nAgents);
			
			step++;
			clearAllPercepts();
	
			if(step <nStep){
				readyAgent = 0;
				generateCoupleList();
				startACycle();
			}
			
			//logger.info("new cycle already started... ");
			
			printAgentsPossession();
		}
	}

	//update the copy of string in environment 
	void updateEnvCopiedStringByAgentName(String dummy,String newString){
		int dummyIndex = Integer.parseInt(dummy.replaceAll("dummy", ""));
		Agent a = agentList.get(dummyIndex-1);
		a.myString = newString;
	}
		
	//update string result Deprecated
	void updateAnotherEnvCopiedStringByAgentName(String dummy,String newString){
		Couple p =  getCoupleByAgentName(dummy);
		int dummyIndex = Integer.parseInt(dummy.replaceAll("dummy", ""));
		int anotherIdx = (p.a == dummyIndex)? p.b : p.a;
		
		Agent a = agentList.get(anotherIdx-1);
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
				return p;
			}
			
		}
			return null;
	}
	
	public int gethammingDistance(String s1,String s2){
		
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
	
	public String getQoLString(String s1,String s2){
		if(!shouldBlindCopy)
			return getQoLStringFixed(s1,s2);
		else
			return getQoLStringRandom(s1,s2);
	}
	/**
		s1: the main String of the Leader
		s2: the String of the follower
	*/
	public String getQoLStringFixed(String s1,String s2){
		String s="";
		if(s1.length()!=s2.length()){
			System.out.println("ERRORE: #002 STRINGHE DIVERSE "+s1+":"+s1.length()+" "+s2+":"+s2.length());
		}
		int dist=0;
		for(int i=0;i<s1.length();i++){
			if((s1.charAt(i)!=s2.charAt(i))&(dist<qoL)){
				dist++;
				s+=""+s1.charAt(i);
			}else{
				s+=""+s2.charAt(i);
			}
		}
		return s;		
	}
	
	
	/**
		s1: the main String of the Leader
		s2: the String of the follower
	*/
	public String getQoLStringRandom(String s1,String s2){
		String s="";
		if(s1.length()!=s2.length()){
			System.out.println("ERRORE: #003 STRINGHE DIVERSE "+s1+":"+s1.length()+" "+s2+":"+s2.length());
		}
		char c[]=new char[s2.length()];
		for(int i=0;i<s1.length();i++){
			c[i]=s2.charAt(i);
		}
		ArrayList<Integer> prec=new ArrayList<Integer>();
		for(int i=0;i<qoL;i++){
			int pos;
			do{
				pos=(int)(Math.random()*s2.length());
			}while(prec.indexOf(new Integer(pos))!=-1);
			
			c[pos]=s1.charAt(pos);
		}		
		for(int i=0;i<s1.length();i++){
			s+=""+c[i];
		}
		return s;		
	}
	
	void printAgentsPossession(){
		double media=0;
		 
		
		for(int i = 0 ; i< agentList.size(); i++){
			Agent a = agentList.get(i);
			int d=gethammingDistance(a.myString.replaceAll("\"",""),realString);
			media+=d;
			if(step%20==0){
				logger.info("===============  START AGENTE REPORT ["+step+"/"+nStep+"]==========");
				logger.info("DUMMY"+(i+1)+": myString: "+a.myString+", myConfidence: "+a.myConfidence+" D="+d);
				
			}
		}
		
		media=media/agentList.size();
		logger.info("AVG Correct Bits: "+(realString.length()-media)+"["+step+"/"+nStep+"]");
		
		if(step%20==0){
		logger.info("realString: "+realString);
		logger.info("=============== END AGENTE REPORT [GOAL ORIENTED VERSION] ==============");
		}
		save.print(realString.length()-media);
		if(step>=nStep)
			save.close(); 
	}
	
    /*
     * ====================================================
     *  		generate methods
     * ====================================================
     * */
	
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
    
	//   ============  PERFECT MATCHING   ============ 
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
			//logger.info(""+coupleList.get(coupleList.size()-1));
		}
	}

    //   ============  FLAW MATCHING   ================
    public class Dummy{
		int id;
		boolean occupied = false;

		public Dummy(int a,boolean b){
			this.id=a;
			this.occupied=b;
		}
	}
    
	ArrayList<Dummy> dlist = new ArrayList<Dummy>();
	
	void generateCoupleList2(){
		coupleList = new ArrayList<Couple>();
		for(int i=1;i<=nAgents;i++)
		{
			//System.out.println("aaa"+(int)(Math.random()*nAgents+1));
			Dummy d = new Dummy(i,false);
			dlist.add(d);
		}   
		Collections.shuffle(dlist);
		for(int i=0;i<nAgents;i++)
		{
			
			int parteneridx = (int)(Math.random()*nAgents);
	
			Dummy p = dlist.get(parteneridx);
			Dummy d = dlist.get(i);
					
			if(p.id != d.id && !p.occupied && !d.occupied){
				
				p.occupied = true;
				d.occupied = true;

				int order = (int)(Math.random()*nAgents+1);

				if(order > (nAgents/2))	
					coupleList.add(new Couple(p.id,d.id));
				else
					coupleList.add(new Couple(d.id,p.id));
			}
		}
	}
}
