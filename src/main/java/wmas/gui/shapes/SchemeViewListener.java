package wmas.gui.shapes;

public interface SchemeViewListener {
	public void select(DrawableShape s);

	public boolean remove(DrawableShape s);

	public void click(DrawableShape s);

	public void mouseMoved(double x, double y);

	public void mouseOut();
}
