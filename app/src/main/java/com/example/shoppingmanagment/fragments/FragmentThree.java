package com.example.shoppingmanagment.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingmanagment.CustomeAdapter;
import com.example.shoppingmanagment.DataModel;
import com.example.shoppingmanagment.R;
import com.example.shoppingmanagment.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentThree extends Fragment {

    private ArrayList<DataModel> dataSet;
    private RecyclerView recyclerView;
    private CustomeAdapter adapter;

    private FirebaseAuth auth;


    private DatabaseReference userRef;

    public FragmentThree() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_three, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String email = "Hello" +" "+  user.getEmail() + "!";
        TextView userEmail = view.findViewById(R.id.textView3);
        userEmail.setText(email.substring(0, email.indexOf('@')));



        userRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the dataset
        dataSet = new ArrayList<>();
        adapter = new CustomeAdapter(dataSet, getContext());
        recyclerView.setAdapter(adapter);

        // Load data from Firebase
        loadDataFromFirebase();

        // Search functionality
        EditText searchBar = view.findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save button


        // Floating Action Button (Add Item)
        view.findViewById(R.id.fab_add_item).setOnClickListener(v -> {
            showAddItemDialog();
        });
    }

     //Load data from Firebase
     private void loadDataFromFirebase() {
         String userId = auth.getCurrentUser().getUid(); // Get the logged-in user's ID
         userRef.child(userId).child("recyclerViewData").addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 dataSet.clear(); // Clear the dataset to avoid duplicates
                 for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                     DataModel item = dataSnapshot.getValue(DataModel.class);  // Load DataModel with count
                     if (item != null) {
                         dataSet.add(item);  // Add the item with updated count to the dataset
                     }
                 }
                 adapter.notifyDataSetChanged(); // Notify the adapter of the data change
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
             }
         });
     }



    // Method to show the dialog for adding a product
    private void showAddItemDialog() {
        // Create an EditText for input
        EditText input = new EditText(getContext());
        input.setHint("Enter product name");

        // Create and show the dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Add Product")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String productName = input.getText().toString().trim();
                    if (!productName.isEmpty()) {
                        // Add the item to the dataset
                        DataModel newItem = new DataModel(productName, 0, dataSet.size() + 1);
                       dataSet.add(newItem);
                        adapter.notifyItemInserted(dataSet.size() - 1); // Notify the adapter

                        // Save the new item to Firebase
                        String userId = auth.getCurrentUser().getUid();
                        userRef.child(userId).child("recyclerViewData").push().setValue(newItem);
                    }
                    else {
                        Toast.makeText(getContext(), "Product name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}