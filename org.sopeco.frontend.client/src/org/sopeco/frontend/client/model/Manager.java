package org.sopeco.frontend.client.model;

import java.util.logging.Logger;

import org.sopeco.frontend.client.entities.AccountDetails;
import org.sopeco.frontend.client.entities.ScenarioDetails;
import org.sopeco.frontend.client.helper.DBManager;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Marius Oehler
 * 
 */
public final class Manager {

	private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());

	private static Manager manager;

	/** Possible status of an MEController. */
	public enum ControllerStatus {
		/** Controller is online. */
		ONLINE,
		/** Controller is offline. */
		OFFLINE,
		/** Unknown status. E.g: controller was not checked. */
		UNKNOWN
	}

	/** Attributes with default values */
	// private String controllerProtocol = "rmi://";
	// private String controllerHost = "localhost";
	// private String controllerName = "";
	// private int controllerPort = 1099;
	private long controllerLastCheck = -1;
	private ControllerStatus controllerLastStatus = ControllerStatus.UNKNOWN;
	private AccountDetails accountDetails = null;

	/**
	 * Sends the current AccountDetails to the backend to store it in the
	 * database.
	 */
	public void storeAccountDetails() {
		LOGGER.info("Store AccountDetails");
		DBManager.getDbManager().storeAccountDetails(accountDetails, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
			}

			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	/**
	 * Returns the ScenariODetail object of the current selected scenario.
	 * 
	 * @return
	 */
	public ScenarioDetails getCurrentScenarioDetails() {
		return accountDetails.getScenarioDetail(accountDetails.getSelectedScenario());
	}

	/**
	 * @return the accountDetails
	 */
	public AccountDetails getAccountDetails() {
		return accountDetails;
	}

	/**
	 * @param pAccountDetails
	 *            the accountDetails to set
	 */
	public void setAccountDetails(AccountDetails pAccountDetails) {
		this.accountDetails = pAccountDetails;
	}

	/**
	 * The full compounded URL of the MEController.
	 * 
	 * @return
	 */
	public String getControllerUrl() {
		if (getCurrentScenarioDetails() == null) {
			return "";
		}

		return getCurrentScenarioDetails().getControllerProtocol() + getCurrentScenarioDetails().getControllerHost()
				+ ":" + getCurrentScenarioDetails().getControllerPort() + "/"
				+ getCurrentScenarioDetails().getControllerName();
	}

	/**
	 * @return the controllerLastStatus
	 */
	public ControllerStatus getControllerLastStatus() {
		return controllerLastStatus;
	}

	/**
	 * @param pControllerLastStatus
	 *            the controllerLastStatus to set
	 */
	public void setControllerLastStatus(ControllerStatus pControllerLastStatus) {
		this.controllerLastStatus = pControllerLastStatus;
	}

	/**
	 * @return the controllerLastCheck
	 */
	public long getControllerLastCheck() {
		return controllerLastCheck;
	}

	/**
	 * @param pControllerLastCheck
	 *            the controllerLastCheck to set
	 */
	public void setControllerLastCheck(long pControllerLastCheck) {
		this.controllerLastCheck = pControllerLastCheck;
	}

	// /**
	// * @param pControllerProtocol
	// * the controllerProtocol to set
	// */
	// public void setControllerProtocol(String pControllerProtocol) {
	// this.controllerProtocol = pControllerProtocol;
	// }
	//
	// /**
	// * @return the controllerHost
	// */
	// public String getControllerHost() {
	// return controllerHost;
	// }
	//
	// /**
	// * @param pControllerHost
	// * the controllerHost to set
	// */
	// public void setControllerHost(String pControllerHost) {
	// this.controllerHost = pControllerHost;
	// }
	//
	// /**
	// * @return the controllerName
	// */
	// public String getControllerName() {
	// return controllerName;
	// }
	//
	// /**
	// * @param pControllerName
	// * the controllerName to set
	// */
	// public void setControllerName(String pControllerName) {
	// this.controllerName = pControllerName;
	// }
	//
	// /**
	// * @return the controllerPort
	// */
	// public int getControllerPort() {
	// return controllerPort;
	// }
	//
	// /**
	// * @param pControllerPort
	// * the controllerPort to set
	// */
	// public void setControllerPort(int pControllerPort) {
	// this.controllerPort = pControllerPort;
	// }

	private Manager() {
	}

	public static Manager get() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}
}
