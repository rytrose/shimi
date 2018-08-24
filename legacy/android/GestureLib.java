package com.example.annie_travislistening;

import mr1.robots.travis.moves.GestureController;
import mr1.robots.travis.moves.ThreadScheduler;

public class GestureLib implements ThreadScheduler{
	
	private GestureController gestureController;
	private int id;
	private boolean goDown = true;
	private boolean left = true;
	private int t = 2;
	
	
	public GestureLib(GestureController gc, int id) {
		this.gestureController = gc;
		this.id = id;
	}
	
	public void neckUD() {
		if (goDown) {
			gestureController.gesture.neckUDMove(.4f, 1.0f);
			goDown = false;
		}
		else {
			gestureController.gesture.neckUDMove(-.6f, 1.0f);
			goDown = true;
		}
		gestureController.gesture.tap(1.0f);
	}
	
	public void lookUpRL(int counter) {
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
	}
	
	public void lookUpRLD(int counter) {
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
	}
	
	public void diagonal() {
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
	}
	
	public void shake(int counter) {
		gestureController.gesture.shakeHead(.5f);
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
	}
	
	public void swoopRL(int counter) {
		if (left) {
			gestureController.gesture.swoopLeft(.5f);
			left = false;
		}
		else {
			if (counter == 7) 
				gestureController.gesture.home(.5f);
			else
				gestureController.gesture.swoopRight(.5f);
			gestureController.gesture.tap(1.0f);
			left = true;
		}
	}
	
	public void seqLeft(int counter) {
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
	}
	
	public void seqRight(int counter) {
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
	}
	
	public void seqRL(int counter) {
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
	}
	
	public void rotateL() {
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
	
	public void rotateR() {
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
	
	public void rotate() {
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
	}
	
	public void nodLeft(int counter) {
		gestureController.gesture.home();
		if (goDown) {
			gestureController.gesture.headMove(-.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%2 == 0) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 1.0f);
				gestureController.gesture.neckRLMove(.2f, 1.0f);
				left = false;
			} else {
				gestureController.gesture.neckUDMove(.6f, 1.0f);
				gestureController.gesture.neckRLMove(-.2f, 1.0f);
				left = true;
			}
			
		}
	}
	
	public void nodFront(int counter) {
		gestureController.gesture.home();	
		if (goDown) {
			gestureController.gesture.headMove(-.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%2 == 0) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 1.0f);
				left = false;
			} else {
				gestureController.gesture.neckUDMove(.6f, 1.0f);
				left = true;
			}
		}
	}
	
	public void nodRight(int counter) {
		gestureController.gesture.home();
		if (goDown) {
			gestureController.gesture.headMove(-.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%2 == 0) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 1.0f);
				gestureController.gesture.neckRLMove(-.2f, 1.0f);
				left = false;
			} else {
				gestureController.gesture.neckUDMove(.6f, 1.0f);
				gestureController.gesture.neckRLMove(.2f, 1.0f);
				left = true;
			}
		}
	}
	
	public void nodLeftD(int counter) {
		gestureController.gesture.home();
		if (goDown) {
			gestureController.gesture.headMove(-.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%4 == 0) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 2.0f);
				gestureController.gesture.neckRLMove(.2f, 2.0f);
				left = false;
			} else {
				gestureController.gesture.neckUDMove(.6f, 2.0f);
				gestureController.gesture.neckRLMove(-.2f, 2.0f);
				left = true;
			}
		}
	}
	
	public void nodFrontD(int counter) {
		gestureController.gesture.home();
		if (goDown) {
			gestureController.gesture.headMove(-.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%4 == 0) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 2.0f);
				left = false;
			} else {
				gestureController.gesture.neckUDMove(.6f, 2.0f);
				left = true;
			}
		}
	}
	
	public void nodRightD(int counter) {
		gestureController.gesture.home();
		if (goDown) {
			gestureController.gesture.headMove(-.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%4 == 0) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 2.0f);
				gestureController.gesture.neckRLMove(-.2f, 2.0f);
				left = false;
			} else {
				gestureController.gesture.neckUDMove(.6f, 2.0f);
				gestureController.gesture.neckRLMove(.2f, 2.0f);
				left = true;
			}
		}
	}
	
	public void groove(int counter) {
		gestureController.gesture.home();
		if (goDown) {
			if (left) {
				gestureController.gesture.neckUDMove(0f, 1.0f);
				if ((counter/4)%2 == 0)
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
			}
		}
	}
	
	public void circle(int counter) {
		if (counter%4 == 0) 
			gestureController.gesture.lookDownLeft(1.0f);
		else if (counter%4 == 1)
			gestureController.gesture.neckRight(1.0f);
		else if (counter%4 == 2) {
			gestureController.gesture.neckUDMove(.6f, 1.0f);
			gestureController.gesture.headMove(0f, 1.0f);
		} else
			gestureController.gesture.neckLeft(1.0f);
	}
	
	public void nodCircle(int counter) {
		gestureController.gesture.home();	
		if (goDown) {
			gestureController.gesture.headMove(.5f, .5f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(-.5f, .5f);
			goDown = true;
		}
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
	}
	
	public void lineup(int counter) {
		if (goDown) {
			gestureController.gesture.headMove(.5f, .4f);
			goDown = false;
		} else {
			gestureController.gesture.headMove(-.5f, .5f);
			gestureController.gesture.tap(1.0f);
			goDown = true;
		}
		if (counter%8 == 0) {
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
		}
	}
	
	public void halfNod(int counter) {
		gestureController.gesture.home();
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
	}
	
}
