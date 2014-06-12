package com.turbomanage.storm.sample.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.turbomanage.storm.sample.R;
import com.turbomanage.storm.sample.model.Contact;
import com.turbomanage.storm.sample.model.dao.ContactDao;

public class NewContactActivity extends ActionBarActivity {

    public static final int RESULT_CODE_CREATED = 201;

    private EditText mFirstName;
    private EditText mLastName;

    private ContactDao contactDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);

        contactDao = new ContactDao(getApplicationContext());

        mFirstName = (EditText) findViewById(R.id.first_name);
        mLastName = (EditText) findViewById(R.id.last_name);

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();

                if(TextUtils.isEmpty(firstName)) mFirstName.setError(getString(R.string.field_not_empty));
                else if(TextUtils.isEmpty(lastName)) mLastName.setError(getString(R.string.field_not_empty));
                else {
                    contactDao.insert(new Contact(firstName, lastName));
                    setResult(RESULT_CODE_CREATED);
                    finish();
                }
            }
        });
    }
}
