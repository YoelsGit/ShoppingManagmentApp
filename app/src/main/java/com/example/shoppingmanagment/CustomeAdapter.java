package com.example.shoppingmanagment;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomeAdapter extends RecyclerView.Adapter<CustomeAdapter.ViewHolder> implements Filterable {

    private ArrayList<DataModel> dataSet;
    private ArrayList<DataModel> filteredDataSet;
    private Context context;

    public CustomeAdapter(ArrayList<DataModel> dataSet, Context context) {
        this.dataSet = dataSet;
        this.filteredDataSet = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataModel dataModel = filteredDataSet.get(position);
        holder.productName.setText(dataModel.getName());
        holder.textCount.setText(String.valueOf(dataModel.getCount()));
    }

    @Override
    public int getItemCount() {
        return filteredDataSet.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView textCount;
        Button plus, minus;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewCard);
            textCount = itemView.findViewById(R.id.text_count);
            plus = itemView.findViewById(R.id.button_plus);
            minus = itemView.findViewById(R.id.button_minus);

            // Handle long press to show the options dialog
            itemView.setOnLongClickListener(v -> {
                showOptionsDialog(getAdapterPosition());
                return true;
            });

            plus.setOnClickListener(v -> {
                int currentCount = Integer.parseInt(textCount.getText().toString());
                currentCount++;
                int currentPosition = getAdapterPosition();
                DataModel dataModel = filteredDataSet.get(currentPosition);
                dataModel.setCount(currentCount);
                updateItemInFirebase(dataModel);
                textCount.setText(String.valueOf(currentCount));
            });

            minus.setOnClickListener(v -> {
                int currentCount = Integer.parseInt(textCount.getText().toString());
                currentCount = (currentCount > 0) ? currentCount - 1 : 0;
                int currentPosition = getAdapterPosition();
                DataModel dataModel = filteredDataSet.get(currentPosition);
                dataModel.setCount(currentCount);
                updateItemInFirebase(dataModel);
                textCount.setText(String.valueOf(currentCount));
            });
        }

        // Show the dialog with options to Edit, Delete, or Cancel
        private void showOptionsDialog(int position) {
            final DataModel selectedItem = filteredDataSet.get(position);

            new AlertDialog.Builder(context)
                    .setTitle("Choose Action")
                    .setItems(new String[]{"Edit", "Delete", "Back"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // Edit
                                showEditDialog(selectedItem);
                                break;
                            case 1: // Delete
                                deleteItem(position, selectedItem);
                                break;
                            case 2: // Back (Do nothing)
                                dialog.dismiss();
                                break;
                        }
                    })
                    .show();
        }

        // Show dialog to edit the product name
        private void showEditDialog(DataModel selectedItem) {
            EditText input = new EditText(context);
            input.setText(selectedItem.getName());
            input.setSelection(input.getText().length());

            new AlertDialog.Builder(context)
                    .setTitle("Edit Product Name")
                    .setView(input)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            selectedItem.setName(newName);
                            updateItemInFirebase(selectedItem);
                            notifyDataSetChanged(); // Refresh the RecyclerView
                        } else {
                            Toast.makeText(context, "Product name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        // Delete the item from Firebase
        private void deleteItem(int position, DataModel selectedItem) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("recyclerViewData");

            // Ensure the position is valid and the list is not empty
            if (position < 0 || position >= filteredDataSet.size()) {
                Log.e("DeleteItem", "Invalid position or empty dataset: " + position);
                return; // Exit if the position is out of bounds or dataset is empty
            }

            database.orderByChild("id_").equalTo(selectedItem.getId_()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            itemSnapshot.getRef().removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Remove the item from the local dataset
                                            if (position >= 0 && position < filteredDataSet.size()) {
                                                filteredDataSet.remove(position);

                                                // Check if the list is empty after removal
                                                if (filteredDataSet.isEmpty()) {
                                                    // Handle empty state UI here
                                                    notifyDataSetChanged(); // Refresh entire RecyclerView
                                                } else {
                                                    // Notify the adapter about the removal at the given position
                                                    notifyItemRemoved(position);
                                                }

                                                // Optionally reload the data to ensure consistency
                                                loadDataFromFirebase();
                                                Log.d("DeleteItem", "Item deleted successfully!");
                                            } else {
                                                Log.e("DeleteItem", "Position is invalid after removal: " + position);
                                            }
                                        } else {
                                            Log.e("DeleteItem", "Failed to delete item", task.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.e("DeleteItem", "Item not found in Firebase.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DeleteItem", "Firebase error: ", error.toException());
                }
            });
        }





        // Reload data from Firebase to make sure the RecyclerView reflects the latest data
        private void loadDataFromFirebase() {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("recyclerViewData");

            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Clear current data and reload fresh data
                    filteredDataSet.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        DataModel dataModel = dataSnapshot.getValue(DataModel.class);
                        filteredDataSet.add(dataModel);
                    }

                    // Notify the adapter that data has changed
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Failed to fetch data", error.toException());
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = dataSet;
                    results.count = dataSet.size();
                } else {
                    List<DataModel> filteredList = new ArrayList<>();
                    for (DataModel item : dataSet) {
                        if (item.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(item);
                        }
                    }
                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredDataSet = (ArrayList<DataModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private void updateItemInFirebase(DataModel dataModel) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("recyclerViewData");

        database.orderByChild("id_").equalTo(dataModel.getId_()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        itemSnapshot.getRef().setValue(dataModel)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("Firebase", "Item updated successfully!");
                                    } else {
                                        Log.e("Firebase", "Failed to update item", task.getException());
                                    }
                                });
                    }
                } else {
                    Log.e("Firebase", "Item not found in Firebase for update.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error occurred during the update", error.toException());
            }
        });
    }
}
