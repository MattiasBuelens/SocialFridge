package be.kuleuven.cs.chikwadraat.socialfridge.util;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Mattias on 16/04/2014.
 */
public abstract class SectionedAdapter<T extends ListAdapter, K> extends BaseAdapter {

    private final T sourceAdapter;
    private final DataSetObserver observer = new SourceObserver();
    private final AtomicBoolean dataSetChanged = new AtomicBoolean(true);

    private final Map<K, Section> sections = new HashMap<K, Section>();
    private final List<K> sectionOrder = new ArrayList<K>();

    public SectionedAdapter(T sourceAdapter) {
        this.sourceAdapter = sourceAdapter;
        sourceAdapter.registerDataSetObserver(observer);
    }

    private void buildSections() {
        // Create sections
        sections.clear();
        int count = getSource().getCount();
        for (int i = 0; i < count; i++) {
            K sectionKey = getSectionKey(i);
            Section section = sections.get(sectionKey);
            if (section == null) {
                section = new Section(sectionKey);
                sections.put(sectionKey, section);
            }
            section.addItem();
        }
        // Sort sections
        sectionOrder.clear();
        sectionOrder.addAll(sections.keySet());
        Collections.sort(sectionOrder, getSectionOrdering());
        // Notify changed
        notifyDataSetChanged();
    }

    private void updateSections() {
        if (dataSetChanged.getAndSet(false)) {
            buildSections();
        }
    }

    protected final T getSource() {
        return sourceAdapter;
    }

    protected abstract K getSectionKey(int sourcePosition);

    protected abstract Comparator<K> getSectionOrdering();

    private PositionData getPositionData(int position) {
        int sectionsBefore = 0;
        int positionsBefore = 0;
        for (K sectionKey : sectionOrder) {
            Section section = sections.get(sectionKey);
            int sectionCount = section.getCount();
            if (position == positionsBefore) {
                // Header
                return new PositionData(sectionKey);
            } else if (position < positionsBefore) {
                // Item
                break;
            } else {
                positionsBefore += sectionCount + 1;
                sectionsBefore++;
            }
        }
        // Item
        int sourcePosition = position - sectionsBefore;
        return new PositionData(sourcePosition);
    }

    @Override
    public int getCount() {
        updateSections();

        return getSource().getCount() + sections.size();
    }

    @Override
    public Object getItem(int position) {
        updateSections();

        PositionData data = getPositionData(position);
        if (data.isHeader) {
            return null;
        } else {
            return getSource().getItem(data.sourcePosition);
        }
    }

    @Override
    public long getItemId(int position) {
        updateSections();

        PositionData data = getPositionData(position);
        if (data.isHeader) {
            return -1;
        } else {
            return getSource().getItemId(data.sourcePosition);
        }
    }

    @Override
    public int getItemViewType(int position) {
        updateSections();

        PositionData data = getPositionData(position);
        if (data.isHeader) {
            return getSource().getViewTypeCount() + getHeaderViewType(data.sectionKey);
        } else {
            return getSource().getItemViewType(data.sourcePosition);
        }
    }

    @Override
    public int getViewTypeCount() {
        return getHeaderViewTypeCount() + getSource().getViewTypeCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        updateSections();

        PositionData data = getPositionData(position);
        if (data.isHeader) {
            return getHeaderView(data.sectionKey, convertView, parent);
        } else {
            return getItemView(data.sourcePosition, convertView, parent);
        }
    }

    protected abstract View getHeaderView(K sectionKey, View convertView, ViewGroup parent);

    protected int getHeaderViewType(K sectionKey) {
        return 0;
    }

    protected int getHeaderViewTypeCount() {
        return 1;
    }

    protected View getItemView(int sourcePosition, View convertView, ViewGroup parent) {
        return getSource().getView(sourcePosition, convertView, parent);
    }

    private class PositionData {

        private final boolean isHeader;
        private final K sectionKey;
        private final int sourcePosition;

        private PositionData(K sectionKey) {
            this.isHeader = true;
            this.sectionKey = sectionKey;
            this.sourcePosition = -1;
        }

        private PositionData(int sourcePosition) {
            this.isHeader = false;
            this.sectionKey = null;
            this.sourcePosition = sourcePosition;
        }

    }

    private class Section {

        private final K key;
        private int count = 0;

        public Section(K key) {
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        private void addItem() {
            count++;
        }

        public int getCount() {
            return count;
        }

    }

    private class SourceObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            dataSetChanged.set(true);
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            sourceAdapter.unregisterDataSetObserver(this);
        }
    }

}
