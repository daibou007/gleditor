package animation.editor.tool;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.undo.CompoundEdit;

import animation.editor.AnimationEditor;
import animation.editor.ScalableViewer;
import animation.world.Animation;
import animation.world.Module;

/**
 * �ƶ��п鹤��, �����������кõ�ͼ��
 * 
 * <p>
 * <li>����Ҽ�����, ȡ��ѡ���ͼ��</li>
 * <li>��������ק���߰������, �ƶ�ͼ��</li>
 * <li>�����, �ƶ�ͼ��</li>
 * <li>Delete��, ɾ��ͼ��</li>
 * 
 * @author ����
 * @time 2012-9-15
 */
public class MoveImageTool extends ToolAdapter {
	/**
	 * ѡ��ģ�鹤��
	 */
	private SelectModuleTool tool;
	/**
	 * ��һ�����λ��
	 */
	private Point lastMousePoint;
	/**
	 * ���Ͽɳ����༭
	 */
	private CompoundEdit compoundEdit;

	public MoveImageTool(ScalableViewer viewer) {
		super(viewer);
		tool = new SelectModuleTool(getViewer());
	}

	@Override
	public void endSession() {
		Animation.aniImage().flatImage();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		tool.mouseClicked(e);
		if (e.getButton() == MouseEvent.BUTTON3) {
			Module.getModel().getSelectionModel().clearSelection();
			getViewer().repaint();
		}
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		tool.mousePressed(e);
		lastMousePoint = e.getPoint();
		if (compoundEdit == null) {
			compoundEdit = new CompoundEdit();
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
	public void mouseDragged(MouseEvent e) {
		int dx = e.getX() - lastMousePoint.x;
		int dy = e.getY() - lastMousePoint.y;
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0
				&& Module.getModel().getSelection() != null) {
			moveImage(dx, dy);
		}
		lastMousePoint = e.getPoint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (Module.getModel().getSelection() == null)
			return;

		int dx = 0;
		int dy = 0;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DELETE:
			getCompoundEdit().addEdit(
					Animation.aniImage().removeImage(
							Module.getModel().getSelections()));
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
		moveImage(dx, dy);
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		mouseReleased(null);
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

	/**
	 * �ƶ�ͼƬ
	 * 
	 * @param dx
	 * @param dy
	 */
	private void moveImage(int dx, int dy) {
		getCompoundEdit().addEdit(
				Animation.aniImage().moveImage(
						Module.getModel().getSelections(), dx, dy));
	}
}
