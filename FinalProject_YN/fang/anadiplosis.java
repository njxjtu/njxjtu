package fang;
import fang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.lang.Object.*;
import java.math.*;
import java.net.URL;

import tetris.LoopTetris.FirstTracker;

/**This is a fun, simple game I made using
 * the FANG Engine.
 * @author Your Name Here
 */
public class anadiplosis extends GameLoop
{
	private ImageSprite cover=new ImageSprite("cover.gif");
	private ImageSprite cover1=new ImageSprite("cover1.gif");
	private ImageSprite cover2=new ImageSprite("cover2.gif");
	private ImageSprite table=new ImageSprite("table.gif");
	private Sound process=new Sound("process.wav");	
	private Sprite center;
	private Sprite tri ;
	private StringSprite scoreSprite;
	private int score;
	private int timeLeft;
	private StringSprite timerSprite;
	private int timer=0;
	private int j,t;
	private double i,k,timePassed;
	///////////////////////////////////////////////////////////////
	public void startGame()
	{

		toggleAudible();

		table.setScale(1);
		table.setLocation(0.5,0.5);
		canvas.addSprite(table);

		score=0;
		timeLeft=100;
		makeSprites();
		addSprites();
		scheduleRelative(new TimeUpdater(), 1);
		setHelpText("Lots of help text here later");

		cover1.setScale(1);
		cover1.setLocation(0.25,0.5);
		canvas.addSprite(cover1);

		cover2.setScale(1);
		cover2.setLocation(0.75,0.5);
		canvas.addSprite(cover2);

		cover.setScale(1);
		cover.setLocation(0.5,0.5);
		canvas.addSprite(cover);
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
		center=new OvalSprite(1, 1);
		center.setScale(0.2);
		center.setLocation(0.5, 0.5);
		center.setColor(Color.BLACK);
		
		  i=Math.random();
		  i=(float)i;
		  i=i*4;
		  i=Math.ceil(i);
		  j=(int)i;
		  switch(j){
		  case 1:tri=new OvalSprite(1, 1);tri.setColor( Color.CYAN );break;
		  case 2:tri=new OvalSprite(1, 1);tri.setColor( Color.BLUE ) ;break;
		  case 3:tri=new OvalSprite(1, 1);tri.setColor( Color.ORANGE ) ;break;
		  case 4:tri=new OvalSprite(1, 1);tri.setColor( Color.YELLOW ) ;break;
		  }
	      
		  k=Math.random();
		  k=(float)k;
		  k=k*4;
		  k=Math.ceil(k);
		  t=(int)k;
		  switch(t){
		  case 1:tri.setLocation(random.nextDouble(),0);break;
		  case 2:tri.setLocation(0,random.nextDouble());break;
		  case 3:tri.setLocation(1,random.nextDouble());break;
		  case 4:tri.setLocation(random.nextDouble(),1);break;
		  }
		  tri.setScale( 0.2 );

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
		canvas.addSprite(center);
		canvas.addSprite(tri);
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
	////////////////////////////////////////////////////////////////////
	public void advanceFrame(double timePassed)
	   {
		   if(timeLeft>0)	   
	   {
        tri.move();
	   }
	   }
	///////////////////////////////////

	}
//***////////////////////////////////////////////////////////////////
