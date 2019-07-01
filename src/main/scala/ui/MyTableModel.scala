package scala.ui

import javax.swing.table.AbstractTableModel
import scala.Array.canBuildFrom

class MyTableModel(var rowData: Array[Array[Any]], val columnNames: Seq[String]) extends AbstractTableModel {

	override def getColumnName( column: Int) = columnNames(column).toString

	def getRowCount = rowData.length

	def getColumnCount = columnNames.length

	def getValueAt(row: Int, col: Int): AnyRef = rowData(row)(col).asInstanceOf[AnyRef]

	override def isCellEditable( row: Int, column: Int) = false

	override def setValueAt(value: Any, row: Int, col: Int)= rowData(row)(col) = value

	def setRowValue(rowValue: Array[String], row: Int) = setValueAtLoop(rowValue, row, 0)

	def setTable(table: Array[Array[Any]]) = rowData = table

	def addRow(data: Array[AnyRef]) = rowData ++= Array(data.asInstanceOf[Array[Any]])
	
	def addBlankRow = {
	  val row = new Array[AnyRef](getColumnCount)
	  addRow(row)
	}

	def removeRow(index: Int) = setTable(remove(rowData, index + 1))

	private def remove(array: Array[Array[Any]], index: Int) : Array[Array[Any]] = {
		val arrayHead = array.take(index - 1)
		val arrayTail = array.drop(index)
		arrayHead ++ arrayTail
	}

	private def setValueAtLoop(array: Array[String], row: Int, col: Int){
		setValueAt(array(col), row, col)
		if(col < array.length - 1) setValueAtLoop(array, row, col + 1) 
	}

}