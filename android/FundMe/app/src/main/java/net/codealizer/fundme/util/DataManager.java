package net.codealizer.fundme.util;

import android.content.Context;
import android.widget.Filter;

import net.codealizer.fundme.assets.SearchItem;
import net.codealizer.fundme.util.db.LocalDatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Pranav on 12/24/16.
 */

public class DataManager {

    private static List<SearchItem> sItemSuggestions;

    public static void init(Context context) {
        sItemSuggestions = new LocalDatabaseManager(context).getAllSearchItems();
    }

    public interface OnFindSuggestionsListener {
        void onResults(List<SearchItem> results);
    }

    public static List<SearchItem> getHistory(Context context, int count) {
        List<SearchItem> suggestionList = new ArrayList<>();
        SearchItem colorSuggestion;
        for (int i = 0; i < sItemSuggestions.size(); i++) {
            colorSuggestion = sItemSuggestions.get(i);
            colorSuggestion.setIsHistory(true);
            suggestionList.add(colorSuggestion);
            if (suggestionList.size() == count) {
                break;
            }
        }
        return suggestionList;
    }

    public static void resetSuggestionsHistory() {
        for (SearchItem colorSuggestion : sItemSuggestions) {
            colorSuggestion.setIsHistory(false);
        }
    }

    public static void findSuggestions(Context context, String query, final int limit, final long simulatedDelay,
                                       final OnFindSuggestionsListener listener) {
        new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                try {
                    Thread.sleep(simulatedDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DataManager.resetSuggestionsHistory();
                List<SearchItem> suggestionList = new ArrayList<>();
                if (!(constraint == null || constraint.length() == 0)) {

                    for (SearchItem suggestion : sItemSuggestions) {
                        if (suggestion.getBody().toUpperCase()
                                .startsWith(constraint.toString().toUpperCase())) {

                            suggestionList.add(suggestion);
                            if (limit != -1 && suggestionList.size() == limit) {
                                break;
                            }
                        }
                    }
                }

                FilterResults results = new FilterResults();
                Collections.sort(suggestionList, new Comparator<SearchItem>() {
                    @Override
                    public int compare(SearchItem lhs, SearchItem rhs) {
                        return lhs.getIsHistory() ? -1 : 0;
                    }
                });
                results.values = suggestionList;
                results.count = suggestionList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (listener != null) {
                    listener.onResults((List<SearchItem>) results.values);
                }
            }
        }.filter(query);

    }


    public static void findColors(Context context, String query, final OnFindSuggestionsListener listener) {

        new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {


                List<SearchItem> suggestionList = new ArrayList<>();

                if (!(constraint == null || constraint.length() == 0)) {

                    for (SearchItem color : sItemSuggestions) {
                        if (color.getName().toUpperCase()
                                .startsWith(constraint.toString().toUpperCase())) {

                            suggestionList.add(color);
                        }
                    }

                }

                FilterResults results = new FilterResults();
                results.values = suggestionList;
                results.count = suggestionList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (listener != null) {
                    listener.onResults((List<SearchItem>) results.values);
                }
            }
        }.filter(query);

    }


}
