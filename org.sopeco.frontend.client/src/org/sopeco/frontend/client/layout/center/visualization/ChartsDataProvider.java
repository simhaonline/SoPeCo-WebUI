package org.sopeco.frontend.client.layout.center.visualization;

import java.util.List;

import org.sopeco.frontend.client.layout.MainLayoutPanel;
import org.sopeco.frontend.client.layout.center.visualization.VisualizationController.Status;
import org.sopeco.frontend.client.rpc.RPC;
import org.sopeco.frontend.shared.entities.Visualization;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class ChartsDataProvider extends AsyncDataProvider<Visualization> {
	private VisualizationController visualizationController;
	
	public ChartsDataProvider(VisualizationController visualizationController) {
		this.visualizationController = visualizationController;
	}

	@Override
	protected void onRangeChanged(HasData<Visualization> display) {
		final Range range = display.getVisibleRange();
		if (visualizationController.getStatus() != Status.BUSY){
			visualizationController.setStatus(Status.LOADING);
		}
		RPC.getVisualizationRPC().getVisualizations(range.getStart(), range.getLength(), new AsyncCallback<List<Visualization>>() {
			
			@Override
			public void onSuccess(List<Visualization> result) {
				updateRowData(range.getStart(), result);
				if (visualizationController.getStatus() != Status.BUSY){
					visualizationController.setStatus(Status.READY);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
	}

	public void addDataDisplay(CellList<Visualization> visualizationList) {
		super.addDataDisplay(visualizationList);
	}

}
