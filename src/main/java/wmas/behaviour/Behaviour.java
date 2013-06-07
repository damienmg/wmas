package wmas.behaviour;

import java.awt.Color;

import wmas.expression.Variables;
import wmas.world.EntityInterface;
import wmas.xml.XMLEntity;

public interface Behaviour extends XMLEntity {

	/**
	 * Initialize the process
	 * 
	 * @param object
	 *            Associated object for this task (can be null)
	 * @param t
	 *            Starting time of the task
	 */
	public void init(EntityInterface object, Variables varSet, double t);

	/**
	 * Update the process during the execution
	 * 
	 * @param t
	 *            Current time
	 */
	public void update(double t);

	/**
	 * Step by step execution
	 * 
	 * @param t
	 *            Current time
	 * @returns true on step execution finished for instant t
	 */
	public boolean updateStep(double t);

	/**
	 * Initialize the process - step by step execution
	 * 
	 * @param object
	 *            Associated object for this task (can be null)
	 * @param t
	 *            Starting time of the task
	 */
	public void initStep(EntityInterface object, Variables varSet, double t);

	/**
	 * Suspend the process
	 * 
	 * @param t
	 *            Current time
	 */
	public void suspend(double t);

	/**
	 * Resume the process after a suspension
	 * 
	 * @param t
	 *            Current time
	 */
	public void unsuspend(double t);

	/**
	 * @return true if the process has terminated
	 */
	public boolean terminated();

	/**
	 * @return a copy of this behaviour
	 */
	public Behaviour copy();

	/**
	 * Colorized this behaviour with selected color when running
	 */
	public void colorize(Color c);

	/**
	 * Reset the behaviour
	 */
	public void reset();

	/**
	 * Terminates the behaviour
	 */
	public void terminate();

	/**
	 * Returns a data representing the behaviour
	 */
	public BehaviourData getRepresentation();
}
