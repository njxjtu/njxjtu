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
public class ana extends GameLoop
{
	private ImageSprite cover=new ImageSprite("cover.gif");
	private ImageSprite cover1=new ImageSprite("cover1.gif");
	private ImageSprite cover2=new ImageSprite("cover2.gif");
	private ImageSprite table=new ImageSprite("table.gif");
	private ImageSprite end=new ImageSprite("end.gif");
	private Sound process=new Sound("process.wav");
	private Sound gotme=new Sound("gotme.wav");
//	private Sound ender=new Sound("ender.wav");	
	private Sprite tri ;
	private int score,timeLeft,storecounter,timer=0,j,t,n=100000,d=1;
	private Sprite[] store=new Sprite[n];	
	private int[] sc=new int[n];
	private StringSprite scoreSprite,timerSprite;	
	private double i,k,timePassed,r,alpha,delta,PI=3.1416;
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
		timeLeft=100;
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
		
		process.play();
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
		store[0].setScale(0.1);
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
		  k=k*4;
		  k=Math.ceil(k);
		  t=(int)k;
		  switch(t){
		  case 1:tri.setLocation(0,0);break;
		  case 2:tri.setLocation(0,1);break;
		  case 3:tri.setLocation(1,0);break;
		  case 4:tri.setLocation(1,1);break;
		  }
		  tri.setScale( 0.1 );

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
		canvas.addSprite(timerSprite);
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
		
 double degree=(tri.getLocation().y-0.5)/(tri.getLocation().x-0.5);   
           if(tri.getLocation().x<0.5&&tri.getLocation().y<0.5){		  
			   if(degree>10){tri.translate(0.001,degree*0.001);}
			   if(degree<10){tri.translate(0.01,degree*0.01);}
		   }
		   if(tri.getLocation().x>0.5&&tri.getLocation().y<0.5){
			   if(degree<-10){tri.translate(-0.001,-degree*0.001);}
			   if(degree>-10){tri.translate(-0.01,-degree*0.01);}
		   }
		   
		   if(tri.getLocation().x<0.5&&tri.getLocation().y>0.5){
			   if(degree<-10){tri.translate(0.001,degree*0.001);}
			   if(degree>-10){tri.translate(0.01,degree*0.01);}
		   }
		   
		   if(tri.getLocation().x>0.5&&tri.getLocation().y>0.5){
			   if(degree>10){tri.translate(-0.001,-degree*0.001);}
			   if(degree<10){tri.translate(-0.01,-degree*0.01);}
			   
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
		store[storecounter].setScale(0.1);
		store[storecounter].setLocation(tri.getLocation());
		store[storecounter].setColor(tri.getColor());
		canvas.addSprite(store[storecounter]);
		sc[storecounter]=1;
		tri.kill();
		existance=false;
		break;}
		if(tri.getColor()==store[i].getColor()){
			gotme.play();
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
if(tri.getLocation().x>=0.5&&tri.getLocation().y>=0.5){
	r=Math.sqrt(Math.pow(tri.getLocation().y-0.5, 2)+Math.pow(tri.getLocation().x-0.5, 2));
	delta=Math.asin((tri.getLocation().y-0.5)/r);
	tri.setLocation(r*Math.cos(delta+alpha)+0.5,r*Math.sin(delta+alpha)+0.5);}	

if(tri.getLocation().x>0.5&&tri.getLocation().y<=0.5){
	r=Math.sqrt(Math.pow(tri.getLocation().y-0.5, 2)+Math.pow(tri.getLocation().x-0.5, 2));
	delta=Math.asin((tri.getLocation().y-0.5)/r);
	tri.setLocation(r*Math.cos(delta+alpha)+0.5,r*Math.sin(delta+alpha)+0.5);}	

if(tri.getLocation().x<0.5&&tri.getLocation().y<=0.5){
	r=Math.sqrt(Math.pow(tri.getLocation().y-0.5, 2)+Math.pow(tri.getLocation().x-0.5, 2));
	delta=Math.asin((tri.getLocation().y-0.5)/r);
	tri.setLocation(0.5-r*Math.cos(delta-alpha),r*Math.sin(delta-alpha)+0.5);}	

if(tri.getLocation().x<0.5&&tri.getLocation().y>=0.5){
	r=Math.sqrt(Math.pow(tri.getLocation().y-0.5, 2)+Math.pow(tri.getLocation().x-0.5, 2));
	delta=Math.asin((0.5-tri.getLocation().y)/r);
	tri.setLocation(0.5-r*Math.cos(delta+alpha),0.5-r*Math.sin(delta+alpha));}	
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
	public void checkdead(){
		if(timeLeft>0){
		   for(int i=0;i<storecounter;i++){
			   if(Math.sqrt(Math.pow(store[i].getLocation().y-0.5, 2)+Math.pow(store[i].getLocation().x-0.5, 2))>=0.4){
					end.setScale(1);
					end.setLocation(0.5,0.5);
					canvas.addSprite(end);
				   dead=true;break;
			   }
		   }
		}
		
		if(dead==true&&d==1){
			end.setScale(1);
			end.setLocation(0.5,0.5);
			canvas.addSprite(end);
		//	process.clearAll();
		//	ender.play();
		}
		dead=false;
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
		   if(timeLeft>0){	
			   moveCenter();
			   input();
			   handlecollision();
			   checkdead();


			}
				}
	   
	   }
	///////////////////////////////////
	}

