package ru.werklogic.werklogic.activities;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.DataModel;
import ru.werklogic.werklogic.dm.SensorState;
import ru.werklogic.werklogic.dm.events.EventStorage;
import ru.werklogic.werklogic.dm.events.EventType;
import ru.werklogic.werklogic.dm.events.EventsReader;
import ru.werklogic.werklogic.utils.Utils;

/**
 * Created by bmw on 09.02.14.
 */
public class HistoryFragment extends ListFragment implements AbsListView.OnScrollListener {
    public static final String SENSOR = "sensor";
    private final static String TAG = HistoryFragment.class.getName();
    private DataModel dataModel;
    private SensorState sensor;
    private View view;
    private HistoryAdapter adapter;
    private EventsReader reader;
    private HistoryViewMode historyViewMode = new HistoryViewMode();
    private View loadingView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.dataModel = Utils.getApplication(getActivity()).getDataModel();
        this.sensor = getArguments().getParcelable(SENSOR);

        setListAdapter(null);
        initReader();
        getListView().addFooterView(loadingView, null, false);
        adapter = new HistoryAdapter(getActivity());
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
    }

    private void initReader() {
        if (reader != null)
            reader.close();
        if (historyViewMode.oneSensorView()) {
            reader = EventStorage.getEventsReader(sensor);
        } else {
            reader = EventStorage.getEventsReader(dataModel.getSensorsStates());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_history, container, false);

        loadingView = view.inflate(getActivity(), R.layout.history_loading, null);

        historyViewMode.reset(getActivity(), view, new Runnable() {

                    @Override
                    public void run() {
                        initReader();
                        adapter.clearEvents();
                    }
                }
        );

        return view;
    }

    @Override
    public void onDestroy() {
        reader.close();
        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view,
                         int firstVisible, int visibleCount, int totalCount) {
        if (reader.eof()) {
            loadingView.setVisibility(View.GONE);
            return;
        }

        loadingView.setVisibility(View.VISIBLE);
        if (firstVisible + visibleCount >= totalCount - 1 /* -1, т.к. используется footer "Загрузка..." */) {
            requestEvents(30);
        }
    }

    private void requestEvents(final int count) {
        AsyncTask<Void, Void, List<EventStorage.Event>> t = new AsyncTask<Void, Void, List<EventStorage.Event>>() {
            @Override
            protected List<EventStorage.Event> doInBackground(Void... params) {
                List<EventStorage.Event> events = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    if (reader.eof())
                        break;
                    EventStorage.Event e = reader.next();
                    if (e != null && e.getEventType() != EventType.READ)
                        events.add(e);
                }
                return events;
            }

            @Override
            protected void onPostExecute(List<EventStorage.Event> events) {
                adapter.addEvents(events);
            }

        };
        t.execute();
    }
}