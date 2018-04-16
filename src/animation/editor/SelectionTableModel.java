package animation.editor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import animation.world.Animation;
import animation.world.Prototype;

/**
 * ѡ�������ģ��
 * 
 * @author ����
 * 
 * @time 2012-9-14
 * 
 */
public abstract class SelectionTableModel<E> extends AbstractTableModel {
	private static final long serialVersionUID = 2254650410657518804L;

	Prototype<E> prototype;
	ArrayList<E> dataArrayList;
	JTable table;
	ListSelectionModel selectionModel;

	public SelectionTableModel(ArrayList<E> data) {
		setDataArrayList(data);
		selectionModel = new DefaultListSelectionModel();
	}

	public SelectionTableModel(ArrayList<E> data, Prototype<E> prototype) {
		this(data);
		this.prototype = prototype;
	}

	/**
	 * ���ñ�����
	 * 
	 * @param data
	 */
	public void setDataArrayList(ArrayList<E> data) {
		dataArrayList = data;
		fireTableDataChanged();
	}

	/**
	 * ��ȡ������
	 */
	public ArrayList<E> getDataArrayList() {
		return dataArrayList;
	}

	/**
	 * ����ʹ�ø�ģ�͵ı�
	 * 
	 * @param table
	 */
	public void setTable(JTable table) {
		this.table = table;
		table.setModel(this);
		table.setSelectionModel(selectionModel);
		table.setDragEnabled(true);

		InputMap im = new InputMap();
		im.setParent(table.getInputMap(JTable.WHEN_FOCUSED));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
		table.setInputMap(JTable.WHEN_FOCUSED, im);

		ActionMap am = new ActionMap();
		am.setParent(table.getActionMap());
		am.put("Delete", new AbstractAction() {
			private static final long serialVersionUID = 3997804177680990063L;

			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		table.setActionMap(am);
	}

	public void addSelectionListener(ListSelectionListener l) {
		selectionModel.addListSelectionListener(l);
	}

	public ListSelectionModel getSelectionModel() {
		return table.getSelectionModel();
	}

	/**
	 * �����
	 */
	public void addRow() {
		if (Animation.instance().isReadOnly())
			return;
		if (dataArrayList == null) {
			dataArrayList = new ArrayList<E>();
		}
		insertRow(getRowCount(), prototype.clone());
	}

	/**
	 * ��ȡ�����ݶ���
	 * 
	 * @param row
	 */
	public E getRowObject(int row) {
		return dataArrayList == null ? null : dataArrayList.get(row);
	}

	/**
	 * ��ȡѡ��������ݶ���
	 */
	public E getSelection() {
		return (dataArrayList == null || table.getSelectedRow() < 0) ? null
				: dataArrayList.get(table.getSelectedRow());
	}

	/**
	 * ��ȡѡ�����ID
	 */
	public int getSelectionID() {
		return dataArrayList == null ? -1 : table.getSelectedRow();
	}

	/**
	 * ��ȡѡ���һ�������ݶ���
	 */
	public ArrayList<E> getSelections() {
		ArrayList<E> selections = new ArrayList<E>();
		int[] rows = table.getSelectedRows();
		for (int row : rows) {
			selections.add(dataArrayList.get(row));
		}

		return selections;
	}

	/**
	 * ѡ����һ��
	 */
	public void selectNext() {
		if (table.getRowCount() == 0)
			return;
		int row = table.getSelectedRow();
		++row;
		if (row >= table.getRowCount()) {
			row = 0;
		}
		table.getSelectionModel().setSelectionInterval(row, row);

	}

	/**
	 * ѡ��ǰһ��
	 */
	public void selectPrevious() {
		if (table.getRowCount() == 0)
			return;
		int row = table.getSelectedRow();
		--row;
		if (row < 0) {
			row = table.getRowCount() - 1;
		}
		table.getSelectionModel().setSelectionInterval(row, row);
	}

	/**
	 * ����ж���
	 * 
	 * @param e
	 */
	public void addRow(E e) {
		if (Animation.instance().isReadOnly())
			return;
		int row = getRowCount();
		insertRow(row, e);
		table.getSelectionModel().setSelectionInterval(row, row);
	}

	/**
	 * ��ָ���в��뵥���ж���, ֮ǰָ���м����Ժ�Ĳ��ֶ���Ӧ����
	 * 
	 * @param row
	 * @param e
	 */
	public void insertRow(int row, E e) {
		if (dataArrayList == null)
			return;

		ArrayList<E> rowList = new ArrayList<E>(1);
		rowList.add(e);
		AnimationEditor.undoManager.addEdit(new InsertRowsUndoableEdit(row,
				rowList));

		dataArrayList.add(row, e);
		fireTableRowsInserted(row, row);
	}

	/**
	 * ��ָ���в���һ���ж���, ֮ǰָ���м����Ժ�Ĳ��ֶ���Ӧ����
	 * 
	 * @param row
	 * @param rowList
	 */
	public void insertRows(int row, ArrayList<E> rowList) {
		if (dataArrayList == null)
			return;

		AnimationEditor.undoManager.addEdit(new InsertRowsUndoableEdit(row,
				rowList));
		dataArrayList.addAll(row, rowList);
		fireTableRowsInserted(row, row + rowList.size() - 1);
	}

	class InsertRowsUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = -95665676698453301L;

		int row;
		ArrayList<E> rowList;

		@SuppressWarnings("unchecked")
		public InsertRowsUndoableEdit(int row, ArrayList<E> rowList) {
			this.row = row;
			this.rowList = (ArrayList<E>) rowList.clone();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			dataArrayList.removeAll(rowList);
			fireTableRowsDeleted(row, row + rowList.size() - 1);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			dataArrayList.addAll(rowList);
			fireTableRowsInserted(row, row + rowList.size() - 1);
		}
	}

	/**
	 * ���ѡ���ж���
	 * 
	 * @param e
	 */
	public void addSelectObject(E e) {
		int index;
		if (dataArrayList == null || (index = dataArrayList.indexOf(e)) < 0)
			return;
		table.getSelectionModel().addSelectionInterval(index, index);
	}

	/**
	 * ѡ���ж���
	 * 
	 * @param e
	 */
	public void selectObject(E e) {
		int index;
		if (dataArrayList == null || (index = dataArrayList.indexOf(e)) < 0)
			return;
		table.getSelectionModel().setSelectionInterval(index, index);
	}

	@Override
	public int getRowCount() {
		return dataArrayList == null ? 0 : dataArrayList.size();
	}

	public void remove() {
		if (Animation.instance().isReadOnly())
			return;
		ArrayList<E> selections = getSelections();
		AnimationEditor.undoManager.addEdit(new RemoveRowUndoableEdit(
				selections));
		dataArrayList.removeAll(selections);
		fireTableDataChanged();
	}

	class RemoveRowUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 45634412318384065L;

		ArrayList<E> rowList;
		ArrayList<E> backupData;

		@SuppressWarnings("unchecked")
		public RemoveRowUndoableEdit(ArrayList<E> rowList) {
			rowList = (ArrayList<E>) rowList.clone();
			backupData = (ArrayList<E>) dataArrayList.clone();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			dataArrayList.clear();
			dataArrayList.addAll(backupData);
			fireTableDataChanged();
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			dataArrayList.removeAll(rowList);
			fireTableDataChanged();
		}
	}

	/**
	 * ��ѡ�����Ŀ�����ƶ�
	 */
	public void moveUp() {
		if (Animation.instance().isReadOnly())
			return;
		try {
			ListSelectionModel selectionModel = table.getSelectionModel();
			int start = selectionModel.getMinSelectionIndex();
			int end = selectionModel.getMaxSelectionIndex();
			int to = start - 1;
			moveRow(start, end, to);
			AnimationEditor.undoManager.addEdit(new MoveRowUndoableEdit(start,
					end, to));
			selectionModel.setSelectionInterval(start - 1, end - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ѡ�����Ŀ�����ƶ�
	 */
	public void moveDown() {
		if (Animation.instance().isReadOnly())
			return;
		try {
			ListSelectionModel selectionModel = table.getSelectionModel();
			int start = selectionModel.getMinSelectionIndex();
			int end = selectionModel.getMaxSelectionIndex();
			int to = start + 1;
			moveRow(start, end, to);
			AnimationEditor.undoManager.addEdit(new MoveRowUndoableEdit(start,
					end, to));
			selectionModel.setSelectionInterval(start + 1, end + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MoveRowUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 4932039152471675050L;

		int start;
		int end;
		int to;

		public MoveRowUndoableEdit(int start, int end, int to) {
			this.start = start;
			this.end = end;
			this.to = to;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			int shift = to - start;
			try {
				moveRow(start + shift, end + shift, to - shift);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			try {
				moveRow(start, end, to);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void moveRow(int start, int end, int to) {
		int shift = to - start;
		/**
		 * ��Ӱ��������������
		 */
		int first;
		/**
		 * ��Ӱ��������������
		 */
		int last;
		if (shift < 0) {
			first = to;
			last = end;
		} else {
			first = start;
			last = to + end - start;
		}
		rotate(dataArrayList, first, last + 1, shift);
		fireTableRowsUpdated(first, last);
	}

	private void rotate(ArrayList<E> v, int a, int b, int shift) {
		int size = b - a;
		int r = size - shift;
		int g = gcd(size, r);
		for (int i = 0; i < g; ++i) {
			int to = i;
			E tmp = v.get(a + to);
			for (int from = (to + r) % size; from != i; from = (to + r) % size) {
				v.set(a + to, v.get(a + from));
				to = from;
			}
			v.set(a + to, tmp);
		}
	}

	/**
	 * ��ȡ���Լ��
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int gcd(int i, int j) {
		return j == 0 ? i : gcd(j, i % j);
	}

	/**
	 * �ƶ�һ���е�ָ����ǰ��
	 * 
	 * @param rows
	 * @param before
	 */
	public void moveRows(int[] rows, int before) {
		AnimationEditor.undoManager.addEdit(new MoveRowsUndoableEdit(rows,
				before));
		ArrayList<E> data = new ArrayList<E>(rows.length);
		/**
		 * ͳ��Ҫ�ƶ�����������beforeС�ĸ���
		 */
		int small = 0;
		for (int row : rows) {
			if (row < before) {
				++small;
			}
			data.add(dataArrayList.get(row));
		}
		dataArrayList.removeAll(data);
		int i = 0;
		for (E e : data) {
			/**
			 * before - small + i�ǳ�ʼbefore��Ӧ�Ķ������ڵ�λ��
			 */
			dataArrayList.add(before - small + i, e);
			++i;
		}
		fireTableDataChanged();
	}

	class MoveRowsUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = -6742755437980321537L;

		int[] rows;
		int before;
		ArrayList<E> backupData;

		@SuppressWarnings("unchecked")
		public MoveRowsUndoableEdit(int[] rows, int before) {
			this.rows = rows;
			this.before = before;
			backupData = (ArrayList<E>) dataArrayList.clone();
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			dataArrayList.clear();
			dataArrayList.addAll(backupData);
			fireTableDataChanged();
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			moveRows(rows, before);
		}
	}

	/**
	 * ��¡ѡ���һ���ж���
	 */
	public void cloneRow() {
		for (E e : getSelections()) {
			addRow(prototype.clone(e));
		}
	}

	class SetValueAtUndoableEdit extends AbstractUndoableEdit {
		private static final long serialVersionUID = 6934954576557900102L;
		Object oldValue;
		Object newValue;
		int rowIndex;
		int columnIndex;

		public SetValueAtUndoableEdit(Object oldValue, Object newValue,
				int rowIndex, int columnIndex) {
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.rowIndex = rowIndex;
			this.columnIndex = columnIndex;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			setValueAt(oldValue, rowIndex, columnIndex);
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			setValueAt(newValue, rowIndex, columnIndex);
		}
	}
}
