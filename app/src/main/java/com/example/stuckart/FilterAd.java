package com.example.stuckart;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterAd extends Filter {
    private AdapterAd adapter;
    private ArrayList<ModelAd> filterList;

    /* Constructor
     * @param adapter The instance of AdapterAd class
     * @param filterList The list of Ads
     */

    public FilterAd(AdapterAd adapter, ArrayList<ModelAd> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        //perform filter based on what user type

        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0){
            //convert the typed query to upper case to make search not case sensitive e.g. Samsung S23 Ultra SAMSUNG 523 ULTRA
            constraint = constraint.toString().toUpperCase();
            //hold the filtered list of Ads based on user searched query
            ArrayList<ModelAd> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //Ad filter based on Brand, Category, Condition, Title. If any of these matches add it to the filteredModels list
                if (filterList.get(i).getBrand().toUpperCase().contains(constraint) ||
                        filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                        filterList.get(i).getCondition().toUpperCase().contains(constraint) ||
                        filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    //Filter matched add to filtered Models list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.adArrayList = (ArrayList<ModelAd>) results.values;

        adapter.notifyDataSetChanged();

       }
}

