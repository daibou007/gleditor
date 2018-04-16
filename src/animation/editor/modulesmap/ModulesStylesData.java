package animation.editor.modulesmap;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.jdom2.Element;

import animation.editor.SelectionTableModel;
import animation.world.Animation;
import animation.world.Prototype;

/**
 * ģ����ʽ����
 * 
 * @author ����
 * @time 2012-9-19
 */
public class ModulesStylesData {
	private static SelectionTableModel<ModulesMapData> model;

	private static ArrayList<ModulesMapData> modulesMapData;
	public static int modulesStylesIndex;

	/**
	 * ���ģ����ʽ����
	 */
	public static void clear() {
		modulesMapData = null;
		getModel().fireTableDataChanged();
	}

	/**
	 * ��ȡ���µ�ģ����ʽ����
	 */
	public static ModulesMapData getModulesMapData() {
		return getModulesMapData(modulesStylesIndex);
	}

	/**
	 * ��ȡģ����ʽ����
	 * 
	 * @param n
	 */
	public static ModulesMapData getModulesMapData(int n) {
		return (n < 0 || n >= modulesMapData.size()) ? null : modulesMapData
				.get(n);
	}

	/**
	 * ��ȡģ����ʽ����
	 */
	public static int getModulesStylesNumber() {
		return modulesMapData == null ? 0 : modulesMapData.size();
	}

	/**
	 * ʹ��ָ��ģ����ʽ
	 * 
	 * @param index
	 */
	public static void setModulesStyles(int index) {
		modulesStylesIndex = index;
	}

	/**
	 * ���ģ����ʽ
	 * 
	 * @param modulesMapName
	 *            ģ��ӳ������
	 */
	public static void addModulesStyles(String modulesMapName) {
		if (modulesMapData != null) {
			modulesMapData = new ArrayList<ModulesMapData>();
		}

		ModulesMapData mapData = new ModulesMapData();
		mapData.setMapName(modulesMapName);
		modulesMapData.add(mapData);
		modulesStylesIndex = modulesMapData.size() - 1;
	}

	/**
	 * ���ģ����ʽ
	 * 
	 * @param modulesMapName
	 *            ģ��ӳ������
	 * @param mapData
	 *            ӳ������
	 */
	public static void addModulesStyles(String modulesMapName,
			ModulesMapData mapData) {
		modulesMapData.add(mapData);
		mapData.setMapName(modulesMapName);
		modulesStylesIndex = modulesMapData.size() - 1;
	}

	/**
	 * ����ģ����ʽ��ģ��ӳ������
	 * 
	 * @param modulesMapName
	 * @param index
	 */
	public static void setModulesMapName(String modulesMapName, int index) {
		if (modulesMapData != null && modulesMapData.get(index) != null) {
			modulesMapData.get(index).setMapName(modulesMapName);
		} else {
			addModulesStyles(modulesMapName);
		}
	}

	/**
	 * �Ƴ�ģ����ʽ
	 * 
	 * @param index
	 */
	public static void removeModulesStyles(int index) {
		if (index < 0 || index >= modulesMapData.size())
			return;
		modulesMapData.remove(index);
	}

	/**
	 * ��XML�ļ�����ģ����ʽ����
	 * 
	 * @param root
	 */
	public static void loadMapFromXml(Element root) {
		Element child;
		List<Element> children;
		if ((child = root.getChild("ModulesStyles")) != null
				&& (children = child.getChildren("ModulesMap")) != null) {
			modulesMapData = new ArrayList<ModulesMapData>();

			for (Element moduleMapElement : children) {
				ModulesMapData mapData = new ModulesMapData();
				mapData.setMapName(moduleMapElement.getAttributeValue("name"));

				for (Element moduleIDElement : moduleMapElement
						.getChildren("ModuleID")) {
					mapData.addModulesMapData(Integer.parseInt(moduleIDElement
							.getAttributeValue("source")), Integer
							.parseInt(moduleIDElement
									.getAttributeValue("destination")));
				}
				modulesMapData.add(mapData);
			}
			getModel().setDataArrayList(
					new ArrayList<ModulesMapData>(modulesMapData));
		}
	}

	/**
	 * ��ģ����ʽת�ɳ�XML����
	 */
	public static Element saveMapToXml() {
		Element result = new Element("ModulesStyles");

		for (ModulesMapData mapData : modulesMapData) {
			Element modulesMapElement = new Element("ModulesMap");
			modulesMapElement.setAttribute("name", mapData.getMapName());

			for (int[] data : mapData.getMapData()) {
				Element moduleIDElement = new Element("ModuleID");
				moduleIDElement.setAttribute("source",
						Integer.toString(data[0]));
				moduleIDElement.setAttribute("destination",
						Integer.toString(data[1]));
				modulesMapElement.addContent(moduleIDElement);
			}
			result.addContent(modulesMapElement);
		}

		return result;
	}

	public static SelectionTableModel<ModulesMapData> getModel() {
		if (model == null) {
			model = new ModulesMapTableModel(new Prototype<ModulesMapData>() {
				private int count = 1;

				@Override
				public ModulesMapData clone() {
					ModulesStylesData.addModulesStyles("Untitled" + count);
					++count;
					return ModulesStylesData.getModulesMapData();
				}

				@Override
				public ModulesMapData clone(ModulesMapData modulesMapData) {
					ModulesStylesData.addModulesStyles("copy of "
							+ modulesMapData.getMapName());

					ModulesMapData result = ModulesStylesData
							.getModulesMapData();
					result.setMapData(modulesMapData.getMapData());

					return result;
				}

			});
		}

		return model;
	}

	public static void setTable(JTable table) {
		getModel().setTable(table);
	}

	static class ModulesMapTableModel extends
			SelectionTableModel<ModulesMapData> {
		static final String[] columnNames = { "Name", "MapId" };
		static final Class<?>[] columnClasses = { String.class, Integer.class };
		static final boolean[] columnEditables = { true, false };

		public static final int COLUMN_NAME = 0;
		public static final int COLUMN_MAP_ID = 1;

		private static final long serialVersionUID = -5926235825615195502L;

		public ModulesMapTableModel(Prototype<ModulesMapData> prototype) {
			super(null, prototype);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == COLUMN_NAME) {
				ModulesStylesData.setModulesMapName((String) aValue, rowIndex);
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case COLUMN_NAME:
				ModulesMapData map = ModulesStylesData
						.getModulesMapData(rowIndex);
				return map == null ? null : map.getMapName();
			case COLUMN_MAP_ID:
				return rowIndex;
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (Animation.instance().isReadOnly())
				return false;

			return columnEditables[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}

	}
}
