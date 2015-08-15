package ru.werklogic.werklogic.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.werklogic.werklogic.R;
import ru.werklogic.werklogic.dm.events.EventStorage;
import ru.werklogic.werklogic.utils.Utils;

public class HistoryAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<EventStorage.Event> events = new ArrayList<>();
    private Context context;
    private SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat fe = new SimpleDateFormat("dd MMMM");


    public void addEvents(List<EventStorage.Event> list) {
        if (list != null && list.size() > 0) {
            events.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void clearEvents() {
        events.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView time;
        TextView source;
        boolean withHeader;
        TextView headerDate;
    }

    public HistoryAdapter(Context context) {
        super();
        this.context = context;
        mLayoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        EventStorage.Event event = events.get(position);
        Date currentDate = event.getEventTime();
        Date prevDate = position == 0 ? null : events.get(position - 1).getEventTime();
        Date currentDay = Utils.getDay(currentDate);
        boolean withHeader = prevDate == null || !currentDay.equals(Utils.getDay(prevDate));

        if (convertView == null || withHeader != ((ViewHolder) convertView.getTag()).withHeader) {

            holder = new ViewHolder();
            holder.withHeader = withHeader;
            if (withHeader) {
                convertView = mLayoutInflater.inflate(R.layout.history_header,
                        null);
                holder.headerDate = (TextView) convertView
                        .findViewById(R.id.header);
            } else {
                convertView = mLayoutInflater.inflate(R.layout.history_row,
                        null);
            }
            holder.source = (TextView) convertView
                    .findViewById(R.id.source);
            holder.time = (TextView) convertView
                    .findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder.withHeader) {
            holder.headerDate.setText(fe.format(currentDate).toUpperCase());
        }

        holder.time.setText(f.format(currentDate));

        String sensor = event.getSensor().getValidName(context);
        holder.source.setText(sensor);
        return convertView;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
