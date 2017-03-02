package org.geogebra.web.web.gui.util;

import java.util.HashMap;

import org.geogebra.common.gui.util.SelectionTable;
import org.geogebra.common.main.App;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.html5.gui.GPopupPanel;
import org.geogebra.web.html5.gui.util.Slider;
import org.geogebra.web.html5.gui.util.SliderInputHandler;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.css.GuiResources;
import org.geogebra.web.web.euclidian.EuclidianStyleBarW;
import org.geogebra.web.web.gui.images.ImgResourceHelper;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Table popup for selecting properties of objects
 *
 */
public class PopupMenuButtonW extends MyCJButton
		implements ChangeHandler, SliderInputHandler {
	
	/**
	 * App
	 */
	protected AppW app;
	/**
	 * Icons / texts in the table
	 */
	protected ImageOrText[] data;
	private ButtonPopupMenu myPopup;
	private PopupMenuHandler popupHandler;
	private Slider mySlider;
	private Label sliderLabel;
	private SelectionTableW myTable;
	private boolean hasTable;
	/** flag to determine if the popup should persist after a mouse click */
	private boolean keepVisible = true;
	private boolean isIniting = true;
	private ImageOrText fixedIcon;
	private boolean isFixedIcon = false;
	private boolean multiselectionEnabled = false;
	private StyleBarW2 changeEventHandler;
	protected HashMap<Integer, Integer> lineStyleMap;

	/**
	 * @param app
	 *            {@link AppW}
	 * @param data
	 *            {@link ImageOrText}
	 * @param rows
	 *            {@code Integer}
	 * @param columns
	 *            {@code Integer}
	 * @param mode
	 *            {@link SelectionTableW}
	 */
	public PopupMenuButtonW(App app, ImageOrText[] data, Integer rows,
			Integer columns, SelectionTable mode) {
		this(app, data, rows, columns, mode, true, false, null);
	}

	/**
	 * @param app
	 *            {@link AppW}
	 * @param data
	 *            {@link ImageOrText}
	 * @param rows
	 *            {@code Integer}
	 * @param columns
	 *            {@code Integer}
	 * @param mode
	 *            {@link SelectionTableW}
	 * @param hasTable
	 *            {@code boolean}
	 * @param hasSlider
	 *            {@code boolean}
	 * @param lineStyleMap0
	 *            maps item index to line style
	 */
	public PopupMenuButtonW(App app, ImageOrText[] data, Integer rows,
			Integer columns, SelectionTable mode, final boolean hasTable,
			boolean hasSlider, HashMap<Integer, Integer> lineStyleMap0) {
		this(app, data, rows, columns, mode, hasTable, hasSlider, null,
				lineStyleMap0);
	}

	
	/**
	 * @param app
	 *            {@link AppW}
	 * @param data
	 *            {@link ImageOrText}
	 * @param rows
	 *            {@code Integer}
	 * @param columns
	 *            {@code Integer}
	 * @param mode
	 *            {@link SelectionTableW}
	 * @param hasTable
	 *            {@code boolean}
	 * @param hasSlider
	 *            {@code boolean}
	 * @param selected
	 *            which items are selected
	 * @param lineStyleMap0
	 *            maps item index to line style
	 */
	public PopupMenuButtonW(App app, ImageOrText[] data, Integer rows,
			Integer columns, SelectionTable mode, final boolean hasTable,
			boolean hasSlider, boolean[] selected,
			HashMap<Integer, Integer> lineStyleMap0) {
		super();
		this.app = (AppW) app;
		this.hasTable = hasTable;
		this.lineStyleMap = lineStyleMap0;
		if (selected != null) {
			multiselectionEnabled = true;
		}

		createPopup();

		// add a mouse listener to our button that triggers the popup
		addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				if (!PopupMenuButtonW.this.isEnabled()) {
					return;
				}
				handleClick();
			}
		});
	
		addDomHandler(new TouchStartHandler() {
			@Override
			public void onTouchStart(TouchStartEvent event) {
				event.stopPropagation();
			}
		}, TouchStartEvent.getType());

		addDomHandler(new TouchEndHandler() {
			@Override
			public void onTouchEnd(TouchEndEvent event) {
				event.stopPropagation();
			}
		}, TouchEndEvent.getType());

		if (hasTable) {
			createSelectionTable(data, rows, columns, mode, selected);
		}
		if (selected != null) {
			// myTable.initSelectedItems(selected); // TODO remove?
		}

		// create slider
		if (hasSlider) {
			getMySlider();
		}
		isIniting = false;
	}


	/**
	 * creates a new {@link ButtonPopupMenu}
	 */
	private void createPopup() {
		myPopup = new ButtonPopupMenu(app.getPanel()) {
			@Override
			public void setVisible(boolean visible) {
				super.setVisible(visible);

				// if another button is pressed only the visibility is changed,
				// by firing the event we can react as if it was closed
				CloseEvent.fire(this, this, false);
			}

			@Override
			public void hide() {
				super.hide();
				if (EuclidianStyleBarW.getCurrentPopup().equals(this)) {
					EuclidianStyleBarW.setCurrentPopup(null);
				}
			}
		};
		myPopup.setAutoHideEnabled(true);
    }

	/**
	 * handle click on {@link PopupMenuButtonW this button}
	 */
	void handleClick() {
		onClickAction();
		if (EuclidianStyleBarW.getCurrentPopup() != myPopup
		        || !app.wasPopupJustClosed()) {
			if (EuclidianStyleBarW.getCurrentPopup() != null) {
				EuclidianStyleBarW.getCurrentPopup().hide();
			}
			EuclidianStyleBarW.setCurrentPopup(myPopup);

			app.registerPopup(myPopup);
			myPopup.showRelativeTo(getWidget());
			myPopup.getFocusPanel().getElement().focus();
		} else {
			myPopup.setVisible(false);
			EuclidianStyleBarW.setCurrentPopup(null);
		}
	}
	
	/**
	 * @param newData
	 *            icons
	 * @param rows
	 *            number of rows
	 * @param columns
	 *            number of columns
	 * @param mode
	 *            selection mode
	 */
	private void createSelectionTable(ImageOrText[] newData, Integer rows,
			Integer columns, SelectionTable mode, boolean[] selected) {
		this.data = newData;

		myTable = new SelectionTableW(newData, rows, columns, mode,
		        multiselectionEnabled);
		if (!multiselectionEnabled) {
			setSelectedIndex(0);
		} else {
			myTable.initSelectedItems(selected);
		}

		myTable.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				handlePopupActionEvent();
			}
		});

		myPopup.getPanel().add(myTable);
    }

	/**
	 * Show or hide item at given position
	 * 
	 * @param col
	 *            item column
	 * @param show
	 *            visibility flag
	 */
	protected void showTableItem(int col, boolean show) {
		myTable.getWidget(0, col).setVisible(show);
	}

	/**
	 * @param popupMenuHandler
	 *            {@link PopupMenuHandler}
	 */
	public void addPopupHandler(PopupMenuHandler popupMenuHandler) {
		this.popupHandler = popupMenuHandler;
	}
	
	/**
	 * @return {@link PopupPanel}
	 */
	public GPopupPanel getMyPopup() {
		return myPopup;
	}

	/**
	 * @return {@link SelectionTableW}
	 */
	public SelectionTableW getMyTable() {
		return myTable;
	}

	/**
	 * called by click on button
	 */
	protected void onClickAction() {
		// called by click on button
		// overridden in (e.g.) EuclidianStayleBar3DW
	}
	
	/**
	 * Pass a popup action event up to the button invoker. If the first button
	 * click triggered our popup (the click was in the triangle region), then we
	 * must pass action events from the popup to the invoker
	 */
	public void handlePopupActionEvent(){
		if (popupHandler != null) {
			popupHandler.fireActionPerformed(this);
		} else {
			Log.debug("PopupMenubutton has null popupHandler");
		}
		
		updateGUI();
		if(!keepVisible) {
			myPopup.hide();
		}
	}
	
	private void updateGUI(){
		if(isIniting) {
			return;
		}

		if (hasTable && !multiselectionEnabled) {
			setIcon(getButtonIcon());
		}
	}
	
	// ==============================================
	// Icon Handling
	// ==============================================

	/**
	 * @return {@link ImageOrText}
	 */
	public ImageOrText getButtonIcon() {
		return data[getSelectedIndex()];
	}
		
	/**
	 * Append a downward triangle image to the right hand side of an input icon.
	 */
	@Override
	public void setIcon(ImageOrText icon) {

		if (isFixedIcon) {			
			super.setIcon(fixedIcon);
			return;
		}

		// add a down_triangle image to the left of the icon
		if (icon != null) {
			super.setIcon(icon);
		} else {
			ImgResourceHelper.setIcon(GuiResources.INSTANCE.toolbar_further_tools(), this);
		}
	}


	/**
	 * @param icon
	 *            {@link ImageOrText}
	 */
	public void setFixedIcon(ImageOrText icon){
		isFixedIcon = true;
		this.fixedIcon = icon;
		setIcon(icon);
	}

	/**
	 * @param selectedIndex
	 *            {@code Integer}
	 */
	public void setSelectedIndex(Integer selectedIndex) {
		myTable.setSelectedIndex(selectedIndex == null ? -1 :selectedIndex.intValue());
		updateGUI();
	}
	
	/**
	 * @param index
	 *            index to be changed
	 * @param selected
	 *            target value for that index
	 */
	public void changeMultiSelection(int index, boolean selected) {
		myTable.changeMultiSelection(index, selected);
		updateGUI();
	}

	@Override
	public void onChange(ChangeEvent event) {
		Log.debug("onchange");
		onSliderInput();
	}

	@Override
	public void onSliderInput() {
		if(mySlider != null) {
			   setSliderValue(mySlider.getValue());
			   
		}
		if (changeEventHandler != null) {
			changeEventHandler.fireActionPerformed(this);
		} else {
			((EuclidianStyleBarW) app.getActiveEuclidianView().getStyleBar())
					.fireActionPerformed(this);
		}
		fireActionPerformed();
		updateGUI();
	}
	
	/**
	 * Fires on index change
	 */
	protected void fireActionPerformed() {
	    //implemented in subclass
    }

	/**
	 * @return {@link Slider}
	 */
	public Slider getMySlider() {
		if (mySlider == null) {
			initSlider();
		}
		return mySlider;
	}
	
	public void showSlider(boolean show) {
		if (show) {
			getMySlider().setVisible(show);
			sliderLabel.setVisible(show);
		} else if (mySlider != null) {
			mySlider.setVisible(false);
			sliderLabel.setVisible(false);
		}
	}

	private void initSlider() {
		
		mySlider = new Slider(0,100);
		mySlider.setMajorTickSpacing(25);
		mySlider.setMinorTickSpacing(5);
		mySlider.addChangeHandler(this);
		Slider.addInputHandler(mySlider.getElement(), this);

		sliderLabel = new Label();
		FlowPanel panel = new FlowPanel();
		panel.add(mySlider);
		panel.add(sliderLabel);
		sliderLabel.addStyleName("popupSliderLabel");
		panel.addStyleName("panelRow");
		myPopup.getPanel().add(panel);
	}
	


	/**
	 * @param value
	 *            {@code int}
	 */
	public void setSliderValue(int value) {
		if (mySlider == null) {
			return;
		}
		mySlider.setValue(value);
		sliderLabel.setText(value + "");
		updateGUI();
	}

	/**
	 * @return selected index of the table
	 */
	public int getSelectedIndex() {
		return myTable.getSelectedIndex();
	}

	/**
	 * @param index
	 *            index
	 * @return whether item with given index is selected
	 */
	public boolean isSelected(int index) {
		return myTable.isSelected(index);
	}

	/**
	 * @param array
	 *            elements (usually GeoElements) whose state is displayed in
	 *            this table
	 * @return
	 */
	public void update(Object[] array) {
	    // will be overwritten from instances
    }
	
	/**
	 * @return selected Object of the {@link SelectionTableW table}
	 */
	public Object getSelectedValue() {
		return myTable.getSelectedValue();
	}
	
	/**
	 * @return {@code int} or {@code -1} if the {@link Slider slider} is null
	 */
	public int getSliderValue() {
		return mySlider == null ? -1 : mySlider.getValue();
	}

	/**
	 * @param euclidianStyleBar
	 *            {@link EuclidianStyleBarW}
	 */
	public void removeActionListener(EuclidianStyleBarW euclidianStyleBar) {
	}

	/**
	 * @param keepVisible
	 *            {@code boolean}
	 */
	public void setKeepVisible(boolean keepVisible) {
		this.keepVisible = keepVisible;
	}
	
	/**
	 * @return {@code true} if it has a slider, {@code false} otherwise
	 */
	protected boolean hasSlider() {
		return mySlider != null;
	}

	/**
	 * explicitly sets who should receive the change events
	 * 
	 * @param handler
	 *            change handler
	 */
	public void setChangeEventHandler(StyleBarW2 handler){
		this.changeEventHandler = handler;
	}
}
