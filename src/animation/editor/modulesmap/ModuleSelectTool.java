package animation.editor.modulesmap;

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import animation.editor.ModulePane;
import animation.editor.ScalableViewer;
import animation.editor.tool.ToolAdapter;
import animation.world.Animation;
import animation.world.Module;

/**
 * ģ��ѡ�񹤾�
 * 
 * @author ����
 * @time 2012-9-15
 */
public class ModuleSelectTool extends ToolAdapter {
	private static boolean isSelect = false;
	public static int preModuleID;

	public ModuleSelectTool(ScalableViewer viewer) {
		super(viewer);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (ModuleMapSelectTool.isSelect() && e.getClickCount() == 2
				&& SwingUtilities.isLeftMouseButton(e)) {
			/**
			 * �޸Ļ����µ�ģ��ӳ���ϵ
			 */
			int id = e.getX() / ModuleSrcViewer.MODULE_WIDTH;
			Module module = Animation.instance().getModuleList().get(id);
			if (id == preModuleID) {
				isSelect = !isSelect;
			}

			Module.getModel().addSelectObject(module);
			getViewer().repaint();
			preModuleID = id;

			int[] tempMapData = new int[2];
			tempMapData[0] = ModuleMapSelectTool.currentModuleID;
			tempMapData[1] = id;
			boolean findIt = false;
			ModulesMapData modulesMapData = ModulesStylesData
					.getModulesMapData();
			for (int i = 0; i < modulesMapData.getModulesMapSize(); ++i) {
				if (tempMapData[0] != modulesMapData.getModulesMapSrcData(i))
					continue;
				modulesMapData.setModulesMapData(tempMapData, i);
				findIt = true;
				break;
			}
			if (!findIt)
				modulesMapData.addModulesMapData(tempMapData);
			ModulePane.mapJScrollPane.repaint();
			ModulePane.frameJScrollPane.repaint();
		}
	}

	public static boolean isSelect() {
		return isSelect;
	}
}
