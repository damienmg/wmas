package wmas.behaviour;

import java.awt.Component;

import wmas.behaviour.graph.BehaviourGraphFactory;

public interface BehaviourFactory {
	// Behaviour factory
	public int getNbBehaviour();

	public String getBehaviourName(int elem);

	public Behaviour getBehaviour(int elem);

	// Editing panel
	public Component getEditor(BehaviourGraphFactory parent, Behaviour behaviour);

	public String getBehaviourDescription(int elem);
}
