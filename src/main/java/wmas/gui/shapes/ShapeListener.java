package wmas.gui.shapes;

public interface ShapeListener {
	public void moved(DrawableShape s);

	public void resized(DrawableShape s);

	public void changed(DrawableShape s);
}
