// Agent dummy in project people

/* Initial beliefs and rules */

/* Initial goals */

/*
+myString(X):true <- .print(X).

+confidence(X):true <- .print("confidenza",X).*/

+confidence(X):true <- .print("SONO ad ",X).

+comunicate(ME,YOU):confidence(Y) 
					<- .print("Voglio parlare con ",YOU);
						+me(ME);
						+you(YOU);
						.send(YOU,tell,anotherConfidence(ME,Y)).

+anotherConfidence(YOU,Y):confidence(X) & X>=Y
					<- .print("Ho ricevuto la confidenza, sono il leader");
						+you(YOU);
						.send(YOU,tell,leader(no));
					   +leader(si).
					   
					
+anotherConfidence(YOU,Y):confidence(X) & X<Y
					<- .print("Ho ricevuto la confidenza, ",YOU," è il leader");
					+you(YOU);
					.send(YOU,tell,leader(si));
					+leader(no).
					
			
+leader(si): you(YOU) & myString(S)
			<- .print("Invio la mia Stringa a ",YOU," leader ",S);
			   .send(YOU,tell,anotherString(YOU,S)).

+anotherString(YOU,S2): myString(S1) & leader(no)
			<- .print("Ho ricevuto la stringa voglio confrontarle ",S1," =?= ",S2);
				hammingDistance(S1,S2).

+distance(D): confidence(X) & D<X & you(YOU) & myString(S)
			<- .print("La distanza è ",D," vogliamo lavorare insieme");
			   .send(YOU,tell,workTogether(S)).

+distance(D): confidence(X) & D>=X
			<- .print("La distanza è ",D," failure");
				+concludeCommunication.

+workTogether(S2): myString(S1)
				<-.print("Confronto con l'ambiente");
					-workTogether(_)[source(_)];
					hammingDistanceEnv(S1).
					
+result(success): workTogether(S2) & myString(S1)
			<- .print("E' stato un successo");
			   updateString(S1,S2).

+result(failure):true
			<- +concludeCommunication.
			   
+comunicateUpdate(S): you(YOU)
			<- .print("Aggiorna la tua credenza");
				.send(YOU,tell,updateMyString(S)).

+updateMyString(S): true
			<- .print("Aggiorno la stringa ",S);
			   -+myString(S)[source(percept)];
			   +concludeCommunication.

+concludeCommunication: you(YOU)
			<- 	-comunicate(_,_)[source(_)];
				-anotherConfidence(_,_)[source(_)];
				-anotherString(_,_)[source(_)];
				-distance(_)[source(_)];
				-result(_)[source(_)];
				-comunicateUpdate(_)[source(_)];
				-updateMyString(_)[source(_)];
				-concludeCommunication(_)[source(_)];
				-you(_)[source(_)];
				-me(_)[source(_)];
				.send(YOU,tell,concludeCommunicationHard).

+concludeComunicationHard:true
			<- 	+concludeComunication;
				-concludeComunicationHard;
				speechConcluded.