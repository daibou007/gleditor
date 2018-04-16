package animation.world;

import static animation.world.Util.round;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import animation.editor.Configuration;
import animation.editor.modulesmap.ModulesMapData;

/**
 * ͼƬ�ṩ��
 * 
 * @author ����
 * @time 2012-9-15
 */
public class ImageProvider {
	public static final Color COLOR_BACKGROUND = new Color(0, 0, 0, 0);

	private BufferedImage source;

	private BufferedImage normalDisplay;
	private BufferedImage scale1Display;
	private BufferedImage scale2Display;

	String imagePath;
	ArrayList<Line2D> paintLine = new ArrayList<Line2D>();
	ArrayList<Color> paintColor = new ArrayList<Color>();
	HashMap<Module, BufferedImage> imageLayer = new HashMap<Module, BufferedImage>();
	int mod;
	private PropertyChangeSupport support = new PropertyChangeSupport(this);

	/**
	 * ���
	 */
	public void clear() {
		mod = 0;
		source = null;
		normalDisplay = null;
		scale1Display = null;
		scale2Display = null;
		imagePath = null;
		imageLayer.clear();
		paintLine.clear();
		paintColor.clear();
		firePropertyChange("size");
	}

	/**
	 * ͼƬ�Ƿ��޸Ĺ�
	 */
	public boolean isModifed() {
		return mod > 0;
	}

	/**
	 * ��ȡͼƬ���
	 */
	public int getWidth() {
		return normalDisplay == null ? 0 : normalDisplay.getWidth();
	}

	/**
	 * ��ȡͼƬ�߶�
	 */
	public int getHeight() {
		return normalDisplay == null ? 0 : normalDisplay.getHeight();
	}

	/**
	 * ��ȡͼƬ·��
	 */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * �Ƿ�û������ͼƬ
	 */
	public boolean isEmpty() {
		return source == null;
	}

	/**
	 * ����ͼƬ
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void loadImageFile(String filename) throws IOException {
		loadImageFile(Animation.instance(), filename);
	}

	/**
	 * ��������ָ��������ͼƬ
	 * 
	 * @param ani
	 * @param filename
	 * @throws IOException
	 */
	public void loadImageFile(Animation ani, String filename)
			throws IOException {
		clear();
		File file = new File(filename);
		if (!file.exists() || !file.canRead())
			throw new FileNotFoundException("Image is not found " + filename);
		source = GraphicsUtilities.loadCompatibleImage(file.toURI().toURL());
		imagePath = filename;
		initImages();
		checkOutside(ani);
		firePropertyChange("image");
		firePropertyChange("size");
	}

	/**
	 * ��ʼ����ʾͼƬ
	 */
	private void initImages() {
		if (isEmpty())
			return;
		normalDisplay = GraphicsUtilities.createLargeImage(source, 1.0D);
		scale1Display = GraphicsUtilities.createLargeImage(source, 1.0D);
		scale2Display = GraphicsUtilities.createLargeImage(source, 1.0D);
	}

	/**
	 * ���ģ���Ƿ񳬳�ͼƬ�߽�
	 * 
	 * @param ani
	 */
	private void checkOutside(Animation ani) {
		Rectangle rect = new Rectangle(0, 0, getWidth(), getHeight());
		for (Module module : ani.getModuleList()) {
			if (!rect.contains(module)) {
				System.out.println("Module " + module.getId()
						+ " is outside of image.");
			}
		}
	}

	/**
	 * ����ͼƬ
	 * 
	 * @param filename
	 */
	public void saveImage(String filename) {
		if (isModifed()) {
			try {
				ImageIO.write(source, "PNG", new File(filename));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ͼƬ�����·���չ
	 */
	public void expandImage() {
		if (Animation.instance().isReadOnly())
			return;
		if (isEmpty())
			return;

		int width = round(getWidth() * Configuration.EXPAND_SCALE);
		int height = round(getHeight() * Configuration.EXPAND_SCALE);
		BufferedImage temp = GraphicsUtilities.createCompatibleImage(source,
				width, height);
		Graphics2D g2 = temp.createGraphics();
		g2.drawImage(source, 0, 0, null);
		g2.dispose();

		source = temp;
		initImages();
		++mod;
		firePropertyChange("size");
	}

	/**
	 * ����ͼƬ, ���ж����ͼƬ
	 */
	public void shrinkImage() {
		if (Animation.instance().isReadOnly())
			return;
		if (isEmpty())
			return;

		Rectangle rect = Module.getAllModuleBounds();
		if (rect == null)
			return;

		BufferedImage temp = GraphicsUtilities.createCompatibleImage(source,
				rect.width, rect.height);
		Graphics2D g2 = temp.createGraphics();
		g2.drawImage(source, -rect.x, -rect.y, null);
		g2.dispose();

		source = temp;
		initImages();
		if (rect.x != 0 || rect.y != 0) {
			Module.moveAllModules(-rect.x, -rect.y);
		}
		++mod;
		firePropertyChange("size");
	}

	/**
	 * ��ͼƬ�༭�������ݺϲ�
	 */
	public void flatImage() {
		boolean needFlat = false;

		if (!imageLayer.isEmpty()) {
			Graphics2D g2 = source.createGraphics();
			for (Module module : imageLayer.keySet()) {
				BufferedImage cut = imageLayer.get(module);
				g2.drawImage(cut, module.x, module.y, null);
			}
			g2.dispose();
			imageLayer.clear();
			needFlat = true;
		}
		if (!paintLine.isEmpty()) {
			Graphics2D g2 = source.createGraphics();
			g2.setComposite(AlphaComposite.Src);
			for (int i = 0; i < paintLine.size(); ++i) {
				Line2D line = paintLine.get(i);
				Color color = paintColor.get(i);
				g2.setColor(color);
				g2.draw(line);
			}
			g2.dispose();

			paintLine.clear();
			paintColor.clear();
			needFlat = true;
		}

		if (needFlat) {
			initImages();
			firePropertyChange("image");
		}
	}

	/**
	 * �Ƴ�ָ����ģ�鼰��ָ��������
	 * 
	 * @param selectedModule
	 *            ѡ���ģ��
	 */
	public UndoableEdit removeImage(ArrayList<Module> selectedModule) {
		RemoveImageUndoableEdit edit = new RemoveImageUndoableEdit(
				selectedModule);
		edit.removeImage();
		++mod;
		firePropertyChange("image");
		return edit;
	}

	class RemoveImageUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 2373405202024058373L;

		/**
		 * ģ���б�
		 */
		ArrayList<Module> moduleList;
		/**
		 * ��{@link #moduleList}һһ��Ӧ���п�ͼƬ�б�
		 */
		ArrayList<BufferedImage> imageList;

		@SuppressWarnings("unchecked")
		public RemoveImageUndoableEdit(ArrayList<Module> selectedModule) {
			moduleList = (ArrayList<Module>) selectedModule.clone();
			imageList = new ArrayList<BufferedImage>();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			for (int i = 0; i < moduleList.size(); ++i) {
				imageLayer.put(moduleList.get(i), imageList.get(i));
			}
			Animation.instance().getModuleList().addAll(moduleList);
			--mod;
			firePropertyChange("image");
		}

		public void removeImage() {
			imageList.clear();
			for (Module module : moduleList) {
				if (imageLayer.containsKey(module)) {
					imageList.add(imageLayer.remove(module));
				} else {
					imageList.add(clearImageRect(module));
				}
				Animation.instance().getModuleList().remove(module);
			}
			++mod;
			firePropertyChange("image");
		}
	}

	/**
	 * �ƶ�ͼƬ�ϵ�ָ������
	 * 
	 * @param rects
	 *            �����б�
	 * @param dx
	 * @param dy
	 */
	public UndoableEdit moveImage(ArrayList<Module> rects, int dx, int dy) {
		MoveImageUndoableEdit edit = new MoveImageUndoableEdit(rects, dx, dy);
		edit.moveImage();
		return edit;
	}

	class MoveImageUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = -5665067309121075871L;

		ArrayList<Module> moduleList;
		int dx;
		int dy;

		@SuppressWarnings("unchecked")
		public MoveImageUndoableEdit(ArrayList<Module> moduleList, int dx,
				int dy) {
			for (Module module : moduleList) {
				if (module.x + dx < 0)
					dx = -module.x;
				if (module.y + dy < 0)
					dy = -module.y;
				if (module.x + dx + module.width > getWidth())
					dx = getWidth() - module.x - module.width;
				if (module.y + dy + module.height > getHeight())
					dy = getHeight() - module.y - module.height;
			}
			this.moduleList = (ArrayList<Module>) moduleList.clone();
			this.dx = dx;
			this.dy = dy;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			moveImage();
			mod -= 2;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			moveImage(-dx, -dy);
		}

		void moveImage() {
			moveImage(dx, dy);
		}

		void moveImage(int dx, int dy) {
			for (Module module : moduleList) {
				if (!imageLayer.containsKey(module)) {
					imageLayer.put(module, clearImageRect(module));
				}
				module.translate(dx, dy);
			}
			++mod;
			firePropertyChange("image");
		}
	}

	/**
	 * ɾ��ͼƬ��ָ��������
	 * 
	 * @param rect
	 */
	BufferedImage clearImageRect(Rectangle rect) {
		BufferedImage temp = GraphicsUtilities.getSubImage(source, rect.x,
				rect.y, rect.width, rect.height);

		Graphics2D g2 = source.createGraphics();
		g2.setColor(COLOR_BACKGROUND);
		g2.setComposite(AlphaComposite.Src);
		g2.fill(rect);
		g2.dispose();
		initImages();
		return temp;
	}

	/**
	 * ��ͼƬ���һ����
	 * 
	 * @param line
	 * @param color
	 */
	public UndoableEdit addLine2D(Line2D line, Color color) {
		LineUndoableEdit edit = new LineUndoableEdit(line, color);
		edit.add();
		return edit;
	}

	class LineUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 139455781528552873L;

		Line2D line;
		Color color;

		public LineUndoableEdit(Line2D line, Color color) {
			this.line = line;
			this.color = color;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			paintLine.remove(line);
			paintColor.remove(color);
			--mod;
			firePropertyChange("image");
		}

		void add() {
			paintLine.add(line);
			paintColor.add(color);
			++mod;
			firePropertyChange("image");
		}
	}

	public void firePropertyChange(String key) {
		support.firePropertyChange(key, false, true);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		support.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		support.removePropertyChangeListener(l);
	}

	/**
	 * ʰȡͼƬ�ϵ���ɫ
	 * 
	 * @param x
	 * @param y
	 */
	public Color getColor(int x, int y) {
		return new Color(source.getRGB(x, y));
	}

	/**
	 * ��ȡ���ź��ͼƬ
	 * 
	 * @param at
	 *            ����{@link animation.editor.ScalableViewer#getTransform()},
	 *            ��������������ת
	 */
	public BufferedImage getScaleImage(AffineTransform at) {
		AffineTransform newat = new AffineTransform(at);
		if (newat.getScaleY() == 0.0D) {
			newat.concatenate(AffineTransform.getRotateInstance(-90.0D));
		}

		return getScaleImage(newat.getScaleX());
	}

	/**
	 * ��ȡ���ŵ�ͼƬ
	 * 
	 * @param scale
	 */
	public BufferedImage getScaleImage(double scale) {
		if (scale == 1.0D)
			return normalDisplay;
		if (getWidth() * scale == scale1Display.getWidth())
			return scale1Display;
		if (getWidth() * scale == scale2Display.getHeight())
			return scale2Display;
		if (getWidth() == scale1Display.getWidth()) {
			scale1Display = GraphicsUtilities.createLargeImage(source, scale);
			return scale1Display;
		}
		scale2Display = GraphicsUtilities.createLargeImage(source, scale);
		return scale2Display;
	}

	/**
	 * ��Ⱦģ��ͼƬ
	 * 
	 * @param g2
	 * @param at
	 */
	public void render(Graphics2D g2, AffineTransform at) {
		if (!isEmpty()) {
			BufferedImage display = getScaleImage(at);
			Point2D origin = at.transform(new Point(), null);

			g2.drawImage(display, (int) origin.getX(), (int) origin.getY(),
					null);
			paintLayers(g2, at);
			paintLines(g2, at);
		}
	}

	/**
	 * �����п�ͼ��
	 * 
	 * @param g2
	 * @param at
	 */
	public void paintLayers(Graphics2D g2, AffineTransform at) {
		for (Module module : imageLayer.keySet()) {
			BufferedImage cut = imageLayer.get(module);
			if (at.getScaleX() != cut.getWidth() / module.width) {
				cut = GraphicsUtilities.createLargeImage(cut, at.getScaleX());
			}

			Point2D origin = at.transform(new Point(module.x, module.y), null);
			g2.drawImage(cut, round(origin.getX()), round(origin.getY()), null);
		}
	}

	/**
	 * ����
	 * 
	 * @param g2
	 * @param at
	 */
	public void paintLines(Graphics2D g2, AffineTransform at) {
		if (!paintLine.isEmpty()) {
			BufferedImage bi = GraphicsUtilities
					.createTranslucentCompatibleImage(getWidth(), getHeight());
			Graphics2D g2D = bi.createGraphics();
			g2D.setBackground(COLOR_BACKGROUND);
			g2D.setComposite(AlphaComposite.Src);
			for (int i = 0; i < paintLine.size(); ++i) {
				Line2D line = paintLine.get(i);
				Color color = paintColor.get(i);
				g2D.setColor(color);
				g2D.draw(line);
			}
			g2D.dispose();

			g2.drawImage(bi, new AffineTransformOp(at,
					AffineTransformOp.TYPE_NEAREST_NEIGHBOR), 0, 0);
		}
	}

	/**
	 * ��Ⱦģ��
	 * 
	 * @param g2
	 * @param src
	 * @param x
	 * @param y
	 * @param at
	 * @param xor
	 */
	public void render(Graphics2D g2, Rectangle src, int x, int y,
			AffineTransform at, boolean xor) {
		render(g2, src, AffineTransform.getTranslateInstance(x, y), at, xor);
	}

	/**
	 * ��Ⱦ����
	 * 
	 * @param g2
	 * @param src
	 * @param spriteAt
	 * @param at
	 * @param xor
	 */
	public void render(Graphics2D g2, Rectangle src, AffineTransform spriteAt,
			AffineTransform at, boolean xor) {
		if (!isEmpty()) {
			BufferedImage bi;
			if (!imageLayer.isEmpty() || !paintLine.isEmpty()) {
				bi = GraphicsUtilities.createCompatibleImage(source,
						source.getWidth(), source.getHeight());
				Graphics2D g2D = bi.createGraphics();
				g2D.drawImage(source, 0, 0, null);
				AffineTransform none = new AffineTransform();
				paintLayers(g2D, none);
				paintLines(g2D, none);
				g2D.dispose();
			} else {
				bi = source;
			}

			AffineTransform tx = g2.getTransform();
			AffineTransform newAt = new AffineTransform(tx);
			newAt.concatenate(at);
			/**
			 * Խ��concatenate, Խ�ȱ任
			 */
			newAt.concatenate(spriteAt);
			g2.setTransform(newAt);

			int w = src.width;
			int h = src.height;
			if (src.x < bi.getWidth() && src.y < bi.getHeight()) {
				if (src.x + w > bi.getWidth())
					w = bi.getWidth() - src.x;
				if (src.y + h > bi.getHeight())
					h = bi.getHeight() - src.y;
				g2.drawImage(bi, 0, 0, w, h, src.x, src.y, src.x + w,
						src.y + h, null);
			}

			if (xor) {
				/**
				 * ģʽ������: XOR, Paint(Ĭ��)
				 */
				g2.setXORMode(new Color(0xFFFFFF));
				g2.fillRect(0, 0, w, h);
				g2.setPaintMode();
			}

			g2.setTransform(tx);
		}
	}

	/**
	 * ��Ⱦ֡
	 * 
	 * @param g2
	 * @param at
	 * @param frame
	 * @param x
	 * @param y
	 */
	public void renderFrame(Graphics2D g2, AffineTransform at, AniFrame frame,
			int x, int y) {
		renderFrame(g2, at, frame, null, x, y);
	}

	/**
	 * ��Ⱦ֡
	 * 
	 * @param g2
	 * @param at
	 * @param frame
	 * @param modulesMapData
	 *            ģ��ӳ������
	 * @param x
	 * @param y
	 */
	public void renderFrame(Graphics2D g2, AffineTransform at, AniFrame frame,
			ModulesMapData modulesMapData, int x, int y) {
		int id = 0;
		for (Sprite sprite : frame.getSprites()) {
			AffineTransform spriteAt = new AffineTransform(sprite);
			spriteAt.translate(x, y);

			int destModuleID = sprite.module.getId();
			if (modulesMapData != null) {
				for (int i = 0; i < modulesMapData.getModulesMapSize(); ++i) {
					if (destModuleID == modulesMapData.getModulesMapSrcData(i)) {
						destModuleID = modulesMapData.getModulesMapDesData(i);
						break;
					}
				}
			}
			Animation.aniImage().render(g2,
					Animation.instance().getModule(destModuleID), spriteAt, at,
					Sprite.getModel().getSelectionModel().isSelectedIndex(id));
			++id;
		}
	}

	/**
	 * ��Ⱦģ��֡
	 * 
	 * @param g2
	 * @param at
	 * @param frame
	 * @param ani
	 * @param spAt
	 * @param xor
	 */
	public void renderTemplateFrame(Graphics2D g2, AffineTransform at,
			AniFrame frame, Animation ani, AffineTransform spAt, boolean xor) {
		AffineTransform newAt = new AffineTransform(at);
		/**
		 * ��translate����at������
		 */
		newAt.translate(spAt.getTranslateX(), spAt.getTranslateY());

		for (Sprite sprite : frame.getSprites()) {
			if (sprite.module != null) {
				render(g2, sprite.module, sprite, newAt, xor);
			} else {
				Animation animation = sprite.templateWind.getAnimation();
				animation.getImageProvider().renderTemplateFrame(g2, newAt,
						animation.getFrameList().get(sprite.frameIDWind),
						animation, sprite, xor);
			}
		}
	}
}
