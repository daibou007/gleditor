package animation.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import animation.world.Animation;
import animation.world.ImageProvider;
import animation.world.Module;
import animation.world.Sprite;

/**
 * ģ����ͼ
 * 
 * @author ����
 * @time 2012-9-15
 */
public class ModuleViewer extends ScalableViewer implements Localizable {
	private static final long serialVersionUID = -4296814707914161722L;

	public ModuleViewer() {
		try {
			jbInit();
			updateLocalization();
			listenerInit();
			setViewScale(Configuration.VIEW_SCALE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ��ģ����ͼ���
	 */
	private void jbInit() {
		setDoubleBuffered(false);
		setAutoscrolls(true);
		setBorder(BorderFactory.createMatteBorder(16, 16, 16, 16, Color.BLACK));
		setLayout(new BorderLayout());
	}

	@Override
	public void updateLocalization() {

	}

	/**
	 * ��ʼ��������
	 * <p>
	 * <li>���øı����ػ�ģ����ͼ</li>
	 * <li>ͼƬ�ı����ػ�ģ����ͼ, ���ͼƬ��С�����仯�����²���</li>
	 * <li>�����ѡ��ı���ģ���ѡ����Ӧ�ı䲢�ػ�ģ����ͼ</li>
	 * <li>ģ���ѡ��ı����ػ�ģ����ͼ</li>
	 * <li>֧��drag, �ṩMODULE_FLAVOR</li>
	 */
	private void listenerInit() {
		Configuration.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				repaint();
			}

		});
		Animation.aniImage().addPropertyChangeListener(
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("size")) {
							zoomViewer();
						}
						repaint();
					}

				});
		Sprite.getModel().addSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel selectionModel = Module.getModel()
						.getSelectionModel();
				selectionModel.clearSelection();
				for (Sprite sprite : Sprite.getModel().getSelections()) {
					Module module = sprite.module;
					if (module != null) {
						int id = module.getId();
						selectionModel.addSelectionInterval(id, id);
					}
				}
				repaint();
			}

		});
		Module.getModel().addSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				repaint();
			}

		});
		setTransferHandler(new TableTransferHandler(
				TableTransferHandler.MODULE_FLAVOR, null) {
			private static final long serialVersionUID = -281198366908167885L;

			@Override
			public boolean canImport(TransferSupport support) {
				return false;
			}

			@Override
			public Transferable createTransferable(JComponent c) {
				ArrayList<Integer> v = new ArrayList<Integer>();
				for (Module module : Module.getModel().getSelections()) {
					v.add(module.getId());
				}
				return new TableTransferHandler.ArrayListTransferable(v,
						TableTransferHandler.MODULE_FLAVOR);
			}

		});
	}

	@Override
	public Dimension getOriginSize() {
		ImageProvider provider = Animation.aniImage();
		if (provider.isEmpty()) {
			return new Dimension(64, 64);
		} else {
			return new Dimension(provider.getWidth(), provider.getHeight());
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (!Animation.aniImage().isEmpty()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Configuration.color_background);
			g2.fill(g2.getClipBounds());
			Animation.aniImage().render(g2, getTransform());
			g2.setStroke(new BasicStroke(1.0F));
			paintModuleBorder(g2);
			paintModuleSelection(g2);
		}
	}

	/**
	 * ����ģ��߿�
	 * 
	 * @param g2
	 */
	private void paintModuleBorder(Graphics2D g2) {
		g2.setColor(Configuration.color_module_border);
		for (Module module : Animation.instance().getModuleList()) {
			g2.draw(transformCoordinate(module));
		}
	}

	/**
	 * ����ѡ�е�ģ��߿�
	 * 
	 * @param g2
	 */
	private void paintModuleSelection(Graphics2D g2) {
		g2.setColor(Configuration.color_selected_border);
		for (Module module : Module.getModel().getSelections()) {
			g2.draw(transformCoordinate(module));
		}
	}

}
