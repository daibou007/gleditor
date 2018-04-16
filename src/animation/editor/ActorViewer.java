package animation.editor;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import animation.editor.tool.AbstractTool;
import animation.world.AniFrame;
import animation.world.Animation;
import animation.world.Module;
import animation.world.Sequence;
import animation.world.Sprite;
import animation.world.Template;

/**
 * ��ɫ��ͼ
 * 
 * @author ����
 * @time 2012-9-17
 */
public class ActorViewer extends ScalableViewer implements Localizable {
	private static final long serialVersionUID = -3399534513054136440L;

	private AniFrame aniFrame;
	private JTextField collisionBoxText;
	private JTextField attackBoxText;
	/**
	 * ��ײ��͹������ı���ʽ
	 */
	private MessageFormat boxFormat = new MessageFormat(
			"[{0, number, ###}, {1, number, ###}, {2, number, ###}, {3, number, ###}]");
	/**
	 * �Ƿ��������
	 */
	private boolean hasGrid = true;
	/**
	 * �Ƿ���ʾ��ײ��͹�����
	 */
	private boolean showBox = false;

	public ActorViewer() {
		try {
			jbInit();
			updateLocalization();
			listenerInit();
			setViewScale(Configuration.VIEW_SCALE);
			setOrigin(-160, -160);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ����ɫ��ͼ���
	 */
	private void jbInit() {
		setDoubleBuffered(false);
		setAutoscrolls(true);
	}

	@Override
	public void updateLocalization() {

	}

	/**
	 * ��ʼ����ɫ��ͼ������
	 * <p>
	 * <li>���øı��ػ��ɫ��ͼ</li>
	 * <li>ͼƬ�ı��ػ��ɫ��ͼ</li>
	 * <li>֡��ѡ��ı����������ý�ɫ��ͼ��֡����</li>
	 * <li>ģ������ݸı����ػ��ɫ��ͼ</li>
	 * <li>�����ѡ��ı����ػ��ɫ��ͼ</li>
	 * <li>��������ݸı����ػ��ɫ��ͼ</li>
	 * <li>֡���б�ı����������ý�ɫ��ͼ��֡����</li>
	 * <li>֧��Drop, ����MODULE_FLAVOR��TEMPLATE_FRAME_FLAVOR</li>
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
						repaint();
					}

				});
		AniFrame.getModel().addSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				setFrame(AniFrame.getModel().getSelection());
			}

		});
		Template.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				repaint();
			}
		});
		Sprite.getModel().addSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				repaint();
			}
		});
		Sprite.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				repaint();
			}
		});
		Sequence.getModel().addSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				Sequence sequence = Sequence.getModel().getSelection();
				setFrame(sequence == null ? null : sequence.getFrame());
			}
		});
		setDropTarget(new DropTarget(this, new DropTargetAdapter() {

			@SuppressWarnings("unchecked")
			@Override
			public void drop(DropTargetDropEvent dtde) {
				Transferable t = dtde.getTransferable();
				if (t.isDataFlavorSupported(TableTransferHandler.MODULE_FLAVOR)) {
					try {
						Point p = transformCoordinate(dtde.getLocation());
						ArrayList<Integer> v = (ArrayList<Integer>) t
								.getTransferData(TableTransferHandler.MODULE_FLAVOR);
						for (Integer i : v) {
							Module module = Animation.instance().getModule(i);
							Sprite sprite = new Sprite(module);
							sprite.translate(p.x, p.y);
							Sprite.getModel().insertRow(
									Sprite.getModel().getRowCount(), sprite);
						}
						dtde.dropComplete(true);
						return;
					} catch (UnsupportedFlavorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (t.isDataFlavorSupported(TableTransferHandler.TEMPLATE_FRAME_FLAVOR)) {
					Point p = transformCoordinate(dtde.getLocation());
					try {
						ArrayList<Integer> v = (ArrayList<Integer>) t
								.getTransferData(TableTransferHandler.TEMPLATE_FRAME_FLAVOR);
						Iterator<Integer> iter = v.iterator();
						while (iter.hasNext()) {
							int templateID = iter.next();
							Template template = Template
									.getTemplate(templateID);
							int frameID = iter.next();
							Sprite sprite = new Sprite(template, frameID);
							sprite.translate(p.x, p.y);
							Sprite.getModel().insertRow(
									Sprite.getModel().getRowCount(), sprite);
						}
						dtde.dropComplete(true);
						return;
					} catch (UnsupportedFlavorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				dtde.dropComplete(false);
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				if (aniFrame != null) {
					for (DataFlavor flavor : dtde.getCurrentDataFlavorsAsList()) {
						if (flavor.getHumanPresentableName().equals(
								TableTransferHandler.MODULE_FLAVOR
										.getHumanPresentableName())) {
							dtde.acceptDrag(TransferHandler.COPY);
							return;
						}
						if (flavor.getHumanPresentableName().equals(
								TableTransferHandler.TEMPLATE_FRAME_FLAVOR
										.getHumanPresentableName())) {
							dtde.acceptDrag(TransferHandler.COPY);
							return;
						}
					}
				}
				dtde.rejectDrag();
			}
		}));
	}

	@Override
	public Dimension getOriginSize() {
		return new Dimension(320, 320);
	}

	@Override
	public AbstractTool getTool() {
		return aniFrame == null ? null : super.getTool();
	}

	/**
	 * ��ȡ��ɫ��ͼ��ǰ֡
	 */
	public AniFrame getFrame() {
		return aniFrame;
	}

	/**
	 * ���ý�ɫ��ͼ��ǰ֡
	 * 
	 * @param frame
	 */
	public void setFrame(AniFrame frame) {
		if (aniFrame != frame) {
			aniFrame = frame;
			loadBoxData();
			repaint();
		}
	}

	/**
	 * �ڽ�������ʾ��ɫ��ǰ֡��ײ��͹�����
	 */
	public void loadBoxData() {
		getCollisonBoxTextField().setText("");
		getAttackBoxTextField().setText("");
		if (aniFrame != null) {
			Rectangle box = aniFrame.collisionBox;
			String text = boxFormat.format(new Integer[] { box.x, box.y,
					box.x + box.width, box.y + box.height });
			collisionBoxText.setText(text);

			box = aniFrame.attackBox;
			text = boxFormat.format(new Integer[] { box.x, box.y,
					box.x + box.width, box.y + box.height });
			attackBoxText.setText(text);
		}
	}

	/**
	 * ��ȡ��ײ���ı���
	 */
	public JTextField getCollisonBoxTextField() {
		if (collisionBoxText == null) {
			collisionBoxText = buildBoxText();
		}

		return collisionBoxText;
	}

	/**
	 * ��ȡ�������ı���
	 */
	public JTextField getAttackBoxTextField() {
		if (attackBoxText == null) {
			attackBoxText = buildBoxText();
		}

		return attackBoxText;
	}

	/**
	 * ����һ�����ı���
	 */
	private JTextField buildBoxText() {
		JTextField boxText = new JTextField();
		boxText.setPreferredSize(new Dimension(100, 24));
		boxText.setText("");
		boxText.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				saveBoxData();
			}

		});
		boxText.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveBoxData();
			}

		});

		return boxText;
	}

	/**
	 * �����ɫ��ǰ֡��ײ��͹���������
	 */
	private void saveBoxData() {
		if (aniFrame == null)
			return;
		saveBoxData(collisionBoxText, aniFrame.collisionBox);
		saveBoxData(attackBoxText, aniFrame.attackBox);
		repaint();
	}

	/**
	 * ���������
	 * 
	 * @param boxText
	 *            ���ı���
	 * @param box
	 *            ��
	 */
	private void saveBoxData(JTextField boxText, Rectangle box) {
		if (boxText.getText().length() <= 0) {
			boxText.setText(boxFormat.format(new Integer[] { 0, 0, 0, 0 }));
		}
		try {
			Object[] array = boxFormat.parse(boxText.getText());
			box.x = Integer.parseInt(array[0].toString());
			box.y = Integer.parseInt(array[1].toString());
			box.width = Integer.parseInt(array[0].toString()) - box.x;
			box.height = Integer.parseInt(array[0].toString()) - box.y;
		} catch (ParseException e) {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	/**
	 * ��ѡ�еľ������X�ᷭת
	 */
	public void flipX() {
		if (Animation.instance().isReadOnly())
			return;
		AffineTransform at = new AffineTransform();
		at.scale(-1.0D, 1.0D);
		AnimationEditor.undoManager.addEdit(transform(at));
	}

	/**
	 * ��ѡ�еľ������Y�ᷭת
	 */
	public void flipY() {
		if (Animation.instance().isReadOnly())
			return;

		AffineTransform at = new AffineTransform();
		at.scale(1.0D, -1.0D);
		AnimationEditor.undoManager.addEdit(transform(at));
	}

	/**
	 * ��ѡ�еľ������90����ʱ����ת
	 */
	public void rotate() {
		if (Animation.instance().isReadOnly())
			return;

		AffineTransform at = new AffineTransform();
		at.rotate(Math.toRadians(90.0D));
		AnimationEditor.undoManager.addEdit(transform(at));
	}

	/**
	 * ��ȡ����任�Ŀɳ����༭
	 * 
	 * @param at
	 */
	private UndoableEdit transform(AffineTransform at) {
		TransformSpriteUndoableEdit edit = new TransformSpriteUndoableEdit(
				Sprite.getModel().getSelections(), at);
		edit.transform(at);

		return edit;
	}

	class TransformSpriteUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = -248988277191366932L;

		ArrayList<Sprite> spriteList;
		AffineTransform at;

		@SuppressWarnings("unchecked")
		public TransformSpriteUndoableEdit(ArrayList<Sprite> spriteList,
				AffineTransform at) {
			this.spriteList = (ArrayList<Sprite>) spriteList.clone();
			this.at = at;
		}

		/**
		 * �Ծ��鼯�Ͻ��б任
		 * 
		 * @param at
		 */
		public void transform(AffineTransform at) {
			for (Sprite sprite : spriteList) {
				sprite.concatenate(at);
				Sprite.getModel().fireTableCellUpdated(
						aniFrame.getSprites().indexOf(sprite),
						Sprite.SpriteTableModel.COLUMN_TRANS);
			}
			repaint();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			try {
				transform(at.createInverse());
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			transform(at);
		}
	}

	/**
	 * ���ý�ɫ��ײ��͹������Ƿ���ʾ
	 * 
	 * @param b
	 */
	public void setShowBox(boolean b) {
		this.showBox = b;
		repaint();
	}

	/**
	 * �����Ƿ���ʾ����
	 * 
	 * @param enable
	 */
	public void setEnableGird(boolean enable) {
		this.hasGrid = enable;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		g2.setColor(Configuration.color_background);
		g2.fill(g2.getClipBounds());

		g2.setStroke(new BasicStroke(1.0F));
		if (hasGrid) {
			paintGrid(g2);
		}

		if (aniFrame != null && !Animation.aniImage().isEmpty()) {
			paintFrame(g2);
			if (showBox) {
				paintCollisionBox(g2, aniFrame, getTransform());
				paintAttackBox(g2, aniFrame, getTransform());
			}
		}
		paintAxis(g2);
	}

	/**
	 * ��������
	 * 
	 * @param g2
	 */
	private void paintGrid(Graphics2D g2) {
		int w = 160;
		int h = 160;

		g2.setColor(Configuration.color_grid);
		for (int x = 0; x <= w; x += Configuration.size_grid) {
			g2.draw(getTransform().createTransformedShape(
					new Line2D.Double(-x, -h, -x, h)));
			g2.draw(getTransform().createTransformedShape(
					new Line2D.Double(x, -h, x, h)));
		}
		for (int y = 0; y <= h; y += Configuration.size_grid) {
			g2.draw(getTransform().createTransformedShape(
					new Line2D.Double(-w, -y, w, -y)));
			g2.draw(getTransform().createTransformedShape(
					new Line2D.Double(-w, y, w, y)));
		}
	}

	/**
	 * ���ƽ�ɫ��ǰ֡
	 * 
	 * @param g2
	 */
	private void paintFrame(Graphics2D g2) {
		int id = 0;
		for (Sprite sprite : aniFrame.getSprites()) {
			boolean xor = Sprite.getModel().getSelectionModel()
					.isSelectedIndex(id);
			if (sprite.module != null) {
				Animation.aniImage().render(g2, sprite.module, sprite,
						getTransform(), xor);
			} else {
				Animation ani = sprite.templateWind.getAnimation();
				ani.getImageProvider().renderTemplateFrame(g2, getTransform(),
						ani.getFrameList().get(sprite.frameIDWind), ani,
						sprite, xor);
			}
			++id;
		}
	}

	/**
	 * ���ƽ�ɫָ��֡������ײ��
	 * 
	 * @param g2
	 * @param aniFrame
	 * @param trans
	 */
	private void paintCollisionBox(Graphics2D g2, AniFrame aniFrame,
			AffineTransform trans) {
		if (trans == null)
			return;
		g2.setColor(Configuration.color_collision_box);

		Shape shape = trans.createTransformedShape(aniFrame.collisionBox);
		if (shape != null) {
			g2.draw(shape);
		}

		for (Sprite sprite : aniFrame.getSprites()) {
			if (sprite.module == null) {
				AffineTransform newTrans = new AffineTransform(trans);
				newTrans.translate(sprite.getTranslateX(),
						sprite.getTranslateY());
				paintCollisionBox(g2, sprite.templateWind.getAnimation()
						.getFrameList().get(sprite.frameIDWind), newTrans);
			}
		}
	}

	/**
	 * ���ƽ�ɫָ��֡���й�����
	 * 
	 * @param g2
	 * @param aniFrame
	 * @param trans
	 */
	private void paintAttackBox(Graphics2D g2, AniFrame aniFrame,
			AffineTransform trans) {
		if (trans == null)
			return;
		g2.setColor(Configuration.color_attack_box);

		Shape shape = trans.createTransformedShape(aniFrame.attackBox);
		if (shape != null) {
			g2.draw(shape);
		}

		for (Sprite sprite : aniFrame.getSprites()) {
			if (sprite.module == null) {
				AffineTransform newTrans = new AffineTransform(trans);
				newTrans.translate(sprite.getTranslateX(),
						sprite.getTranslateY());
				paintAttackBox(g2, sprite.templateWind.getAnimation()
						.getFrameList().get(sprite.frameIDWind), newTrans);
			}
		}
	}

	/**
	 * ����������
	 * 
	 * @param g2
	 */
	private void paintAxis(Graphics2D g2) {
		g2.setColor(Configuration.color_axis);
		g2.draw(getTransform().createTransformedShape(
				new Line2D.Double(-160.0D, 0.0D, 160.0D, 0.0D)));
		g2.draw(getTransform().createTransformedShape(
				new Line2D.Double(0.0D, -160.0D, 0.0D, 160.0D)));
	}
}
