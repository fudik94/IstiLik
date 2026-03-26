package com.example.istivat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryManager historyManager;
    private PreferencesHelper prefsHelper;
    private HistoryAdapter adapter;
    private List<String[]> entries;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PreferencesHelper.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        historyManager = new HistoryManager(prefs);
        prefsHelper = new PreferencesHelper(prefs);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        entries = historyManager.getEntries();
        adapter = new HistoryAdapter(entries, this::onEntryClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        attachSwipeToDelete();
        updateEmptyState();
    }

    @Override
    public void onResume() {
        super.onResume();
        entries.clear();
        entries.addAll(historyManager.getEntries());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void onEntryClicked(String[] entry) {
        requireActivity().getSharedPreferences(PreferencesHelper.PREFS_NAME,
                android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(PreferencesHelper.KEY_AREA, entry[1])
                .apply();
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).switchToCalculator();
        }
    }

    private void attachSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                historyManager.deleteEntryAt(position);
                adapter.removeAt(position);
                updateEmptyState();
                Toast.makeText(requireContext(),
                        getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void updateEmptyState() {
        boolean isEmpty = entries.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
