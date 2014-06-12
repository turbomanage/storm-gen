package com.turbomanage.storm.sample.loader;

import android.content.Context;

import com.turbomanage.storm.sample.model.Contact;
import com.turbomanage.storm.sample.model.dao.ContactDao;

import java.util.List;

/**
 * Created by galex on 11/06/14.
 */
public class ContactLoader extends AbstractAsyncTaskLoader<List<Contact>> {

    private ContactDao mContactDao;

    public ContactLoader(Context context) {

        super(context);
        mContactDao = new ContactDao(context);
    }

    @Override
    public List<Contact> loadInBackground() {
        return mContactDao.listAll();
    }
}
