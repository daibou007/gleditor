package animation.editor.modulesmap;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import animation.editor.ModulePane;
import animation.editor.ScalableViewer;
import animation.editor.tool.ToolAdapter;
import animation.world.Animation;
import animation.world.Module;

/**
 * ģ��ӳ��ѡ�񹤾�
 * 
 * @author ����
 * @time 2012-9-15
 */
public class ModuleMapSelectTool extends ToolAdapter {
	private static boolean isSelect = false;
	/**
	 * ��ǰѡ����ԭʼģ��
	 */
	public static int currentModuleID = 0;

	public ModuleMapSelectTool(ScalableViewer viewer) {
		super(viewer);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 1) {
				int id = e.getX() / ModuleSrcViewer.MODULE_WIDTH;
				Module module = Animation.instance().getModuleList().get(id);
				Module.getModel().selectObject(module);
				getViewer().repaint();
				isSelect = true;
				currentModuleID = id;
			} else if (e.getClickCount() == 2) {
				/**
				 * �Ƴ���ѡ��ģ���ӳ��
				 */
				int id = e.getX() / ModuleSrcViewer.MODULE_WIDTH;
				ModulesMapData modulesMapData = ModulesStylesData
						.getModulesMapData();
				for (int i = 0; i < modulesMapData.getModulesMapSize(); ++i) {
					if (id != modulesMapData.getModulesMapSrcData(i))
						continue;
					modulesMapData.removeMapData(i);
				}
				ModulePane.frameJScrollPane.repaint();
				getViewer().repaint();
			} else {
				isSelect = false;
			}
		}
	}

	/**
	 * �Ƿ�ѡ��һ��ģ��
	 */
	public static boolean isSelect() {
		return isSelect;
	}
}
