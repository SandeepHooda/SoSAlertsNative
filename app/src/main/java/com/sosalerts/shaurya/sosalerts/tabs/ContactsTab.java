package com.sosalerts.shaurya.sosalerts.tabs;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sosalerts.shaurya.sosalerts.MainActivity;
import com.sosalerts.shaurya.sosalerts.R;
import com.sosalerts.shaurya.sosalerts.db.Storage;
import com.sosalerts.shaurya.sosalerts.services.address.SavedContacts;
import com.sosalerts.shaurya.sosalerts.services.util.ReadOut;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by shaurya on 1/23/2017.
 */

public class ContactsTab extends Fragment {

    View view = null;
    ListView listView ;
    public static String actionName = "ContactsTab";
    private final String fileName = actionName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.contacts_tab, container, false);
        Button button = (Button)  view.findViewById(R.id.btn_savecontact);
        EditText text = (EditText) view.findViewById(R.id.conact_phone);
        text.setTextColor(Color.WHITE);
        text.setHintTextColor(Color.CYAN);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact(view);
            }
        });

        getSavedContacts();
         InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        return view;

    }
    private void getSavedContacts(){

        Set<String> savedContacts = Storage.getFromDBDBStringSet(Storage.savedContacts,getActivity());
        Log.e(fileName, "getSavedLocations  "+savedContacts);
        if (savedContacts != null && savedContacts.size() > 0){
            List<SavedContacts> savedContactList = new ArrayList<SavedContacts>();
            for (String aPhoneNo: savedContacts){
                SavedContacts newContact = new SavedContacts();
                newContact.setContactNo(aPhoneNo);
                newContact.setContactName(MainActivity.allContacts.get(Storage.getOnlyNumbersLastTen(aPhoneNo)));
                savedContactList.add(newContact);

            }
            SavedContacts[] savedContactArray  = new SavedContacts[savedContactList.size()];
            savedContactArray = savedContactList.toArray(savedContactArray);
            ArrayAdapter<SavedContacts> adapter;
            adapter = new ArrayAdapter<SavedContacts>(view.getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1,savedContactArray){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    // Get the Item from ListView
                    View view = super.getView(position, convertView, parent);

                    // Initialize a TextView for ListView each Item
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                    // Set the text color of TextView (ListView Item)
                    tv.setTextColor(Color.WHITE);
                    tv.setBackground( ResourcesCompat.getDrawable(getResources(),R.drawable.black,null));
                    // Generate ListView Item using TextView
                    return view;
                }
            };

            listView = (ListView) view.findViewById(R.id.savedcontactsview);

            listView.setAdapter(adapter);
            ContactDeleteListener listner = new ContactDeleteListener();
            listner.listView = listView;
            listView.setOnItemLongClickListener( listner);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    // ListView Clicked item index
                    int itemPosition     = position;

                    // ListView Clicked item value
                    SavedContacts  itemValue    = (SavedContacts) listView.getItemAtPosition(position);
                    if(itemValue.getLongPressTime() == null || (new Date().getTime() -itemValue.getLongPressTime().getTime() > 4000)){
                        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                        String phoneNo = itemValue.getContactNo().replaceAll("[^\\d+]", "").trim();;
                        phoneIntent.setData(Uri.parse("tel:"+phoneNo));
                        phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        view.getContext().startActivity(phoneIntent);

                    }


                }

            });
        }


    }

    private void addContact(View v) {
        EditText editText = (EditText) view.findViewById(R.id.conact_phone);
        String phoneNo = editText.getText().toString();
        if(null != phoneNo && phoneNo.trim().length() >= 6){
            phoneNo = phoneNo.replaceAll("[^\\d()\\-+ ]", "").trim();
            Storage.storeinDBStringSet(Storage.savedContacts,phoneNo,getActivity());
            Intent speakIntent = new Intent(getActivity(), ReadOut.class);
            speakIntent.putExtra(ReadOut.textToSpeak,"If you click on the contact you will be able to make a phone call to that contact. In order to delete a saved contact you need to press it for a longer duration of time.");
            speakIntent.putExtra(MainActivity.orignationActivityName,fileName);
            getActivity().startService(speakIntent);
        }

    }

}
