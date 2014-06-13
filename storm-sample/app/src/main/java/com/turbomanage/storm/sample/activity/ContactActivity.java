package com.turbomanage.storm.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.turbomanage.storm.sample.R;
import com.turbomanage.storm.sample.adapter.ContactAdapter;
import com.turbomanage.storm.sample.loader.ContactLoader;
import com.turbomanage.storm.sample.model.Contact;
import com.turbomanage.storm.sample.model.dao.ContactDao;

import java.util.List;

import de.timroes.android.listview.EnhancedListView;


public class ContactActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<List<Contact>> {

    private static final int REQUEST_CODE = 101;

    private EnhancedListView mListView;
	private ContactAdapter mAdapter;
    private ContactDao mContactDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);
        mListView = (EnhancedListView) findViewById(android.R.id.list);

        mContactDao = new ContactDao(this);

        mListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, int i) {

                Contact contact = mAdapter.getItem(i);
                mContactDao.delete(contact.getId());
                mAdapter.remove(i);
                return null;
            }
        });

        mListView.enableSwipeToDismiss();
        getSupportLoaderManager().initLoader(1, null, this);
    }
	
	/**
     * The default content for this Activity has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
		if (mListView != null){
			View emptyView = mListView.getEmptyView();

			if (emptyText instanceof TextView) {
				((TextView) emptyView).setText(emptyText);
			}
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add) {

            startActivityForResult(new Intent(this, NewContactActivity.class), REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == NewContactActivity.RESULT_CODE_CREATED){

            getSupportLoaderManager().restartLoader(1, null, this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
        return new ContactLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Contact>> loader, List<Contact> data) {

        mAdapter = new ContactAdapter(this, data);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Contact>> loader) {

        setEmptyText("No data");
    }
}
