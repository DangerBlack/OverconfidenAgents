/* Initial beliefs and rules */

/* Initial goals */


+receiveString(X):true <- +myString(X);printj(X).

+receiveConfidence(X):true <- +myConfidence(X);printj("confidenza",X).

/* start */
+communicate(ME,YOU):myConfidence(Y) 
					<- printj("===New conversation started successfully=== \n Voglio parlare con ",YOU);
						+me(ME);
						+you(YOU);
						.send(YOU,tell,anotherConfidence(ME,Y)).

/* exchange confidence */
+anotherConfidence(YOU,Y):myConfidence(X) & X>=Y
					<- printj("Ho ricevuto la confidenza, sono il leader");
						+you(YOU);
						.send(YOU,tell,leader(no));
						+leader(si).
					   					   
					
+anotherConfidence(YOU,Y):myConfidence(X) & X<Y
					<- printj("Ho ricevuto la confidenza, ",YOU," � il leader");
					+you(YOU);
					+leader(no);
					.send(YOU,tell,leader(si)).
					
					
/* leader trigger */			
+leader(si): you(YOU) & myString(S)
			<- printj("Leader invia la Stringa a ",YOU," leader ",S);
			   .send(YOU,tell,anotherString(YOU,S)).

/* exchange string */
+anotherString(YOU,S2): myString(S1) & leader(no)
			<- printj("Ho ricevuto la stringa voglio confrontarle ",S1," =?= ",S2);
				hammingDistance(S1,S2).
				
+anotherString(YOU,S2): myString(S1)
			<- printj("WTF ",S1," =?= ",S2).

/* string distance */			
+distance(D): myConfidence(X) & D<=X & you(YOU) & myString(S)
			<- printj("La distanza � ",D," vogliamo lavorare insieme");
			   .send(YOU,tell,workTogether(S)).

+distance(D): myConfidence(X) & D>X
			<- printj("La distanza � ",D," failure");
				concludeCommunication.
/* work trigger */
+workTogether(S2): myString(S1)
				<-printj("Confronto con l'ambiente");
					hammingDistanceEnv(S1).

/* working result */					
+result(success): workTogether(S2) & myString(S1)
			<- printj("E' stato un successo");
			   updateString(S1,S2).

+result(failure):true
			<- concludeCommunication.
			

/* update results */			   
+communicateUpdate(S): you(YOU)
			<- printj("Aggiorna la tua credenza del mio partner");
				.send(YOU,tell,updateMyString(S)).

+updateMyString(S): true
			<- printj("Aggiorno la mia stringa ",S);
			   -+myString(S);
			   concludeCommunication.


/* reset believes */
+reset(belief): true
			<- printj("CANCELLO LE MIE CREDENZE");
				-communicate(_,_)[source(_)];
				-anotherConfidence(_,_)[source(_)];
				-anotherString(_,_)[source(_)];
				-distance(_)[source(_)];
				-result(success)[source(_)];
				-result(failure)[source(_)];
				-result(_)[source(_)];
				-communicateUpdate(_)[source(_)];
				-updateMyString(_)[source(_)];
				-workTogether(_)[source(_)];
				-concludeCommunication(_)[source(_)];
				-me(_)[source(_)];
				-you(_)[source(_)];
				-leader(_)[source(_)];
				-resetBelief[source(_)];
				 
				printj("****CANCELLATE LE MIE CREDENZE");
				informReady.