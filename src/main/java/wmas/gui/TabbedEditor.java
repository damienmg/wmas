package wmas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class TabbedEditor extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	private static final Icon crossIcon = new ImageIcon(
			TabbedEditor.class.getResource("icons/cross.gif"));
	private static final Icon crossOverIcon = new ImageIcon(
			TabbedEditor.class.getResource("icons/crossover.gif"));

	private Set<EditorInterface> interfaces = new HashSet<EditorInterface>();
	protected Map<Object, EditorInterface> tabEditors = new HashMap<Object, EditorInterface>();
	protected Map<Component, Object> tabObjects = new HashMap<Component, Object>();

	protected EditorInterfaceFactory factory;

	public EditorInterface getEditor(Object o) {
		return tabEditors.get(o);
	}

	public TabbedEditor(EditorInterfaceFactory factory) {
		super();
		this.factory = factory;
		setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	private void pushInterface(Object o) {
		if (o == null)
			return;
		EditorInterface i = tabEditors.get(o);
		if (i == null)
			return;
		tabEditors.remove(o);
		tabObjects.remove(i.getEditor());
		interfaces.add(i);
	}

	public EditorInterface getInterface(Object o) {
		return tabEditors.get(o);
	}

	private EditorInterface popInterface(Object o) {
		if (o == null)
			return null;
		for (EditorInterface i : interfaces) {
			if (i.canEditObject(o)) {
				interfaces.remove(i);
				tabEditors.put(o, i);
				tabObjects.put(i.getEditor(), o);
				i.setObject(o);
				return i;
			}
		}
		if (factory != null) {
			EditorInterface i = factory.create(o);
			if (i != null) {
				tabEditors.put(o, i);
				tabObjects.put(i.getEditor(), o);
			}
			return i;
		}
		return null;
	}

	public EditorInterface edit(Object o) {
		if (tabEditors.containsKey(o)) {
			setSelectedComponent(tabEditors.get(o).getEditor());
			return tabEditors.get(o);
		}
		EditorInterface i = popInterface(o);
		if (i == null) {
			return null;
		}
		addTab(i.getTitle(), i.getEditor());
		int ind = getTabCount() - 1;
		setTabComponentAt(ind, createTabComponent(o));
		setSelectedComponent(i.getEditor());
		return i;
	}

	private JPanel createTabComponent(Object o) {
		final JPanel newPane = new JPanel(new BorderLayout());
		newPane.setOpaque(false);
		JLabel label = new JLabel() {
			private static final long serialVersionUID = 1L;

			public String getText() {
				int i = indexOfTabComponent(newPane);
				if (i >= 0) {
					Object o = tabObjects.get(TabbedEditor.this
							.getComponentAt(i));
					if (o != null && tabEditors.containsKey(o)) {
						return tabEditors.get(o).getTitle();
					}
				}
				return null;
			}

			@Override
			public Icon getIcon() {
				int i = indexOfTabComponent(newPane);
				if (i >= 0) {
					Object o = tabObjects.get(TabbedEditor.this
							.getComponentAt(i));
					if (o != null && tabEditors.containsKey(o)) {
						return tabEditors.get(o).getIcon();
					}
				}
				return null;
			}

			@Override
			public void paint(Graphics g) {
				Font f = getFont();
				f = f.deriveFont(Font.PLAIN);
				int i = indexOfTabComponent(newPane);
				if (i >= 0) {
					Object o = tabObjects.get(TabbedEditor.this
							.getComponentAt(i));
					if (o != null && tabEditors.containsKey(o)) {
						if (tabEditors.get(o).isModified()) {
							f = f.deriveFont(Font.BOLD);
						}
					}
				}
				setFont(f);
				super.paint(g);
			}
		};

		newPane.add(label, BorderLayout.WEST);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

		JButton button = new JButton(crossIcon);
		button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		button.setRolloverIcon(crossOverIcon);
		button.setRolloverEnabled(true);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove(indexOfTabComponent(newPane));
			}
		});
		newPane.add(button, BorderLayout.EAST);
		newPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

		return newPane;
	}

	public void remove(Object o) {
		if (tabEditors.containsKey(o)) {
			remove(tabEditors.get(o).getEditor());
		}
	}

	@Override
	public void remove(Component c) {
		if (tabObjects.containsKey(c)) {
			Object o = tabObjects.get(c);
			pushInterface(o);
		}
		super.remove(c);
	}

	@Override
	public void remove(int index) {
		removeTabAt(index);
	}

	@Override
	public void removeAll() {
		for (EditorInterface i : tabEditors.values()) {
			interfaces.add(i);
		}
		tabEditors.clear();
		tabObjects.clear();
		super.removeAll();
	}

	@Override
	public void removeTabAt(int i) {
		Component c = getComponentAt(i);
		if (tabObjects.containsKey(c)) {
			Object o = tabObjects.get(c);
			pushInterface(o);
		}
		super.removeTabAt(i);
	}

}
