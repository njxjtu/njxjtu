package anadiplosis;

import fang.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.lang.*;
import java.math.*;
import java.net.URL;
import java.io.*;

/**This is a fun, simple game I made using
 * the FANG Engine.
 * @author Your Name Here
 */
public class ana2 extends GameLoop
{
	private ImageSprite cover=new ImageSprite("cover.gif");
	private ImageSprite cover1=new ImageSprite("cover1.gif");
	private ImageSprite cover2=new ImageSprite("cover2.gif");
	private ImageSprite table=new ImageSprite("table.gif");
	private ImageSprite end=new ImageSprite("end.gif");
	private ImageSprite levelone=new ImageSprite("levelone.gif");
	private ImageSprite leveltwo=new ImageSprite("leveltwo.gif");
	private ImageSprite levelthree=new ImageSprite("levelthree.gif");
	private ImageSprite levelfour=new ImageSprite("levelfour.gif");
	private ImageSprite levelfive=new ImageSprite("levelfive.gif");
	private ImageSprite win=new ImageSprite("win.gif");
	private Sound process=new Sound("process.wav");
	private Sound gotme=new Sound("gotme.wav");
	private Sound over=new Sound("over.wav");	
	private Sprite tri ;
	private int score,timeLeft,storecounter,timer=0,j,t,n=100000,d=1,two=0;
	private Sprite[] store=new Sprite[n];	
	private int[] sc=new int[n];
	private StringSprite scoreSprite,timerSprite;	
	private double i,k,timePassed,r,alpha,theta,PI=3.1416,v=0.0005,scale=0.1;
	private boolean existance=false,dead;
	private char keyPressed;

	///////////////////////////////////////////////////////////////
		
	public void startGame()
	{
		toggleAudible();

		table.setScale(1);
		table.setLocation(0.5,0.5);
		canvas.addSprite(table);

		score=0;
		timeLeft=1000;
		storecounter=1;
		for(int i=0;i<n;i++){sc[i]=0;}		
		for(int i=0;i<n;i++){store[i]=null;}

		makeSprites();
		addSprites();
		
		scheduleRelative(new TimeUpdater(), 1);
		setHelpText("Make the dots with the same color meet by rotating the table pressing 'a' or 'l'. You will lose the game if any dot on the table exceeds the border");

		cover1.setScale(1);
		cover1.setLocation(0.25,0.5);
		canvas.addSprite(cover1);

		cover2.setScale(1);
		cover2.setLocation(0.75,0.5);
		canvas.addSprite(cover2);

		cover.setScale(1);
		cover.setLocation(0.5,0.5);
		canvas.addSprite(cover);
		
		levelone.setScale(1);
		levelone.setLocation(0,0.5);
		
		leveltwo.setScale(1);
		leveltwo.setLocation(0,0.5);
		
		levelthree.setScale(1);
		levelthree.setLocation(0,0.5);
		
		levelfour.setScale(1);
		levelfour.setLocation(0,0.5);
		
		levelfive.setScale(1);
		levelfive.setLocation(0,0.5);
		
		win.setScale(1);
		win.setLocation(0,0.5);
		
		process.play();
//		ender.play();
	}
	class TimeUpdater implements Alarm
	{
		public void alarm()
		{
			timeLeft--;
			updateTimer();
			if(timeLeft>0)
			{
				scheduleRelative(this, 1);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	private void makeSprites()
	{
		store[0]=new OvalSprite(1, 1);
		store[0].setScale(scale);
		store[0].setLocation(0.5, 0.5);
		store[0].setColor(Color.BLACK);
		
		if(timer>100&&!existance){		
		  i=Math.random();
		  i=(float)i;
		  i=i*4;
		  i=Math.ceil(i);
		  j=(int)i;
		  switch(j){
		  case 1:tri=new OvalSprite(1, 1);tri.setColor( Color.CYAN );break;
		  case 2:tri=new OvalSprite(1, 1);tri.setColor( Color.MAGENTA ) ;break;
		  case 3:tri=new OvalSprite(1, 1);tri.setColor( Color.RED ) ;break;
		  case 4:tri=new OvalSprite(1, 1);tri.setColor( Color.YELLOW ) ;break;
		  }
	      
		  k=Math.random();
		  k=(float)k;
		  k=k*8;
		  k=Math.ceil(k);
		  t=(int)k;
		  switch(t){
		  case 1:tri.setLocation(0,0);break;
		  case 2:tri.setLocation(0,1);break;
		  case 3:tri.setLocation(1,0);break;
		  case 4:tri.setLocation(1,1);break;
		  case 5:tri.setLocation(0.5,0);break;
		  case 6:tri.setLocation(0,0.5);break;
		  case 7:tri.setLocation(0.5,1);break;
		  case 8:tri.setLocation(1,0.5);break;
		  default:;
		  }

		  { tri.setScale( scale );}

		}

		scoreSprite=new StringSprite("Score: "+score);
		scoreSprite.setHeight(0.1);
		scoreSprite.rightJustify();
		scoreSprite.topJustify();
		scoreSprite.setLocation(1, 0);

		timerSprite=new StringSprite("Timer: "+timeLeft);
		timerSprite.leftJustify();
		timerSprite.topJustify();
		timerSprite.setHeight(0.1);
		timerSprite.setLocation(0, 0);
	}
	/////////////////////////////////////////////////////////////
	private void addSprites()
	{
		canvas.addSprite(store[0]);
		if(timer>100&&!existance) canvas.addSprite(tri);
		
		canvas.addSprite(scoreSprite);
	//	canvas.addSprite(timerSprite);
	}

	///////////////////////////////////////////////////////////////////////

	private void updateTimer()
	{
		timerSprite.setText("Timer: "+timeLeft);
	}
	///////////////////////////////////////////////////////////
	private void updateScore()
	{
		scoreSprite.setText("Score: "+score);
	}
///////////////////////////////////////////////////////////////////
	public void moveCenter(){
			scoreSprite.kill();
			timerSprite.kill();		
		makeSprites();addSprites();
		
		switch(two){
		case 0:v=0.0005;break;
		case 1:v=0.0007;break;
		case 2:v=0.001;break;
		case 3:v=0.0015;break;
		case 4:v=0.002;break;
		}
  
           if(tri.getLocation().x<0.5&&tri.getLocation().y<0.5){	
        	   double degree=(tri.getLocation().y-0.5)/(tri.getLocation().x-0.5);  
			   if(degree>10){tri.translate(v,degree*v);}
			   if(degree<10){tri.translate(10*v,degree*10*v);}
		   }
		   if(tri.getLocation().x>0.5&&tri.getLocation().y<0.5){
			   double degree=(tri.getLocation().y-0.5)/(tri.getLocation().x-0.5);  
			   if(degree<-10){tri.translate(-v,-degree*v);}
			   if(degree>-10){tri.translate(-10*v,-degree*10*v);}
		   }
		   
		   if(tri.getLocation().x<0.5&&tri.getLocation().y>0.5){
			   double degree=(tri.getLocation().y-0.5)/(tri.getLocation().x-0.5);  
			   if(degree<-10){tri.translate(v,degree*v);}
			   if(degree>-10){tri.translate(v*10,degree*10*v);}
		   }
		   
		   if(tri.getLocation().x>0.5&&tri.getLocation().y>0.5){
			   double degree=(tri.getLocation().y-0.5)/(tri.getLocation().x-0.5);  
			   if(degree>10){tri.translate(-v,-degree*v);}
			   if(degree<10){tri.translate(-10*v,-degree*10*v);}			   
		   }
		   
		   if(Math.abs(tri.getLocation().x-0.5)<0.001&&tri.getLocation().y<0.5){
			   tri.translate(0,14*v);
		   }
		   
		   if(Math.abs(tri.getLocation().y-0.5)<0.001&&tri.getLocation().x<0.5){
			   tri.translate(14*v,0);
		   }
		   
		   if(Math.abs(tri.getLocation().x-0.5)<0.001&&tri.getLocation().y>0.5){
			   tri.translate(0,-14*v);
		   }
		   
		   if(Math.abs(tri.getLocation().y-0.5)<0.001&&tri.getLocation().x>0.5){
			   tri.translate(-14*v,0);
		   }
 existance=true;
	}
	////////////////////////////////////////////////////////////////////
	public void handlecollision(){
	for(int i=0;i<storecounter;i++){
		if(store[i]!=null){
		if(tri.intersects(store[i])&&sc[storecounter]==0)
		{
			if(tri.getColor()!=store[i].getColor()){
		store[storecounter]=new OvalSprite(1, 1);
		{store[storecounter].setScale(scale);}
		store[storecounter].setLocation(tri.getLocation());
		store[storecounter].setColor(tri.getColor());
		canvas.addSprite(store[storecounter]);
		sc[storecounter]=1;
		tri.kill();
		existance=false;
		break;}
		if(tri.getColor()==store[i].getColor()){
			gotme.play();
			score++;
			store[i].kill();
			for(int j=i+1;j<storecounter;j++){store[j-1]=store[j];sc[j-1]=sc[j];}
			sc[storecounter-1]=0;
			storecounter--;
			tri.kill();existance=false;}
		}
		}
	}
		if(sc[storecounter]==1){storecounter++;};
		}
		
	///////////////////////////////////////////
	public void flywith(double alpha, Sprite tri){
		r=Math.sqrt(Math.pow(tri.getLocation().y-0.5, 2)+Math.pow(tri.getLocation().x-0.5, 2));
		theta=(Math.atan2(tri.getLocation().y-0.5,tri.getLocation().x-0.5));
		theta=theta+alpha;
		tri.setLocation(r*Math.cos(theta)+0.5,r*Math.sin(theta)+0.5);
	}
	
	
	////////////////////////////////////////////////////////////////////
	public void input(){	
		alpha=PI/10;
		keyPressed=getPlayer().getKeyboard().getLastKey();
	    if(keyPressed=='a'){table.rotateDegrees(-alpha);
	    for(int i=0;i<storecounter;i++){flywith(-alpha,store[i]);};
	    }
	    if(keyPressed=='l'){table.rotateDegrees(alpha);
	    for(int i=0;i<storecounter;i++){flywith(alpha,store[i]);};
	    }
	}
	/////////////////////////////////////////////////////
	/*public void flylevel(){
        if(score==)
		store[0].setScale(0.05);
		store[0].setLocation(0.5, 0.5);
		store[0].setColor(Color.BLACK);
		
	}
	*/
	//////////////////////////////////////////////////////
	public void checkdead(){
		if(timeLeft>0){
		   for(int i=0;i<storecounter;i++){
			   if(Math.sqrt(Math.pow(store[i].getLocation().y-0.5, 2)+Math.pow(store[i].getLocation().x-0.5, 2))>=0.47){
					end.setScale(1);
					end.setLocation(0.5,0.5);
					canvas.addSprite(end);
					timeLeft=1;
					process.pause();
//					over.play();
				   dead=true;break;
			   }
		   }
		}
		
	/*	if(dead==true&&d==1){
			end.setScale(1);
			end.setLocation(0.5,0.5);
			canvas.addSprite(end);
		//	process.clearAll();
		//	ender.play();
		}
		*/
		d++;
		}

	///////////////////////////////////////////////////
	public void advanceFrame(double timePassed)
	   { timer++;
	   
	   if(timer<65)cover.rotateDegrees(25.0);
	   
	   if(timer==65)cover.kill();
	   
	   if(timer>65&&timer<=100){
		   cover1.setLocation(cover1.getLocation().x-0.5*timePassed,cover1.getLocation().y);
		   cover2.setLocation(cover2.getLocation().x+0.5*timePassed,cover2.getLocation().y);
	   }
	   
	   if(timer>100){
		   canvas.addSprite(levelone);levelone.setLocation(levelone.getLocation().x+0.5*timePassed,levelone.getLocation().y);
		   if(timeLeft>0&&dead!=true){	
			   moveCenter();
			   handlecollision();
			   input();

			   checkdead();
			}
		   if(score>3){
			   canvas.addSprite(leveltwo);
			   leveltwo.setLocation(leveltwo.getLocation().x+0.5*timePassed,leveltwo.getLocation().y);
			   
			   two=1;
     //          v=0.001;
			   }
		   
		   if(score>6){
			   canvas.addSprite(levelthree);
			   levelthree.setLocation(levelthree.getLocation().x+0.5*timePassed,levelthree.getLocation().y);
			   
			   two=2;
		   }
		   
		   if(score>9){
			   canvas.addSprite(levelfour);
			   levelfour.setLocation(levelfour.getLocation().x+0.5*timePassed,levelfour.getLocation().y);
			   
			   two=3;
		   }
		  
		   if(score>12){
			   canvas.addSprite(levelfive);
			   levelfive.setLocation(levelfive.getLocation().x+0.5*timePassed,levelfive.getLocation().y);
			   
			   two=4;
		   }
		   
	/*	   if(score>15){
			   canvas.addSprite(win);
			   win.setLocation(win.getLocation().x+0.5*timePassed,win.getLocation().y);

		   }
		   */
	//	   if(dead==true&&d==1){ender.play();d++;}
				}
	   
	  
	   
	   }
	///////////////////////////////////
	}

