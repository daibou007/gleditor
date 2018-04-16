package animation.editor;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import animation.editor.tool.AbstractTool;
import animation.world.Animation;

/**
 * ��������ͼ
 * 
 * @author ����
 * @time 2012-9-15
 */
public abstract class ScalableViewer extends JPanel implements Scrollable {
	private static final long serialVersionUID = 5635745204638385201L;

	/**
	 * ��������
	 */
	protected double viewScale = 1.0D;
	/**
	 * ����
	 */
	private AbstractTool tool;
	/**
	 * ��һ�������ק��λ��
	 */
	protected Point lastMousePoint;
	/**
	 * ������
	 */
	protected int componentWidth;
	/**
	 * ����߶�
	 */
	protected int componentHeight;
	/**
	 * λ�ñ�ǩ
	 */
	private JLabel locationLabel;
	/**
	 * λ�ø�ʽ
	 */
	private MessageFormat locationFormat = new MessageFormat(
			"Point: [{0,number,###}, {1,number,###}]");
	/**
	 * ��ͼ����ϵ��ԭ�����������ϵ�µ�X��λ��
	 */
	private int originX;
	/**
	 * ��ͼ����ϵ��ԭ�����������ϵ�µ�Y��λ��
	 */
	private int originY;

	/**
	 * ������������ͼ, ע�����,����������, ����ͼ��ǰ���õı༭���ߴ�������һϵ���¼�
	 */
	public ScalableViewer() {
		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				translateMousePoint(e);
				if (getTool() != null) {
					getTool().mouseReleased(e);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				translateMousePoint(e);
				AbstractTool tool2 = getTool();
				if (tool2 != null) {
					tool2.mousePressed(e);
				}
				lastMousePoint = e.getPoint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				translateMousePoint(e);
				if (getTool() != null) {
					getTool().mouseExited(e);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				translateMousePoint(e);
				if (getTool() != null) {
					getTool().mouseEntered(e);
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				translateMousePoint(e);
				if (getTool() != null) {
					getTool().mouseClicked(e);
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				translateMousePoint(e);
				if (getTool() != null) {
					getTool().mouseMoved(e);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
				scrollRectToVisible(r);

				translateMousePoint(e);
				if (e.getPoint().distance(lastMousePoint) == 0.0D)
					return;
				if (getTool() != null) {
					getTool().mouseDragged(e);
				}
				lastMousePoint = e.getPoint();
			}
		});
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (getTool() != null) {
					getTool().keyTyped(e);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (getTool() != null) {
					getTool().keyReleased(e);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (getTool() != null) {
					getTool().keyPressed(e);
				}
			}
		});
	}

	/**
	 * ��ȡ��ͼ��ԭʼ��С
	 */
	public abstract Dimension getOriginSize();

	/**
	 * ��ȡ��ͼ��������
	 */
	public double getViewScale() {
		return viewScale;
	}

	/**
	 * ������ͼ��������
	 * 
	 * @param scale
	 */
	public void setViewScale(double scale) {
		this.viewScale = scale;
		revalidate();
	}

	/**
	 * ��ȡ��ͼ��Ӧ�ķ���任
	 */
	public AffineTransform getTransform() {
		AffineTransform result = new AffineTransform();
		Insets insets = getInsets();
		result.translate(insets.left, insets.top);
		result.scale(viewScale, viewScale);
		result.translate(originX, originY);

		return result;
	}

	/**
	 * �����������ϵԭ������ͼ����ϵ�µ�λ��
	 * 
	 * @param x
	 * @param y
	 */
	public void setOrigin(int x, int y) {
		originX = -x;
		originY = -y;
		revalidate();
	}

	/**
	 * ��ȡ��ͼ����ϵԭ��X��λ��
	 */
	public int getXOrigin() {
		return originX;
	}

	/**
	 * ��ȡ��ͼ����ϵԭ��Y��λ��
	 */
	public int getYOrigin() {
		return originY;
	}

	/**
	 * ��ȡ�༭��ͼ�Ĺ���
	 */
	public AbstractTool getTool() {
		return Animation.instance().isReadOnly() ? null : tool;
	}

	/**
	 * ���ñ༭��ͼ�Ĺ���
	 * 
	 * @param aTool
	 */
	public void setTool(AbstractTool aTool) {
		if (tool != null) {
			tool.endSession();
		}
		tool = aTool;
		tool.beginSession();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		Insets insets = getInsets();
		Dimension originSize = getOriginSize();
		int w = (int) (originSize.width * viewScale);
		int h = (int) (originSize.height * viewScale);
		componentWidth = w + insets.left + insets.right;
		componentHeight = h + insets.top + insets.bottom;
		setPreferredSize(new Dimension(componentWidth, componentHeight));
		super.setBounds(x, y, componentWidth, componentHeight);
	}

	/**
	 * �Ŵ���ͼ
	 */
	public void zoomIn() {
		if (viewScale < 8.0D) {
			viewScale += 1.0D;
			zoomViewer();
		}
	}

	/**
	 * ��С��ͼ
	 */
	public void zoomOut() {
		if (viewScale > 1.0D) {
			viewScale -= 1.0D;
			zoomViewer();
		}
	}

	/**
	 * ������ͼ
	 */
	protected void zoomViewer() {
		Insets insets = getInsets();
		Dimension originSize = getOriginSize();
		int w = (int) (originSize.width * viewScale);
		int h = (int) (originSize.height * viewScale);
		componentWidth = w + insets.left + insets.right;
		componentHeight = h + insets.top + insets.bottom;
		setPreferredSize(new Dimension(componentWidth, componentHeight));
		revalidate();
		repaint();
	}

	/**
	 * ��ȡ��ͼ�����λ�õı�ǩ
	 */
	public JLabel getLocationLabel() {
		if (locationLabel == null) {
			locationLabel = new JLabel();
			locationLabel.setPreferredSize(new Dimension(100, 24));
			locationLabel.setVerticalAlignment(JLabel.CENTER);
			locationLabel.setHorizontalAlignment(JLabel.LEFT);
			locationLabel.setText("");
		}

		return locationLabel;
	}

	/**
	 * �����λ��ӳ�䵽ԭʼ��ͼ����ϵ��
	 * 
	 * @param e
	 */
	protected void translateMousePoint(MouseEvent e) {
		Point point = transformCoordinate(e.getPoint());
		e.translatePoint(point.x - e.getX(), point.y - e.getY());
		getLocationLabel().setText(
				locationFormat.format(new Integer[] { point.x, point.y }));
	}

	/**
	 * �ѵ�ӳ�䵽ԭʼ��ͼ����ϵ��
	 * 
	 * @param p
	 */
	public Point transformCoordinate(Point p) {
		Point result = new Point();
		try {
			getTransform().inverseTransform(p, result);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * �Ѿ���ӳ�䵽��ǰ��ͼ����ϵ��
	 * 
	 * @param r
	 */
	public Rectangle transformCoordinate(Rectangle r) {
		return getTransform().createTransformedShape(r).getBounds();
	}

	public Dimension getPreferredScrollableViewportSize() {
		return null;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 16;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 64;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
