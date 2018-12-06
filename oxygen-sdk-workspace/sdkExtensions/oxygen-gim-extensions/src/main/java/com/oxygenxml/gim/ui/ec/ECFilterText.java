package com.oxygenxml.gim.ui.ec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Filter text field
 * 
 */
public abstract class ECFilterText extends SourceViewer {

  /**
   * Ignore modifications.
   */
  private boolean ignoreModificationEvent = false;
  /**
   * The gray color.
   */
  private Color grayColor = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
  /**
   * The regular color.
   */
  private Color normalColor = null;
  /**
   * A custom filter hint
   */
  private final String filterHint;

  /**
   * Constructor. 
   * 
   * @param parent  The parent composite.
   * @param customFilterHint The filter hint
   */
  public ECFilterText(Composite parent, String customFilterHint) {
    super(parent, null, null, false, SWT.SINGLE);
    this.filterHint = customFilterHint;
    
    // Set a document in order to have Undo/Redo operations available.
    setDocument(new Document());
    
    // Configure the source viewer.
    configure(new SourceViewerConfiguration());
    
    // Set the find hint text.
    reset();

    // Watches the changes in the text field.
    getTextWidget().addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        update();
      }
    });
    
    // Set the original search hint or reset it on focus gained/lost.
    getTextWidget().addFocusListener(new FocusListener() {
      @Override
      public void focusLost(FocusEvent e) {
        if (getTextWidget().getText().length() == 0) {
          // If nothing was written, put back the HINT...
          reset();
        }
      }
    
      @Override
      public void focusGained(FocusEvent e) {
        if (isShowingHint()) {
          ignoreModificationEvent = true;
          try {
            // It is showing the HINT. Clean it up.
            getTextWidget().setText("");
          } finally {
            ignoreModificationEvent = false;
          }
        }
      }
    });
    
    getTextWidget().addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        if (normalColor != null) {
          normalColor.dispose();
          normalColor = null;
        }
      }
    });
  }
  
  /**
   * Update the field.
   */
  private void update() {
    if (!ignoreModificationEvent) {
      getTextWidget().setForeground(normalColor);
      filterWasChanged(getTextWidget().getText());
    }
    Iterator<UpdateableAction> iter = actions.values().iterator();
    while (iter.hasNext()) {
      iter.next().updateActionState();
    }
  }
  
  /**
   * Updateable action.
   */
  private class UpdateableAction extends Action {
    /**
     * The action's ID.
     */
    private int actionID;

    /**
     * Updateable action's constructor.
     * 
     * @param text      The action's name.
     * @param actionID  The action's id.
     */
    public UpdateableAction(String text, int actionID) {
      super(text);
      this.actionID = actionID;
    }
    
    /**
     * Update the action's state.
     */
    public void updateActionState() {
      setEnabled(canDoOperation(actionID));
    }
  }
  
  /**
   * @param text The filter text.
   */
  protected abstract void filterWasChanged(String text);

  public void reset() {
    try {
      if (normalColor == null) {
        normalColor = getTextWidget().getForeground();
      }
      ignoreModificationEvent = true;
      // Set the italic font.
      // Set the gray color.
      getTextWidget().setForeground(grayColor);
      getTextWidget().setText(filterHint);
      setSelectedRange(0, getTextWidget().getText().length());
      // Reset the undo manager in order not to present the filter hint...
      getUndoManager().reset();
    } finally {
      ignoreModificationEvent = false;
    }
  }
  
  /**
   * @return True if is showing hint.
   */
  public boolean isShowingHint() {
    if (filterHint != null && getTextWidget() != null) {
      return filterHint.equals(getTextWidget().getText());
    }
    return false;
  }

  /**
   * Set field background color.
   * 
   * @param color The background color.
   */
  public void setFieldBgColor(Color color) {
    getTextWidget().setBackground(color);
  }
  
  /**
   * The actions map.
   */
  private Map<String, UpdateableAction> actions = new HashMap<String, UpdateableAction>();
  
  /**
   * Get the action for the specified id if possible.
   * 
   * @param id  The action's ID.
   * @return  The action if available.
   */
  public IAction getAction(String id) {
    UpdateableAction action = actions.get(id);
    if (action == null) {
      if (id.equals(ActionFactory.CUT.getId())) {
        action = new UpdateableAction("Cut", CUT) {
          /**
           * @see org.eclipse.jface.action.Action#run()
           */
          @Override
          public void run() {
            if (canDoOperation(CUT)) {
              doOperation(CUT);
            }
          }
        };
      } else if (id.equals(ActionFactory.COPY.getId())) {
        action = new UpdateableAction("Copy", COPY) {
          /**
           * @see org.eclipse.jface.action.Action#run()
           */
          @Override
          public void run() {
            if (canDoOperation(COPY)) {
              doOperation(COPY);
            }
          }
        };
      } else if (id.equals(ActionFactory.PASTE.getId())) {
        action = new UpdateableAction("Paste", PASTE) {
          /**
           * @see org.eclipse.jface.action.Action#run()
           */
          @Override
          public void run() {
            if (canDoOperation(PASTE)) {
              doOperation(PASTE);
              update();
            }
          }
        };
      } else if (id.equals(ActionFactory.DELETE.getId())) {
        action = new UpdateableAction("Delete", DELETE) {
          /**
           * @see org.eclipse.jface.action.Action#run()
           */
          @Override
          public void run() {
            if (canDoOperation(DELETE)) {
              doOperation(DELETE);
              update();
            }
          }
        };
      } else if (id.equals(ActionFactory.UNDO.getId())) {
        action = new UpdateableAction("Undo", UNDO) {
          /**
           * @see org.eclipse.jface.action.Action#run()
           */
          @Override
          public void run() {
            if (canDoOperation(UNDO)) {
              doOperation(UNDO);
              update();
            }
          }
        };
      } else if (id.equals(ActionFactory.REDO.getId())) {
        action = new UpdateableAction("Redo", REDO) {
          /**
           * @see org.eclipse.jface.action.Action#run()
           */
          @Override
          public void run() {
            if (canDoOperation(REDO)) {
              doOperation(REDO);
              update();
            }
          }
        };
      }
      actions.put(id, action);
    }
    return action;
  }
}