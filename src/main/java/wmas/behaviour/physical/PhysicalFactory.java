package wmas.behaviour.physical;

import java.awt.Component;

import wmas.behaviour.Behaviour;
import wmas.behaviour.BehaviourFactory;
import wmas.behaviour.graph.BehaviourGraphFactory;
import wmas.expression.functions.FunctionList;
import wmas.gui.GuiModificationListener;
import wmas.gui.behaviour.physical.DropLiftBehaviourEditor;
import wmas.gui.behaviour.physical.MoveBehaviourEditor;
import wmas.gui.behaviour.physical.SpeedAttributeEditor;
import wmas.gui.run.EntityAttributeView;
import wmas.gui.world.entity.EntityAttributeFactory;
import wmas.gui.world.entity.EntityEditor;
import wmas.reports.ReportDescription;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class PhysicalFactory implements BehaviourFactory,
		EntityAttributeFactory {

	private SpeedAttributeEditor speedEditor = null;
	private MoveBehaviourEditor moveEditor = null;
	private DropLiftBehaviourEditor dropLiftEditor = null;

	@Override
	public Behaviour getBehaviour(int elem) {
		switch (elem) {
		case 0:
			return new MoveBehaviour();
		case 1:
			return new LiftBehaviour();
		case 2:
			return new DropBehaviour();
		}
		return null;
	}

	@Override
	public String getBehaviourName(int elem) {
		switch (elem) {
		case 0:
			return "Move";
		case 1:
			return "Lift";
		case 2:
			return "Drop";
		}
		return null;
	}

	@Override
	public Component getEditor(BehaviourGraphFactory parent, Behaviour behaviour) {
		if (behaviour instanceof MoveBehaviour) {
			if (moveEditor == null)
				moveEditor = new MoveBehaviourEditor((MoveBehaviour) behaviour,
						parent, null /* TODO */);
			else
				moveEditor
						.setBehaviour((MoveBehaviour) behaviour, parent, null /* TODO */);
			return moveEditor;
		} else if (behaviour instanceof DropBehaviour) {
			if (dropLiftEditor == null)
				dropLiftEditor = new DropLiftBehaviourEditor(
						(DropBehaviour) behaviour, parent);
			else
				dropLiftEditor.setOwner((DropBehaviour) behaviour, parent);
			return dropLiftEditor;
		} else if (behaviour instanceof LiftBehaviour) {
			if (dropLiftEditor == null)
				dropLiftEditor = new DropLiftBehaviourEditor(
						(LiftBehaviour) behaviour, parent);
			else
				dropLiftEditor.setOwner((LiftBehaviour) behaviour, parent);
			return dropLiftEditor;
		}
		return null;
	}

	@Override
	public int getNbBehaviour() {
		return 3;
	}

	@Override
	public XMLEntity createAttribute(int id) {
		if (id == 2) {
			return new SpeedAttribute();
		}
		return null;
	}

	@Override
	public Component getAttributeEditor(XMLEntity parent, int id, XMLEntity e,
			GuiModificationListener listener) {
		if (e instanceof SpeedAttribute) {
			if (speedEditor == null) {
				speedEditor = new SpeedAttributeEditor((SpeedAttribute) e,
						listener);
			} else
				speedEditor.setAttribute((SpeedAttribute) e, listener);
			return speedEditor;
		}
		return null;
	}

	@Override
	public String getAttributeName(int id) {
		switch (id) {
		case 0:
			return "collide";
		case 1:
			return "collision";
		case 2:
			return "speed";
		case 3:
			return "liftable";
		case 4:
			return "carrier";
		}
		return null;
	}

	@Override
	public int getNbAttribute() {
		return 5;
	}

	public static void registerAll() {
		ReportDescription.registerDataReport("color", true);
		ReportDescription.registerEventReport("collision");

		PhysicalFactory f = new PhysicalFactory();
		BehaviourGraphFactory.register(f);
		EntityEditor.registerAttributeFactory(f);
		FunctionList
				.register(
						new DoCarryFunction(),
						"Returns true if the entity carries an object. If an argument is provided, the function will returns true only if the entity carries an entity with a name prefixed by the argument.");
		FunctionList
				.register(
						new CanLiftFunction(),
						"Returns true if an object can be lifted by the entity. If an argument is provided, the function will returns true only if the entity can lift an object with a name prefixed by the argument.");
		FunctionList
				.register(
						new CollisionFunction(),
						"Returns true if the entity has collides in the last time step. If an argument is provided, the function will returns true only if the object that has collide has a name prefixed by the argument.");
		World.registerStaticAttribute("collision",
				new PhysicalCollisionAttribute());
		World.registerStaticAttribute("lifting", new LiftedAttribute());
	}

	@Override
	public EntityAttributeView getAttributeViewer(int id) {
		return null;
	}

	@Override
	public String getBehaviourDescription(int elem) {
		switch (elem) {
		case 0:
			return "Move the entity";
		case 1:
			return "Lift an object";
		case 2:
			return "Drop a carried object";
		}
		return null;
	}

	@Override
	public String getAttributeDescription(int id) {
		switch (id) {
		case 0:
			return "The entity do collides. If this attribute is set, then collisions will be triggered and movement will be stopped upon encounter of an entity with the collision attribute.";
		case 1:
			return "The entity generates collisions. If this attribute is set, then an object with the collide attribute encountering this entity will have collision.";
		case 2:
			return "The entity can move. It has several maximum speeds depending on the coloring of the movement. If a coloring is not specified, the default speed will be used.";
		case 3:
			return "The entity can be lifted.";
		case 4:
			return "The entity can carry liftable entities";
		}
		return null;
	}
}
