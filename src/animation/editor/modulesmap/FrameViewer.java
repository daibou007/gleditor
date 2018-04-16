package animation.editor.modulesmap;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import animation.editor.ScalableViewer;
import animation.world.AniFrame;
import animation.world.Animation;

/**
 * ֡��ͼ, һ��֡�ų���
 * 
 * @author ����
 * @time 2012-9-19
 */
public class FrameViewer extends ScalableViewer {
	public static final int FRAME_WIDTH = 30;
	private static final long serialVersionUID = -5393014087659843583L;

	public FrameViewer() {
		setViewScale(1.5D);
		zoomViewer();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int dx = 10;
		int dy = 40;
		for (AniFrame frame : Animation.instance().getFrameList()) {
			if (ModulesStylesData.getModulesMapData() != null) {
				/**
				 * ��ָ����ģ��ӳ��������Ⱦ֡
				 */
				Animation.aniImage().renderFrame((Graphics2D) g,
						getTransform(), frame,
						ModulesStylesData.getModulesMapData(), dx, dy);
			} else {
				Animation.aniImage().renderFrame((Graphics2D) g,
						getTransform(), frame, dx, dy);
			}

			dx += FRAME_WIDTH;
		}
	}

	@Override
	public Dimension getOriginSize() {
		return new Dimension(Animation.instance().getFrameList().size()
				* FRAME_WIDTH, 80);
	}

}
