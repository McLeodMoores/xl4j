package com.mcleodmoores.excel4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.javacode.InvokerFactory;
import com.mcleodmoores.excel4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.excel4j.values.XLSheetId;

/**
 * A mock implementation of the Excel interface for use in testing.
 */
public class MockExcel implements Excel {
  
  private ConcurrentMap<XLSheetId, WorksheetHeap> _heaps = new ConcurrentHashMap<>();
  private ThreadLocal<XLSheetId> _sheetId = new ThreadLocal<XLSheetId>();
  
  @Override
  public byte[] getBinaryName(final long handle, final long length) {
    return new byte[0];
  }

  @Override
  public InvokerFactory getInvokerFactory() {
    return new ReflectiveInvokerFactory();
  }
  
  @Override
  public WorksheetHeap getWorksheetHeap() {
    // disable multiple heaps for now.
    XLSheetId currentSheetId = XLSheetId.of(0);
    WorksheetHeap worksheetHeap = _heaps.get(currentSheetId);
    if (worksheetHeap == null) {
      WorksheetHeap newWorksheetHeap = new WorksheetHeap(currentSheetId);
      worksheetHeap = _heaps.putIfAbsent(currentSheetId, newWorksheetHeap);
      if (worksheetHeap == null) {
        worksheetHeap = newWorksheetHeap;
      }
    }
    return worksheetHeap;
  }
  
  /**
   * Get the current Sheet Id.
   * @return the sheet id in the current thread
   */
  public XLSheetId getCurrentSheetId() {
    return _sheetId.get();
  }
  
  /**
   * Set the current Sheet Id for this thread.
   * This should not be called by user code.
   * @param sheetId the sheet id
   */
  /*package*/ void setCurrentSheetId(final XLSheetId sheetId) {
    _sheetId.set(sheetId);
  }

}
