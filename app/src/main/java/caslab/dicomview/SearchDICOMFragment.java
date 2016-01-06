package caslab.dicomview;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SearchDICOMFragment extends Fragment  {

    private final String LOG_TAG = SearchDICOMFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TYPE = "TYPE";
    private static final String ID = "ID";
    private static final String OPTION = "OPTION";

    // TODO: Rename and change types of parameters
    private String mParam1;/* type */
    private String mParam2;/* id */
    private String mParam3;/* option */

    private OnFragmentInteractionListener mListener;

    /**
     *PACS data
     * */
    private JSONObject mSelData;
    private JSONArray mListData;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<String> mAdapter;

    // TODO: Rename and change types of parameters
    public static SearchDICOMFragment newInstance(String param1, String param2, String param3) {
        SearchDICOMFragment fragment = new SearchDICOMFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, param1);
        args.putString(ID, param2);
        args.putString(OPTION, param3);
        fragment.setArguments(args);
        Log.d("fucl", "m1 = " + param1 + ",M2 = " + param2 + ",M3 = " + param3+",B="+args.toString());
        return fragment;
    }

    public SearchDICOMFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }

    private void updateList() {
        FetchListTask task =  new FetchListTask();
        task.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle=getArguments();
            mParam1 = getArguments().getString(TYPE);
            mParam2 = getArguments().getString(ID);
            mParam3 = getArguments().getString(OPTION);
        }

        Log.d(LOG_TAG,"m1 = "+mParam1+",M2 = "+ mParam2 + ",M3 = "+mParam3+","+getArguments().toString());

        // TODO: Change Adapter to display your content
        String[] exampleArray = {
        };

        List<String> list = new ArrayList<String>(
                Arrays.asList(exampleArray)
        );

        mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String id = null;
                String type = null;
                try {
                    mSelData = mListData.getJSONObject(position);
                    id = mSelData.getString("ID");
                    Log.d(LOG_TAG, "click:" + id);
                    switch (mParam1) {
                        case "instances":
                            Log.d(LOG_TAG, "show bitmap!:" + id);
                            type = mParam1;
                        case "patients":
                            if (mParam2 == null)
                                type = "patients";
                            else
                                type = "studies";
                            break;
                        case "studies":
                            type = "series";
                            break;
                        case "series":
                            type = "instances";
                            break;
                        default:
                            Log.d(LOG_TAG, "you shoud not see this!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "onItemClick", e);
                }


                if(type == "instances"){
                    Log.d(LOG_TAG, "show bitmap!:" + id);
                    Intent detailActivity_Intent = new Intent(getActivity(), ImageActivity.class)
                            .putExtra(ID, id);
                    startActivity(detailActivity_Intent);
                }
                else {
                    Intent detailActivity_Intent = new Intent(getActivity(), MainActivity.class)
                            .putExtra(TYPE, type)
                            .putExtra(ID, id);
                    startActivity(detailActivity_Intent);
                }
            }
        });

        return view;
    }


    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public class FetchListTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchListTask.class.getSimpleName();

        private static final String PACS_BASE_URL =
                "http://140.116.102.47:8042/";

        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String searchJsonStr = null;

            try {
                Uri.Builder buildUri = Uri.parse(PACS_BASE_URL).buildUpon();
                buildUri.appendPath(mParam1);
                if(mParam2 != null){
                    buildUri.appendPath(mParam2);
                    switch (mParam1){
                        case "patients":
                            buildUri.appendPath("studies");
                            break;
                        case "studies":
                            buildUri.appendPath("series");
                            break;
                        case "series":
                            buildUri.appendPath("instances");
                            break;
                        default:
                            Log.d(LOG_TAG,"You should not see this!");
                    }
                            if(mParam3 != null)
                                buildUri.appendPath(mParam3);
                }
                else
                    buildUri.appendQueryParameter("expand", "true");
                buildUri.build();

                Log.d(LOG_TAG, "m1 = " + mParam1 + ",M2 = " + mParam2 + ",M3 = " + mParam3);
                Log.d(LOG_TAG,buildUri.toString());
                URL url = new URL(buildUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    searchJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    searchJsonStr = null;
                }
                searchJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                searchJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getListDataFromJson(searchJsonStr);
            } catch (JSONException e) {
                Log.e("PlaceholderFragment", "JSONError ", e);
            }
            return null;
        }

        private String[] getListDataFromJson(String searchJsonStr)
                throws JSONException {
            Log.d(LOG_TAG+":decode", "m1 = " + mParam1 + ",M2 = " + mParam2 + ",M3 = " + mParam3);
            String[] resultStrs = null;
            switch (mParam1) {
                case "patients":
                    if (mParam2 == null) {
                        mListData = new JSONArray(searchJsonStr);
                        resultStrs = new String[mListData.length()];
                        for (int i = 0; i < mListData.length(); i++) {
                            resultStrs[i] = mListData.getJSONObject(i).getJSONObject("MainDicomTags").getString("PatientName");
                        }
                        Log.d(LOG_TAG + ":decode", "patient");
                    } else {
                        mListData = (new JSONArray(searchJsonStr));
                        resultStrs = new String[mListData.length()];
                        for (int i = 0; i < mListData.length(); i++) {
                            resultStrs[i] = mListData.getJSONObject(i).getJSONObject("MainDicomTags").getString("StudyDescription");
                        }
                        Log.d(LOG_TAG + ":decode", "patient with id");
                    }
                    break;
                case "studies":
                    mListData = (new JSONArray(searchJsonStr));
                    resultStrs = new String[mListData.length()];
                    for (int i = 0; i < mListData.length(); i++) {
                        resultStrs[i] = mListData.getJSONObject(i).getJSONObject("MainDicomTags").getString("SeriesDescription");
                    }
                    Log.d(LOG_TAG + ":decode", "studies with id");
                    break;
                case "series":
                    mListData = new JSONArray(searchJsonStr);
                    resultStrs = new String[mListData.length()];
                    for (int i = 0; i < mListData.length(); i++) {
                        resultStrs[i] = mListData.getJSONObject(i).getJSONObject("MainDicomTags").getString("InstanceNumber");;
                    }
                    Log.d(LOG_TAG + ":decode", "series with id");
                    break;
                default:
                    Log.d(LOG_TAG + ":decode", "wtf?");
            }

            Log.d(LOG_TAG, resultStrs[0]);
            return resultStrs;
        }

        protected void onPostExecute(String[] result) {
            if(result != null) {
                mAdapter.clear();
                mAdapter.addAll(result);
            }
        }

    }

}
