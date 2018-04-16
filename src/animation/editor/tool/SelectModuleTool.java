package animation.editor.tool;

import java.awt.event.MouseEvent;

import animation.editor.ScalableViewer;
import animation.world.Animation;
import animation.world.Module;

/**
 * ѡ��ģ�鹤��
 * 
 * @author ����
 * 
 * @time 2012-9-20
 * 
 */
public class SelectModuleTool extends ToolAdapter {

	public SelectModuleTool(ScalableViewer viewer) {
		super(viewer);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		selectModule(e, true);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		selectModule(e, false);
	}

	/**
	 * ѡ��ģ��
	 * 
	 * @param e
	 * @param change
	 *            ǿ��ˢ��ѡ���ģ��
	 */
	public void selectModule(MouseEvent e, boolean change) {
		Module module = Module.getModel().getSelection();
		if (module == null || change || !module.contains(e.getPoint())) {
			module = Animation.instance().getModuleAtPoint(e.getPoint());
		}
		/**
		 * ��סCTRL���Ƕ�ѡ, �����ǵ�ѡ
		 */
		if (!e.isControlDown()) {
			Module.getModel().getSelectionModel().clearSelection();
		}
		if (module != null) {
			int id = module.getId();
			Module.getModel().getSelectionModel().addSelectionInterval(id, id);
		}
	}
}
