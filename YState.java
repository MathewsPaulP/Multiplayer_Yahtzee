public class YState 
{
	//private String myThreadName;
	private boolean accessing=false; // true a thread has a lock, false otherwise
	private int threadsWaiting=0; // number of waiting writers

	//Scoreboard
	private double scoreBoard[]=new double[]{0, 0, 0, 0, 0 };
	//player turn board
	private String[] order=new String[] {"","0","0","0","0"};
	
	//linked to leader board below
	double score[]=new double[]{0, 0, 0, 0, 0 };
	//The Actual Leader Board, ie stores player positions
	String[] positions=new String[] {"","","","",""};
	
	int readyflags=0; //game start flags
	int checkflags=0; //game end flags
	boolean start=false; //game start status
	boolean end=false;	//game end status 
	
	//variable to hold turn and round
	int pturn=0;
	int round=0;
	
	//returns player turn
	public synchronized int np()
	{int ret=pturn;
	 return ret;
	}
	
	//returns current round
	public synchronized int ground()
	{int ret=(round+1);
	 return ret;
	}
	
	//function to pass ready flag
	public synchronized boolean state()
	{boolean ret=start;
	 return ret;
	}
	
	//function of pass round finish flag
	public synchronized boolean condition()
	{boolean ret=end;
	 return ret;
	}
	
	//returns current player turn
	public synchronized String turn()
	{//System.out.println("tuen");
		String ret=order[pturn];
	 return ret;
	}
	
		//initiates the order of play
		public synchronized void initiate(String myThreadName) 
		{
		 order[pturn]=myThreadName;
 	     pturn++;
		}
		
		//function to check ready flag and notify threads
		public synchronized void ready() 
		{
			++readyflags;
		 if(readyflags==3)
		 {start=true;
		  pturn=0;
		  notifyAll();
		 }
		 try {
			releaseLock();
		} catch (InterruptedException e) 
		 {e.printStackTrace();
		}
		}
		
		//function to check round finish flag and notify threads
		public synchronized void finish() 
		{
			++checkflags;
		 if(checkflags==3)
		 {end=true;
		  pturn=0;
		  //Tplayers=pturn;
		  checkflags=0;
		  notifyAll();
		 }
		 try {
			releaseLock();
		} catch (InterruptedException e) 
		 {e.printStackTrace();
		}
		}
		
		//recieves scores, stores them in leaderboard and sends back an acknowledgement
		public synchronized String processInput(String myThreadName, double theInput) 
		{	   
			//System.out.println(myThreadName + " sent "+ theInput);
    		String theOutput="";
			if(order[pturn].equals(myThreadName))
			{
			System.out.println(Thread.currentThread()+" is starting input process");
			
			//score processed acknowledgement turn send to client
    		String test="k";
    		//checks if round game is over or not
    		if(round<13)
    		{
    			//checks player turn count
    			if(pturn<3)
    				{	
    				System.out.println(pturn);
    				System.out.println(round);
    				  scoreBoard[pturn]=theInput;
    				
    				  //alternatively could've sorted at end of turn
					  positions[pturn]=order[pturn];
					  score[pturn]=scoreBoard[pturn];
				    	
					  //compares scores and stores in leaderboard
					  double temp=0;
				    	 String t="";
					     boolean sorted = false;
					     while(!sorted)
					     {sorted=true;
					    	 for (int i = 0; i < 3; i++) 
					        {if (score[i] < score[i+1]) 
					            {   temp = score[i];
					            	t=positions[i];
					                score[i] = score[i+1];
					                positions[i] = positions[i+1];
					                score[i+1] = temp;
					                positions[i+1] = t;
					                sorted = false;
					            }
					        }
					     }
					     
					     theOutput=test;
    					//increments turn
					     pturn++;
    					
					     //checks if all players have played
    					 if(pturn==3)
    					 {++round;
    					    pturn=0;
    						readyflags=0; 
    						start=false;
    					 }
    				}
    		}
    		else
    		{	
    			//token to end game
    			theOutput="GAME OVER";
    		}
			}
			
			//returns acknowledgement
    		return theOutput;
		}

		//print function, cause yesus
		public synchronized String resurrect() 
		{String theOutput="";
		 String test="";
		 for(int i=0;i<3;++i)
		 	{test=test+(i+1)+":"+positions[i]+" with "+score[i]+" points.  ";
			}
		 //System.out.println(test);
		 theOutput=test;
		 return theOutput;
		}
		
		//lock function from lab 4
	  public synchronized void acquireLock() throws InterruptedException{
	        Thread me = Thread.currentThread(); // get a ref to the current thread
	        //System.out.println(me.getName()+" is attempting to acquire a lock!");	
	        ++threadsWaiting;
		    while (accessing) {  // while someone else is accessing or threadsWaiting > 0
		      System.out.println(me.getName()+" waiting to get a lock as someone else is accessing...");
		      //wait for the lock to be released - see releaseLock() below
		      //Thread.sleep(100000);
		    }
		    // nobody has got a lock so get one
		    --threadsWaiting;
		    accessing = true;
		    //System.out.println(me.getName()+" got a lock!"); 
		  }

		  // Releases a lock to when a thread is finished
		  
	  		//release function from lab 4
		  public synchronized void releaseLock() throws InterruptedException {
			  //release the lock and tell everyone
		      accessing = false;
		      notifyAll();
		      Thread me = Thread.currentThread(); // get a ref to the current thread
		      //System.out.println(me.getName()+" released a lock!");
		      //Thread.sleep(100000);
		  }

}
