// Agent dummy in project people

/* Initial beliefs and rules */

/* Initial goals */

/*
+myString(X):true <- .print(X).

+confidence(X):true <- .print("confidenza",X).*/

+confidence(X):true <- .print("SONO ad ",X).

+comunicate(ME,YOU):confidence(Y) 
					<- .print("Voglio parlare con ",YOU);
						.send(YOU,tell,anotherConfidence(ME,Y)).

+anotherConfidence(YOU,Y):confidence(X) & X>=Y
					<- .print("Ho ricevuto la confidenza, sono il leader");
						.send(YOU,tell,leader(no));
					   +leader(si).
					   
					
+anotherConfidence(YOU,Y):confidence(X) & X<Y
					<- .print("Ho ricevuto la confidenza, ",YOU," è il leader");
					.send(YOU,tell,leader(si));
					+leader(no).
					
+leader(X):true
			<- .print(X," leader").