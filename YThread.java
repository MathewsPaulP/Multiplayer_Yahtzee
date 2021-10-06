import java.net.*;
import java.io.*;

public class YThread extends Thread
{
	  private Socket Tsock = null;
	  private YState game;
	  private String Threadname;

	  private BufferedReader in = null;
	  private PrintWriter out = null;
	
	  
	  //variable holding game state, i.e. round started or not
	  boolean initialised=false;
	   
	  //Setup the thread
	  	public YThread(Socket Ssock, String ActionServerThreadName,YState SharedObject) 
	  	{ Tsock = Ssock;
		  game = SharedObject;
		  Threadname = ActionServerThreadName;
		  try
		  	{out = new PrintWriter(Tsock.getOutputStream(), true);
		  	in = new BufferedReader(new InputStreamReader(Tsock.getInputStream()));
		  	}
		  catch(IOException e)
		  {System.err.println("cant listen on port");			  
		  }
		}

	  //this is where the tokens recieved from client is processed
	  public void run() 
	  {
	    try 
	    {
	      System.out.println(Threadname + " initialising.");
	      String inputLine, outputLine;

	      //while loop runs as long as client speaks
	      while ((inputLine = in.readLine()) != null) 
	      {
	    	  //token letting server know client has started 
	    	  if(inputLine.equals("go"))
	      	  {		try
	      			{game.acquireLock();
	      			 game.initiate(Threadname);
	      			 game.releaseLock();
	      			//prompt telling client to ready up
	      			 out.println("Welcome "+Threadname+", Press R to ready up");//prompt telling client to ready up
	      			}
	      			catch(InterruptedException e) 
	  				{System.err.println("Failed to get lock when reading:"+e);
	  				}
	      	  }
	    	  else
	    	  {		
	    		  	//token letting server know client is ready
	    		  	if(inputLine.equalsIgnoreCase("r"))
	      			{int r=0;
	    		  		try
	      				{game.acquireLock();
	      				game.ready();//increments the readyflags
	      				r=game.ground();//just holds round to print server messages
	      				game.releaseLock();
	      				//server acknowledges player ready and prompts to check if other players are ready
	      				out.println("waiting for turn, press c to check if its your turn");
	      				}
	      			catch(InterruptedException e) 
	      				{System.err.println("Failed to get lock when reading:"+e);
	      				}
	    		  		System.out.println(Threadname+" ready for Round "+r);
	      			}	
	    	  		else
	    	  		{
	    	  			//token letting server know client is ready to send score
	    	  			if(inputLine.equals("done"))
	    	  			{
	    	  				System.out.println(Threadname+" sending score");
	    	  				//server telling client its ready to recieve score
	    	  				out.println("ok");
	    	  				inputLine=in.readLine();
	    	  				//converts input string to double
	    	  				double temp = Double.valueOf(inputLine);
	    	  					try
	    	  					{game.acquireLock();
	    	  					//adds score to score board
	    	  					outputLine = game.processInput(Threadname, temp);
	    	  					game.releaseLock();
	    	  					//server sends k token to acknowledge score added to scoreboard
	    	  					out.println(outputLine);
	    	  					}
	    	  					catch(InterruptedException e) 
	    	  					{System.err.println("Failed to get lock when reading:"+e);
	    	  					}
	    	  					//System.out.println(temp);

	    	  			}	
	    	  			else 
	    	  			{
	    	  					//token from client asking if its their turn
	    	  					if(inputLine.equalsIgnoreCase("c"))
	    	  					{	System.out.println(Threadname+" is checking if its their turn");
	    	  						String test="";
	    	  						try
	    	  						{game.acquireLock();
	    	  						initialised=game.state();//all ready flag
	    	  						test=game.turn();//player turn flag
	    	  						game.releaseLock();
	    	  						}
	    	  						catch(InterruptedException e) 
	    	  						{System.err.println("Failed to get lock when reading:"+e);
	    	  						}
	      		
	    	  						//System.out.println(test);
	    	  						//System.out.println(initialised);
	    	  						
	    	  						//checks if all players ready and its clients turn
	    	  						if((initialised)&&(Threadname.equals(test)))
	    	  						{	System.out.println(Threadname+ " Started turn");
	    	  							out.println("Press any key to start");
	    	  							outputLine="GG";//token that lets client start round
	    	  							out.println(outputLine);
	    	  							initialised=false;
	    	  							//started=true;
	    	  						}
	    	  						else
	    	  						{out.println("It's not your turn, press c to check again");
	    	  						}
	    	  						initialised=false;
	    	  						//started=true;
	    	  					}
	    	  					else 
	    	  					{
	    	  						//token telling server client wants to start next round
	    	  						if(inputLine.equals("next"))
	    	  						{	
	    	  							//loops back to ready if statement to check if all players ready
	    	  							out.println("Press R to get ready for next round");
	    	  						}
	    	  						else
	    	  						{	//token telling server client has finished turn
	    	  							if(inputLine.equals("e"))
	    	  							{
	    	  								System.out.println(Threadname+" finished turn");
	    	  								//String temp="";
		    	  							try
		    	  							{game.acquireLock();
		    	  							game.finish();//increments the round end checkflags
		    	  							game.releaseLock();
		    	  							//server tells client to check if scoreboard is ready
		    	  							out.println("Press V to view the Score Board");
											/*
											 * System.out.println(temp); out.println(temp);
											 */
		    	  							}
		    	  							catch(InterruptedException e) 
		    	  							{System.err.println("Failed to get lock when reading:"+e);
		    	  							}
	    	  							}
	    	  							else
	    	  							{
	    	  								//token asking server for the score board
	    	  								if(inputLine.equalsIgnoreCase("v"))
	    	  								{
	    	  									System.out.println(Threadname+" accessing Score Board");
	    	  									int ch=0;
	    	  									boolean flag=false;
			    	  							try
			    	  							{game.acquireLock();
			    	  							flag=game.condition();//calls the round end flag
			    	  							ch=game.np();//gets the player turn
			    	  							game.releaseLock();

			    	  							}
			    	  							catch(InterruptedException e) 
			    	  							{System.err.println("Failed to get lock when reading:"+e);
			    	  							}	
			    	  							
			    	  							//checks if round end flag and player turn flags
				    	  						if(flag&&ch==0)
				    	  						{	System.out.println("viewing Score Board");
				    	  							String sb="";
				    	  							//token letting client know server is ready to send score
				    	  							out.println("n");
			    	  								try
			    	  								{game.acquireLock();
			    	  								 //gets score
			    	  								 sb=game.resurrect();
			    	  								 game.releaseLock();
			    	  								}
			    	  								catch(InterruptedException e) 
			    	  								{System.err.println("Failed to get lock when reading:"+e);
			    	  								}
				    	  							
			    	  								//sends score
				    	  							outputLine=sb;
				    	  							out.println(outputLine);
				    	  							
				    	  							//started=true;
				    	  						}
				    	  						else
				    	  						{//asks client to send token again
				    	  							out.println("Round not over, press v to try again");
				    	  						}
				    	  						initialised=false;//resets ready flag for next round
	    	  								}
										/*
										 * this gives me errors for some reason
										 * else {out.println("wrong command, try again"); }
										 */
	    	  								
	    	  							}
	    	  						}//else bracket next
	    	  					}//else bracket c
	    	  				}//else bracket done	    	  				
	      		}//else bracket r			
	    	  }//else bracket go  
	      }//while bracket	      
	    }//trybracket
	     catch (IOException e) 
	     {	e.printStackTrace();
	     }
	 }	   
}



