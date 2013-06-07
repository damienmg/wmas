package wmas.gui.shapes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import wmas.util.Util;

public class SchemeView extends Component implements MouseListener,
		MouseMotionListener, KeyListener, ActionListener {

	private static final long serialVersionUID = -7261948396555836918L;

	public static final int MARGIN = 5;
	private static final int KEY_MOVE_STEP = 10;

	double zoom = 1.0;
	Color selectingColor = Color.BLUE;
	Color bgColor = Color.WHITE;
	boolean editable = false;
	Component parent = null;

	JPopupMenu menu = null;
	JMenuItem[] mnuItems = null;

	ShapeFactory editor = null;
	DrawableShape selected = null;
	DrawableShape nextSelected = null;
	boolean mouseDown = false;
	boolean mouseMoved = false;

	int drawLine = -1;
	int drawConnector = -1;

	double mouseX;
	double mouseY;
	double mouseOrigX;
	double mouseOrigY;

	Set<SchemeViewListener> listeners;
	LinkedList<DrawableShape> shapes = new LinkedList<DrawableShape>();

	private void init() {
		mouseDown = false;
		listeners = new HashSet<SchemeViewListener>();
		listeners.clear();
		setFocusable(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	private void constructMenu() {
		if (editor != null) {
			menu = new JPopupMenu();
			JMenuItem submenu;
			String[] el = editor.getAddableElement();
			mnuItems = new JMenuItem[el.length];
			for (int i = 0; i < el.length; i++) {
				submenu = new JMenuItem("Add " + el[i]);
				submenu.setActionCommand("add:" + el[i]);
				submenu.addActionListener(this);
				menu.add(submenu);
				if (editor.isConnector(i))
					mnuItems[i] = submenu;
				else
					mnuItems[i] = null;
			}
		}
	}

	public SchemeView() {
		super();
		init();
	}

	public SchemeView(ShapeFactory editor) {
		super();
		this.editor = editor;
		constructMenu();
		init();
	}

	private AffineTransform transform;

	private void restoreAT(Graphics2D g) {
		g.setTransform(transform);
	}

	private void setAT(Graphics2D g) {
		transform = g.getTransform();
		// Rectangle r = getBounds();
		AffineTransform at = new AffineTransform(zoom, 0, 0, zoom, 0, 0);
		g.transform(at);
	}

	void drawAll(Graphics2D g) {

		for (DrawableShape shape : shapes) {
			shape.paint(g);
		}

		if (selected != null) {
			ShapeDrawer.drawBorder(g, selectingColor, selected);
		}
		if (selected != null && mouseDown && mouseMoved) {
			ShapeDrawer.drawMouseDown(g, Color.LIGHT_GRAY,
					fromScreen(mouseOrigX), fromScreen(mouseOrigY),
					fromScreen(mouseX), fromScreen(mouseY), selected);
		}
		if (drawConnector >= 0) {
			DrawableShape ds = findConnectAtPoint(fromScreen(mouseX),
					fromScreen(mouseY), drawConnector);
			double[] xy0 = selected.getPosition();
			double[] xy1 = fromScreen(new double[] { mouseX, mouseY });
			double dx = xy0[0] - xy1[0];
			double dy = xy0[1] - xy1[1];
			if (ds != null) {
				xy1 = ds.getPosition();
				dx = xy0[0] - xy1[0];
				dy = xy0[1] - xy1[1];
				xy1 = ds.getConnectionPoint(dx, dy);
			}
			xy0 = selected.getConnectionPoint(-dx, -dy);
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine((int) fromScreen(xy0[0]), (int) fromScreen(xy0[1]),
					(int) fromScreen(xy1[0]), (int) fromScreen(xy1[1]));
		}
		if (drawLine >= 0) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine((int) fromScreen(mouseOrigX),
					(int) fromScreen(mouseOrigY), (int) fromScreen(mouseX),
					(int) fromScreen(mouseY));
		}
	}

	private double fromScreen(double x) {
		return x / zoom;
	}

	private double[] fromScreen(double[] ds) {
		ds[0] = ds[0] / zoom;
		ds[1] = ds[1] / zoom;
		return ds;
	}

	private double[] fromScreen(Point ds) {
		return new double[] { ds.x / zoom, ds.y / zoom };
	}

	@Override
	public void paint(Graphics g1) {
		super.paint(g1);
		Graphics2D g = (Graphics2D) g1;
		Color c = g.getColor();
		g.setColor(bgColor);
		Rectangle b = g.getClipBounds();
		if (b != null) {
			g.fillRect(Util.toInteger(b.getX()), Util.toInteger(b.getY()),
					Util.toInteger(b.getWidth()), Util.toInteger(b.getHeight()));
		} else {
			g.setBackground(bgColor);
		}
		g.setColor(c);
		setAT(g);
		drawAll(g);
		restoreAT(g);
	}

	public synchronized void setParentPane(Component parent) {
		this.parent = parent;
	}

	@Override
	public Dimension getPreferredSize() {
		double[] bounds = { 0, 0, MARGIN, MARGIN };
		for (DrawableShape shape : shapes) {
			double[] r = shape.getBounds();
			if (r[0] < bounds[0])
				bounds[0] = r[0];
			if (r[1] < bounds[1])
				bounds[1] = r[1];
			if (bounds[2] < r[0] + r[2] + MARGIN)
				bounds[2] = r[0] + r[2] + MARGIN;
			if (bounds[3] < r[1] + r[3] + MARGIN)
				bounds[3] = r[1] + r[3] + MARGIN;
		}
		double width = bounds[2];
		double height = bounds[3];
		if (width < MARGIN)
			width = MARGIN;
		if (height < MARGIN)
			height = MARGIN;
		return new Dimension(Util.toInteger(width * zoom),
				Util.toInteger(height * zoom));
	}

	private boolean firstRepaint = true;

	@Override
	public void repaint() {
		if (parent != null && firstRepaint) {
			firstRepaint = false;
			parent.validate();
			parent.repaint();
		}
		super.repaint();
		firstRepaint = true;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void add(DrawableShape s) {
		shapes.add(s);
		repaint();
	}

	public void remove(DrawableShape s) {
		boolean r = true;
		for (SchemeViewListener l : listeners)
			r = l.remove(s) && r;
		if (!r)
			return;
		shapes.remove(s);
		if (s == selected) {
			selected = null;
			for (SchemeViewListener l : listeners)
				l.select(null);
		}
		repaint();
	}

	public void moveUp(DrawableShape s) {
		int index = shapes.indexOf(s);
		if (index > 0) {
			shapes.remove(s);
			shapes.add(index - 1, s);
			repaint();
		}
	}

	public void moveDown(DrawableShape s) {
		int index = shapes.indexOf(s);
		if (index >= 0 && index < shapes.size() - 1) {
			shapes.remove(s);
			shapes.add(index + 1, s);
			repaint();
		}
	}

	public void addListener(SchemeViewListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SchemeViewListener listener) {
		listeners.remove(listener);
	}

	/* Finding object positions on the screen */
	private synchronized LinkedList<DrawableShape> findAtPoint(double x,
			double y, boolean selectable) {
		LinkedList<DrawableShape> result = new LinkedList<DrawableShape>();
		for (DrawableShape s : this.shapes) {
			if ((s.isSelectable() || !selectable)
					&& ShapeDrawer.contains(s, x, y))
				result.add(0, s);
		}
		return result;
	}

	private synchronized LinkedList<DrawableShape> findAtPoint(double[] xy) {
		return findAtPoint(xy[0], xy[1], false);
	}

	private synchronized DrawableShape findConnectAtPoint(double x, double y,
			int type) {
		for (DrawableShape s : this.shapes) {
			if (editor.checkCanConnect(s, type)
					&& ShapeDrawer.contains(s, x, y))
				return s;
		}
		return null;
	}

	/* Object actions */
	private void selectNext(double[] xy) {
		selectNext(xy[0], xy[1]);
	}

	private void selectNext(double x, double y) {
		if (editable) {
			DrawableShape olds = selected;
			LinkedList<DrawableShape> r = findAtPoint(x, y, true);
			if (!r.isEmpty()) {
				int i = r.indexOf(selected);
				if (i < 0)
					selected = r.getFirst();
				else {
					selected = r.get((i + 1) % r.size());
				}
			} else
				selected = null;
			if (olds != selected) {
				for (SchemeViewListener l : listeners)
					l.select(selected);
				repaint();
			}
		}
	}

	/* Mouse events */
	public void mouseClicked(MouseEvent e) {
		requestFocus();
		if (!editable) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				this.requestFocus();
				LinkedList<DrawableShape> res = findAtPoint(fromScreen(e
						.getPoint()));
				for (DrawableShape s : res) {
					for (SchemeViewListener l : listeners) {
						l.click(s);
					}
				}
			}
		} else {
			if (e.getButton() == MouseEvent.BUTTON3) {
				if (drawLine >= 0) {
					drawLine = -1;
					repaint();
				} else if (drawConnector >= 0) {
					drawConnector = -1;
					repaint();
				} else if (menu != null) {
					mouseX = e.getX();
					mouseY = e.getY();
					mouseDown = false;
					if (selected == null) {
						for (int i = 0; i < mnuItems.length; i++)
							if (mnuItems[i] != null)
								mnuItems[i].setEnabled(false);
					} else {
						for (int i = 0; i < mnuItems.length; i++)
							if (mnuItems[i] != null)
								mnuItems[i].setEnabled(editor.checkCanConnect(
										selected, i));
					}
					repaint();
					menu.show(this, e.getX(), e.getY());
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		for (SchemeViewListener l : listeners)
			l.mouseOut();
		if (mouseDown) {
			mouseDown = false;
			repaint();
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseDown = false;
			double[] xy = fromScreen(e.getPoint());
			if (drawLine > 0 && editor != null) {
				DrawableShape s = editor.create(drawLine);
				if (s != null) {
					double dx = xy[0] - mouseOrigX;
					double dy = xy[1] - mouseOrigY;
					s.setSize(dx, dy);
					s.setPosition(xy[0] - dx / 2, xy[1] - dy / 2);
					shapes.add(s);
					drawLine = -1;
				}
			} else if (drawConnector > 0 && editor != null) {
				DrawableShape ds = findConnectAtPoint(xy[0], xy[1],
						drawConnector);
				if (ds != null) {
					DrawableShape s = editor
							.create(drawConnector, selected, ds);
					if (s != null)
						shapes.add(s);
				}
				drawConnector = -1;
			} else if (selected != null
					&& ShapeDrawer.contains(selected, xy[0], xy[1])) {
				if (selected.isMoveable()) {
					mouseDown = true;
					mouseMoved = false;
					mouseX = e.getX();
					mouseY = e.getY();
					mouseOrigX = e.getX();
					mouseOrigY = e.getY();
				}
			} else if (selected != null) {
				selected = null;
				for (SchemeViewListener l : listeners)
					l.select(null);
			}
			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (editable) {
				if (mouseDown) {
					mouseDown = false;
					if (e.getX() != mouseOrigX || e.getY() != mouseOrigY
							|| mouseMoved) {
						if (selected.isMoveable() || selected.isResizeable()) {
							ShapeDrawer.doMouseRelease(fromScreen(mouseOrigX),
									fromScreen(mouseOrigY),
									fromScreen(e.getX()), fromScreen(e.getY()),
									selected);
							repaint();
						}
					} else {
						selectNext(fromScreen(e.getPoint()));
					}
				} else {
					selectNext(fromScreen(e.getPoint()));
				}
			}
		}
	}

	private void mMouseMoved(MouseEvent e) {
		double[] p = fromScreen(e.getPoint());
		for (SchemeViewListener l : listeners)
			l.mouseMoved(p[0], p[1]);
		if (editable) {
			if (mouseDown || drawLine >= 0 || drawConnector >= 0) {
				if (e.getX() != mouseX || e.getY() != mouseY) {
					mouseMoved = true;
					mouseX = e.getX();
					mouseY = e.getY();
				}
				repaint();
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		mMouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		mMouseMoved(e);
	}

	/* Key events */
	public void keyPressed(KeyEvent e) {
		if (editable) {
			if (!mouseDown && drawConnector < 0 && drawLine < 0
					&& (selected != null)) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_DELETE:
				case KeyEvent.VK_BACK_SPACE:
					remove(selected);
					break;
				case KeyEvent.VK_UP:
					if (selected.isMoveable()) {
						double[] pos = selected.getPosition();
						selected.setPosition(pos[0],
								pos[1] > KEY_MOVE_STEP ? pos[1] - KEY_MOVE_STEP
										: 0);
						repaint();
					}
					break;
				case KeyEvent.VK_DOWN:
					if (selected.isMoveable()) {
						double[] pos = selected.getPosition();
						selected.setPosition(pos[0], pos[1] + KEY_MOVE_STEP);
						repaint();
					}
					break;
				case KeyEvent.VK_LEFT:
					if (selected.isMoveable()) {
						double[] pos = selected.getPosition();
						selected.setPosition(pos[0] > KEY_MOVE_STEP ? pos[0]
								- KEY_MOVE_STEP : 0, pos[1]);
						repaint();
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (selected.isMoveable()) {
						double[] pos = selected.getPosition();
						selected.setPosition(pos[0] + KEY_MOVE_STEP, pos[1]);
						repaint();
					}
					break;
				}

			}
		}

	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().startsWith("add:")) {
			if (editor == null)
				return;
			String cmd = e.getActionCommand().substring(4);
			String[] res = editor.getAddableElement();
			for (int i = 0; i < res.length; i++) {
				if (res[i].equals(cmd)) {
					if (editor.isConnector(i)) {
						if (selected != null
								&& editor.checkCanConnect(selected, i)) {
							drawConnector = i;
							mouseOrigX = mouseX;
							mouseOrigY = mouseY;
							repaint();
						}
					} else if (editor.isLine(i)) {
						drawLine = i;
						mouseOrigX = mouseX;
						mouseOrigY = mouseY;
						repaint();
					} else {
						DrawableShape s = editor.create(i);
						if (s != null) {
							s.setPosition(mouseX, mouseY);
							shapes.add(s);
							repaint();
						}
					}
				}
			}
		}
	}

	public void saveSVG(File f) throws IOException {
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		paint(svgGenerator);
		boolean useCSS = true;
		FileWriter out = new FileWriter(f);
		svgGenerator.stream(out, useCSS);
	}

	public void removeAll() {
		if (selected != null) {
			selected = null;
			for (SchemeViewListener l : listeners)
				l.select(null);
		}
		shapes.clear();
		repaint();
	}

	public void replace(DrawableShape curShape, DrawableShape newShape) {
		int i = shapes.indexOf(curShape);

		if (i >= 0) {
			shapes.remove(i);
			shapes.add(i, newShape);
			if (selected == curShape) {
				selected = newShape;
			}
		}
		repaint();
	}

	public void select(DrawableShape drawingShape) {
		if (!shapes.contains(drawingShape))
			drawingShape = null;
		if (drawingShape != selected) {
			selected = drawingShape;
			for (SchemeViewListener l : listeners)
				l.select(selected);
			repaint();
		}
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
		this.repaint();
	}

	public boolean contains(DrawableShape drawingShape) {
		return shapes.contains(drawingShape);
	}

	public void addTop(DrawableShape drawingShape) {
		shapes.addFirst(drawingShape);
		repaint();
	}

	public List<DrawableShape> listShapes() {
		return shapes;
	}

	public DrawableShape getSelected() {
		return selected;
	}
}
