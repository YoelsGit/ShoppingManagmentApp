package com.example.shoppingmanagment.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.shoppingmanagment.R;
import com.example.shoppingmanagment.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentTwo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTwo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentTwo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentTwo.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTwo newInstance(String param1, String param2) {
        FragmentTwo fragment = new FragmentTwo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_two, container, false);
        Button buttonReg = view.findViewById(R.id.buttonregister);
        EditText emailEditText = view.findViewById(R.id.email);
        EditText Password = view.findViewById(R.id.pass);
        EditText repass = view.findViewById(R.id.repass);
        EditText Phone = view.findViewById(R.id.phone);
        TextView errorTextView = view.findViewById(R.id.error);



        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString().trim();
                String password = Password.getText().toString().trim();
                String rePassword = repass.getText().toString().trim();
                String phone = Phone.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword) || TextUtils.isEmpty(phone)) {
                    errorTextView.setText("All fields must be filled!");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }


                if (!password.equals(rePassword)) {
                    errorTextView.setText("Passwords do not match!");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
//
                MainActivity mainactivity = (MainActivity) getActivity();
                mainactivity.register();
                //mainactivity.addData();

                Navigation.findNavController(view).navigate(R.id.action_fragmentTwo_to_fragmentOne);

            }
        });




        return view;



    }
}