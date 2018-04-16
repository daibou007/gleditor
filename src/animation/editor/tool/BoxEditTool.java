package animation.editor.tool;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import animation.editor.ActorViewer;
import animation.editor.AnimationEditor;
import animation.world.AniFrame;

/**
 * ��༭����
 * 
 * <p>
 * ��סALT��, ����Թ��������; ��סCTRL��, �������ײ�����
 * <li>����������, �Թ��������ײ�����ѡ��</li>
 * <li>ALT+��������ק, ���ɹ�����</li>
 * <li>ALT+�����, �ƶ�������</li>
 * <li>CTRL+��������ק, ������ײ��</li>
 * <li>CTRL+�����, �ƶ���ײ��</li>
 * <li>SHIFT+��������ק, �ı���ײ���ѡ���Ĵ�С</li>
 * <li>��������ק, �ƶ���ײ���ѡ���</li>
 * 
 * @author ����
 * @time 2012-9-17
 */
public class BoxEditTool extends ToolAdapter {
	/**
	 * ��һ������λ��
	 */
	private Point lastMousePoint;
	/**
	 * ���Ͽɳ����༭
	 */
	private CompoundEdit compoundEdit;
	/**
	 * ��ɫ��ͼ
	 */
	private ActorViewer viewer;
	/**
	 * ѡ��Ŀ�
	 */
	private Rectangle selectedBox;

	public BoxEditTool(ActorViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastMousePoint = e.getPoint();
		if (viewer.getFrame() == null)
			return;
		if (compoundEdit == null) {
			compoundEdit = new CompoundEdit();
		}
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
			selectBox(lastMousePoint);
		}
	}

	/**
	 * ѡ���
	 * 
	 * @param p
	 */
	private void selectBox(Point p) {
		selectedBox = null;
		AniFrame frame = viewer.getFrame();
		if (inBox(frame.collisionBox, p)) {
			selectedBox = frame.collisionBox;
		}
		if (inBox(frame.attackBox, p)
				&& (selectedBox == null || frame.collisionBox
						.contains(frame.attackBox))) {
			selectedBox = frame.attackBox;
		}
	}

	/**
	 * ���Ƿ��ڿ���
	 * 
	 * @param box
	 * @param p
	 */
	private boolean inBox(Rectangle box, Point p) {
		return box != null && box.contains(p);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int dx = e.getX() - lastMousePoint.x;
		int dy = e.getY() - lastMousePoint.y;

		if ((dx == 0 && dy == 0) || viewer.getFrame() == null)
			return;
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
			if (e.isAltDown()) {
				/**
				 * ���ﲻ����lastMousePoint, ����������קÿ�����´���һ������Ŀ򸲸Ǿɿ�<br>
				 * ��MoveModuleTool��������������, ����ᴴ��������п�
				 */
				getCompoundEdit().addEdit(newAttackBox(dx, dy));
			} else if (e.isControlDown()) {
				getCompoundEdit().addEdit(newCollisionBox(dx, dy));
			} else if (e.isShiftDown()) {
				getCompoundEdit().addEdit(growBox(dx, dy));
				lastMousePoint = e.getPoint();
			} else {
				getCompoundEdit().addEdit(moveBox(dx, dy));
				lastMousePoint = e.getPoint();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (compoundEdit != null && compoundEdit.isSignificant()) {
			compoundEdit.end();
			AnimationEditor.undoManager.addEdit(compoundEdit);
		}
		compoundEdit = null;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (viewer.getFrame() == null)
			return;
		int dx = 0;
		int dy = 0;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
			return;
		case KeyEvent.VK_UP:
			dy = -1;
			break;
		case KeyEvent.VK_DOWN:
			dy = 1;
			break;
		case KeyEvent.VK_LEFT:
			dx = -1;
			break;
		case KeyEvent.VK_RIGHT:
			dx = 1;
			break;
		default:
			return;
		}
		if (e.isAltDown()) {
			selectedBox = viewer.getFrame().attackBox;
		} else if (e.isControlDown()) {
			selectedBox = viewer.getFrame().collisionBox;
		} else {
			return;
		}
		moveBox(dx, dy);
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		mouseReleased(null);
	}

	private UndoableEdit newCollisionBox(int dx, int dy) {
		Rectangle box = new Rectangle(lastMousePoint, new Dimension(dx, dy));
		NewCollisionBoxUndoableEdit edit = new NewCollisionBoxUndoableEdit(
				viewer.getFrame(), box);
		viewer.getFrame().collisionBox = box;
		selectedBox = box;
		viewer.loadBoxData();
		viewer.repaint();

		return edit;
	}

	class NewCollisionBoxUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 8664378692390635918L;

		private AniFrame frame;
		private Rectangle box;
		private Rectangle backupBox;

		public NewCollisionBoxUndoableEdit(AniFrame frame, Rectangle box) {
			this.frame = frame;
			this.box = box;
			backupBox = frame.collisionBox;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			frame.collisionBox = backupBox;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			frame.collisionBox = box;
		}
	}

	private UndoableEdit newAttackBox(int dx, int dy) {
		Rectangle box = new Rectangle(lastMousePoint, new Dimension(dx, dy));
		NewAttackBoxUndoableEdit edit = new NewAttackBoxUndoableEdit(
				viewer.getFrame(), box);
		viewer.getFrame().attackBox = box;
		selectedBox = box;
		viewer.loadBoxData();
		viewer.repaint();

		return edit;
	}

	class NewAttackBoxUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 8664378692390635918L;

		private AniFrame frame;
		private Rectangle box;
		private Rectangle backupBox;

		public NewAttackBoxUndoableEdit(AniFrame frame, Rectangle box) {
			this.frame = frame;
			this.box = box;
			backupBox = frame.attackBox;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			frame.attackBox = backupBox;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			frame.attackBox = box;
		}
	}

	/**
	 * �ı���С
	 * 
	 * @param dx
	 * @param dy
	 */
	private UndoableEdit growBox(int dx, int dy) {
		GrowBoxUndoableEdit edit = new GrowBoxUndoableEdit(selectedBox, dx, dy);
		edit.grow();
		return edit;
	}

	class GrowBoxUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 8119396552073254384L;

		private Rectangle box;
		private int dx;
		private int dy;

		public GrowBoxUndoableEdit(Rectangle box, int dx, int dy) {
			if (dx + box.width < 0)
				dx = -box.width;
			if (dy + box.height < 0)
				dy = -box.height;
			this.box = box;
			this.dx = dx;
			this.dy = dy;
		}

		void grow() {
			grow(dx, dy);
		}

		void grow(int dx, int dy) {
			box.setSize(box.width + dx, box.height + dy);
			viewer.loadBoxData();
			viewer.repaint();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			grow(-dx, -dy);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			grow(dx, dy);
		}
	}

	/**
	 * �ƶ���
	 * 
	 * @param dx
	 * @param dy
	 */
	private UndoableEdit moveBox(int dx, int dy) {
		MoveBoxUndoableEdit edit = new MoveBoxUndoableEdit(selectedBox, dx, dy);
		edit.move();

		return edit;
	}

	class MoveBoxUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 4386436014515238796L;

		private Rectangle box;
		private int dx;
		private int dy;

		public MoveBoxUndoableEdit(Rectangle box, int dx, int dy) {
			this.box = box;
			this.dx = dx;
			this.dy = dy;
		}

		void move() {
			move(dx, dy);
		}

		void move(int dx, int dy) {
			box.translate(dx, dy);
			viewer.loadBoxData();
			viewer.repaint();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			move(-dx, -dy);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			move(dx, dy);
		}
	}

	/**
	 * ���Ͽɳ����༭
	 */
	private CompoundEdit getCompoundEdit() {
		if (compoundEdit == null) {
			compoundEdit = new CompoundEdit();
		}

		return compoundEdit;
	}
}
