package people;

import jason.asSyntax.*;
import jason.environment.Environment;

import java.util.logging.Logger;

//import couch.HouseENV;

public class ENV  extends Environment{
	
	int sipCount        = 10; // how many sip the beer lasts

    // common literals
    public static final Literal sb  = Literal.parseLiteral("sip(beer)");
    public static final Literal hob = Literal.parseLiteral("has(couchPotato,beer)");
    public final String bs = new String("compare");
    //compare(Num,MyStr)

    
    static Logger logger = Logger.getLogger(ENV.class.getName());

    @Override
    public void init(String[] args) {
    	logger.info("num of agents "+args[0]);
        updatePercepts();
        
        //example, how evn controls agente belief
        Literal col1Pos = Literal.parseLiteral("newbe");
        addPercept("dummy1",col1Pos);
    }
    
    /** creates the agents percepts  */
    void updatePercepts() {
     
    	/**
    	clearPercepts("couchPotato");
        if (sipCount > 0) {
		addPercept("couchPotato", hob);}
		**/
    }
    

    @Override
    public boolean executeAction(String ag, Structure action) {
        System.out.println("["+ag+"] doing: "+action);
        
        boolean result = true;
        if (action.getFunctor().equals(bs)) {
        	
        	//example how env get information from agents when an action is performed
        	logger.info("Look!!! term1: "+action.getTerm(0)+" term2:"+action.getTerm(1));
        }
        /**
 		if (action.equals(sb)) {
            result = sipBeer();
            
        } 
        else {
        	 result = true;
           // logger.info("Failed to execute action "+action);
        }

        if (result) {
            updatePercepts();
            try { Thread.sleep(100); } catch (Exception e) {}
        }**/
        return result;
    }
    
   boolean sipBeer() {
	   /**
        if (sipCount > 0) {
            sipCount--;
            return true;
        } else {
            return false;
        }
        **/
	   return false;
   }
    

}
