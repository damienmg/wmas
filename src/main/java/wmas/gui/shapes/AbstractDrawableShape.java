package wmas.gui.shapes;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDrawableShape implements DrawableShape {

	protected Color colorized = null;

	Set<ShapeListener> listeners = new HashSet<ShapeListener>();

	public void addListener(ShapeListener l) {
		listeners.add(l);
	}

	public void removeListener(ShapeListener l) {
		listeners.remove(l);
	}

	public void resized() {
		for (ShapeListener l : listeners)
			l.resized(this);
	}

	public void moved() {
		for (ShapeListener l : listeners)
			l.moved(this);
	}

	public void changed() {
		for (ShapeListener l : listeners)
			l.changed(this);
	}

	public void colorize(Color c) {
		colorized = c;
	}

}
