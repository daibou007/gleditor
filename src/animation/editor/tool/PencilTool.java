package animation.editor.tool;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.undo.CompoundEdit;

import animation.editor.AnimationEditor;
import animation.editor.Configuration;
import animation.editor.ScalableViewer;
import animation.world.Animation;
import animation.world.Module;

/**
 * ���ʹ���
 * 
 * <p>
 * <li>ALT+��굥��, ʰȡ��ɫ</li>
 * <li>ALT+�����ק, ʰȡ��ɫ</li>
 * <li>��굥��, ����ֱ��</li>
 * <li>�����ק, ����ֱ��</li>
 * 
 * @author ����
 * @time 2012-9-15
 */
public class PencilTool extends ToolAdapter {
	/**
	 * ���ʵ���ɫ
	 */
	private Color color;
	/**
	 * ��һ������λ��
	 */
	private Point lastMousePoint;
	/**
	 * ���Ͽɳ����༭
	 */
	private CompoundEdit compoundEdit;

	public PencilTool(ScalableViewer viewer) {
		super(viewer);
	}

	@Override
	public void beginSession() {
		if (color == null) {
			color = Animation.aniImage().getColor(0, 0);
		}
		Module.getModel().getSelectionModel().clearSelection();
		getViewer().repaint();
	}

	@Override
	public void endSession() {
		Animation.aniImage().flatImage();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		lastMousePoint = e.getPoint();
		if (e.isAltDown()) {
			color = Animation.aniImage().getColor(e.getX(), e.getY());
		} else {
			AnimationEditor.undoManager.addEdit(Animation.aniImage().addLine2D(
					new Line2D.Double(e.getPoint(), e.getPoint()), color));
			getViewer().repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (e.isAltDown()) {
			color = Animation.aniImage().getColor(e.getX(), e.getY());
		} else {
			getCompoundEdit()
					.addEdit(
							Animation
									.aniImage()
									.addLine2D(
											new Line2D.Double(lastMousePoint, e
													.getPoint()),
											(e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ? color
													: Configuration.color_background));
			getViewer().repaint();
		}
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
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
