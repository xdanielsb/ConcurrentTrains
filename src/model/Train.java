package model;

import logic.ControlRailway;
import logic.Line;

public class Train extends Thread {
	
	/**
	 * Name of the train
	 */
	private String name;
	/**
	 * Current pos of the train
	 */
	private ElementRail currentPos;
	/**
	 * Initial origin train
	 */
	private Station origin;
	/**
	 * Destiny of the current train
	 */
	private Station destiny;
	/**
	 * Direction of the current train
	 */
	private Direction direction;
	/**
	 * Monitor, that let synchronize with
	 * other trains in the railway.
	 */
	private Line currentLine;
	private Coordinate cord;
	
	private ControlRailway ctrl;
	
	
	
	public Train(String _name, Line line,ControlRailway control) {
		name = _name;
		currentLine = line;
		ctrl = control;
		cord = new Coordinate();
	}

	/**
	 * Allows a train advance in the railway.
	 * 
	 */
	public void advance() { //omg
		ElementRail lst = currentPos;

		if (currentPos == getOrigin() && direction == Direction.RL ||
			currentPos == getDestiny() && direction == Direction.LR) {
			System.out.println("Change of line of the Train: " + this);
			currentLine.decrementTrainsInTraject();
			currentLine = ctrl.getNextLine( currentLine, direction);
		}
		if (currentPos instanceof Station) {
			Station st;	
			st = currentLine.nextStation(currentPos);
			st.isPossibleGo();
			st.incrementNumberComingTrain();
		}
		ElementRail nxt = currentLine.getNext( currentPos, direction);
		currentLine.isTheSameDirection( direction);
		if( currentPos instanceof Station) {
			if( currentLine.getNumberOfTrainsInTraject() == 0) {
				// this means there is no other train
				// using the line
				
				currentLine.incrementTrainsInTraject();
				
				currentLine.setCurrentDirection(direction);
			}else {
				currentLine.incrementTrainsInTraject();
			}
		}
		nxt.arrive();

		currentPos = nxt;
		getCord().setX(nxt.getCord().getX());
		// just leaves when is possible arrive
		lst.leave();
	}

	/**
	 * Set the target station of a train
	 * @param tgt
	 */
	public void setDestiny(Station tgt) {
		destiny = tgt;
	}
	
	/**
	 * Set the direction of a train
	 * @param dir
	 */
	public void setDirection(Direction dir) {
		direction = dir;
	}
	
	public Station getOrigin() {
		return origin;
	}

	public Station getDestiny() {
		return destiny;
	}

	/**
	 * Set the traject of a train
	 * @param src source of the traject
	 * @param _destiny destiny of the traject
	 */
	public void addTraject(Station src, Station _destiny) {
		currentPos = src;
		origin = src;
		destiny = _destiny;
		//find the direction in which the train
		//have to go
		direction = ctrl.getIndex(src) < ctrl.getIndex(_destiny)?
			        Direction.LR : Direction.RL;
		cord.setX(src.getCord().getX());
		System.out.println(src +" to "+ _destiny + " dir= " + direction);
	}
		
	public Coordinate getCord() {
		return cord;
	}
	
	public void setOrigin(Station origin) {
		origin.incrementNumberCurrentTrain();
		this.origin = origin;
	}

	public void setCord(Coordinate cord) {
		this.cord = cord;
	}

	@Override
	public String toString() {
		return this.name;
	}
	
	public Direction getDirection() {
		return this.direction;
	}
	
	@Override
	public void run() {
		while( currentPos != destiny) {
			System.out.println("Current pos  "+this.name+ " = "+currentPos );
			advance();
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ctrl.getWindow().repaint();
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~"+this.name +" Arrives ");
		currentLine.decrementTrainsInTraject();
		
	}

	public Line getCurrentLine() {
		return currentLine;
	}
	
	
}
