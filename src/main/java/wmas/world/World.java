package wmas.world;

import java.io.File;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import wmas.behaviour.Behaviour;
import wmas.expression.Variables;
import wmas.gui.shapes.DrawableShape;
import wmas.gui.shapes.SchemeView;
import wmas.xml.Copiable;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class World implements XMLEntity {

	private static Map<String, WorldBehaviour> globalBehaviours = new HashMap<String, WorldBehaviour>();
	private Map<String, WorldBehaviour> rGlobalBehaviours = new HashMap<String, WorldBehaviour>();

	private static Map<String, Copiable> staticAttributes = new HashMap<String, Copiable>();
	private Map<String, Copiable> rStaticAttributes = new HashMap<String, Copiable>();

	public static void registerStaticAttribute(String name, Copiable o) {
		staticAttributes.put(name, o);
	}

	public static void unregisterStaticAttribute(String name) {
		staticAttributes.remove(name);
	}

	public static void registerGlobalBehaviour(String name, WorldBehaviour o) {
		globalBehaviours.put(name, o);
	}

	public static void unregisterGlobalBehaviour(String name) {
		globalBehaviours.remove(name);
	}

	private Map<String, XMLEntity> attributes = new HashMap<String, XMLEntity>();
	private Map<String, XMLEntity> attributesCopy = null;

	private List<EntityInterface> entities = new LinkedList<EntityInterface>();
	private List<EntityInterface> activeEntities = new LinkedList<EntityInterface>();
	private boolean activated = false;

	private Map<String, Object> models = new HashMap<String, Object>(); // just
																		// used
																		// by
																		// the
																		// tree
																		// structure
																		// for
																		// editing
	private Map<String, Simulator> simulators = new HashMap<String, Simulator>(); // Available
																					// simulation
																					// runs

	private Simulator simulator = null;
	private List<DrawableShape> runningTopShapes = new LinkedList<DrawableShape>();
	private List<DrawableShape> runningBottomShapes = new LinkedList<DrawableShape>();
	private UpdateManager display = null;

	// Search facilities
	private HashMap<String, Set<EntityInterface>> searchableEntities = new HashMap<String, Set<EntityInterface>>();

	class BehaviourCollection extends AbstractCollection<Behaviour> {
		private LinkedList<Behaviour> list = new LinkedList<Behaviour>();

		private void constructList() {
			list.clear();
			for (Object o : models.values()) {
				if (o != null) {
					if (o instanceof Behaviour)
						list.add((Behaviour) o);
					else if (o instanceof EntityInterface) {
						list.addAll(((EntityInterface) o).getBehaviours());
					} else if (o instanceof Model) {
						list.addAll(((Model) o).getAvailableBehaviours());
					}
				}
			}
		}

		@Override
		public Iterator<Behaviour> iterator() {
			constructList();
			return list.iterator();
		}

		@Override
		public int size() {
			int r = 0;
			for (Object o : models.values()) {
				if (o != null) {
					if (o instanceof Behaviour)
						r++;
					else if (o instanceof EntityInterface) {
						r += ((EntityInterface) o).getBehaviours().size();
					} else if (o instanceof Model) {
						r += ((Model) o).getAvailableBehaviours().size();
					}
				}
			}
			return r;
		}
	}

	public void addAttribute(String name, XMLEntity attribute) {
		this.attributes.put(name, attribute);
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public boolean hasAttribute(String name) {
		return this.attributes.containsKey(name);
	}

	public XMLEntity getAttribute(String name) {
		return this.attributes.get(name);
	}

	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		for (int i = 0; i < e.getChildNodes().getLength(); i++) {
			Node n = e.getChildNodes().item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (el.getTagName().equals("attribute")) {
					String attrName = el.getAttribute("name");
					XMLEntity c = null;
					for (int j = 0; j < el.getChildNodes().getLength()
							&& c == null; j++) {
						Node n1 = el.getChildNodes().item(i);
						if (n1.getNodeType() == Node.ELEMENT_NODE) {
							c = XMLInterpretor.convert((Element) n1, refs);
						}
					}
					attributes.put(attrName, c);
				} else if (el.getTagName().equals("model")) {
					String name = el.getAttribute("path");
					models.put(name, null); // We don't load on opening the
											// model
				} else {
					XMLEntity o = XMLInterpretor.convert(el, refs);
					if (o != null) {
						if (o instanceof EntityInterface) {
							entities.add((EntityInterface) o);
							((EntityInterface) o).setParent(this);
						} else if (o instanceof Simulator) {
							simulators.put(el.getAttribute("name"),
									(Simulator) o);
						}
					}
				}
			}
		}
	}

	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		if (refs == null)
			refs = new XMLCrossRef();
		Element e = root.createElement(this.getClass().getName());
		for (Map.Entry<String, XMLEntity> me : attributes.entrySet()) {
			Element attr = root.createElement("attribute");
			attr.setAttribute("name", me.getKey());
			if (me.getValue() != null)
				attr.appendChild(me.getValue().toXML(root, refs));
			e.appendChild(attr);
		}
		for (EntityInterface ent : entities) {
			e.appendChild(XMLInterpretor.makeCrossReferencedElement(root, ent,
					refs));
		}
		for (String s : models.keySet()) {
			Element attr = root.createElement("model");
			attr.setAttribute("path", s);
			e.appendChild(attr);
		}
		for (Map.Entry<String, Simulator> m : simulators.entrySet()) {
			Element el = m.getValue().toXML(root, refs);
			el.setAttribute("name", m.getKey());
			e.appendChild(el);
		}
		return e;
	}

	public XMLEntity copy() {
		World r = new World();
		for (EntityInterface e : entities) {
			r.entities.add((EntityInterface) e.copy());
			r.entities.get(r.entities.size() - 1).setParent(this);
		}
		for (Map.Entry<String, XMLEntity> attr : attributes.entrySet())
			r.attributes.put(attr.getKey(), (XMLEntity) attr.getValue().copy());
		for (Map.Entry<String, Object> m : models.entrySet())
			r.models.put(m.getKey(), m.getValue());
		for (Map.Entry<String, Simulator> m : simulators.entrySet()) {
			r.simulators.put(m.getKey(), m.getValue().copy(r));
		}
		return r;
	}

	public void reset() {
		Variables varSet = new Variables();
		if (attributesCopy == null) {
			attributesCopy = new HashMap<String, XMLEntity>();
			for (Map.Entry<String, XMLEntity> xe : attributes.entrySet()) {
				attributesCopy.put(xe.getKey(),
						xe.getValue() != null ? (XMLEntity) xe.getValue()
								.copy() : null);
			}
		} else {
			attributes.clear();
			for (Map.Entry<String, XMLEntity> xe : attributesCopy.entrySet()) {
				attributes.put(xe.getKey(),
						xe.getValue() != null ? (XMLEntity) xe.getValue()
								.copy() : null);
			}
		}
		activeEntities.clear();
		for (EntityInterface e : entities) {
			e.reset();
			varSet.clear();
			if (e.getEnabledExpression().verified(e, varSet)) {
				activeEntities.add(e);
			}
		}
		activated = true;
	}

	private SchemeView lastWorldView = null;
	private HashMap<DrawableShape, EntityInterface> shapeMap = new HashMap<DrawableShape, EntityInterface>();

	public EntityInterface getEntityByShape(DrawableShape shape) {
		return shapeMap.get(shape);
	}

	public void transfer() {
		transfer(true);
	}

	private void transfer(boolean force) {
		if (force) {
			lastWorldView = null;
		}
		if (this.display != null) {
			if (lastWorldView != this.display.getWorldView()) {
				lastWorldView = this.display.getWorldView();
				if (lastWorldView != null) {
					lastWorldView.removeAll();
					shapeMap.clear();
					for (DrawableShape s : runningBottomShapes) {
						lastWorldView.addTop(s);
					}
					for (EntityInterface e : activeEntities) {
						if (e.getDrawingShape() != null) {
							lastWorldView.add(e.getDrawingShape());
							shapeMap.put(e.getDrawingShape(), e);
						}
					}
					for (DrawableShape s : runningTopShapes) {
						lastWorldView.add(s);
					}
				}
			}
		}

	}

	private double lastTime;
	private Set<EntityInterface> stepFinished = new HashSet<EntityInterface>();

	public void init(Simulator simu, boolean stepExecution) {
		lastTime = -1;
		stepFinished.clear();
		searchableEntities.clear();
		runningTopShapes.clear();
		runningBottomShapes.clear();
		this.simulator = simu;
		attributeEntities.clear();
		rStaticAttributes.clear();
		for (Map.Entry<String, Copiable> me : staticAttributes.entrySet()) {
			rStaticAttributes.put(me.getKey(), me.getValue().copy());
		}
		if (rGlobalBehaviours.size() != globalBehaviours.size()) {
			rGlobalBehaviours.clear();
			for (String s : globalBehaviours.keySet()) {
				rGlobalBehaviours.put(s, globalBehaviours.get(s).copy());
			}
		}
		for (EntityInterface e : activeEntities) {
			e.setWorld(this);
		}
		transfer();
		for (WorldBehaviour b : rGlobalBehaviours.values()) {
			b.setWorld(this);
			b.init();
		}
		for (EntityInterface e : activeEntities) {
			if (stepExecution)
				e.initStep();
			else
				e.init();
		}
	}

	public UpdateManager getDisplay() {
		return this.display;
	}

	public void setDisplay(UpdateManager display) {
		this.display = display;
		transfer();
	}

	public void addShape(DrawableShape s, boolean top) {
		if (s == null)
			return;
		if (runningBottomShapes.contains(s)) {
			if (!top)
				return;
			if (lastWorldView != null) {
				lastWorldView.remove(s);
			}
			runningBottomShapes.remove(s);
		}
		if (runningTopShapes.contains(s)) {
			if (top)
				return;
			if (lastWorldView != null) {
				lastWorldView.remove(s);
			}
			runningTopShapes.remove(s);
		}
		if (top) {
			runningTopShapes.add(s);
			if (lastWorldView != null) {
				lastWorldView.add(s);
			}
		} else {
			runningBottomShapes.add(s);
			if (lastWorldView != null) {
				lastWorldView.addTop(s);
			}
		}
	}

	public void removeShape(DrawableShape s) {
		if (s == null)
			return;
		runningBottomShapes.remove(s);
		runningTopShapes.remove(s);
		if (lastWorldView != null) {
			lastWorldView.remove(s);
		}
	}

	public double getTime() {
		return simulator == null ? 0 : simulator.getInternalTime();
	}

	public void update(double t) {
		for (WorldBehaviour b : rGlobalBehaviours.values()) {
			b.update(t);
		}
		for (EntityInterface e : activeEntities) {
			e.update(t);
		}
		transfer(false);
	}

	public boolean updateStep(double t) {
		if (t != lastTime) {
			for (WorldBehaviour b : rGlobalBehaviours.values()) {
				b.update(t);
			}
			lastTime = t;
			stepFinished.clear();
		}
		for (EntityInterface e : activeEntities) {
			if (!stepFinished.contains(e)) {
				if (e.updateStep(t))
					stepFinished.add(e);
			}
		}
		transfer(false);
		return activeEntities.size() == stepFinished.size();
	}

	public boolean terminated() {
		for (EntityInterface b : activeEntities) {
			if (!b.terminated()) {
				return false;
			}
		}
		return true;
	}

	public List<EntityInterface> listEntities() {
		return activated ? activeEntities : entities;
	}

	public List<EntityInterface> listActiveEntities() {
		return activeEntities;
	}

	public Map<String, Object> getModels() {
		return models;
	}

	// Just a property for the tree structure
	private File worldDirectory = null;

	public File getWorldDirectory() {
		return worldDirectory;
	}

	public void setWorldDirectory(File worldDirectory) {
		this.worldDirectory = worldDirectory;
	}

	private BehaviourCollection collect = null;

	public Collection<Behaviour> getAvailableBehaviours() {
		if (collect == null)
			collect = new BehaviourCollection();
		return collect;
	}

	private HashMap<String, Iterable<EntityInterface>> attributeEntities = new HashMap<String, Iterable<EntityInterface>>();

	public Iterable<EntityInterface> entitiesWithAttribute(String string) {
		if (!attributeEntities.containsKey(string)) {
			LinkedList<EntityInterface> entities = new LinkedList<EntityInterface>();
			for (EntityInterface e : this.activeEntities) {
				if (e.hasAttribute(string)) {
					entities.add(e);
				}
			}
			attributeEntities.put(string, entities);
		}
		return attributeEntities.get(string);
	}

	public boolean hasStaticAttribute(String string) {
		return rStaticAttributes.containsKey(string);
	}

	public Copiable getStaticAttribute(String string) {
		return rStaticAttributes.get(string);
	}

	private void constructSearchableEntity() {
		if (searchableEntities.isEmpty()) {
			for (EntityInterface e : activeEntities) {
				if (!searchableEntities.containsKey(e.getName()))
					searchableEntities.put(e.getName(),
							new HashSet<EntityInterface>());
				searchableEntities.get(e.getName()).add(e);
			}
		}
	}

	public Collection<EntityInterface> getEntitiesByName(String name) {
		constructSearchableEntity();
		return searchableEntities.get(name);
	}

	public Collection<EntityInterface> getEntitiesByPrefix(String prefix) {
		if (prefix == null || prefix.isEmpty())
			return activeEntities;
		constructSearchableEntity();
		Set<String> keys = searchableEntities.keySet();
		Set<EntityInterface> result = new HashSet<EntityInterface>();
		for (String s : keys) {
			if (s != null && s.startsWith(prefix))
				result.addAll(searchableEntities.get(s));
		}
		return result;
	}

	public WorldBehaviour getWorldBehaviour(String string) {
		return rGlobalBehaviours.get(string);
	}

	public Collection<String> getAttributes() {
		return attributes.keySet();
	}

	public void addReport(double t, String name, String param, double value) {
		if (simulator != null) {
			if (simulator.getReporter() != null) {
				simulator.getReporter().addReport(t, name, param, value);
			}
		}
	}

	public void addEvent(double t, String name, String param,
			EntityInterface entity) {
		if (simulator != null) {
			if (simulator.getReporter() != null) {
				simulator.getReporter().addEvent(t, name, param, entity);
			}
		}
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public Simulator getSimulator(String s) {
		return simulators.get(s);
	}

	public Set<String> listSimulators() {
		return simulators.keySet();
	}

	public void removeSimulator(String s) {
		simulators.remove(s);
	}

	public void setSimulator(String s, Simulator simu) {
		simulators.put(s, simu);
	}
}
