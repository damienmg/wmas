package wmas.gui.world.memory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import wmas.gui.run.EntityAttributeView;
import wmas.util.Util;
import wmas.world.EntityInterface;
import wmas.world.memory.Data;
import wmas.world.memory.Memory;
import wmas.world.memory.MemoryListener;

public class MemoryDisplay extends Component implements MouseListener,
		MouseMotionListener, EntityAttributeView {

	class MemoryTreeModel extends DefaultTreeCellRenderer implements TreeModel,
			MemoryListener {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getChild(Object arg0, int arg1) {
			if (arg0 == null)
				return null;
			if (arg0 instanceof Memory) {
				int i = ((Memory) arg0).getDataIndex(arg1);
				return i;
			} else if (arg0 instanceof Integer) {
				Data d = ((Memory) getRoot()).getData((Integer) arg0);
				return d.getSubData(arg1);
			} else if (arg0 instanceof Data) {
				return ((Data) arg0).getSubData(arg1);
			}
			return null;
		}

		@Override
		public int getChildCount(Object arg0) {
			if (arg0 == null)
				return 0;
			if (arg0 instanceof Memory) {
				return ((Memory) arg0).getNbData();
			} else if (arg0 instanceof Data) {
				Data d = (Data) arg0;
				if (d.nbSubData() < 0)
					return 0;
				return d.nbSubData();
			} else if (arg0 instanceof Integer) {
				Data d = ((Memory) getRoot()).getData((Integer) arg0);
				if (d.nbSubData() < 0)
					return 0;
				return d.nbSubData();
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			for (int i = 0; i < getChildCount(parent); i++) {
				if (getChild(parent, i) == child)
					return i;
			}
			return -1;
		}

		@Override
		public Object getRoot() {
			if (entity == null)
				return null;
			if (!entity.hasAttribute("memory"))
				return null;
			return entity.getAttribute("memory");
		}

		@Override
		public boolean isLeaf(Object node) {
			return getChildCount(node) == 0;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
		}

		public void refresh() {
			TreeModelEvent event = new TreeModelEvent(this,
					new Object[] { getRoot() });
			for (TreeModelListener l : listeners) {
				l.treeStructureChanged(event);
			}
		}

		private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void changed(int[] path) {
			TreePath p = new TreePath(getRoot());
			Data parent = null;
			for (int i : path) {
				if (parent == null) {
					parent = ((Memory) getRoot()).getData(i);
					p = p.pathByAddingChild(parent);
				} else {
					parent = parent.getSubData(i);
					p = p.pathByAddingChild(parent);
				}
			}
			TreeModelEvent event = new TreeModelEvent(this, p.getParentPath());
			for (TreeModelListener l : listeners) {
				l.treeStructureChanged(event);
			}
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			if (value != null) {
				if (value instanceof Integer) {
					value = "[" + value.toString() + "] "
							+ ((Memory) getRoot()).getData((Integer) value);
				} else if (value instanceof Memory) {
					value = getViewName();
				} else if (value instanceof Data) {
					value = value.toString();
				}
			}
			return super.getTreeCellRendererComponent(tree, value, sel,
					expanded, leaf, row, hasFocus);
		}

	}

	private static final long serialVersionUID = 5822817690552627009L;

	protected Color bgColor;
	protected Color blockColor;

	protected double xover;
	protected double yover;

	protected double marginLeft;
	protected double marginTop;

	protected JScrollPane parent;

	protected JSplitPane splitter;
	protected JTree tree;
	protected MemoryTreeModel treeModel;

	protected EntityInterface entity;

	public MemoryDisplay() {
		super();
		this.entity = null;
		bgColor = Color.WHITE;
		marginLeft = 50;
		marginTop = 50;
		this.parent = null;
		xover = yover = -1;
		treeModel = new MemoryTreeModel();
		tree = new JTree(treeModel);
		tree.setCellRenderer(treeModel);
		tree.setEditable(false);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setRightComponent(new JScrollPane(tree));
		parent = new JScrollPane(this);
		splitter.setLeftComponent(parent);
		splitter.setDividerLocation(200);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void setEntity(EntityInterface entity) {
		if (this.entity != null) {
			if (this.entity.hasAttribute("memory")) {
				Memory mem = (Memory) this.entity.getAttribute("memory");
				mem.removeListener(treeModel);
			}
		}
		this.entity = entity;
		if (this.entity != null) {
			if (this.entity.hasAttribute("memory")) {
				Memory mem = (Memory) this.entity.getAttribute("memory");
				mem.addListener(treeModel);
			}
		}
		treeModel.refresh();
		repaint();
	}

	protected String drawMemory(Graphics2D g, double x, double y, double width,
			double height) {
		if (entity == null || !entity.hasAttribute("memory"))
			return "";
		Memory mem = (Memory) entity.getAttribute("memory");
		long msize = mem.getTotalSize();
		double pixelPerByte = width / msize;
		Color c = g.getColor();
		g.setColor(blockColor);
		g.drawRect((int) x, (int) y, (int) width, (int) height);
		Font f = g.getFont();
		String r = "";
		for (Integer i : mem) {
			Data d = mem.getData(i);
			double w = (pixelPerByte * d.getSize());
			g.drawRect((int) x, (int) y, (int) w, (int) height);
			if (xover > x && xover < x + w && yover > y && yover < y + height) {
				g.setFont(f.deriveFont(Font.BOLD));
				g.drawRect((int) x + 1, (int) y + 1, (int) w, (int) height);
				r += "\n" + d.toString();
			} else
				g.setFont(f);
			String s = i.toString();
			g.drawString(s, (int) (x + w / 2 - g.getFontMetrics()
					.stringWidth(s) / 2), (int) (y + height / 2 + g
					.getFontMetrics().getHeight() / 2));
			x += w;
		}
		g.setFont(f);
		g.setColor(c);
		if (r.length() > 0)
			r = r.substring(1);
		return r;
	}

	public synchronized JScrollPane getScrollPane() {
		return parent;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(600, Util.toInteger(4 * marginTop));
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
		String s = drawMemory(g, marginLeft, marginTop, b.getWidth() - 2
				* marginLeft, b.getHeight() - 2 * marginTop);
		paintOver(s, g);
	}

	@Override
	public void repaint() {
		super.repaint();
		if (parent != null)
			parent.revalidate();
	}

	static private List<String[]> tableDecomposition(String s) {
		LinkedList<String[]> ll = new LinkedList<String[]>();
		String[] r = s.split("\n");
		for (int i = 0; i < r.length; i++)
			ll.add(r[i].split("\t"));
		return ll;
	}

	static private double[] tableWidth(List<String[]> r, FontMetrics fm) {
		double wspace = fm.stringWidth(" ");
		int nbResults = 1;
		double[] res;
		for (String[] e : r) {
			nbResults = Math.max(nbResults, e.length);
		}
		res = new double[nbResults + 1];
		for (String[] e : r) {
			double cSpace = wspace * (e.length - 1);
			for (int i = 0; i < e.length; i++) {
				double lw = fm.stringWidth(e[i]);
				res[i + 1] = Math.max(res[i + 1], lw);
				cSpace += lw;
			}
			res[0] = Math.max(res[0], cSpace);
		}
		double w = 0;
		for (int i = 1; i < res.length; i++) {
			w += res[i];
		}
		w += (res.length - 2) * wspace;
		res[0] = Math.max(res[0], w);
		return res;
	}

	private void paintOver(String s, Graphics2D g) {
		if (s.length() == 0)
			return;
		double y = yover + 20;
		double x = xover;
		Color c = g.getColor();
		List<String[]> r = tableDecomposition(s);
		double wspace = g.getFontMetrics().stringWidth(" ");
		double hspace = g.getFontMetrics().getHeight();
		double[] ld = tableWidth(r, g.getFontMetrics());
		double w = ld[0];
		double h = (r.size() + 0.5) * hspace;
		if (x < w / 2 + wspace)
			x = w / 2 + wspace;
		g.setColor(Color.DARK_GRAY);
		g.drawRect((int) (x - w / 2 - wspace), (int) y, (int) (w + 2 * wspace),
				(int) h);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect((int) (x - w / 2 - wspace), (int) y, (int) (w + 2 * wspace),
				(int) h);
		g.setColor(c);
		for (String[] t : r) {
			y += hspace;
			if (t.length > 1) {
				double xinit = x - w / 2;
				for (int i = 0; i < t.length; i++) {
					g.drawString(t[i], (float) xinit, (float) y);
					if (i < t.length - 1) {
						xinit += ld[i + 1] + wspace;
					}
				}
			} else {
				g.drawString(t[0],
						(float) (x - g.getFontMetrics().stringWidth(t[0]) / 2),
						(float) y);
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		xover = e.getX();
		yover = e.getY();
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		xover = -1;
		yover = -1;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public Component getComponent() {
		return splitter;
	}

	@Override
	public String getViewName() {
		if (entity == null || !entity.hasAttribute("memory"))
			return "";
		String r = entity.getName();
		if (r == null)
			r = "";
		if (r.length() > 0) {
			return "Memory of " + r + " (size = "
					+ ((Memory) entity.getAttribute("memory")).getMemorySize()
					+ ")";
		}
		return ((Memory) entity.getAttribute("memory")).getMemorySize()
				+ " memory";
	}

}
