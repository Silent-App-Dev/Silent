package com.example.silent_ver_1.ui.premium;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silent_ver_1.R;
import com.example.silent_ver_1.UserHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactEditMessage extends AppCompatActivity {
    private String currUser;
    private RecyclerView recyclerView;
    private ArrayList<ContactModel> arrayList = new ArrayList<>();
    private MainAdapter mainAdapter;
    private EditText msg;
    private Button saveBtn;
    private int curPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currUser = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        setContentView(R.layout.activity_contact_edit_message);
        msg = findViewById(R.id.editPersonMessage);
        recyclerView = findViewById(R.id.recyclerView);
        saveBtn = findViewById(R.id.saveBtn);
        checkPermission();
    }

    /**
     * Request permission from the user to read the contacts
     */
    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(ContactEditMessage.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ContactEditMessage.this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else{
            // If the application got the permissions from the user then call the function to get the list of contacts
            getContactList();
        }
    }

    /**
     * Get a list of contacts from the user's phone and show it to the user in the recycler view
     */
    private void getContactList() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor cursor = getContentResolver().query(uri, null, null,
                null, sort);
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                // getting the id of the contact
                int str1 = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                String id = cursor.getString(str1);
                //getting the display name of the contact
                int str2 = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String name = cursor.getString(str2);

                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                        + " =?";
                Cursor phoneCursor = getContentResolver().query(uriPhone,
                        null, selection, new String[]{id}, null);

                if(phoneCursor.moveToNext()){
                    int str3 = phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = phoneCursor.getString(str3);
                    // Creating a contractModel that hold the data and adding it to the arraylist
                    ContactModel model = new ContactModel(name, number, currUser);
                    arrayList.add(model);
                    phoneCursor.close();
                }
            }
            cursor.close();
        }
        // Show the list of the user's contacts with the specific message for the chosen contact in the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainAdapter = new MainAdapter(this, arrayList);
        recyclerView.setAdapter(mainAdapter);
        mainAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                msg.setText(arrayList.get(position).getMessage());
                curPosition = position;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getContactList();
        }else{
            Toast.makeText(ContactEditMessage.this, "Permission Denied", Toast.LENGTH_LONG).show();
            checkPermission();
        }
    }

    /**
     * If the user wish to save a specific message for a certain contact then this function will upload it to the Database
     * @param view
     */
    public void onSaveClick(View view){
        if(curPosition != -1){
            arrayList.get(curPosition).setMessage(msg.getText().toString());
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://silent-android-application-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference myRef = database.getReference(currUser+"/Contacts/"+arrayList.get(curPosition).getNumber());
            myRef.setValue(msg.getText().toString());
        }
    }

    public String getCurrUser() {
        return currUser;
    }

}