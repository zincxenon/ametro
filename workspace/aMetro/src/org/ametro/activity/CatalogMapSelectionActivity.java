package org.ametro.activity;

import java.util.ArrayList;
import java.util.HashSet;

import org.ametro.ApplicationEx;
import org.ametro.GlobalSettings;
import org.ametro.R;
import org.ametro.adapter.CheckedCatalogAdapter;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.CatalogMapPairEx;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.CatalogStorageStateProvider;
import org.ametro.util.CollectionUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class CatalogMapSelectionActivity extends Activity implements ICatalogStateProvider, OnClickListener {

	public static final String EXTRA_TITLE = "EXTRA_TITLE";
	public static final String EXTRA_REMOTE_ID = "EXTRA_REMOTE_ID";
	public static final String EXTRA_FILTER = "EXTRA_FILTER";
	public static final String EXTRA_CHECKABLE_STATES = "EXTRA_CHECKABLE_STATES";
	public static final String EXTRA_VISIBLE_STATES = "EXTRA_VISIBLE_STATES";
	public static final String EXTRA_SORT_MODE = "EXTRA_SORT_MODE";
	public static final String EXTRA_SELECTION = "EXTRA_SELECTION";

	private CatalogStorage mStorage;
	private CatalogStorageStateProvider mStorageState;

	private CheckedCatalogAdapter mAdapter;
	private ListView mList;

	private final int MAIN_MENU_DONE = 1;
	private final int MAIN_MENU_SELECT_ALL = 2;
	private final int MAIN_MENU_SELECT_NONE = 3;

	private Catalog mLocal;
	private Catalog mRemote;

	private int mLocalId;
	private int mRemoteId;

	private int mDiffMode = CatalogMapPair.DIFF_MODE_REMOTE;
	private int mDiffColors;

	private String mFilter;
	private int mSortMode;
	
	private Button mActionButton;
	private Button mCancelButton;
	
	private HashSet<Integer> mCheckableStates;
	private HashSet<Integer> mVisibleStates;
	
	public boolean onSearchRequested() {
		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int doneText = mRemoteId == CatalogStorage.ONLINE ? R.string.menu_download : R.string.menu_import;
		menu.add(0, MAIN_MENU_DONE, Menu.NONE, doneText).setIcon( android.R.drawable.ic_menu_add);
		menu.add(0, MAIN_MENU_SELECT_ALL, 1, R.string.menu_select_all).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, MAIN_MENU_SELECT_NONE, 2, R.string.menu_select_none).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_DONE:
			invokeFinishWithResult();
			return true;
		case MAIN_MENU_SELECT_ALL:
			setCheckAll();
			mList.invalidateViews();
			return true;
		case MAIN_MENU_SELECT_NONE:
			setCheckNone();
			mList.invalidateViews();
			return true;
			
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!extractData()){
			invokeFinish();
			return;
		}
		
		setContentView(R.layout.catalog_map_selection_list);
		
		mActionButton = (Button)findViewById(R.id.btn_action);
		mCancelButton = (Button)findViewById(R.id.btn_cancel);
		
		mActionButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		mList = (ListView) findViewById(R.id.list);
		mList.setItemsCanFocus(true);
		mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		ArrayList<CatalogMapPairEx> data = getFilteredData();
		if(data == null || data.size() == 0){
			invokeFinish();
			return;
		}
		mAdapter = new CheckedCatalogAdapter(this, mList, data, mDiffColors, this, mSortMode);
		mList.setAdapter(mAdapter);
	}
	
	private boolean extractData() {
		Intent data = getIntent();
		if (data == null) {
			return false;
		}

		String title = data.getStringExtra(EXTRA_TITLE);
		if(title!=null){
			setTitle(title);
		}
		
		mLocalId = CatalogStorage.LOCAL;
		mRemoteId = data.getIntExtra(EXTRA_REMOTE_ID, -1);
		mFilter = data.getStringExtra(EXTRA_FILTER);
		mCheckableStates = CollectionUtil.toHashSet(data.getIntArrayExtra(EXTRA_CHECKABLE_STATES));
		mVisibleStates = CollectionUtil.toHashSet(data.getIntArrayExtra(EXTRA_VISIBLE_STATES));
		mSortMode = data.getIntExtra(EXTRA_SORT_MODE, CheckedCatalogAdapter.SORT_MODE_COUNTRY);
		
		mDiffMode = CatalogMapPair.DIFF_MODE_REMOTE;
		mDiffColors = mRemoteId == CatalogStorage.ONLINE ? R.array.online_catalog_map_state_colors : R.array.import_catalog_map_state_colors;
		mStorage = ((ApplicationEx) getApplicationContext()) .getCatalogStorage();
		mStorageState = new CatalogStorageStateProvider(mStorage);

		mLocal = mStorage.getCatalog(mLocalId);
		mRemote = mStorage.getCatalog(mRemoteId);
		return mRemote != null && mLocal != null;
	}

	/*package*/ ArrayList<CatalogMapPairEx> getFilteredData() {
		ArrayList<CatalogMapPair> values = CatalogMapPair.diff(mLocal, mRemote, mDiffMode);

		final int count = values.size();
		final String code = GlobalSettings.getLanguage(this);
		final ArrayList<CatalogMapPairEx> newValues = new ArrayList<CatalogMapPairEx>(count);
		for (int i = 0; i < count; i++) {
		    final CatalogMapPair originalValue = values.get(i);
		    int state = getCatalogState(originalValue.getLocal(), originalValue.getRemote());
		    boolean visible = mVisibleStates==null || mVisibleStates.contains(state);
		    if(!visible){
		    	continue;
		    }
		    boolean checkable = mCheckableStates==null || mCheckableStates.contains(state);
		    final CatalogMapPairEx value = new CatalogMapPairEx(originalValue, checkable, false, true);

		    final String cityName = originalValue.getCity(code).toString().toLowerCase();
		    final String countryName = originalValue.getCountry(code).toString().toLowerCase();
		    
		    // First match against the whole, non-splitted value
		    if (mFilter==null || cityName.startsWith(mFilter) || countryName.startsWith(mFilter)) {
		        newValues.add(value);
		    } else {
		    	boolean added = false;
		        final String[] cityWords = cityName.split(" ");
		        final int cityWordCount = cityWords.length;

		        for (int k = 0; k < cityWordCount; k++) {
		            if (cityWords[k].startsWith(mFilter)) {
		                newValues.add(value);
		                added = true;
		                break;
		            }
		        }
		        if(!added){
			        final String[] countryWords = countryName.split(" ");
			        final int countryWordCount = countryWords.length;

			        for (int k = 0; k < countryWordCount; k++) {
			            if (countryWords[k].startsWith(mFilter)) {
			                newValues.add(value);
			                break;
			            }
			        }
		        }
		    }
		}
		return newValues;
	}

	protected void invokeFinish() {
		setResult(RESULT_CANCELED);
		finish();
	}

	protected void invokeFinishWithResult() {
		ArrayList<String> selection = new ArrayList<String>();
		final int len = mList.getCount();
		for (int i = 0; i < len; i++) {
			if (mList.isItemChecked(i)) {
				CatalogMapPair pair = (CatalogMapPair) mList.getItemAtPosition(i);
				selection.add(pair.getSystemName());
			}
		}
		Intent data = new Intent();
		data.putExtra(EXTRA_SELECTION, (String[]) selection.toArray(new String[selection.size()]));
		setResult(RESULT_OK, data);
		finish();
	}

	public int getCatalogState(CatalogMap local, CatalogMap remote) {
		return mRemoteId == CatalogStorage.ONLINE ? mStorageState
				.getOnlineCatalogState(local, remote) : mStorageState
				.getImportCatalogState(local, remote);
	}

	
	public void setCheckAll() {
		final int len = mList.getCount();
		for(int i=0;i<len;i++){
			mList.setItemChecked(i, true);
		}
	}

	public void setCheckNone() {
		final int len = mList.getCount();
		for(int i=0;i<len;i++){
			mList.setItemChecked(i, false);
		}
	}

	public void onClick(View v) {
		if(v == mActionButton){
			invokeFinishWithResult();
		}else  if(v == mCancelButton){
			invokeFinish();
		}
	}
	
}
