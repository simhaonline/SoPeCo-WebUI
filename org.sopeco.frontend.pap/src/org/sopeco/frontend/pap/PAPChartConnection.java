/**
 * Copyright (c) 2013 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.frontend.pap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sopeco.engine.registry.ISoPeCoExtension;
import org.sopeco.frontend.server.chartconnector.IChartConnection;
import org.sopeco.frontend.shared.entities.ChartOptions;
import org.sopeco.frontend.shared.entities.ChartParameter;
import org.sopeco.frontend.shared.entities.Visualization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class PAPChartConnection implements IChartConnection {
	private static final String BASE_URL = "http://deqkal279.qkal.sap.corp:8080/pap/j/result/s/";
	private static PAPConnector papConnector = new PAPConnector();
	private static final String SQL_DUMMY = "SELECT * FROM perf_pap.pvexperiment";
	private static HashMap<String, String> savedProjects = new HashMap<String, String>();// projectname, pid
	private static String uid;
	
	private ISoPeCoExtension<?> provider;

	public PAPChartConnection(ISoPeCoExtension<?> provider) {
		this.provider = provider;
		papConnector.addUser("testuser", "testmail@test.de");
		uid = papConnector.verifyUser("testuser");
		String pList = papConnector.getProjectList(uid);
		//TODO check pList for null
		JsonElement elem = new JsonParser().parse(pList).getAsJsonObject()
				.get("adProject");
		JsonArray jArray;
		if (elem instanceof JsonArray){
			jArray = elem.getAsJsonArray();
		}
		else {
			jArray = new JsonArray();
			jArray.add(elem);
		}
		
		for (int i = 0; i < jArray.size(); i++) {
			String pid = jArray.get(i).getAsJsonObject().get("PID")
					.getAsString();
			String pname = jArray.get(i).getAsJsonObject().get("projectName")
					.getAsString();
			savedProjects.put(pname, pid);
		}
	}

	private String createProcessScript(Double[][] data, String[] chartParameter){
		StringBuffer rValue = new StringBuffer();

		for (int i = 0; i < data.length; i++) {
			rValue.append(chartParameter[i]);
			rValue.append(i);
			rValue.append(" <- c(");
			for (int j = 0; j < data[i].length; j++) {
				rValue.append(data[i][j]);
				if (j < data[i].length - 1) {
					rValue.append(", ");
				}
			}
			rValue.append(")\n");
		}
		return rValue.toString();
	}
	
	private String creatOutputScript(ChartOptions options, String[] chartParameter){
		StringBuffer outputScript = new StringBuffer();
		switch (options.getType()){
		case BARCHART:
			outputScript.append("data <- do.call(rbind, list(");
			for (int i = 1; i < chartParameter.length; i++){
				outputScript.append(chartParameter[i]+i);
				if (i < chartParameter.length-1){
					outputScript.append(",");
				}
			}
			outputScript.append("))\n");
			outputScript.append("matr <- as.matrix(data,nrow=" + (chartParameter.length-1) +")\n");
			outputScript.append("barplot(matr,");
			
			outputScript.append("names.arg="+chartParameter[0]+0);
			outputScript.append(",beside=TRUE,col=rainbow("+(chartParameter.length-1)+"))");
			break;
		case PIECHART:
			outputScript.append("pie3D(");
			outputScript.append(chartParameter[1]+1+","+chartParameter[0]+0);
			outputScript.append(",explode=0.1,col=rainbow(length("+chartParameter[0]+0+")))");
			break;
		default:
			outputScript.append("plot(");
			outputScript.append(chartParameter[0]+0);
			outputScript.append(","+chartParameter[1]+1);
			outputScript.append(",type=\"l\",col=rainbow("+(chartParameter.length-1)+")[1])");
			for (int i = 2; i < chartParameter.length; i++){
				outputScript.append("\nlines(");
				outputScript.append(chartParameter[i]+i);
				outputScript.append(",type=\"l\",col=rainbow("+(chartParameter.length-1)+")["+i+"])");
			}
			RBuilder.createLegend(outputScript, chartParameter);
		}
//		legend("topleft", c("outNs.out1","inputNs.input2"), col=rainbow(2), 
//				   lwd=2, bty="n");
		return outputScript.toString();
	}

	private String getOrCreateProject(String projectname) {
		String pid = savedProjects.get(projectname);
		if (pid == null) {
			pid = papConnector.addProject(uid, projectname);
		}
		return pid;
	}

	private static String generateID() {
		String id;
		try {
			id = URLEncoder.encode(Calendar.getInstance().getTime().toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			id = "badid";
		}
		return "+"+id;
	}

	@Override
	public ISoPeCoExtension<?> getProvider() {
		return provider;
	}

	@Override
	public Visualization createVisualization(String experimentName,
			Double[][] data, List<ChartParameter> chartParameter,
			ChartOptions options) {
		String[] columnNames = new String[chartParameter.size()];
		for (int i = 0; i < columnNames.length; i++){
			columnNames[i] = chartParameter.get(i).getParameterName();
		}
		String processScript = createProcessScript(data, columnNames);
		String outputScript = creatOutputScript(options, columnNames);
		String pid = getOrCreateProject(experimentName);
		String qid = papConnector.addQuery(pid, "testquery" + generateID(),
				SQL_DUMMY, "empty");
		String procid = papConnector.addProcess(pid, "testprocess"
				+ generateID(), 1, processScript);
		String oid = papConnector.addOutput(pid, "testoutput" + generateID(),
				1, outputScript, "desc");
		String bid = papConnector.addResult(pid, procid, qid, "testresult"
				+ generateID(), uid, oid);
		String link = BASE_URL + uid + "/" + pid + "/" + bid;
		Visualization visualization = new Visualization();
		visualization.setLink(link);
		visualization.setName(experimentName);
		visualization.setChartParameters(chartParameter);
		visualization.setOptions(options);
		return visualization;
	}
}
