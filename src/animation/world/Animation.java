package animation.world;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import animation.editor.modulesmap.ModulesStylesData;

/**
 * ����
 * 
 * @author ����
 * 
 * @time 2012-9-14
 * 
 */
public class Animation implements Cloneable, Serializable {
	private static final long serialVersionUID = -7674134598482084258L;

	private static Animation instance;
	private static ImageProvider imageProvider;

	private String xmlPath;
	private long lastModified;
	private boolean isReadOnly;
	private ArrayList<Module> moduleList;
	private ArrayList<AniFrame> frameList;
	private ArrayList<AniAction> actionList;
	private ArrayList<Template> templateList;
	private ImageProvider _imageProvider;

	public Animation(String path) {
		this();
		loadAnimationDataFromXml(path);
	}

	public Animation() {
		moduleList = new ArrayList<Module>();
		templateList = new ArrayList<Template>();
		frameList = new ArrayList<AniFrame>();
		actionList = new ArrayList<AniAction>();
		_imageProvider = new ImageProvider();
	}

	/**
	 * ��ȡ�ö���ͼƬ�ṩ��
	 */
	public ImageProvider getImageProvider() {
		return _imageProvider;
	}

	/**
	 * ��ȡ����ģ���б�
	 */
	public ArrayList<Module> getModuleList() {
		return moduleList;
	}

	/**
	 * ��ȡ����ģ���б�
	 */
	public ArrayList<Template> getTemplateList() {
		return templateList;
	}

	/**
	 * ��ȡ����֡�б�
	 */
	public ArrayList<AniFrame> getFrameList() {
		return frameList;
	}

	/**
	 * ��ȡ���������б�
	 */
	public ArrayList<AniAction> getActionList() {
		return actionList;
	}

	/**
	 * ��ȡ��ѡ��ģ�� , ���������Сģ��
	 * 
	 * @param p
	 */
	public Module getModuleAtPoint(Point p) {
		Module result = null;
		for (Module module : moduleList) {
			if (!module.contains(p))
				continue;
			if (result != null && module.contains(result))
				continue;
			result = module;
		}

		return result;
	}

	/**
	 * ��ȡģ��
	 * 
	 * @param id
	 */
	public Module getModule(int id) {
		return moduleList.get(id);
	}

	/**
	 * ��ȡģ��
	 * 
	 * @param id
	 */
	public Template getTemplate(int id) {
		return templateList.get(id);
	}

	/**
	 * ��ȡ�����ļ�·��
	 */
	public String getFilePath() {
		return xmlPath;
	}

	/**
	 * ����ָ���Ķ���
	 * 
	 * @param path
	 */
	public void reload(String path) {
		actionList.clear();
		frameList.clear();
		templateList.clear();
		moduleList.clear();
		xmlPath = null;
		_imageProvider.clear();
		loadAnimationDataFromXml(path);
	}

	/**
	 * ��ȡ��һ���޸�����
	 */
	public long getLastModifed() {
		return lastModified;
	}

	/**
	 * ������һ���޸�����
	 * 
	 * @param lastModifed
	 */
	public void setLastModifed(long lastModifed) {
		this.lastModified = lastModifed;
	}

	/**
	 * ��⶯���ļ��Ƿ�ֻ��, ���Ƿ񲻿��޸�
	 */
	public void checkReadOnly() {
		if (xmlPath == null) {
			isReadOnly = false;
		} else {
			isReadOnly = !new File(xmlPath).canWrite();
		}
	}

	/**
	 * �����ļ��Ƿ�ֻ��
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	public boolean isModified() {
		if (aniImage().isModifed())
			return true;
		if (xmlPath == null && moduleList.isEmpty() && templateList.isEmpty()
				&& frameList.isEmpty() && actionList.isEmpty())
			return false;
		if (xmlPath == null)
			return true;

		StringWriter writer = new StringWriter();
		saveAnimationToXml(writer);

		boolean isModifed = false;
		try {
			LineNumberReader fileReader = new LineNumberReader(new FileReader(
					xmlPath));
			LineNumberReader xmlReader = new LineNumberReader(new StringReader(
					writer.toString()));

			String fileLine = fileReader.readLine();
			String xmlLine = xmlReader.readLine();

			while (fileLine != null) {
				if (!fileLine.equals(xmlLine)) {
					isModifed = true;
					break;
				}

				fileLine = fileReader.readLine();
				xmlLine = xmlReader.readLine();
			}
			if (xmlLine != null) {
				isModifed = true;
			}
			fileReader.close();
			xmlReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return isModifed;
	}

	/**
	 * �Զ�����������
	 * 
	 * @param d
	 *            ��������
	 */
	public void scale(double d) {
		Module.scale(d);
		AniFrame.scale(d);
		AniAction.scale(d);
	}

	/**
	 * ��XML�ļ����붯������
	 * 
	 * @param filePath
	 */
	private void loadAnimationDataFromXml(String filePath) {
		try {
			File xmlFile = new File(filePath);
			xmlPath = xmlFile.getAbsolutePath();
			lastModified = xmlFile.lastModified();
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new FileInputStream(xmlFile));
			Element root = doc.getRootElement();
			if (!root.getName().equals("Animation")) {
				System.out.println("Not a animation file");
				return;
			}
			Module.loadModuleFromXml(this, root);
			Template.loadTemplateFromXml(this, root);
			ModulesStylesData.loadMapFromXml(root);
			AniFrame.loadFrameFromXml(this, root);
			AniAction.loadActionFromXml(this, root);
			_imageProvider.loadImageFile(this, root.getAttributeValue("image"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��XML�ļ����붯������
	 * 
	 * @param filePath
	 */
	public void loadAnimationFromXml(String filename) {
		try {
			File xmlFile = new File(filename);
			xmlPath = xmlFile.getAbsolutePath();
			lastModified = xmlFile.lastModified();
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new FileInputStream(xmlFile));
			Element root = doc.getRootElement();
			if (!root.getName().equals("Animation")) {
				System.out.println("Not a animation file");
				return;
			}
			Module.loadModuleFromXml(root);
			Template.loadTemplateFromXml(root);
			ModulesStylesData.loadMapFromXml(root);
			AniFrame.loadFrameFromXml(root);
			AniAction.loadActionFromXml(root);
			aniImage().loadImageFile(root.getAttributeValue("image"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �Ѷ���ת����XML�ļ�
	 * 
	 * @param filename
	 */
	public void saveAnimationToXml(String filename) {
		try {
			FileWriter writer = new FileWriter(filename);
			saveAnimationToXml(writer);
			aniImage().saveImage(aniImage().getImagePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �Ѷ���ת����XML����, �����ָ������
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void saveAnimationToXml(Writer writer) {
		try {
			Element root = new Element("Animation");
			root.setAttribute("image", aniImage().getImagePath() == null ? ""
					: aniImage().getImagePath());
			root.setAttribute("version", "4");
			root.addContent(Module.saveModuleToXml());
			root.addContent(AniFrame.saveFrameToXml());
			root.addContent(AniAction.saveActionToXml());
			root.addContent(Template.saveTemplateToXml());
			if (ModulesStylesData.getModulesStylesNumber() > 0) {
				root.addContent(ModulesStylesData.saveMapToXml());
			}
			Document doc = new Document(root);
			Format format = Format.getPrettyFormat();
			XMLOutputter outputter = new XMLOutputter(format);
			outputter.output(doc, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��յ�ǰ�༭�������Ķ���
	 */
	public static Animation clear() {
		instance.actionList.clear();
		instance.frameList.clear();
		instance.templateList.clear();
		instance.moduleList.clear();
		instance.xmlPath = null;
		aniImage().clear();
		Module.getModel().fireTableDataChanged();
		AniFrame.getModel().fireTableDataChanged();
		AniAction.getModel().fireTableDataChanged();
		ModulesStylesData.clear();

		return instance;
	}

	public static Animation instance() {
		if (instance == null) {
			instance = new Animation();
		}

		return instance;
	}

	public static ImageProvider aniImage() {
		if (imageProvider == null) {
			imageProvider = new ImageProvider();
		}

		return imageProvider;
	}
}
