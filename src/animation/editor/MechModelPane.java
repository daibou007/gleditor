package animation.editor;

import static animation.editor.MainFrame.useIcon;
import static animation.editor.MainFrame.useText;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import animation.world.AniAction;
import animation.world.MechModel;

/**
 * �˶�ģ�����
 * 
 * @author ����
 * @time 2012-9-15
 */
public class MechModelPane extends JPanel implements Localizable {
	private static final long serialVersionUID = -1798039729962806406L;

	AniAction action;
	MechModel mechModel;

	JLabel titledLabel = new JLabel();
	JPanel titledPanel = new JPanel();

	JPanel gridPanel = new JPanel();
	JPanel contentPanel = new JPanel();

	JToolBar toolBar = new JToolBar();
	JButton copyButton = new JButton();
	JButton pasteButton = new JButton();

	JCheckBox allCheckBox = new JCheckBox();
	JCheckBox speedXCheckBox = new JCheckBox();
	JCheckBox speedYCheckBox = new JCheckBox();
	JCheckBox accelerationXCheckBox = new JCheckBox();
	JCheckBox accelerationYCheckBox = new JCheckBox();

	JTextField speedXText = new JTextField();
	JTextField speedYText = new JTextField();
	JTextField accelerationXText = new JTextField();
	JTextField accelerationYText = new JTextField();

	MechModelChangeAdapter adapter = new MechModelChangeAdapter();
	private MechModel copyMechModel;

	public MechModelPane() {
		try {
			jbInit();
			updateLocalization();
			listenerInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ʼ���˶�ģ�ͽ������
	 */
	private void jbInit() {
		titledLabel.setFont(new Font("Arial", 0, 14));
		titledLabel.setPreferredSize(new Dimension(41, 17));
		titledPanel.setBackground(SystemColor.info);
		titledPanel.setLayout(new BorderLayout());
		titledPanel.add(titledLabel, BorderLayout.NORTH);

		copyButton.setRequestFocusEnabled(false);
		pasteButton.setRequestFocusEnabled(false);
		toolBar.setFloatable(false);
		toolBar.add(copyButton);
		toolBar.add(pasteButton);

		gridPanel.setLayout(new GridLayout(5, 2, 8, 8));
		gridPanel.add(speedXCheckBox);
		gridPanel.add(speedXText);
		gridPanel.add(speedYCheckBox);
		gridPanel.add(speedYText);
		gridPanel.add(accelerationXCheckBox);
		gridPanel.add(accelerationXText);
		gridPanel.add(accelerationYCheckBox);
		gridPanel.add(accelerationYText);
		gridPanel.add(allCheckBox);

		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(toolBar, BorderLayout.NORTH);
		contentPanel.add(gridPanel, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(titledPanel, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	public void updateLocalization() {
		titledLabel.setText("MechModel");

		useText(allCheckBox, "allCheckBox_Text");
		useText(speedXCheckBox, "speedXCheckBox_Text");
		useText(speedYCheckBox, "speedYCheckBox_Text");
		useText(accelerationXCheckBox, "accelerationXCheckBox_Text");
		useText(accelerationYCheckBox, "accelerationYCheckBox_Text");

		useIcon(copyButton, "copyButton_Icon");
		useIcon(pasteButton, "pasteButton_Icon");
	}

	/**
	 * ��ʼ��������
	 * <p>
	 * <li>������ѡ��ı����˶�ģ�������ʾ��Ϣ�ı�</li>
	 * <li>ȫѡ���߷�ѡ�˶���Ϣ</li>
	 * <li>�����˶�ģ����Ϣ</li>
	 * <li>����˶�ģ����Ϣ</li>
	 */
	private void listenerInit() {
		AniAction.getModel().getSelectionModel()
				.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						saveData();
						action = AniAction.getModel().getSelection();
						if (action != null) {
							mechModel = action.getMechModel();
							loadData();
						} else {
							action = null;
						}
					}

				});

		allCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = allCheckBox.isSelected();
				speedXCheckBox.setSelected(selected);
				speedYCheckBox.setSelected(selected);
				accelerationXCheckBox.setSelected(selected);
				accelerationYCheckBox.setSelected(selected);
			}

		});

		copyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (mechModel != null) {
					copyMechModel = (MechModel) mechModel.clone();
				}
			}

		});
		pasteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (copyMechModel != null && mechModel != null) {
					mechModel = (MechModel) copyMechModel.clone();
					action.setMechModel(mechModel);
					loadData();
				}
			}

		});

		speedXText.addFocusListener(adapter);
		speedYText.addFocusListener(adapter);
		accelerationXText.addFocusListener(adapter);
		accelerationYText.addFocusListener(adapter);
	}

	class MechModelChangeAdapter implements ChangeListener, ActionListener,
			FocusListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			saveData();
		}

		@Override
		public void focusGained(FocusEvent e) {

		}

		@Override
		public void focusLost(FocusEvent e) {
			saveData();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			saveData();
		}

	}

	/**
	 * �����˶�ģ����Ϣ����ʾ�ڽ�����
	 */
	public void loadData() {
		/**
		 * ���Ƴ�������, ������<code>setSelected</code>��<code>setText</code>ʱ����
		 * <code>saveData</code>����
		 */
		removeAllListener();

		speedXCheckBox.setSelected(mechModel.hasSpeedX);
		speedYCheckBox.setSelected(mechModel.hasSpeedY);
		accelerationXCheckBox.setSelected(mechModel.hasAccelerationX);
		accelerationYCheckBox.setSelected(mechModel.hasAccelerationY);

		speedXText.setText(Double.toString(mechModel.speedX));
		speedYText.setText(Double.toString(mechModel.speedY));
		accelerationXText.setText(Double.toString(mechModel.accelerationX));
		accelerationYText.setText(Double.toString(mechModel.accelerationY));

		addAllListener();
	}

	/**
	 * �Ƴ��˶�ģ�ͽ����ϵļ�����
	 */
	private void removeAllListener() {
		speedXCheckBox.removeChangeListener(adapter);
		speedYCheckBox.removeChangeListener(adapter);
		accelerationXCheckBox.removeChangeListener(adapter);
		accelerationYCheckBox.removeChangeListener(adapter);

		speedXText.removeActionListener(adapter);
		speedYText.removeActionListener(adapter);
		accelerationXText.removeActionListener(adapter);
		accelerationYText.removeActionListener(adapter);
	}

	/**
	 * ����˶�ģ�ͽ����ϵļ�����
	 */
	private void addAllListener() {
		speedXCheckBox.addChangeListener(adapter);
		speedYCheckBox.addChangeListener(adapter);
		accelerationXCheckBox.addChangeListener(adapter);
		accelerationYCheckBox.addChangeListener(adapter);

		speedXText.addActionListener(adapter);
		speedYText.addActionListener(adapter);
		accelerationXText.addActionListener(adapter);
		accelerationYText.addActionListener(adapter);
	}

	/**
	 * �ӽ����ϱ����޸ĺ���˶�ģ����Ϣ
	 */
	public void saveData() {
		if (mechModel == null)
			return;
		mechModel.hasSpeedX = speedXCheckBox.isSelected();
		mechModel.hasSpeedY = speedYCheckBox.isSelected();
		mechModel.hasAccelerationX = accelerationXCheckBox.isSelected();
		mechModel.hasAccelerationY = accelerationYCheckBox.isSelected();

		try {
			mechModel.speedX = mechModel.adjust(Double.parseDouble(speedXText
					.getText()));
			mechModel.speedY = mechModel.adjust(Double.parseDouble(speedYText
					.getText()));
			mechModel.accelerationX = mechModel.adjust(Double
					.parseDouble(accelerationXText.getText()));
			mechModel.accelerationY = mechModel.adjust(Double
					.parseDouble(accelerationYText.getText()));
		} catch (NumberFormatException e) {
			Toolkit.getDefaultToolkit().beep();
		}
		loadData();
	}
}
