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
	
	int nStep=300;
	int step=0;
	int qoL=5;
	int nAgents;
	String realString="";
	int maxConfidence=4;
	int minConfidence=4;
	int readyAgent = 0;
	/**
	*TRUE if only qoL different bit from the beginning are modified between the two string
	*FALSE if qoL random bit are modified between the two string	
	*/
	boolean shouldBlindCopy=true;
	
	ArrayList<Couple> coupleList;
	ArrayList<Agent> agentList;
    String[] stringList =  {
    		"1010011000","0010000110","0100111000","1011110000","1111111011","0110110000","0110010100",
    		"1110010011","1110011011","0101001001","0110101000","1000101001","1001110111","0000001000",
    		"0101100110","0110010001","0001111111","0010000000","0000010011","0101001101","1110101010",
    		"0001100100","1101001100","1100100111","0111011100","1101001100","0111110101","0000000110",
    		"0011111101","0110111101","0111001101","1001010001","1100100110","1100111101","1101010010",
    		"0010001010","0110111011","1100001000","0100101110","0011001100","1001011110","1011001011",
    		"0000000000","1010001000","0000111001","0100001010","0001010011","0011010000","1011000111",
    		"0000000011","1010101011","0111111001","0101110100","1000001001","0100010001","1110011000",
    		"1101000101","0111101101","0010010011","1110000101","1100110100","1100000010","0100110101",
    		"0011111111","0010111000","1111111101","0110011010","0101001010","0111011001","0100100010",
    		"1000111100","1100101010","0111001111","1011100101","1011010110","0010000111","0101010111",
    		"0011000001","0011001111","1000011101","1111010011","1010001100","0101000100","1100111000",
    		"0010001101","0001110100","0010011010","1100110000","1011011100","1101110000","0001001010",
    		"0111101101","0001100001","0111100101","1010111100","1001100001","0101000001","0011110100",
    		"0001110001","0001001011"};

    // common literals
	public static Literal communicate;
	Literal myString[];
	Literal confidence[];
	public final String hd = new String("hammingDistance");
	public final String hde = new String("hammingDistanceEnv");
	public final String us = new String("updateString");
	public final String cc = new String("concludeCommunication");
	public final String ir = new String("informReady");
	public final String pj = new String("printj");
	

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
		
		//TEST ARC COMPARISON
		realString=generateString();
		//realString= "0100100110";
		
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
			
			//TEST ARC COMPARISON
			String aS = generateString();
			//String aS = stringList[i-1];
			
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
        //System.out.println("["+ag+"] doing: "+action);
        
        boolean result = true;
		if (action.getFunctor().equals(hd)) {
        	/*Calculating hammingDistance */
			int distance=getHummingDistance(action.getTerm(0).toString(),action.getTerm(1).toString());
        	//logger.info("Calculating HammingDistance !!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1)+" D="+distance);
			
			addPercept(ag,Literal.parseLiteral("distance("+distance+")"));
        }
		if (action.getFunctor().equals(hde)) {
        	/*Calculating hammingDistance */
			int distance=getHummingDistance(action.getTerm(0).toString().replaceAll("\"",""),realString);
        	//logger.info("Calculating HammingDistanceENV !!! term1: "+action.getTerm(0)+" term2:"+realString+" D="+distance);
			
			int punteggio=(int)(Math.random()*realString.length());
			//logger.info("Punteggio "+punteggio+" / "+distance);
			if(punteggio>distance){
				//logger.info("Successo "+ag+" "+getCoupleByAgentName(ag).a+" <=> "+getCoupleByAgentName(ag).b);
				addPercept(ag,Literal.parseLiteral("result(success)"));
			}else{
				//logger.info("Failure");
				addPercept(ag,Literal.parseLiteral("result(failure)"));
			}
        }
		if (action.getFunctor().equals(us)) {
			String newString=getQoLString(action.getTerm(0).toString(),action.getTerm(1).toString());
			updateAnotherEnvCopiedStringByAgentName(ag,newString);
			//logger.info("Calculating updateString !!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1)+" "+newString);
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
		if (action.getFunctor().equals(pj)) {
			
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
			//logger.info("ENV Starts a conversation: "+step+"/"+nStep+" ["+p.a+" => "+p.b+"]");
			addPercept("dummy"+p.a,communicate);
		}
    }
	
    //when a agent ends a conversation
	synchronized void incrementReadyAgent(){
		readyAgent++;
		//logger.info("number of ready agents: "+readyAgent+"/"+nAgents);
		if(readyAgent == nAgents){
			if(step <nStep){
			
				//logger.info("ENV Starts Group Conversation: "+step+"/"+nStep);
				step++;
				//logger.info("goin to restart chat cyclte ");
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
				//logger.info("trovato");
				return p;
			}
			
		}
		//logger.info("nn trovato");
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
		 
		logger.info("===============  START AGENTE REPORT ["+step+"/"+nStep+"]==========");
		for(int i = 0 ; i< agentList.size(); i++){
			Agent a = agentList.get(i);
			int d=getHummingDistance(a.myString.replaceAll("\"",""),realString);
			media+=d;
			if(step%20==0)
				logger.info("DUMMY"+(i+1)+": myString: "+a.myString+", myConfidence: "+a.myConfidence+" D="+d);
		}
		media=media/agentList.size();
		if(step%20==0){
			logger.info("realString: "+realString);
			
		}
		logger.info("Distanza media: "+(realString.length()-media));
		logger.info("===============  BELIEF ORIENTED VERSION END AGENTE REPORT ==============");
		 
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
			//logger.info(""+coupleList.get(coupleList.size()-1));
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
