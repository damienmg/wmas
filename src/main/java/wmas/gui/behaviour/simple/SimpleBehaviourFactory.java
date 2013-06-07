package wmas.gui.behaviour.simple;

import java.awt.Component;

import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourFactory;
import wmas.behaviour.graph.BehaviourGraph;
import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.behaviour.simple.ExecuteBehaviour;
import wmas.behaviour.simple.NopBehaviour;
import wmas.behaviour.simple.WaitBehaviour;

public class SimpleBehaviourFactory implements BehaviourFactory {

	private WaitBehaviourEditor waitEditor = null;
	private ExecuteBehaviourEditor execEditor = null;
	private BehaviourGraphEditor graphEditor = null;

	public int getNbBehaviour() {
		return 4;
	}

	public Behaviour getBehaviour(int elem) {
		switch (elem) {
		case 0:
			return new BehaviourGraph();
		case 1:
			return new WaitBehaviour();
		case 2:
			return new NopBehaviour();
		case 3:
			return new ExecuteBehaviour();
		}
		return null;
	}

	public String getBehaviourName(int elem) {
		switch (elem) {
		case 0:
			return "Graph";
		case 1:
			return "Wait";
		case 2:
			return "Nop";
		case 3:
			return "Execute";
		}
		return "";
	}

	public Component getEditor(BehaviourGraphFactory parent, Behaviour behaviour) {
		if (behaviour instanceof WaitBehaviour) {
			if (waitEditor == null)
				waitEditor = new WaitBehaviourEditor(parent,
						(WaitBehaviour) behaviour);
			else
				waitEditor.setOwner(parent, (WaitBehaviour) behaviour);
			return waitEditor;
		} else if (behaviour instanceof ExecuteBehaviour) {
			if (execEditor == null)
				execEditor = new ExecuteBehaviourEditor(parent,
						(ExecuteBehaviour) behaviour);
			else
				execEditor.setOwner(parent, (ExecuteBehaviour) behaviour);
			return execEditor;
		} else if (behaviour instanceof BehaviourGraph) {
			if (graphEditor == null)
				graphEditor = new BehaviourGraphEditor(parent,
						(BehaviourGraph) behaviour);
			else
				graphEditor.setOwner(parent, (BehaviourGraph) behaviour);
			return graphEditor;
		}
		return null;
	}

	@Override
	public String getBehaviourDescription(int elem) {
		switch (elem) {
		case 0:
			return "Complex behaviour represented by a graph";
		case 1:
			return "Wait a given number of seconds";
		case 2:
			return "Do nothing";
		case 3:
			return "Execute the behaviour stored in the given expression";
		}
		return null;
	}
}
