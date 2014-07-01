//has(couchPotato,beer).
//!drink.
//
//
//+!drink : has(couchPotato,X)<-
//sip(beer);
//!drink.
//
//-!drink <- .println("i neeed more beer!").
//+!drink :  true.
 
myString(0101011101).

// gets the price for the product,
// a random value between 100 and 110.
price(X) :- .random(R) & X = (10*R)+100.

+?tellString(String) : myString(Num)
  	<-	.print("emma has string:",String);			
  		.send(emma,tell,anotherString(Num)).
  		
 +has(Str):true 
	<- .print(Str).   
  					

