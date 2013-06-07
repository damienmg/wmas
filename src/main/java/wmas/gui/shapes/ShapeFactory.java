package wmas.gui.shapes;

import wmas.gui.shapes.elements.Connector;

public interface ShapeFactory {
	public String[] getAddableElement();

	public boolean isLine(int elem);

	public boolean isConnector(int elem);

	public boolean checkCanConnect(DrawableShape shape, int elem);

	public DrawableShape create(int elem);

	public Connector create(int elem, DrawableShape source, DrawableShape dest);
}
