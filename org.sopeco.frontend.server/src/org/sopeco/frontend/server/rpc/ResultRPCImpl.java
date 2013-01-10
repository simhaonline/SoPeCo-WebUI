package org.sopeco.frontend.server.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sopeco.frontend.client.rpc.ResultRPC;
import org.sopeco.frontend.server.papconnector.ChartCreator;
import org.sopeco.frontend.server.user.User;
import org.sopeco.frontend.server.user.UserManager;
import org.sopeco.frontend.shared.definitions.result.SharedExperimentRuns;
import org.sopeco.frontend.shared.definitions.result.SharedExperimentSeries;
import org.sopeco.frontend.shared.definitions.result.SharedScenarioInstance;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.SimpleDataSet;
import org.sopeco.persistence.dataset.SimpleDataSetColumn;
import org.sopeco.persistence.dataset.SimpleDataSetRow;
import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.exceptions.DataNotFoundException;
import org.sopeco.persistence.util.DataSetCsvHandler;

/**
 * 
 * @author Marius Oehler
 * 
 */
public class ResultRPCImpl extends SuperRemoteServlet implements ResultRPC {

	/**	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void getResults() {
		// getUser().getCurrentPersistenceProvider().loadAllScenarioInstances().get(0).getExperimentSeriesList().get(0).getLatestExperimentSeriesRun().getSuccessfulResultDataSet().

		try {
			List<ScenarioInstance> instances = getUser().getCurrentPersistenceProvider().loadAllScenarioInstances();

			System.out.println("");

			DataSetAggregated d = new DataSetAggregated();

			SimpleDataSet sd = d.convertToSimpleDataSet();

			DataSetCsvHandler handler = new DataSetCsvHandler(';', '#', true);
			// handler.store(dataset, fileName)
			// handler.
		} catch (DataNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<SharedScenarioInstance> getInstances(String scenarioName) {
		try {
			List<ScenarioInstance> scenarioList = getUser().getCurrentPersistenceProvider().loadScenarioInstances(
					scenarioName);

			List<SharedScenarioInstance> retList = new ArrayList<SharedScenarioInstance>();
			for (ScenarioInstance instance : scenarioList) {
				retList.add(convertInstance(instance));
			}
			return retList;
		} catch (DataNotFoundException e) {
			return new ArrayList<SharedScenarioInstance>();
		}
	}

	/**
	 * Creates a SharedScenarioInstance out of a ScenarioInstance which can send
	 * to the FrontEnd.
	 * 
	 * @param instance
	 * @return
	 */
	private SharedScenarioInstance convertInstance(ScenarioInstance instance) {
		SharedScenarioInstance retInstance = new SharedScenarioInstance();

		retInstance.setScenarioName(instance.getName());
		retInstance.setControllerUrl(instance.getMeasurementEnvironmentUrl());

		for (ExperimentSeries series : instance.getExperimentSeriesList()) {
			SharedExperimentSeries sharedSeries = new SharedExperimentSeries();
			sharedSeries.setExperimentName(series.getName());
			sharedSeries.setVersion(series.getVersion());

			for (ExperimentSeriesRun run : series.getExperimentSeriesRuns()) {
				SharedExperimentRuns sharedRun = new SharedExperimentRuns();
				sharedRun.setTimestamp(run.getTimestamp());
				sharedRun.setLabel(run.getLabel());

				sharedSeries.addExperimentRun(sharedRun);
			}

			retInstance.addExperimentSeries(sharedSeries);
		}

		return retInstance;
	}

	@Override
	public String getResultAsR(String scenario, String exoerimentSeries, String url, long timestamp) {
		try {
			ScenarioInstance instance = getScenarioInstance(getSessionId(), scenario, url);
			ExperimentSeries series = getSeries(instance, exoerimentSeries);
			ExperimentSeriesRun run = getRun(series, timestamp);

			DataSetAggregated dataset = run.getSuccessfulResultDataSet();
			SimpleDataSet simpleDataset = dataset.convertToSimpleDataSet();

			StringBuffer rValue = new StringBuffer();

			int i = 0;
			// for (SimpleDataSetRow row : simpleDataset.getRowList()) {
			for (Iterator<SimpleDataSetRow> rowIter = simpleDataset.getRowList().iterator(); rowIter.hasNext(); i++) {
				rValue.append("r");
				rValue.append(i);
				rValue.append(" <- c(");

				for (Iterator<ParameterValue> colIter = rowIter.next().getRowValues().iterator(); colIter.hasNext();) {
					Object val = colIter.next().getValue();
					if (val instanceof String) {
						rValue.append("\"");
						rValue.append(val.toString());
						rValue.append("\"");
					} else {
						rValue.append(val.toString());
					}
					if (colIter.hasNext()) {
						rValue.append(", ");
					}
				}

				rValue.append(")\n");
			}

			rValue.append("myframe <- data.frame(");
			for (int n = 0; n < i; n++) {
				rValue.append("r");
				rValue.append(n);
				if (n + 1 < i) {
					rValue.append(", ");
				}
			}			
			rValue.append(")\ncolnames(myframe) <- c(");

			for (Iterator<SimpleDataSetColumn> iter = simpleDataset.getColumns().iterator(); iter.hasNext();) {
				rValue.append("\"");
				rValue.append(iter.next().getParameter().getName());
				rValue.append("\"");
				if (iter.hasNext()) {
					rValue.append(", ");
				}
			}			
			rValue.append(")");

			return rValue.toString();
		} catch (DataNotFoundException e) {
			return "No Data Found";
		}
	}

	/**
	 *
	 */
	private ExperimentSeriesRun getRun(ExperimentSeries series, Long timestamp) throws DataNotFoundException {
		for (ExperimentSeriesRun run : series.getExperimentSeriesRuns()) {
			System.out.println(run.getTimestamp() + " " + timestamp);
			if (timestamp.equals(run.getTimestamp())) {
				return run;
			}
		}

		throw new DataNotFoundException("No ExperimentSeriesRun with timestamp '" + timestamp + "' found..");
	}

	/**
	 * 
	 */
	private ExperimentSeries getSeries(ScenarioInstance instance, String name) throws DataNotFoundException {
		for (ExperimentSeries series : instance.getExperimentSeriesList()) {
			if (series.getName().equals(name)) {
				return series;
			}
		}

		throw new DataNotFoundException("No ExperimentSeries '" + name + "' found..");
	}

	/**
	 * 
	 */
	private ScenarioInstance getScenarioInstance(String sId, String scenarioName, String url)
			throws DataNotFoundException {
		User user = UserManager.getUser(sId);
		if (user == null) {
			throw new DataNotFoundException("No user at session '" + sId + "' found..");
		}
		ScenarioInstance instance = user.getCurrentPersistenceProvider().loadScenarioInstance(scenarioName, url);
		return instance;
	}

	@Override
	public String getChartUrl(String scenario, String experimentSeries,
			String url, long timestamp) {
		ScenarioInstance instance;
		try {
			instance = getScenarioInstance(getSessionId(), scenario, url);
			ExperimentSeries series = getSeries(instance, experimentSeries);
			ExperimentSeriesRun run = getRun(series, timestamp);

			DataSetAggregated dataset = run.getSuccessfulResultDataSet();
			SimpleDataSet simpleDataset = dataset.convertToSimpleDataSet();

			int i = 0;
			List<ParameterValue[]> paramList = new ArrayList<ParameterValue[]>();
			for (Iterator<SimpleDataSetRow> rowIter = simpleDataset.getRowList().iterator(); rowIter.hasNext(); i++) {
				paramList.add(rowIter.next().getRowValues().toArray(new ParameterValue[0]));
			}
			
			StringBuffer rValue = new StringBuffer();
			
			for (int j = 0; j < paramList.get(0).length; j++){
				rValue.append("c");
				rValue.append(j);
				rValue.append(" <- c(");
				for (int k = 0; k < paramList.size(); k++){
					rValue.append(paramList.get(k)[j].getValue().toString());
					if (k < paramList.size() -1){
						rValue.append(", ");
					}
					rValue.append(")\n");
				}
			}
			
			String urlString = ChartCreator.getSimpleChartLink(run.getLabel(), rValue.toString());
			return urlString;
		} catch (DataNotFoundException e) {
			return "No Data Found";
		}
		
	}
}
