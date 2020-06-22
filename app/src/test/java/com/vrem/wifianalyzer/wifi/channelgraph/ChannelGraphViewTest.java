/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2020 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.channelgraph;

import android.os.Build;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.vrem.wifianalyzer.MainContextHelper;
import com.vrem.wifianalyzer.RobolectricUtil;
import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.settings.ThemeStyle;
import com.vrem.wifianalyzer.wifi.band.WiFiBand;
import com.vrem.wifianalyzer.wifi.band.WiFiChannel;
import com.vrem.wifianalyzer.wifi.graphutils.GraphLegend;
import com.vrem.wifianalyzer.wifi.graphutils.GraphViewWrapper;
import com.vrem.wifianalyzer.wifi.model.SortBy;
import com.vrem.wifianalyzer.wifi.model.WiFiConnection;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.vrem.wifianalyzer.wifi.graphutils.GraphConstantsKt.MAX_Y;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.P)
@LooperMode(PAUSED)
public class ChannelGraphViewTest {
    private Pair<WiFiChannel, WiFiChannel> wiFiChannelPair;
    private Settings settings;
    private GraphViewWrapper graphViewWrapper;
    private DataManager dataManager;
    private ChannelGraphView fixture;

    @Before
    public void setUp() {
        RobolectricUtil.INSTANCE.getActivity();

        graphViewWrapper = mock(GraphViewWrapper.class);
        dataManager = mock(DataManager.class);

        settings = MainContextHelper.INSTANCE.getSettings();

        withSettings();

        wiFiChannelPair = new Pair<>(WiFiChannel.UNKNOWN, WiFiChannel.UNKNOWN);
        fixture = new ChannelGraphView(WiFiBand.GHZ2, wiFiChannelPair);
        fixture.setGraphViewWrapper(graphViewWrapper);
        fixture.setDataManager(dataManager);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(graphViewWrapper);
        verifyNoMoreInteractions(dataManager);
/*
        verifyNoMoreInteractions(settings);
*/
        MainContextHelper.INSTANCE.restore();
    }

    @Test
    public void testUpdate() {
        // setup
        Set<WiFiDetail> newSeries = Collections.emptySet();
        List<WiFiDetail> wiFiDetails = Collections.emptyList();
        WiFiData wiFiData = new WiFiData(wiFiDetails, WiFiConnection.EMPTY);
        when(dataManager.getNewSeries(wiFiDetails, wiFiChannelPair)).thenReturn(newSeries);
        when(settings.sortBy()).thenReturn(SortBy.CHANNEL);
        // execute
        fixture.update(wiFiData);
        // validate
        verify(dataManager).getNewSeries(wiFiDetails, wiFiChannelPair);
        verify(dataManager).addSeriesData(graphViewWrapper, newSeries, MAX_Y);
        verify(graphViewWrapper).removeSeries(newSeries);
        verify(graphViewWrapper).updateLegend(GraphLegend.RIGHT);
        verify(graphViewWrapper).visibility(View.VISIBLE);
        verify(settings).sortBy();
        verifySettings();
    }

    private void verifySettings() {
        verify(settings, times(2)).channelGraphLegend();
        verify(settings, times(2)).graphMaximumY();
        verify(settings).themeStyle();
    }

    private void withSettings() {
        when(settings.channelGraphLegend()).thenReturn(GraphLegend.RIGHT);
        when(settings.wiFiBand()).thenReturn(WiFiBand.GHZ2);
        when(settings.graphMaximumY()).thenReturn(MAX_Y);
        when(settings.themeStyle()).thenReturn(ThemeStyle.DARK);
    }

    @Test
    public void testGetGraphView() {
        // setup
        GraphView expected = mock(GraphView.class);
        when(graphViewWrapper.getGraphView()).thenReturn(expected);
        // execute
        GraphView actual = fixture.graphView();
        // validate
        assertEquals(expected, actual);
        verify(graphViewWrapper).getGraphView();
        verify(settings).channelGraphLegend();
        verify(settings).graphMaximumY();
        verify(settings).themeStyle();
    }
}