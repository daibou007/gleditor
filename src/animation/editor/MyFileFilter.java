package animation.editor;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.filechooser.FileFilter;

/**
 * JFileChooser�õ��ļ���չ��������
 * 
 * @author ����
 * 
 * @time 2012-9-14
 * 
 */
public class MyFileFilter extends FileFilter {
	/**
	 * [��չ��/������]
	 */
	private HashMap<String, FileFilter> extensionMap;
	/**
	 * �ļ�����
	 */
	private String description;
	/**
	 * �����Ĺ���������: "���������� (��չ��,... ��չ��)"
	 */
	private String fullDescription;
	/**
	 * �������������Ƿ�׷����չ���б�
	 */
	private boolean useExtensionsInDescription = true;

	public MyFileFilter() {
		extensionMap = new HashMap<String, FileFilter>();
	}

	public MyFileFilter(String extension) {
		this(extension, null);
	}

	public MyFileFilter(String extension, String description) {
		if (extension != null)
			addExtension(extension);
		if (description != null)
			setDescription(description);
	}

	public MyFileFilter(String[] filters) {
		this(filters, null);
	}

	public MyFileFilter(String[] filters, String description) {
		if (filters != null)
			for (String filter : filters) {
				addExtension(filter);
			}

		if (description != null)
			setDescription(description);
	}

	@Override
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory())
				return true;
			String extension = getExtension(f);
			if (extension != null && extensionMap.containsKey(extension))
				return true;
		}
		return false;
	}

	/**
	 * �����չ��, ����ת��ΪСд
	 * 
	 * @param extension
	 */
	public void addExtension(String extension) {
		if (extensionMap == null) {
			extensionMap = new HashMap<String, FileFilter>();
		}
		extensionMap.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * ��ȡ�ļ�����չ��
	 * 
	 * @param f
	 * @return ��չ��
	 */
	public String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int index = filename.lastIndexOf(".");
			if (index > 0 && index < filename.length() - 1)
				return filename.substring(index + 1);
		}

		return null;
	}

	/**
	 * ���ù���������
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}

	@Override
	public String getDescription() {
		if (fullDescription == null) {
			if (description == null || isExtensionListInDescription()) {
				StringBuilder sb = new StringBuilder();
				sb.append(description == null ? "" : description).append("(");
				Iterator<String> iter = extensionMap.keySet().iterator();
				if (iter.hasNext()) {
					sb.append(".").append(iter.next());
					while (iter.hasNext()) {
						sb.append(", .").append(iter.next());
					}
				}
				sb.append(")");
				fullDescription = sb.toString();
			} else {
				fullDescription = description;
			}
		}

		return fullDescription == null ? "" : fullDescription;
	}

	/**
	 * �����Ƿ����չ���б���������������
	 * 
	 * @param b
	 */
	public void setExtensionListInDescription(boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * �Ƿ����չ���б���������������
	 */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}
}
