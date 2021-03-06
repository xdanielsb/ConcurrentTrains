package logic;

import java.util.NoSuchElementException;
import model.Direction;
import model.ElementRail;
import model.Station;

/**
 * Monitor of the trains and the Elements of the rail {Section, Station} let synchronize the rail of
 * trains.
 */
public class Line {

  /**
   * The elements composing the line. The first and last elements are stations, the others are
   * sections.
   */
  private ElementRail[] elements;

  /** The current Direction in which every train on the line has to follow. */
  private Direction currentDirection;

  /** The number of trains traveling in the line, to allow for a change of direction. */
  private int numberOfTrainsInTraject;

  /** The id of the line, used to display messages. */
  private int id;

  /**
   * Constructor for the line
   *
   * @param _id : the id of the line, used to display messages.
   */
  public Line(int _id) {
    id = _id;
    numberOfTrainsInTraject = 0;
  }

  /** @return number of trains that are using that line */
  public synchronized int getNumberOfTrainsInTraject() {
    return numberOfTrainsInTraject;
  }

  /**
   * Method that changes the elements of the line, replacing the current ones by the list of
   * elements given in argument. This should be used once during the initialization only.
   *
   * @param els: List of {Station and Sections} in which the first one and last one are stations
   *     {Station1, Section1,...SectionN, Station2}
   */
  public void addLine(ElementRail... els) {
    elements = els;
    for (int i = 1; i < els.length; i++) {
      int nx = elements[i - 1].getCord().getX();
      elements[i].getCord().setX(nx + 50);
    }
  }

  /**
   * Method used by trains to go into their next location. It is synchronized to avoid overcharging
   * the target location and to be sure to keep counts
   *
   * @param pos : the target of the train
   */
  public synchronized void advance(ElementRail pos) {
    pos.arrive();
  }

  /**
   * This method is used by the train to find the next ElementRail in which it needs to go. It is
   * simply an indicator and does not change the resource.
   *
   * @param currentPos : the current element in which the train is located
   * @param dir direction : the Direction of the train (LR or RL).
   * @return the element that is going to be the next train location
   */
  public ElementRail getNext(ElementRail currentPos, Direction dir) {
    ElementRail res = null;
    for (int i = 0; i < elements.length && res == null; i++)
      if (elements[i] == currentPos) {
        res = (dir == Direction.LR) ? elements[i + 1] : elements[i - 1];
      }
    if (res == null) throw new NoSuchElementException("There was not found a valid pos");
    return res;
  }

  /**
   * Gives the current direction of the line, which has to be followed by every train in the line.
   *
   * @return the direction
   */
  public Direction getCurrentDirection() {
    return currentDirection;
  }

  /**
   * Changes the direction of the line. Happens when a train needs to go in a direction that differs
   * from the line current direction. Is synchronized to avoid to trains changing the direction at
   * the same time.
   *
   * @param currentDirection : the current direction of the train.
   */
  public synchronized void setCurrentDirection(Direction currentDirection) {
    System.out.println(this + " has change of direction,  new direction =" + currentDirection);
    this.currentDirection = currentDirection;
    notifyAll();
  }

  /** Method called by trains when one starts moving in the line to keep count */
  public synchronized void incrementTrainsInTraject() {
    numberOfTrainsInTraject++;
  }

  /** Method called by trains when one stops moving in the line to keep count */
  public synchronized void decrementTrainsInTraject() {
    numberOfTrainsInTraject--;
    notifyAll();
  }

  /**
   * Method called by trains, force them to wait if other trains are traveling in the opposite
   * direction
   *
   * @param direction : the current train direction
   */
  public synchronized void isTheSameDirection(Direction direction) {
    while (numberOfTrainsInTraject > 0 && direction != currentDirection) {
      try {
        wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Getter for elements in the line.
   *
   * @return the elements table.
   */
  public ElementRail[] getElements() {
    return elements;
  }

  /**
   * Gives the number of elements in the line.
   *
   * @return the length of the elements table.
   */
  public int getNumElementsRail() {
    return elements.length;
  }

  /**
   * Returns the first element of the line, which has to be a station.
   *
   * @return this station.
   */
  public Station getStart() {
    return (Station) elements[0];
  }

  /**
   * Returns the last element of the line, which has to be a station.
   *
   * @return this station.
   */
  public Station getEnd() {
    return (Station) elements[elements.length - 1];
  }

  @Override
  public String toString() {
    return "Line " + id;
  }

  /**
   * Method called by trains to find the next station they are going to, which is the opposite end
   * of the line
   *
   * @param currentPos : the station in which the train currently is
   * @return the other station in the line
   */
  public Station nextStation(ElementRail currentPos) {
    if (currentPos == this.getStart()) {
      return this.getEnd();
    } else if (currentPos == this.getEnd()) {
      return this.getStart();
    } else {
      throw new IllegalStateException(
          "The train is not at a station but is searching for a new one.");
    }
  }
}
