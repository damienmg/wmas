package wmas.world;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import wmas.behaviour.Behaviour;
import wmas.expression.Expression;
import wmas.geometry.Poly;
import wmas.gui.shapes.DrawableShape;
import wmas.xml.XMLEntity;

public interface EntityInterface extends XMLEntity {

	// Attributes
	public void addAttribute(String name, XMLEntity attribute);

	public void removeAttribute(String name);

	public boolean hasAttribute(String name);

	public XMLEntity getAttribute(String name);

	public Collection<String> listAttributes();

	// Name
	public void setName(String text);

	public String getName();

	// Physics related
	public Poly getShape();

	public DrawableShape getDrawingShape();

	public void setDrawingShape(DrawableShape shape);

	public void colorize(Color c);

	public Color getColorization();

	public void setPosition(double x, double y);

	public double[] getPosition();

	// Behaviours
	public List<Behaviour> getBehaviours();

	public boolean addBehaviour(Behaviour o);

	public boolean removeBehaviour(Behaviour o);

	// Dynamics execution
	// Dynamic activation
	public void setEnabledExpression(Expression expr);

	public Expression getEnabledExpression();

	// Dynamic size and position
	public void setDynamicSize(Expression width, Expression height);

	public void setDynamicSize(Expression size);

	public void setDynamicWidth(Expression width);

	public void setDynamicHeight(Expression height);

	public void setDynamicSize();

	public void setDynamicPosition(Expression x, Expression y);

	public void setDynamicX(Expression x);

	public void setDynamicY(Expression y);

	public void setDynamicPosition();

	public Expression getDynamicWidth();

	public Expression getDynamicHeight();

	public Expression getDynamicX();

	public Expression getDynamicY();

	// Execution part
	public void reset();

	public void init();

	public void init(EntityInterface metaEntity);

	public void update(double t);

	public void terminate();

	public boolean terminated();

	public void initStep();

	public void initStep(EntityInterface metaEntity);

	public boolean updateStep(double t);

	// Parent
	public XMLEntity getParent();

	public void setParent(XMLEntity parent);

	public World getWorld();

	public void setWorld(World world);

	// Copy
	public EntityInterface copy();

	public EntityInterface copy(Map<Behaviour, Behaviour> bMap);

}