package animation.world;

import java.awt.Rectangle;

import org.jdom2.Element;

public class Util {

	/**
	 * ��������
	 * 
	 * @param d
	 * @return
	 */
	public static int round(double d) {
		return (int) (d >= 0.0D ? d + 0.5D : d - 0.5D);
	}

	public static int getInt(Element e, String attname) {
		return Integer.parseInt(e.getAttributeValue(attname));
	}

	public static double getDouble(Element e, String attname) {
		return Double.parseDouble(e.getAttributeValue(attname));
	}

	/**
	 * ���ž��ο�
	 * 
	 * @param rect
	 * @param d
	 *            ��������
	 */
	public static void scaleRect(Rectangle rect, double d) {
		rect.x = round(rect.x * d);
		rect.y = round(rect.y * d);
		rect.width = round(rect.width * d);
		rect.height = round(rect.height * d);
	}
}
