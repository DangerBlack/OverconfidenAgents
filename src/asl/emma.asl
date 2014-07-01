// Agent emma in project people

/* Initial beliefs and rules */
myString(0111100001).

/* Initial goals */

!exchange.

/* Plans */

+!exchange : myString(Num) <- .print("i am going to start.");
					.send(jim, askOne, tellString(Num)).


+anotherString(Num): myString(MyStr) 
	<- .print("jim has string:",Num);
	compare(Num,MyStr).