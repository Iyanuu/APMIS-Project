/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.ui.user.UserSettingsForm;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.VaadinUiUtil;



@SuppressWarnings("serial")
@JavaScript("https://cdn.amcharts.com/lib/5/index.js")
@JavaScript("https://cdn.amcharts.com/lib/5/xy.js")
@JavaScript("https://cdn.amcharts.com/lib/5/themes/Animated.js")

public class LanguageView extends VerticalLayout implements View {

	public static final String VIEW_NAME = "language";

	public LanguageView() throws IOException {
		
		final Page page = Page.getCurrent();

		String allstr = "var root = am5.Root.new(\"chartdiv\");\n"
				+ "root.setThemes([\n"
				+ "  am5themes_Animated.new(root)\n"
				+ "]);\n"
				+ "var chart = root.container.children.push(am5xy.XYChart.new(root, {\n"
				+ "  panX: false,\n"
				+ "  panY: false,\n"
				+ "  wheelX: \"panX\",\n"
				+ "  wheelY: \"zoomX\",\n"
				+ "  layout: root.verticalLayout\n"
				+ "}));\n"
				+ "var data = [{\n"
				+ "  \"year\": \"2021\",\n"
				+ "  \"europe\": 2.5,\n"
				+ "  \"namerica\": 2.5,\n"
				+ "  \"asia\": 2.1,\n"
				+ "  \"lamerica\": 1,\n"
				+ "  \"meast\": 0.8,\n"
				+ "  \"africa\": 0.4\n"
				+ "}, {\n"
				+ "  \"year\": \"2022\",\n"
				+ "  \"europe\": 2.6,\n"
				+ "  \"namerica\": 2.7,\n"
				+ "  \"asia\": 2.2,\n"
				+ "  \"lamerica\": 0.5,\n"
				+ "  \"meast\": 0.4,\n"
				+ "  \"africa\": 0.3\n"
				+ "}, {\n"
				+ "  \"year\": \"2023\",\n"
				+ "  \"europe\": 2.8,\n"
				+ "  \"namerica\": 2.9,\n"
				+ "  \"asia\": 2.4,\n"
				+ "  \"lamerica\": 0.3,\n"
				+ "  \"meast\": 0.9,\n"
				+ "  \"africa\": 0.5\n"
				+ "}]\n"
				+ "var xAxis = chart.xAxes.push(am5xy.CategoryAxis.new(root, {\n"
				+ "  categoryField: \"year\",\n"
				+ "  renderer: am5xy.AxisRendererX.new(root, {}),\n"
				+ "  tooltip: am5.Tooltip.new(root, {})\n"
				+ "}));\n"
				+ "\n"
				+ "xAxis.data.setAll(data);\n"
				+ "\n"
				+ "var yAxis = chart.yAxes.push(am5xy.ValueAxis.new(root, {\n"
				+ "  min: 0,\n"
				+ "  max: 100,\n"
				+ "  numberFormat: \"#'%'\",\n"
				+ "  strictMinMax: true,\n"
				+ "  calculateTotals: true,\n"
				+ "  renderer: am5xy.AxisRendererY.new(root, {})\n"
				+ "}));\n"
				+ "var legend = chart.children.push(am5.Legend.new(root, {\n"
				+ "  centerX: am5.p50,\n"
				+ "  x: am5.p50\n"
				+ "}));\n"
				+ "function makeSeries(name, fieldName) {\n"
				+ "  var series = chart.series.push(am5xy.ColumnSeries.new(root, {\n"
				+ "    name: name,\n"
				+ "    stacked: true,\n"
				+ "    xAxis: xAxis,\n"
				+ "    yAxis: yAxis,\n"
				+ "    valueYField: fieldName,\n"
				+ "    valueYShow: \"valueYTotalPercent\",\n"
				+ "    categoryXField: \"year\"\n"
				+ "  }));\n"
				+ "\n"
				+ "  series.data.setAll(data);\n"
				+ "  series.appear();\n"
				+ "  series.bullets.push(function () {\n"
				+ "    return am5.Bullet.new(root, {\n"
				+ "      sprite: am5.Label.new(root, {\n"
				+ "        text: \"{valueYTotalPercent.formatNumber('#.#')}%\",\n"
				+ "        fill: root.interfaceColors.get(\"alternativeText\"),\n"
				+ "        centerY: am5.p50,\n"
				+ "        centerX: am5.p50,\n"
				+ "        populateText: true\n"
				+ "      })\n"
				+ "    });\n"
				+ "  });\n"
				+ "\n"
				+ "  legend.data.push(series);\n"
				+ "}\n"
				+ "\n"
				+ "makeSeries(\"Europe\", \"europe\");\n"
				+ "makeSeries(\"North America\", \"namerica\");\n"
				+ "makeSeries(\"Asia\", \"asia\");\n"
				+ "makeSeries(\"Latin America\", \"lamerica\");\n"
				+ "makeSeries(\"Middle East\", \"meast\");\n"
				+ "makeSeries(\"Africa\", \"africa\");\n"
				+ "chart.appear(1000, 100);";
		
		
		String allstrx = "var root = am5.Root.new(\"chartdivx\");\n"
				+ "root.setThemes([\n"
				+ "  am5themes_Animated.new(root)\n"
				+ "]);\n"
				+ "var chart = root.container.children.push(am5xy.XYChart.new(root, {\n"
				+ "  panX: false,\n"
				+ "  panY: false,\n"
				+ "  wheelX: \"panX\",\n"
				+ "  wheelY: \"zoomX\",\n"
				+ "  layout: root.verticalLayout\n"
				+ "}));\n"
				+ "var data = [{\n"
				+ "  \"year\": \"2021\",\n"
				+ "  \"europe\": 2.5,\n"
				+ "  \"namerica\": 2.5,\n"
				+ "  \"asia\": 2.1,\n"
				+ "  \"lamerica\": 1,\n"
				+ "  \"meast\": 0.8,\n"
				+ "  \"africa\": 0.4\n"
				+ "}, {\n"
				+ "  \"year\": \"2022\",\n"
				+ "  \"europe\": 2.6,\n"
				+ "  \"namerica\": 2.7,\n"
				+ "  \"asia\": 2.2,\n"
				+ "  \"lamerica\": 0.5,\n"
				+ "  \"meast\": 0.4,\n"
				+ "  \"africa\": 0.3\n"
				+ "}, {\n"
				+ "  \"year\": \"2023\",\n"
				+ "  \"europe\": 2.8,\n"
				+ "  \"namerica\": 2.9,\n"
				+ "  \"asia\": 2.4,\n"
				+ "  \"lamerica\": 0.3,\n"
				+ "  \"meast\": 0.9,\n"
				+ "  \"africa\": 0.5\n"
				+ "}]\n"
				+ "var xAxis = chart.xAxes.push(am5xy.CategoryAxis.new(root, {\n"
				+ "  categoryField: \"year\",\n"
				+ "  renderer: am5xy.AxisRendererX.new(root, {}),\n"
				+ "  tooltip: am5.Tooltip.new(root, {})\n"
				+ "}));\n"
				+ "\n"
				+ "xAxis.data.setAll(data);\n"
				+ "\n"
				+ "var yAxis = chart.yAxes.push(am5xy.ValueAxis.new(root, {\n"
				+ "  min: 0,\n"
				+ "  max: 100,\n"
				+ "  numberFormat: \"#'%'\",\n"
				+ "  strictMinMax: true,\n"
				+ "  calculateTotals: true,\n"
				+ "  renderer: am5xy.AxisRendererY.new(root, {})\n"
				+ "}));\n"
				+ "var legend = chart.children.push(am5.Legend.new(root, {\n"
				+ "  centerX: am5.p50,\n"
				+ "  x: am5.p50\n"
				+ "}));\n"
				+ "function makeSeries(name, fieldName) {\n"
				+ "  var series = chart.series.push(am5xy.ColumnSeries.new(root, {\n"
				+ "    name: name,\n"
				+ "    stacked: true,\n"
				+ "    xAxis: xAxis,\n"
				+ "    yAxis: yAxis,\n"
				+ "    valueYField: fieldName,\n"
				+ "    valueYShow: \"valueYTotalPercent\",\n"
				+ "    categoryXField: \"year\"\n"
				+ "  }));\n"
				+ "\n"
				+ "  series.data.setAll(data);\n"
				+ "  series.appear();\n"
				+ "  series.bullets.push(function () {\n"
				+ "    return am5.Bullet.new(root, {\n"
				+ "      sprite: am5.Label.new(root, {\n"
				+ "        text: \"{valueYTotalPercent.formatNumber('#.#')}%\",\n"
				+ "        fill: root.interfaceColors.get(\"alternativeText\"),\n"
				+ "        centerY: am5.p50,\n"
				+ "        centerX: am5.p50,\n"
				+ "        populateText: true\n"
				+ "      })\n"
				+ "    });\n"
				+ "  });\n"
				+ "\n"
				+ "  legend.data.push(series);\n"
				+ "}\n"
				+ "\n"
				+ "makeSeries(\"Europe\", \"europe\");\n"
				+ "makeSeries(\"North America\", \"namerica\");\n"
				+ "makeSeries(\"Asia\", \"asia\");\n"
				+ "makeSeries(\"Latin America\", \"lamerica\");\n"
				+ "makeSeries(\"Middle East\", \"meast\");\n"
				+ "makeSeries(\"Africa\", \"africa\");\n"
				+ "chart.appear(1000, 100);";
		
		
		
		page.getJavaScript().execute(allstr);
		
			CustomLayout cds = new CustomLayout(new ByteArrayInputStream("<div id=\"chartdiv\" style=\"height: 700px;\"></div>".getBytes()));
			
			
			
			page.getJavaScript().execute(allstrx);
			
			CustomLayout cdsx = new CustomLayout(new ByteArrayInputStream("<div id=\"chartdivx\" style=\"height: 700px;\"></div>".getBytes()));
			
			
			//infoLayout.addComponent(cds);
	
		
		setSizeFull();
		//setStyleName("about-view");
		addComponent(cds);
		
		//addComponent(cdsx);
	//	setComponentAlignment(aboutLayout, Alignment.MIDDLE_CENTER);
	}

	private void showSettingsPopup() {

		Window window = VaadinUiUtil.createPopupWindow();
		window.setCaption(I18nProperties.getString(Strings.headingUserSettings));
		window.setModal(true);

		CommitDiscardWrapperComponent<UserSettingsForm> component = ControllerProvider.getUserController()
				.getUserSettingsComponent(() -> window.close());

		window.setContent(component);
		window.addCloseListener(event -> UI.getCurrent().getPage().setLocation("/sormas-ui"));
		UI.getCurrent().addWindow(window);
	}
}
