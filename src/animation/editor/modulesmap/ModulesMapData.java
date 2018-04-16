package animation.editor.modulesmap;

import java.util.ArrayList;

/**
 * ģ��ӳ������
 * 
 * @author ����
 * @time 2012-9-19
 */
public class ModulesMapData {
	private String modulesMapName;
	private ArrayList<int[]> modulesMapData;

	public ModulesMapData() {
		modulesMapData = new ArrayList<int[]>();
	}

	/**
	 * ����ģ��ӳ������
	 * 
	 * @param mapName
	 */
	public void setMapName(String mapName) {
		modulesMapName = mapName;
	}

	/**
	 * ��ȡģ��ӳ������
	 */
	public String getMapName() {
		return modulesMapName;
	}

	/**
	 * ����ģ��ӳ������
	 * 
	 * @param data
	 */
	public void setMapData(ArrayList<int[]> data) {
		modulesMapData = new ArrayList<int[]>(data);
	}

	/**
	 * ��ȡģ��ӳ������
	 */
	public ArrayList<int[]> getMapData() {
		return modulesMapData;
	}

	/**
	 * ���ģ��ӳ������
	 * 
	 * @param src
	 * @param des
	 */
	public void addModulesMapData(int src, int des) {
		addModulesMapData(new int[] { src, des });
	}

	/**
	 * ���ģ��ӳ������
	 * 
	 * @param mapData
	 */
	public void addModulesMapData(int[] mapData) {
		modulesMapData.add(mapData);
	}

	/**
	 * ��ȡģ��ӳ������, ����Ǹ��������Ǳ���
	 * 
	 * @param index
	 */
	public int[] getModulesMapData(int index) {
		int[] src = modulesMapData.get(index);
		int[] des = new int[src.length];
		System.arraycopy(src, 0, des, 0, src.length);

		return des;
	}

	/**
	 * ��ȡģ��ӳ���ԭʼ����
	 * 
	 * @param index
	 */
	public int getModulesMapSrcData(int index) {
		return modulesMapData.get(index)[0];
	}

	/**
	 * ��ȡģ��ӳ���Ŀ������
	 * 
	 * @param index
	 */
	public int getModulesMapDesData(int index) {
		return modulesMapData.get(index)[1];
	}

	/**
	 * ����ģ��ӳ������
	 * 
	 * @param data
	 * @param index
	 */
	public void setModulesMapData(int[] data, int index) {
		modulesMapData.set(index, data);
	}

	/**
	 * �Ƴ�ģ��ӳ������
	 * 
	 * @param index
	 */
	public void removeMapData(int index) {
		modulesMapData.remove(index);
	}

	/**
	 * ��ȡģ��ӳ�����ݸ���
	 */
	public int getModulesMapSize() {
		return modulesMapData != null ? modulesMapData.size() : 0;
	}
}
