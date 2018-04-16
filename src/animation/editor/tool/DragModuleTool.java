package animation.editor.tool;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.TransferHandler;

import animation.editor.ScalableViewer;
import animation.world.Animation;
import animation.world.Module;

/**
 * ��קģ�鹤��
 * 
 * @author ����
 * @time 2012-9-15
 */
public class DragModuleTool extends ToolAdapter {
	/**
	 * ѡ��ģ�鹤��
	 */
	private SelectModuleTool tool;
	/**
	 * ��갴�µ�λ��, ����ѡ��ģ��
	 */
	private Point lastMousePoint;

	public DragModuleTool(ScalableViewer viewer) {
		super(viewer);
		tool = new SelectModuleTool(getViewer());
	}

	@Override
	public void beginSession() {
		/**
		 * ��ʼ��קģ�鵽��ɫ��ͼ��, ��ʱģ����ͼ�����Զ�����
		 */
		viewer.setAutoscrolls(false);
	}

	@Override
	public void endSession() {
		viewer.setAutoscrolls(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		tool.mouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		tool.mousePressed(e);
		lastMousePoint = e.getPoint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (Module.getModel().getSelection() != null
				&& Animation.instance().getModuleAtPoint(lastMousePoint) != null) {
			viewer.getTransferHandler().exportAsDrag(getViewer(), e,
					TransferHandler.COPY);
		}
	}
}
