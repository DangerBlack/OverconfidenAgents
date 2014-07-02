package people;

import jason.asSyntax.*;
import jason.environment.Environment;

import java.util.logging.Logger;
import java.util.*;

//import couch.HouseENV;

public class ENV  extends Environment{
	public class Couple{
		int a;
		int b;
		public Couple(int a,int b){
			this.a=a;
			this.b=b;
		}
		public String toString(){
			return "["+a+","+b+"]";
		}
	}
	
	int nStep=10;
	int step=0;
	int qoL=3;
	int nAgents=10;
	String realString="0000000000";
	int maxConfidence=9;
	int minConfidence=8;
	ArrayList<Couple> chatRoom;
	

    // common literals
	public final String hd = new String("hammingDistance");
	public final String hde = new String("hammingDistanceEnv");
	public final String us = new String("updateString");
	public final String sc = new String("speechConcluded");
    //compare(Num,MyStr)

    
    static Logger logger = Logger.getLogger(ENV.class.getName());

    @Override
    public void init(String[] args) {
		
		
    	logger.info("num of agents "+args[0]);
		nAgents=Integer.parseInt(args[0]);
		realString=args[1];
		logger.info("realString "+realString);
        updatePercepts();
		generateChatRoom();
		doNextCoupleChat();
    }
    
	Literal myString[];
	Literal confidence[];
    /** creates the agents percepts  */
    void updatePercepts() {
		myString=new Literal[nAgents];
		confidence=new Literal[nAgents];
		for(int i=1;i<=nAgents;i++){
			myString[i-1]= Literal.parseLiteral("myString(\""+generateString()+"\")");
			addPercept("dummy"+i,myString[i-1]);
			
			confidence[i-1]= Literal.parseLiteral("confidence("+generateConfidence()+")");
			addPercept("dummy"+i,confidence[i-1]);
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
	
	/**
	*	This method generate the Chat Room in this env
	*/
	void generateChatRoom(){
		ArrayList<Integer> schatRoom = new ArrayList<Integer>();
		for(int i=1;i<=nAgents;i++)
		{
			schatRoom.add(i);
		}                                       
		Collections.shuffle(schatRoom);
		chatRoom = new ArrayList<Couple>();
		for(int i=0;i<(nAgents-1);i+=2)
		{
			chatRoom.add(new Couple(schatRoom.get(i),schatRoom.get(i+1)));
			logger.info(""+chatRoom.get(chatRoom.size()-1));
		}
	}
	
	/**
		* this method return the next chat room if the chat room is over it return null
		
	*/
	Couple getNextChat(){
		Couple p;
		if(chatRoom.size()>0){
			p=chatRoom.get(0);
			chatRoom.remove(0);
			return p;
		}else{
			return null;
		}
	}
	public void doNextCoupleChat(){
		if(step<nStep){			
			Couple p=getNextChat();
			if(p==null){
				step++;
				generateChatRoom();
				p=getNextChat();
			}
			comunicate= Literal.parseLiteral("comunicate(\"dummy"+p.a+"\" , \"dummy"+p.b+"\")");
			logger.info("FACCIO LO STEP "+step+"/"+nStep+" ["+p.a+" => "+p.b+"]");
			addPercept("dummy"+p.a,comunicate);
		}
	}
	public static Literal comunicate;
	
	public int hummingDistance(String s1,String s2){
		
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
	public String updateString(String s1,String s2){
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
    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("["+ag+"] doing: "+action);
        
        boolean result = true;
		if (action.getFunctor().equals(hd)) {
        	/*Calculating hammingDistance */
			int distance=hummingDistance(action.getTerm(0).toString(),action.getTerm(1).toString());
        	logger.info("Calculating HammingDistance !!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1)+" D="+distance);
			
			addPercept(ag,Literal.parseLiteral("distance("+distance+")"));
        }
		if (action.getFunctor().equals(hde)) {
        	/*Calculating hammingDistance */
			int distance=hummingDistance(action.getTerm(0).toString().replaceAll("\"",""),realString);
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
			//addPercept(ag,Literal.parseLiteral("distance("+distance+")"));
        }
		if (action.getFunctor().equals(us)) {
			String newString=updateString(action.getTerm(0).toString(),action.getTerm(1).toString());
			logger.info("Calculating updateString !!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1)+" "+newString);
			addPercept(ag,Literal.parseLiteral("comunicateUpdate("+newString+")"));
		}
		if (action.getFunctor().equals(sc)) {
			logger.info("Finito  ");
			doNextCoupleChat();
			//clearPercepts(ag);
		}
        return result;
    }   

}
