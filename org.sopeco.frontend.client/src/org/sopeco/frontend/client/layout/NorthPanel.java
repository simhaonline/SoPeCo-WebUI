package org.sopeco.frontend.client.layout;

import java.util.logging.Logger;

import org.sopeco.frontend.client.FrontendEntryPoint;
import org.sopeco.frontend.client.R;
import org.sopeco.frontend.client.layout.dialog.AddScenarioDialog;
import org.sopeco.frontend.client.layout.dialog.LogDialog;
import org.sopeco.frontend.client.layout.popups.Confirmation;
import org.sopeco.frontend.client.layout.popups.InputDialog;
import org.sopeco.frontend.client.layout.popups.InputDialogHandler;
import org.sopeco.frontend.client.manager.Manager;
import org.sopeco.frontend.client.manager.ScenarioManager;
import org.sopeco.frontend.client.resources.FrontEndResources;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.UIObject;

/**
 * The Panel of the top. It displays the current database and scenario.
 * 
 * @author Marius Oehler
 * 
 */
public class NorthPanel extends FlowPanel implements ClickHandler, ChangeHandler, InputDialogHandler {

	private static final Logger LOGGER = Logger.getLogger(NorthPanel.class.getName());

	/**
	 * The height of this panel in EM.
	 */
	public static final String PANEL_HEIGHT = "3";

	private static final String DISABLED_CSS_CLASS = "disabled";
	private static final String GRADIENT_CSS = "gradient-blue";
	private static final String IMAGE_CHANGE_ACCOUNT = "images/logout_invert.png";

	private static final String IMAGE_EXPORT = "images/download_invert.png";
	private static final String IMAGE_LOG = "images/log_invert.png";
	private static final String IMAGE_SATELLITE = "images/satellite_invert.png";
	private static final String IMAGE_SCENARIO_ADD = "images/scenario_add_invert.png";
	private static final String IMAGE_SCENARIO_CLONE = "images/scenario_clone_invert.png";
	private static final String IMAGE_SCENARIO_REMOVE = "images/scenario_delete_invert.png";
	private static final String IMG_BUTTON_CSS_CLASS = "imgButton";
	private static final String NAVI_PANEL_HEIGHT = "2.8em";

	private static final String SAP_RESEARCH_LOGO = "images/sap_research.png";

	private static final String SAP_RESEARCH_LOGO_ID = "sapResearchLogo";

	private static final String SEPARATOR_CSS_CLASS = "separator";
	private HTML connectedToText, htmlSelectScenario;
	private Image imageSatellite, imageExport, researchLogo, imageScenarioAdd, imageScenarioRemove, imageChangeAccount,
			imageLog, imageScenarioClone;
	private InputDialog inputClone;

	private ListBox listboxScenarios;

	private HorizontalPanel navigationPanel;

	public NorthPanel(MainLayoutPanel parent) {
		FrontEndResources.loadTopNavigationCSS();
		initialize();
	}

	@Override
	public void onChange(ChangeEvent event) {
		if (event.getSource() == listboxScenarios) {
			switchScenario(getSelectedItem());
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource() == imageSatellite && isEnabled(imageSatellite)) {
			MEControllerBox.showBox();
		} else if (event.getSource() == imageExport && isEnabled(imageExport)) {
			ExportBox.showExportBox();
		} else if (event.getSource() == imageScenarioAdd && isEnabled(imageScenarioAdd)) {
			AddScenarioDialog addScenarioDialog = new AddScenarioDialog();
			addScenarioDialog.center();
		} else if (event.getSource() == imageScenarioClone && isEnabled(imageScenarioClone)) {
			cloneScenario();
		} else if (event.getSource() == imageScenarioRemove && isEnabled(imageScenarioRemove)) {
			removeScenario();
		} else if (event.getSource() == imageChangeAccount) {
			FrontendEntryPoint.get().changeDatabase();
		} else if (event.getSource() == imageLog) {
			LogDialog.show();
		}
	}

	@Override
	public void onInput(InputDialog source, String value) {
		if (source == inputClone) {
			ScenarioManager.get().cloneCurrentScenario(value);
		}
	}

	/**
	 * Disables or enables the buttons....
	 * 
	 * @param enabled
	 */
	public void setButtonsEnabled(boolean enabled) {
		if (enabled) {
			listboxScenarios.setEnabled(true);
			imageScenarioAdd.removeStyleName(DISABLED_CSS_CLASS);
			imageScenarioClone.removeStyleName(DISABLED_CSS_CLASS);
			imageScenarioRemove.removeStyleName(DISABLED_CSS_CLASS);
			imageExport.removeStyleName(DISABLED_CSS_CLASS);
			imageSatellite.removeStyleName(DISABLED_CSS_CLASS);
		} else {
			listboxScenarios.addItem(R.get("no_scenarios"));
			listboxScenarios.setEnabled(false);
			imageScenarioAdd.addStyleName(DISABLED_CSS_CLASS);
			imageScenarioClone.addStyleName(DISABLED_CSS_CLASS);
			imageScenarioRemove.addStyleName(DISABLED_CSS_CLASS);
			imageExport.addStyleName(DISABLED_CSS_CLASS);
			imageSatellite.addStyleName(DISABLED_CSS_CLASS);
			Manager.get().getAccountDetails().setSelectedScenario(null);
		}
	}

	/**
	 * Refresh the content of the listbox, which contains all names of available
	 * scenarios and where the user can switch the scenario.
	 * 
	 * @param names
	 *            the scenario names
	 */
	public void updateScenarioList() {
		LOGGER.fine("Update the scenario list");
		listboxScenarios.clear();
		String[] names = Manager.get().getAccountDetails().getScenarioNames();
		if (names == null || names.length == 0) {
			setButtonsEnabled(false);
		} else {
			setButtonsEnabled(true);
			for (String name : names) {
				listboxScenarios.addItem(name);
			}
			selectListboxItem(Manager.get().getAccountDetails().getSelectedScenario());
		}
	}

	/**
	 *
	 */
	public void updateScenarioListAndSwitch(String scenarioName) {
		updateScenarioList();
		switchScenario(scenarioName);
	}

	private void cloneScenario() {
		if (inputClone == null) {
			inputClone = new InputDialog(R.get("scenario_clone"), R.get("cloneScenarioName") + ":");
			inputClone.addHandler(this);
		}
		inputClone.setText("");
		inputClone.center();
	}

	/**
	 * Creates a HTML-DIV-Element. The added classes cause a appearance of a
	 * small bar-separator.
	 * 
	 * @return
	 */
	private HTML createSeparator() {
		HTML ret = new HTML();
		ret.addStyleName(SEPARATOR_CSS_CLASS);
		ret.addStyleName(GRADIENT_CSS);
		return ret;
	}

	/**
	 * Returns the selected item of the listbox.
	 * 
	 * @return
	 */
	private String getSelectedItem() {
		return listboxScenarios.getItemText(listboxScenarios.getSelectedIndex());
	}

	/**
	 * initialize the user interface.
	 */
	private void initialize() {
		setSize("100%", NAVI_PANEL_HEIGHT); // .nPanel in CSS Style
		addStyleName("nPanel");

		navigationPanel = new HorizontalPanel();
		navigationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		navigationPanel.addStyleName("north_hPanel");
		navigationPanel.setHeight(NAVI_PANEL_HEIGHT);

		connectedToText = new HTML();
		navigationPanel.add(connectedToText);

		imageChangeAccount = new Image(IMAGE_CHANGE_ACCOUNT);
		imageChangeAccount.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageChangeAccount.setTitle(R.get("Logout"));
		imageChangeAccount.addClickHandler(this);
		navigationPanel.add(imageChangeAccount);

		navigationPanel.add(createSeparator());

		htmlSelectScenario = new HTML(R.get("scenario_select") + ":");
		navigationPanel.add(htmlSelectScenario);

		listboxScenarios = new ListBox();
		listboxScenarios.setSize("120px", "1.8em");
		listboxScenarios.setVisibleItemCount(1);
		listboxScenarios.addChangeHandler(this);
		navigationPanel.add(listboxScenarios);

		imageScenarioAdd = new Image(IMAGE_SCENARIO_ADD);
		imageScenarioAdd.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageScenarioAdd.setTitle(R.get("scenario_add"));
		imageScenarioAdd.addClickHandler(this);
		navigationPanel.add(imageScenarioAdd);

		imageScenarioClone = new Image(IMAGE_SCENARIO_CLONE);
		imageScenarioClone.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageScenarioClone.setTitle(R.get("scenario_clone"));
		imageScenarioClone.addClickHandler(this);
		navigationPanel.add(imageScenarioClone);

		imageScenarioRemove = new Image(IMAGE_SCENARIO_REMOVE);
		imageScenarioRemove.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageScenarioRemove.setTitle(R.get("scenario_remove"));
		imageScenarioRemove.addClickHandler(this);
		navigationPanel.add(imageScenarioRemove);

		navigationPanel.add(createSeparator());

		imageExport = new Image(IMAGE_EXPORT);
		imageExport.addClickHandler(this);
		imageExport.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageExport.setTitle(R.get("exportModel"));
		navigationPanel.add(imageExport);

		navigationPanel.add(createSeparator());

		imageSatellite = new Image(IMAGE_SATELLITE);
		imageSatellite.addClickHandler(this);
		imageSatellite.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageSatellite.setTitle(R.get("mecSettings"));
		navigationPanel.add(imageSatellite);

		navigationPanel.add(createSeparator());

		imageLog = new Image(IMAGE_LOG);
		imageLog.addClickHandler(this);
		imageLog.addStyleName(IMG_BUTTON_CSS_CLASS);
		imageLog.setTitle(R.get("showLog"));
		navigationPanel.add(imageLog);

		navigationPanel.add(createSeparator());

		researchLogo = new Image(SAP_RESEARCH_LOGO);
		researchLogo.getElement().setId(SAP_RESEARCH_LOGO_ID);
		getElement().appendChild(researchLogo.getElement());

		add(navigationPanel);

		connectedToText.setHTML(R.get("connected_to") + ": <b>"
				+ FrontendEntryPoint.get().getConnectedDatabase().getDbName() + "</b>");

		updateScenarioList();
	}

	/**
	 * Returns whether the UIObject is enabled or not. It only checks, if the
	 * class attribute contains the "disabled" class.
	 * 
	 * @param object
	 * @return
	 */
	private boolean isEnabled(UIObject object) {
		for (String c : object.getStyleName().split(" ")) {
			if (c.equals(DISABLED_CSS_CLASS)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Confirmation box whether the current scenario should be deleted. If OK is
	 * clicked, the action is passed to the ScenarioManager to remove it.
	 */
	private void removeScenario() {
		final String selectedScenario = Manager.get().getAccountDetails().getSelectedScenario();
		String msg = R.get("confRemoveScenario") + " <b>'" + selectedScenario + "'</b>?";
		Confirmation.confirm(msg, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ScenarioManager.get().removeScenario(selectedScenario);
			}
		});
	}

	/**
	 * Select the item with the given String in the listbox, if it exists.
	 * 
	 * @param itemToSelect
	 */
	private void selectListboxItem(String itemToSelect) {
		if (itemToSelect == null) {
			return;
		}
		for (int i = 0; i < listboxScenarios.getItemCount(); i++) {
			if (listboxScenarios.getItemText(i).equals(itemToSelect)) {
				listboxScenarios.setSelectedIndex(i);
				return;
			}
		}
	}

	/**
	 * Changes the scenario in the one, that has the specified name.
	 */
	private void switchScenario(String scenarioName) {
		ScenarioManager.get().switchScenario(scenarioName);
	}
}
