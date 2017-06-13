package org.geogebra.web.web.javax.swing;

import org.geogebra.common.awt.GPoint;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.main.Feature;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.gui.GPopupPanel;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.css.GuiResources;
import org.geogebra.web.web.css.MaterialDesignResources;
import org.geogebra.web.web.html5.AttachedToDOM;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.Widget;

/**
 * Popup menu for web.
 * 
 * @author Judit Elias
 */
public class GPopupMenuW implements AttachedToDOM {

	protected GPopupPanel popupPanel;
	protected PopupMenuBar popupMenu;
	private int popupMenuSize = 0;
	/*
	 * popup panel for submenu this field used to avoid having more submenu at
	 * the same time
	 */
	GPopupMenuW subPopup;
	private AppW app;

	/**
	 * @param app
	 * 
	 *            Creates a popup menu. App needed for get environment style
	 */
	public GPopupMenuW(AppW app) {
		this.app = app;
		popupPanel = new GPopupPanel(app.getPanel());
		Browser.scale(popupPanel.getElement(), app.getArticleElement()
		        .getScaleX(), 0, 0);
		popupMenu = new PopupMenuBar(true);
		popupMenu.setAutoOpen(true);
		popupPanel.add(popupMenu);

		popupPanel.addCloseHandler(new CloseHandler<GPopupPanel>() {

			@Override
			public void onClose(CloseEvent<GPopupPanel> event) {
				if (subPopup != null) {
					subPopup.removeFromDOM();
					subPopup = null;
				}
			}

		});

		popupPanel.setAutoHideEnabled(true);
	}

	/**
	 * Constructor for submenu-popups
	 * 
	 * @param mb
	 *            menu
	 */
	public GPopupMenuW(MenuBar mb, AppW app) {
		popupPanel = new GPopupPanel(app.getPanel());
		popupPanel.add(mb);
		if (app.has(Feature.NEW_TOOLBAR)) {
			popupPanel.addStyleName("matSubMenu");
		}
	}

	// public void add(MenuItem mi) {
	// impl.addItem(mi);
	//
	// }

	/**
	 * @param v
	 *            whether to show this
	 */
	public void setVisible(boolean v) {
		popupPanel.setVisible(v);
	}

	/**
	 * Shows the popup menu, ensures that the popup menu must be on the client
	 * area.
	 */
	public final void show(GPoint p) {
		int top = (int) (p.getY() - (app.getPanel().getAbsoluteTop()
				/ app.getArticleElement().getScaleY()));
		int left = (int) (p.getX() - (app.getPanel().getAbsoluteLeft()
				/ app.getArticleElement().getScaleX()));
		boolean newPoz = false;
		showAtPoint(new GPoint(left, top));
		if (left + popupPanel.getOffsetWidth()
		        * app.getArticleElement().getScaleX() > Window.getClientWidth()
		        + Window.getScrollLeft()) {
			left = Window.getClientWidth() - popupPanel.getOffsetWidth()
			        + Window.getScrollLeft();
			newPoz = true;
		} else {
			left = (int) (left * app.getArticleElement().getScaleX());
		}
		if (top + popupPanel.getOffsetHeight()
		        * app.getArticleElement().getScaleY() > Window
		        .getClientHeight() + Window.getScrollTop()) {
			top = Window.getClientHeight() - popupPanel.getOffsetHeight()
			        + Window.getScrollTop();
			newPoz = true;
		} else {
			top = (int) (top * app.getArticleElement().getScaleY());
		}

		if (newPoz || !Kernel.isEqual(1, app.getArticleElement().getScaleX())) {
			popupPanel.setPopupPosition(left, top);
			// App.debug(left + "x" + top);
		}
	}

	/**
	 * Shows the popup menu at the p point, independently of there is enough
	 * place for the popup menu. (Maybe some details of the popup menu won't be
	 * visible.)
	 */
	public void showAtPoint(GPoint p) {
		popupPanel.setPopupPosition(p.getX(), p.getY());
		popupPanel.show();
	}

	public void show(Canvas c, int x, int y) {
		show(new GPoint(
		        (int) (c.getAbsoluteLeft()
						/ app.getArticleElement().getScaleX()
						+ x),
		        (int) (c.getAbsoluteTop() / app.getArticleElement().getScaleY() + y)));
	}

	public void show(Widget c, int x, int y) {
		show(new GPoint(c.getAbsoluteLeft() + x, c.getAbsoluteTop() + y));
	}

	@Override
	public void removeFromDOM() {
		removeSubPopup();
		popupPanel.removeFromParent();
	}

	public void clearItems() {
		popupMenu.clearItems();
	}

	public int getComponentCount() {
		return popupMenuSize;
	}

	public void addSeparator() {
		if (!app.has(Feature.NEW_TOOLBAR)) {
			popupMenu.addSeparator();
		}
	}

	public void addVerticalSeparator() {
		MenuItemSeparator separator = new MenuItemSeparator();
		separator.getElement().getFirstChildElement().setClassName("Separator");
		popupMenu.addSeparator(separator);
	}

	private void addHideCommandFor(MenuItem item) {
		MenuBar submenu = item.getSubMenu();
		if (submenu == null) {
			final ScheduledCommand oldCmd = item.getScheduledCommand();
			ScheduledCommand cmd = new ScheduledCommand() {
				@Override
				public void execute() {
					oldCmd.execute();
					popupPanel.hide();
				}
			};
			item.setScheduledCommand(cmd);
		} else {
			// CloseHandler<PopupPanel> closehandler = new
			// CloseHandler<PopupPanel>(){
			// public void onClose(CloseEvent<PopupPanel> event) {
			// App.debug("popuppanel closed");
			// }
			// };
			// submenu.addCloseHandler(closehandler);
			// submenu.addHandler(closehandler, CloseEvent.getType());

			submenu.addHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					popupPanel.hide();
				}
			}, ClickEvent.getType());
		}
	}

	private static ImageResource getSubMenuIcon(boolean isRTL,
			boolean isNewDesign) {
		if (isNewDesign) {
			return isRTL
					? MaterialDesignResources.INSTANCE.arrow_drop_left_black()
					: MaterialDesignResources.INSTANCE.arrow_drop_right_black();
		}
		return isRTL ? GuiResources.INSTANCE.menuBarSubMenuIconRTL()
		        : GuiResources.INSTANCE.menuBarSubMenuIconLTR();
	}

	// public void addItem(final MenuItem item) {
	// addHideCommandFor(item);
	// popupMenu.addItem(item);
	// popupMenuSize++;
	// }

	public void addItem(final MenuItem item) {
		addItem(item, true);
	}

	public void addItem(final MenuItem item, boolean autoHide) {
		final MenuBar subMenu = item.getSubMenu();
		if (autoHide) {
			addHideCommandFor(item);
		}
		if (subMenu == null) {
			popupMenu.addItem(item);
		} else {
			// The submenu is not added for the menu as submenu,
			// but this will be placed on a different popup panel.
			// In this way we can set this popup panel's position easily.
			String itemHTML = item.getHTML();
			ScheduledCommand itemCommand = null;
			final MenuItem newItem = new MenuItem(itemHTML, true, itemCommand);
			newItem.setStyleName(item.getStyleName());
			newItem.getElement().setAttribute("hasPopup", "true");
			popupMenu.addItem(newItem);
			itemCommand = new ScheduledCommand() {
				@Override
				public void execute() {
					int xCord, yCord;
					if (subPopup != null) {
						subPopup.removeFromDOM();
					}
					subPopup = new GPopupMenuW(subMenu, app);
					subPopup.setVisible(true);
					int xPercent = 0;
					// Calculate the position of the "submenu", and show it
					if (LocaleInfo.getCurrentLocale().isRTL()) {
						xCord = getLeftSubPopupXCord();
						if (xCord < 0) {
							xCord = getRightSubPopupXCord();
						} else {
							xPercent = 100;
						}
					} else {
						xCord = getRightSubPopupXCord();
						if (xCord + getSubPopupWidth() > Window
						        .getClientWidth()) {
							xCord = getLeftSubPopupXCord();
							xPercent = 100;
						}
					}
					yCord = Math.min(
							newItem.getAbsoluteTop()
									- app.getPanel().getAbsoluteTop(),
					        Window.getClientHeight() - getSubPopupHeight());
					Browser.scale(subPopup.getPopupPanel().getElement(), app
					        .getArticleElement().getScaleX(), xPercent, 0);
					subPopup.showAtPoint(new GPoint(xCord, yCord));
				}
			};
			newItem.setScheduledCommand(itemCommand);

			// adding arrow for the menuitem
			Element td = DOM.createTD();
			DOM.setElementProperty(td, "vAlign", "middle");
			td.addClassName("subMenuIcon");
			ImageResource imgRes = getSubMenuIcon(LocaleInfo.getCurrentLocale()
					.isRTL(), app.has(Feature.NEW_TOOLBAR));
			td.setInnerSafeHtml(AbstractImagePrototype.create(imgRes)
			        .getSafeHtml());
			newItem.getElement().setAttribute("colspan", "1");
			DOM.appendChild((Element) newItem.getElement().getParentNode(), td);
		}
		popupMenuSize++;

		item.addStyleName("gPopupMenu_item");
	}

	/**
	 * @return the width of the submenu.
	 */
	public int getSubPopupWidth() {
		int width;
		boolean shown = subPopup.popupPanel.isShowing();
		if (!shown) {
			subPopup.popupPanel.show();
		}
		width = subPopup.popupPanel.getOffsetWidth();
		if (!shown) {
			subPopup.popupPanel.hide();
		}
		return width;
	}

	/**
	 * @return the height of the submenu.
	 */
	public int getSubPopupHeight() {
		int ret;
		boolean shown = subPopup.popupPanel.isShowing();
		if (!shown) {
			subPopup.popupPanel.show();
		}
		ret = subPopup.popupPanel.getOffsetHeight();
		if (!shown) {
			subPopup.popupPanel.hide();
		}
		return ret;
	}

	/**
	 * Gets the submenu's suggested absolute left position in pixels, as
	 * measured from the browser window's client area, in case of the submenu is
	 * on the left side of its parent menu.
	 * 
	 * @return submenu's left position in pixels
	 */
	public int getLeftSubPopupXCord() {
		int xCord;
		xCord = popupPanel.getAbsoluteLeft() - getSubPopupWidth()
				- app.getPanel().getAbsoluteLeft();
		return xCord;
	}

	/**
	 * Gets the submenu's suggested absolute left position in pixels, as
	 * measured from the browser window's client area, in case of the submenu is
	 * on the right side of its parent menu.
	 * 
	 * @return submenu's left position in pixels
	 */
	public int getRightSubPopupXCord() {
		return popupPanel.getAbsoluteLeft()
		        + (int) (popupPanel.getOffsetWidth() * app.getArticleElement()
						.getScaleX())
				- app.getPanel().getAbsoluteLeft();
	}

	public void addItem(GCheckBoxMenuItem item) {
		addItem(item.getMenuItem());

	}

	public void addItem(String s, ScheduledCommand c) {
		addItem(new MenuItem(s, c));
	}

	public void hide() {
		popupPanel.hide();
	}

	public MenuBar getPopupMenu() {
		return popupMenu;
	}

	public GPopupPanel getPopupPanel() {
		return popupPanel;
	}

	public void removeSubPopup() {
		if (subPopup != null) {
			subPopup.removeFromDOM();
			subPopup = null;
		}
	}

	private class PopupMenuBar extends MenuBar {

		public PopupMenuBar(boolean vertical) {
			super(vertical);
		}

		private MenuItem findItem(Element hItem) {
			for (MenuItem item : getItems()) {
				if (DOM.isOrHasChild(item.getElement(), hItem)) {
					return item;
				}
			}
			return null;
		}

		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEOVER: {
				MenuItem item = findItem(DOM.eventGetTarget(event));
				if (item != null) {
					if ("true".equals(item.getElement()
							.getAttribute("hasPopup"))) {
						item.getScheduledCommand().execute();
					} else {
						removeSubPopup();
					}
				}
				break;
			}
			}
			super.onBrowserEvent(event);
		}
	}

}
