package com.example.annie_travislistening;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mr1.robots.travis.moves.Gesture;
import mr1.robots.travis.moves.GestureController;
import mr1.robots.travis.moves.Move;
import mr1.robots.travis.moves.ThreadScheduler;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.os.Handler;

public class TravisListeningActivity extends Activity implements ThreadScheduler {

	private int id = 2;
	private final int port = 7000;
//	private final String serverIP = "10.0.1.7";
	private PowerManager.WakeLock mWakeLock;

	//networking
	private volatile boolean listening = true;
	private volatile String response;
//	private UdpClient sender;
	
	private volatile Gesture gesture;
	private volatile Move move;
	private volatile GestureController gestureController;
	private volatile long updateTime;

	//audio tempo vars
	private volatile float beatDuration = 502;//730-you are beautiful , 994 - apologize, 502 - take your time
	private volatile int beatsPerMeasure = 4;
	private volatile float measureDuration = beatDuration * beatsPerMeasure;
	private volatile float density = 0.3f;
	
	//states
	private volatile boolean update = true;
	private boolean wakeUp = true;
	private volatile int gestureID = 0;
	
	private boolean neckUD = false;
	private boolean swoopRL = false;
	private boolean rotate = false;
	private boolean lookUpRL = false;
	private boolean shake = false;
	private boolean diagonal = false;
	private boolean lookUpD = false;
	private boolean rotateL = false;
	private boolean rotateR = false;
	private volatile boolean nodLeft = false;
	private volatile boolean nodRight = false;
	private volatile boolean nodFront = false;
	private volatile boolean nodLeftD = false;
	private volatile boolean nodRightD = false;
	private volatile boolean nodFrontD = false;
	private volatile boolean circle = false;
	private volatile boolean nodCircle = false;
	private volatile boolean halfNod = false;
	private volatile boolean groove = false;
	private volatile boolean lineup = false;
	private volatile boolean still = false;
	
	private boolean seqLeft = false;
	private boolean seqRight = false;
	private boolean seqRL = false;
	private volatile boolean SEQLlookUpRL = false;
	private volatile boolean SEQPRlookUpRL = false;
	private volatile boolean SEQRneckUD = false;
	private volatile boolean SEQPLneckUD = false;
	private volatile boolean SEQMlookUpD = false;
	private volatile boolean SEQPLrotateL = false;
	private volatile boolean SEQPRrotateR = false;
	
	private Runnable playThread;
	private Runnable updateThread;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tavis_listening);
		
		gesture = new Gesture(this,beatDuration);
		move = new Move(this,gesture.getMotorController());
		gesture.setAlternateMove(move);
		gestureController = new GestureController(this,beatDuration,gesture);
		gestureController.gesture.homeDown();
		
		// Wake Lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		mWakeLock.acquire();
		
		((TextView) findViewById(R.id.dataflow)).setText("zzz...");
		((TextView) findViewById(R.id.textbox)).setText("Sleeping...");
        ((ProgressBar) findViewById(R.id.progressBar1)).setVisibility(1);
        
		// Buttons
		Button button = (Button)findViewById(R.id.listen);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tryNewGesture();
			}
		});
		
		((Button)findViewById(R.id.dance))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		        tryNewGesture();
			}
		});

		((Button)findViewById(R.id.stop))
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goSleep();
			}
		});
		
//		sender = new UdpClient(serverIP, 5678);
        listen();
		goSleep();
		
	}

	
	
	@Override
	protected void onDestroy() {
		listening = false;
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid()); //perhaps to change later - this app won't run in the background
		mWakeLock.release();
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		
	}
	
	private void wakeUp() {
		handler.removeCallbacks(playThread);
		((TextView) findViewById(R.id.dataflow)).setText("What?!");
		((TextView) findViewById(R.id.textbox)).setText("Listening...");
		((Button) findViewById(R.id.listen)).setText("WarmUp...");
		wakeUp = true;
		playThread = new Runnable(){
			Thread cur = Thread.currentThread();
			int count = 0;
			public void run(){
				cur.setPriority(10);
				gestureController.gesture.home();
				if (count < 4) {
					gestureController.gesture.shakeNoDegree(.4f, 0, .2f, 1f);
					count++;
					handler.postDelayed(playThread,(int)(0.4 * beatDuration));
				}
			}
		};
		handler.postDelayed(playThread, 1);
	}
	
	private void tryNewGesture() {
		handler.removeCallbacks(playThread);
		((TextView) findViewById(R.id.dataflow)).setText("new gestures");
		((TextView) findViewById(R.id.textbox)).setText("Listening...");
		wakeUp = true;
		playThread = new Runnable(){
			Thread cur = Thread.currentThread();
			boolean goDown = true;
			boolean left = true;
			boolean home = false;
			int counter = 0;
			public void run(){
				cur.setPriority(10);
				if (!home) {
                    gestureController.gesture.home();
                    home = true;
                }
                if (goDown) {
                    if (left) {
                        gestureController.gesture.neckUDMove(0f, 1.0f);
                        if (counter%2 == 0)
                            gestureController.gesture.neckRLMove(.5f, 1.0f);
                        else
                            gestureController.gesture.neckRLMove(-.5f, 1.0f);
                        left = false;
                    } else {
                        gestureController.gesture.headUp(1.0f);
                        gestureController.gesture.tap(1.0f);
                        left = true;
                        goDown = false;
                    }
                } else {
                    if (left) {
                        gestureController.gesture.neckUDMove(.6f, 1.0f);
                        gestureController.gesture.neckRLMove(0f, 1.0f);
                        left = false;
                    } else {
                        gestureController.gesture.headDown(1.0f);
                        left = true;
                        goDown = true;
                        counter++;
                    }
                }
				handler.postDelayed(playThread,(int)(.5*beatDuration));
			}
		};
		handler.postDelayed(playThread, 0);
	}

	private void startDance() {
		handler.removeCallbacks(playThread);
		((TextView) findViewById(R.id.dataflow)).setText("Dancing...");
		((TextView) findViewById(R.id.textbox)).setText("Listening...");
		((Button) findViewById(R.id.listen)).setText("WarmUp...");
		wakeUp = true;
		playThread = new Runnable(){
			Thread cur = Thread.currentThread();
			boolean home = false;
			boolean up = true;
			boolean left = true;
			boolean goDown = false;
			int counter = 0;
			int num = 0;
			int t=2;
			int idxl = 1;
			int idxr = 3;
			public void run(){
				cur.setPriority(10);
				if (!home) {
					gestureController.gesture.home();
					home = true;
				}
				if (neckUD) {
					update = false;
					if (up) {
						gestureController.gesture.neckUDMove(.4f, 1.0f);
						up = false;
					}
					else {
						gestureController.gesture.neckUDMove(-.6f, 1.0f);
						up = true;
					}
					counter++;
					if (counter == 4) {
						update = true;
						neckUD = false;
						counter = 0;
					}
					gestureController.gesture.tap(1.0f);
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} 
				else if (lookUpRL) {
					update = false;
					if (goDown) {
						if (left) {
							gestureController.gesture.headFrontLeft(.5f);
							left = false;
						}
						else { 
							gestureController.gesture.headFrontRight(.5f);
							left = true;
						}
						goDown = false;
					} else {
						if (counter == 15)
							gestureController.gesture.home(.5f);
						else
							gestureController.gesture.headBackRight(.5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 16) {
						update = true;
						lookUpRL = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} 
				else if (lookUpD) {
					update = false;
					if (goDown) {
						if (t == 0) {
							left = !left;
							t = 2;
						}
						if (left) {
							gestureController.gesture.headFrontLeft(.5f);
							t--;
						}
						else { 
							gestureController.gesture.headFrontRight(.5f);
							t--;
						}
						goDown = false;
					} else {
						if (counter == 15)
							gestureController.gesture.home(.5f);
						else
							gestureController.gesture.headBackRight(.5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 16) {
						update = true;
						lookUpD = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} 
				else if (diagonal) {
					update = false;
					if (goDown) {
						if (id == 1)
							gestureController.gesture.lookUpRight(1.0f);
						else if (id == 2)
							gestureController.gesture.lookUp(1.0f);
						else
							gestureController.gesture.lookUpLeft(1.0f);
						goDown = false;
					} else {
						gestureController.gesture.lookDown(1.0f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 8) {
						update = true;
						diagonal = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				}
				else if (shake) {
					update = false;
					gestureController.gesture.shakeHead(.5f);
					counter++;
					if (counter%2 == 0)
						gestureController.gesture.tap(.5f);
					if (counter == 8) {
						if (goDown) {
							gestureController.gesture.bowDown(2.0f);
							goDown = false;
						}
						else {
							gestureController.gesture.bowUp(2.0f);
							goDown = true;
						}
					}
					if (counter == 16) {
						update = true;
						shake = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (swoopRL) {
					update = false;
					if (left) {
						gestureController.gesture.swoopLeft(1.0f);
						left = false;
					}
					else {
						if (counter == 7) 
							gestureController.gesture.home(1.0f);
						else
							gestureController.gesture.swoopRight(1.0f);
						gestureController.gesture.tap(1.0f);
						left = true;
					}
					counter++;
					if (counter == 8) {
						update = true;
						swoopRL = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (seqLeft) {
					update = false;
					if (counter == 0) {
						gestureController.gesture.homeDown(1.0f);
					} else if (counter == 1) {
						if (id == 3)
							gestureController.gesture.lookUpLeft(1.0f);
					} else if (counter == 2) {
						if (id == 2)
							gestureController.gesture.lookUpLeft(1.0f);
					} else if (counter == 3) {
						if (id == 1)
							gestureController.gesture.lookUpLeft(1.0f);
					}
					counter++;
					if (counter == 4) {
						update = true;
						seqLeft = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (seqRight) {
					update = false;
					if (counter == 0) {
						gestureController.gesture.homeDown(1.0f);
					} else if (counter == 1) {
						if (id == 1)
							gestureController.gesture.lookUpRight(1.0f);
					} else if (counter == 2) {
						if (id == 2)
							gestureController.gesture.lookUpRight(1.0f);
					} else if (counter == 3) {
						if (id == 3)
							gestureController.gesture.lookUpRight(1.0f);
					}
					counter++;
					if (counter == 4) {
						update = true;
						seqRight = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (seqRL) {
					update = false;
					if (counter == 0) {
						gestureController.gesture.home(1.0f);
					} else if (counter == 1) {
						if (id == 3)
							gestureController.gesture.lookUpLeft(1.0f);
					} else if (counter == 2) {
						if (id == 2)
							gestureController.gesture.lookUpLeft(1.0f);
					} else if (counter == 3) {
						if (id == 1)
							gestureController.gesture.lookUpLeft(1.0f);
					} else if (counter == 4) {
						if (id == 1)
							gestureController.gesture.lookUpRight(1.0f);
					} else if (counter == 5) {
						if (id == 2)
							gestureController.gesture.lookUpRight(1.0f);
					} else if (counter == 6) {
						if (id == 3)
							gestureController.gesture.lookUpRight(1.0f);
					}
					counter++;
					if (counter == 7) {
						update = true;
						seqRL = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (rotateL) {
					update = false;
					if (left) {
						gestureController.gesture.goFrontLeft(1.0f);
						left = false;
					}
					else {
						gestureController.gesture.home(1.0f);
						gestureController.gesture.tap(1.0f);
						left = true;
					}
					counter++;
					if (counter == 8) {
						update = true;
						rotateL = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (rotateR) {
					update = false;
					if (left) {
						gestureController.gesture.goFrontRight(1.0f);
						left = false;
					}
					else {
						gestureController.gesture.home(1.0f);
						gestureController.gesture.tap(1.0f);
						left = true;
					}
					counter++;
					if (counter == 8) {
						update = true;
						rotateR = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				}else if (rotate) {
					update = false;
					if (left) {
						if (id == 1)
							gestureController.gesture.goFrontRight(1.0f);
						else if(id == 3)
							gestureController.gesture.goFrontLeft(1.0f);
						else
							gestureController.gesture.lookUp(1.0f);
						left = false;
					}
					else {
						if (id == 2)
							gestureController.gesture.lookDown(1.0f);
						else
							gestureController.gesture.home(1.0f);
						gestureController.gesture.tap(1.0f);
						left = true;
					}
					counter++;
					if (counter == 8) {
						update = true;
						rotate = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (nodLeft) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(-.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 2) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 1.0f);
							gestureController.gesture.neckRLMove(.2f, 1.0f);
							left = false;
						} else {
							gestureController.gesture.neckUDMove(.6f, 1.0f);
							gestureController.gesture.neckRLMove(-.2f, 1.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 8){
						update = true;
						nodLeft = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (nodFront) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(-.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 2) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 1.0f);
							left = false;
						} else {
							gestureController.gesture.neckUDMove(.6f, 1.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 8){
						update = true;
						nodFront = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (nodRight) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(-.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 2) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 1.0f);
							gestureController.gesture.neckRLMove(-.2f, 1.0f);
							left = false;
						} else {
							gestureController.gesture.neckUDMove(.6f, 1.0f);
							gestureController.gesture.neckRLMove(.2f, 1.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 8){
						update = true;
						nodRight = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (nodLeftD) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(-.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 4) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 2.0f);
							gestureController.gesture.neckRLMove(.2f, 2.0f);
							left = false;
						} else {
							gestureController.gesture.neckUDMove(.6f, 2.0f);
							gestureController.gesture.neckRLMove(-.2f, 2.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 4){
						update = true;
						nodLeftD = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (nodFrontD) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(-.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 4) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 2.0f);
							left = false;
						} else {
							gestureController.gesture.neckUDMove(.6f, 2.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 4){
						update = true;
						nodFrontD = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (nodRightD) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(-.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					counter++;
					if (counter == 4) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 2.0f);
							gestureController.gesture.neckRLMove(-.2f, 2.0f);
							left = false;
						} else {
							gestureController.gesture.neckUDMove(.6f, 2.0f);
							gestureController.gesture.neckRLMove(.2f, 2.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 4){
						update = true;
						nodRightD = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (groove) {
					update = false;
					if (goDown) {
						if (left) {
							gestureController.gesture.neckUDMove(0f, 1.0f);
							if (counter%2 == 0)
								gestureController.gesture.neckRLMove(.5f, 1.0f);
							else 
								gestureController.gesture.neckRLMove(-.5f, 1.0f);
							left = false;
						} else {
							gestureController.gesture.headUp(1.0f);
							gestureController.gesture.tap(1.0f);
							left = true;
							goDown = false;
						}
					} else {
						if (left) {
							gestureController.gesture.neckUDMove(.6f, 1.0f);
							gestureController.gesture.neckRLMove(0f, 1.0f);
							left = false;
						} else {
							gestureController.gesture.headDown(1.0f);
							left = true;
							goDown = true;
							counter++;
						}
					}
					if (counter == 8){
						update = true;
						groove = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} 
				else if (circle) {
					update = false;
					if (counter%4 == 0) 
						gestureController.gesture.lookDownLeft(1.0f);
					else if (counter%4 == 1)
						gestureController.gesture.neckRight(1.0f);
					else if (counter%4 == 2) {
						gestureController.gesture.neckUDMove(.6f, 1.0f);
						gestureController.gesture.headMove(0f, 1.0f);
					} else
						gestureController.gesture.neckLeft(1.0f);
					counter++;
					if (counter == 8) {
						update = true;
						circle = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (nodCircle) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (goDown) {
						gestureController.gesture.headMove(.5f, .5f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(-.5f, .5f);
						goDown = true;
					}
					counter++;
					if (counter == 0 || counter == 8) {
						if (left) {
							gestureController.gesture.neckRight(4.0f);
							gestureController.gesture.handLeft(4.0f);
							left = false;
						} else {
							gestureController.gesture.neckLeft(4.0f);
							gestureController.gesture.handRight(4.0f);
							left = true;
						}
					}
					if (counter == 16) {
						update = false;
						nodCircle = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(0.5*beatDuration));
				} else if (halfNod) {
					update = false;
					if (!home) {
						gestureController.gesture.home();
						home = true;
					}
					if (counter == 0) {
						if (left) {
							gestureController.gesture.lookUpLeft(.5f);
							left = false;
						}else {
							gestureController.gesture.lookUpRight(.5f);
							left = true;
						}
					}
					if (counter > 3) {
						if (goDown) {
							gestureController.gesture.headMove(.5f, .5f);
							goDown = false;
						} else {
							gestureController.gesture.headMove(-.5f, .5f);
							goDown = true;
						}
					}
					if (counter == 4) { 
						gestureController.gesture.neckRLMoveVel(0f, 2.0f);
						gestureController.gesture.neckUDMove(0f, 2.0f);
					}
					counter++;
					if (counter > 7) {
						num++;
						counter = 0;
					}
					if (num == 4) {
						update = true;
						halfNod = false;
						num = 0;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(0.5*beatDuration));
				} else if (lineup) {
					update = false;
					if (goDown) {
						gestureController.gesture.headMove(.5f, .4f);
						goDown = false;
					} else {
						gestureController.gesture.headMove(-.5f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
					
					counter++;
					if (counter == 8) {
						if (left) {
							if(id == 1)
								gestureController.gesture.neckUDMove(-.4f, 1.0f);
							else if (id == 2)
								gestureController.gesture.neckUDMove(.1f, 1.0f);
							else
								gestureController.gesture.neckUDMove(.6f, 1.0f);
							left = false;
						} else {
							if(id == 1)
								gestureController.gesture.neckUDMove(.6f, 1.0f);
							else if (id == 2)
								gestureController.gesture.neckUDMove(.1f, 1.0f);
							else
								gestureController.gesture.neckUDMove(-.4f, 1.0f);
							left = true;
						}
						counter = 0;
						num++;
					}
					if (num == 4) {
						update = true;
						lineup = false;
						counter = 0;
						num = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (still) {
					update = false;
					counter++;
					if (counter == 16) {
						update = true;
						still = false;
						counter = 0;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (SEQLlookUpRL) {
					update = false;
					if (id == idxl) {
						if (goDown) {
							if (left) {
								gestureController.gesture.headFrontLeft(.5f);
								left = false;
							}
							else { 
								gestureController.gesture.headFrontRight(.5f);
								left = true;
							}
							goDown = false;
						} else {
							gestureController.gesture.headBackRight(.5f);
							gestureController.gesture.tap(1.0f);
							goDown = true;
						}
					}
					counter++;
					if (counter == 8) {
						idxl++;
						counter = 0;
					}
					if (idxl > 3) {
						update = true;
						SEQLlookUpRL = false;
						idxl = 1;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (SEQPRlookUpRL) {
					update = false;
					if (id >= idxr) {
						if (goDown) {
							if (left) {
								gestureController.gesture.headFrontLeft(.5f);
								left = false;
							}
							else { 
								gestureController.gesture.headFrontRight(.5f);
								left = true;
							}
							goDown = false;
						} else {
							gestureController.gesture.headBackRight(.5f);
							gestureController.gesture.tap(1.0f);
							goDown = true;
						}
					}
					counter++;
					if (counter == 8) {
						idxr--;
						counter = 0;
					}
					if (idxr < 0) {
						update = true;
						SEQPRlookUpRL = false;
						idxr = 3;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (SEQRneckUD) {
					update = false;
					if (id == idxr){
						if (up) {
							gestureController.gesture.neckUDMove(.4f, 1.0f);
							up = false;
						}
						else {
							gestureController.gesture.neckUDMove(-.6f, 1.0f);
							up = true;
						}
					}
					counter++;
					if (counter == 4) {
						counter = 0;
						idxr--;
					}
					if (idxr < 1){
						update = true;
						SEQRneckUD = false;
						idxr = 3;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (SEQPLneckUD) {
					update = false;
					if (id <= idxl){
						if (up) {
							gestureController.gesture.neckUDMove(.4f, 1.0f);
							up = false;
						}
						else {
							gestureController.gesture.neckUDMove(-.6f, 1.0f);
							up = true;
						}
					}
					counter++;
					if (counter == 4) {
						counter = 0;
						idxl++;
					}
					if (idxl > 4){
						update = true;
						SEQPLneckUD = false;
						idxl = 1;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (SEQMlookUpD) {
					update = false;
					if (id > idxl && id < idxr) {
						if (goDown) {
							if (t == 0) {
								left = !left;
								t = 2;
							}
							if (left) {
								gestureController.gesture.headFrontLeft(.5f);
								t--;
							}
							else { 
								gestureController.gesture.headFrontRight(.5f);
								t--;
							}
							goDown = false;
						} else {
							gestureController.gesture.headBackRight(.5f);
							gestureController.gesture.tap(1.0f);
							goDown = true;
						}
					}
					counter++;
					if (counter == 8) {
						counter = 0;
						idxr++;
						idxl--;
					}
					if (idxr > 4) {
						update = true;
						SEQMlookUpD = false;
						idxl = 1;
						idxr = 3;
					}
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				} else if (SEQPLrotateL) {
					update = false;
					if (id <= idxl){
						if (left) {
							gestureController.gesture.goFrontLeft(1.0f);
							left = false;
						}
						else {
							gestureController.gesture.home(1.0f);
							gestureController.gesture.tap(1.0f);
							left = true;
						}
					}
					counter++;
					if (counter == 4) {
						idxl++;
						counter = 0;
					}
					if (idxl > 4){
						update = true;
						SEQPLrotateL = false;
						idxl = 1;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				} else if (SEQPRrotateR) {
					update = false;
					if (id <= idxr){
						if (left) {
							gestureController.gesture.goFrontRight(1.0f);
							left = false;
						}
						else {
							gestureController.gesture.home(1.0f);
							gestureController.gesture.tap(1.0f);
							left = true;
						}
					}
					counter++;
					if (counter == 4) {
						idxr--;
						counter = 0;
					}
					if (idxr < 0){
						update = true;
						SEQPRrotateR = false;
						idxr = 3;
					}
					handler.postDelayed(playThread,(int)(1.0*beatDuration));
				}
				else {
					update = false;
					if (goDown) {
						gestureController.gesture.headMove(density+.2f, .5f);
						goDown = false;
					} else {
						update = true;
						gestureController.gesture.headMove(-density-.2f, .5f);
						gestureController.gesture.tap(1.0f);
						goDown = true;
					}
//					num++;
//					if (num == 4) {
//						double rand = Math.random();
//						if (rand > 0.7)
//							gestureController.gesture.neckRLMove(.4f, 2.0f);
//						else if (rand < 0.3)
//							gestureController.gesture.neckRLMove(-.4f, 2.0f);
//						num = 0;
//					}
					gestureController.gesture.neckUDMove(-.6f, .5f);
					handler.postDelayed(playThread,(int)(.5*beatDuration));
				}
			}
		};
		handler.postDelayed(playThread, 0);
	}

	
	private void rest(){
		
		handler.removeCallbacks(playThread);
		playThread = new Runnable(){
		//playThread = new TimerTask(){
			Thread cur = Thread.currentThread();
			boolean goDown = false;
			boolean isHome = false;
			public void run(){
				cur.setPriority(10);
				if (!isHome) {
					gestureController.gesture.home();
					isHome = true;
				}
				if (goDown) {
					gestureController.gesture.headMove(.2f, .5f);
					goDown = false;
					handler.postDelayed(playThread, (int)(0.5*beatDuration));
				} else {
					gestureController.gesture.headMove(-.4f, .5f);
					goDown = true;
					handler.postDelayed(playThread, (int)(0.5*beatDuration));
				}
				
			}
					
		};
		handler.postDelayed(playThread, (int)measureDuration);
	}

	private void goSleep(){
		
		handler.removeCallbacks(playThread);
		((TextView) findViewById(R.id.dataflow)).setText("zzz...");
		((TextView) findViewById(R.id.textbox)).setText("Sleeping...");
		wakeUp = false;
		playThread = new Runnable(){
		//playThread = new TimerTask(){
			Thread cur = Thread.currentThread();
			double delay = measureDuration;
			boolean goDown = false;
			boolean isHome = false;
			public void run(){
				cur.setPriority(10);
				if (!isHome) {
					gestureController.gesture.homeDown();
					isHome = true;
				}
				if (goDown) {
					gestureController.gesture.headMove(.2f, .8f);
					goDown = false;
					handler.postDelayed(playThread, (int)delay);
				} else {
					gestureController.gesture.headMove(-.4f, .8f);
					goDown = true;
					handler.postDelayed(playThread, (int)(0.5*delay));
				}
				
			}
					
		};
		handler.postDelayed(playThread, (int)measureDuration);
	}
	
	private void updateBeatDuration(final float bd) {
		updateThread = new Runnable(){
			public void run(){
				beatDuration = bd;
				updateTime = System.currentTimeMillis();
			}
		};
		handler.postDelayed(updateThread, (System.currentTimeMillis() - updateTime)%(int)beatDuration);
	}
	
	private void updateDensity(final float d) {
		density = d;
	}
		
	
	private void listen() {
			new Thread(){
				
				boolean started = false;
				long preMessageTime = 0;
				
				public void run(){
					UdpServer listener = new UdpServer(port);
					try {
						listener.listen();
						System.out.println(listening);
						while (listening == true) {
							
							if (started)
								checkTimeOut();
							
							response = listener.update();
							if (response.length() > 0) {
								System.out.println("response = " + response);
								listener.message = "";
								started = true;
								preMessageTime = System.currentTimeMillis();
								String splits[] = response.split(",");
								String command = splits[0];
								String value = "";
								if (splits.length > 1)
									value = splits[1];
								
								if (command.equalsIgnoreCase("DANCE")){
									runOnUiThread(new Runnable(){
										public void run(){
											startDance();
										}
									});							
								}  else if(command.equalsIgnoreCase("REST")){
									runOnUiThread(new Runnable(){
										public void run(){
											rest();
										}
									});							
								} else if(command.equalsIgnoreCase("BEAT")){
									final float newBeatDuration = Float.parseFloat(value);
									runOnUiThread(new Runnable(){
										public void run(){
											updateBeatDuration(newBeatDuration);
										}
									});						
								} else if(command.equalsIgnoreCase("NECKUD")){
									if ( update && qualified(value)) {
										neckUD = true;					
										gestureID = 1;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("swoopRL")){
									if ( update && qualified(value)) {
										swoopRL = true;	
										gestureID = 2;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("ROTATE")){
									if ( update) {
										rotate = true;
										gestureID = 3;
									}
								} else if(command.equalsIgnoreCase("lookUpRL")){
									if ( update && qualified(value)) {
										lookUpRL = true;
										gestureID = 4;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("shake")){
									if ( update && qualified(value)) {
										shake = true;
										gestureID = 5;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("seqL")){
									if ( update) {
										seqLeft = true;
										gestureID = 6;
									}
								} else if(command.equalsIgnoreCase("seqR")){
									if ( update) {
										seqRight = true;
										gestureID = 7;
									}
								} else if(command.equalsIgnoreCase("seqRL")){
									if ( update) {
										seqRL = true;
										gestureID = 8;
									} 
								} else if(command.equalsIgnoreCase("diagonal")){
									if ( update) {
										diagonal = true;
										gestureID = 9;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("lookUpD")){
									if ( update && qualified(value)) {
										lookUpD = true;
										gestureID = 10;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("rotateL")){
									if ( update && qualified(value)) {
										rotateL = true;
										gestureID = 11;
									}
								} else if(command.equalsIgnoreCase("rotateR")){
									if ( update && qualified(value)) {
										rotateR = true;
										gestureID = 12;
									}
								} else if(command.equalsIgnoreCase("nodL")){
									if ( update && qualified(value)) {
										nodLeft = true;
										gestureID = 13;
									}
								} else if(command.equalsIgnoreCase("nodR")){
									if ( update && qualified(value)) {
										nodRight = true;
										gestureID = 14;
									}
								} else if(command.equalsIgnoreCase("nodF")){
									if ( update && qualified(value)) {
										nodFront = true;
										gestureID = 15;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("nodLD")){
									if ( update && qualified(value)) {
										nodLeftD = true;
										gestureID = 16;
									}
								} else if(command.equalsIgnoreCase("nodRD")){
									if ( update && qualified(value)) {
										nodRightD = true;
										gestureID = 17;
									}
								} else if(command.equalsIgnoreCase("nodFD")){
									if ( update && qualified(value)) {
										nodFrontD = true;
										gestureID = 18;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("groove")){
									if ( update && qualified(value)) {
										groove = true;
										gestureID = 19;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("circle")){
									if ( update && qualified(value)) {
										circle = true;
										gestureID = 20;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("nodCircle")){
									if ( update && qualified(value)) {
										nodCircle = true;
										gestureID = 21;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("halfNod")){
									if ( update && qualified(value)) {
										halfNod = true;
										gestureID = 22;
									} else if (value.equals("1"))
										still = true;
								} else if(command.equalsIgnoreCase("nodAll")){
									if ( update ) {
										if (id == 1)
											nodLeft = true;	
										else if (id == 2)
											nodFront = true;
										else
											nodRight = true;
										gestureID = 23;
									} 
								} else if(command.equalsIgnoreCase("nodAllD")){
									if ( update ) {
										if (id == 1)
											nodLeftD = true;	
										else if (id == 2)
											nodFrontD = true;
										else
											nodRightD = true;
										gestureID = 24;
									} 
								} else if(command.equalsIgnoreCase("lineup")){
									if ( update) {
										lineup = true;
										gestureID = 25;
									}
								} else if(command.equalsIgnoreCase("SEQLlookUpRL")){
									if ( update) {
										SEQLlookUpRL = true;
										gestureID = 26;
									}
								} else if(command.equalsIgnoreCase("SEQPRlookUpRL")){
									if ( update) {
										SEQPRlookUpRL = true;
										gestureID = 27;
									}
								} else if(command.equalsIgnoreCase("SEQRneckUD")){
									if ( update) {
										SEQRneckUD = true;
										gestureID = 28;
									}
								} else if(command.equalsIgnoreCase("SEQPLneckUD")){
									if ( update) {
										SEQPLneckUD = true;
										gestureID = 29;
									}
								} else if(command.equalsIgnoreCase("SEQMlookUpD")){
									if ( update) {
										SEQMlookUpD = true;
										gestureID = 30;
									}
								} else if(command.equalsIgnoreCase("SEQPLrotateL")){
									if ( update) {
										SEQPLrotateL = true;
										gestureID = 31;
									}
								} else if(command.equalsIgnoreCase("SEQPRrotateR")){
									if ( update) {
										SEQPRrotateR = true;
										gestureID = 32;
									}
								} else if(command.equalsIgnoreCase("FEEDBACK")){
//									sender.sendPara(Integer.toString(gestureID));				
								} 
								else if(command.equalsIgnoreCase("WAKEUP")){
									runOnUiThread(new Runnable(){
										public void run(){
											wakeUp();
										}
									});							
								}
							}
							
							
							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						listener.closeConnection();
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				
				
				private boolean qualified(String mode) {
					if (mode.equals("1") && id == 2)
						return true;
					if (mode.equals("2") && (id == 1 || id == 3))
						return true;
					if (mode.equals("3"))
						return true;
					return false;
				}
				private void checkTimeOut() {
					if (preMessageTime > 0 && (System.currentTimeMillis() - preMessageTime) > 10000) {
						preMessageTime = 0;
						started = false;
						System.out.println("go back to sleep");
						runOnUiThread(new Runnable(){
							public void run(){
								goSleep();
							}
						});
					}
				} 
				
			}.start();
		}
		
}
