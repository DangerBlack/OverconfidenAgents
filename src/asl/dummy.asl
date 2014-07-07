/**
 * ALMA MATER STUDIORUM BOLOGNA - Universita' degli Studi di Bologna
 * PROGETTO DI Simulazione di sistemi Mod2
 * Anno Accademico 2013/2014
 *
 * Agent ASL, unique one for both Teacher & Student
 * 
 * @author Baschieri Daniele, daniele.baschieri[at]studio.unibo.it
 * @author Liu Tong, tong.liu2[at]studio.unibo.it
 * @version  1.0
 */



/* Initial believes will be added when agents receive notification from the environment */

+receiveString(X):true <- +myString(X).

+receiveConfidence(X):true <- +myConfidence(X).


/* Agent receive notification from environment, goals are triggered ... */

+communicate(ME,YOU)[source(Percepts)]:true
					<-  +me(ME);
						+you(YOU);
						!startALesson.
						
+!startALesson:true
					<-  ?me(ME);
						?you(YOU);
						?myConfidence(Y);
						.send(YOU,achieve,teachMe(ME,Y)).
	
-!startALesson:true		
						<- .print("Oh no! I cannot start the conversation because I do not know the roles and my confidence!"); 
						concludeCommunication.
									
									
									
/* TEACHER: I feel like to be the teacher! I inform you, you are not the teacher */
+!teachMe(YOU,Y):myConfidence(X) & X>=Y
					<-  printj("I am the teacher beacause I am more confident than the student ",YOU);
						+you(YOU);
						.send(YOU,tell,teacher(no));
						+teacher(yes).
									
										
					   					   
/* STUDENT: I am not confident enough and I invite you to be the teacher */					
-!teachMe(YOU,Y): true
					<- printj("Unfortunately I am not confident enough to be the teacher ");
					   +you(YOU);
					   +teacher(no);
					   .send(YOU,tell,teacher(yes)).
					  				
					
					
/* TEACHER: let's first see if you are interested in my string then try to learn it */			
+teacher(yes): true
			<- 	?you(YOU); ?myString(TEACHERSTRING);
				printj("TEACHER sends ",YOU," his string: ",TEACHERSTRING);
			    .send(YOU,achieve,getInterestDistance(TEACHERSTRING)).



/* STUDENT: now I need to calculate the humming distance */
+!getInterestDistance(TEACHERSTRING): teacher(no)
			<-  ?myString(MYSTRING);
				+teacherString(TEACHERSTRING);
				printj("now I need to calculate the humming distance ",MYSTRING," =?= ",TEACHERSTRING);
				hammingDistance(MYSTRING,TEACHERSTRING).
					
				
-!getInterestDistance(_): true
			<- .print("WTF ERROR i am the teacher but i have been ask to calculate distance! That is odd! ",S1," =?= ",S2).



/* STUDENT: I received the distance result */
+interestDistance(D)[source(Percepts)]: true <- !decideToLearn.



/* STUDENT: decide whether to learn */
+!decideToLearn :true
	        <- 	?areWeSimilarEnough;
				!learn.



/* STUDENT: we are not similar */
-!decideToLearn: true
		<-	printj("i am NOT interested");  
		concludeCommunication.



/* STUDENT: test the similarity */
+?areWeSimilarEnough:interestDistance(D) &  myConfidence(X) & D<=10-X
<- printj(" D is",D," X is",X," lower bound is",X-4);printj("distance ", D ,"<= my confidence" ,X," i am interested to learn ").



/* STUDENTE: learn */
+!learn:true
<- ?teacherString(TEACHERSTRING);
	hammingDistanceEnv(TEACHERSTRING). 


-!learn:true
<- .print("I do not have the teacher string any more how can proceed learning !?"); concludeCommunication. 
		


/* STUDENTE: learning outcome  */		
+result(OUTCOME)[source(Percepts)]: true
			<- !integrateNewKnowledge.
			
			
/* STUDENTE: integrate parts of teacher string to my knowledge */			
+!integrateNewKnowledge:true
			<- 	?result(success);
				?teacherString(TEACHERSTRING);?myString(MYSTRING); 
			    getNewString(TEACHERSTRING,MYSTRING).
			    
-!integrateNewKnowledge:true <- printj("i am not able to learn that! ");  concludeCommunication.
			


/* STUDENTE: successfully integrated the knowledge */			
+newKnowledge(NEWSTRING)[source(Percepts)]:true <-  -+myString(NEWSTRING);concludeCommunication.



/* TEACHER & STUDENT reset believes */
+reset(belief)[source(Percepts)]: true
			<- printj("CANCELLO LE MIE CREDENZE");
				-teacherString(_)[source(_)];
				-me(_)[source(_)];
				-you(_)[source(_)];
				-teacher(_)[source(_)];
				-communicate(_,_)[source(_)]; 
				
				-reset(_)[source(_)];
				-newKnowledge(_)[source(_)];
				-result(_)[source(_)];
				-interestDistance(_)[source(_)];
				printj("****CANCELLATE LE MIE CREDENZE");
				informReady.