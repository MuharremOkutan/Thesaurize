package edu.uwec.cs.hurdad.thesaurize;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ThesaurizeActivity extends Activity {
	private final String API_KEY = "9ed13fa88d10a23467efc83f3d0107eb";

	private EditText inputWordEditText;
	private Button thesaurizeButton;
	private TextView statusTextView;

	private ArrayList<String> nouns;
	private ArrayList<String> verbs;
	private ArrayList<String> adjectives;
	private ArrayList<String> adverbs;

	private Button nounButton;
	private Button verbButton;
	private Button adjectiveButton;
	private Button adverbButton;

	private ListView nounListView;
	private ListView verbListView;
	private ListView adjectiveListView;
	private ListView adverbListView;

	private ArrayAdapter<String> nounAdapter;
	private ArrayAdapter<String> verbAdapter;
	private ArrayAdapter<String> adjectiveAdapter;
	private ArrayAdapter<String> adverbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thesaurus_activity);

		// grab handles for full layout, text input, and search synonyms submit button
		inputWordEditText = (EditText) findViewById(R.id.inputWordEditText);
		thesaurizeButton = (Button) findViewById(R.id.thesaurizeButton);
		statusTextView = (TextView) findViewById(R.id.statusTextView);

		// set thesaurizeButton on click to execute synonym request
		thesaurizeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				String word = inputWordEditText.getText().toString();
				if (!word.equals("")) {
					new DownloadTask().execute(word.trim());
				}
			}
		});

		// initialize data backing list views
		nouns = new ArrayList<String>();
		verbs = new ArrayList<String>();
		adjectives = new ArrayList<String>();
		adverbs = new ArrayList<String>();

		// grab handles on list view show / hide buttons
		nounButton = (Button) findViewById(R.id.nounButton);
		verbButton = (Button) findViewById(R.id.verbButton);
		adjectiveButton = (Button) findViewById(R.id.adjectiveButton);
		adverbButton = (Button) findViewById(R.id.adverbButton);

		// grab handles on list views
		nounListView = (ListView) findViewById(R.id.nounListView);
		verbListView = (ListView) findViewById(R.id.verbListView);
		adjectiveListView = (ListView) findViewById(R.id.adjectiveListView);
		adverbListView = (ListView) findViewById(R.id.adverbListView);
		
		// recover interrupted state
		if (savedInstanceState != null) {
			statusTextView.setText(savedInstanceState.getString("status"));
			statusTextView.setVisibility(View.VISIBLE);
			
			nouns = savedInstanceState.getStringArrayList("nouns");
			verbs = savedInstanceState.getStringArrayList("verbs");
			adjectives = savedInstanceState.getStringArrayList("adjectives");
			adverbs = savedInstanceState.getStringArrayList("adverbs");
			
			if (nouns.size() > 0) {
				nounButton.setVisibility(View.VISIBLE);
			}
			nounListView.setVisibility(savedInstanceState.getInt("nounListVisibility"));
			if (verbs.size() > 0) {
				verbButton.setVisibility(View.VISIBLE);
			}
			verbListView.setVisibility(savedInstanceState.getInt("verbListVisibility"));
			if (adjectives.size() > 0) {
				adjectiveButton.setVisibility(View.VISIBLE);
			}
			adjectiveListView.setVisibility(savedInstanceState.getInt("adjectiveListVisibility"));
			if (adverbs.size() > 0) {
				adverbButton.setVisibility(View.VISIBLE);
			}
			adverbListView.setVisibility(savedInstanceState.getInt("adverbListVisibility"));
		}

		nounAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nouns);
		nounListView.setAdapter(nounAdapter);

		verbAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, verbs);
		verbListView.setAdapter(verbAdapter);

		adjectiveAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, adjectives);
		adjectiveListView.setAdapter(adjectiveAdapter);

		adverbAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, adverbs);
		adverbListView.setAdapter(adverbAdapter);

		setupOnClickShowToggle(nounButton, nounListView);
		setupOnClickShowToggle(verbButton, verbListView);
		setupOnClickShowToggle(adjectiveButton, adjectiveListView);
		setupOnClickShowToggle(adverbButton, adverbListView);
	}

	private void setupOnClickShowToggle(View trigger, final View toggled) {

		trigger.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (toggled.getVisibility() == View.GONE) {
					toggled.setVisibility(View.VISIBLE);
				} else {
					toggled.setVisibility(View.GONE);
				}
			}
		});
	}

	private ArrayList<String> findSynonymsByConstruct(String construct,
			JSONObject root) {

		ArrayList<String> synonyms = new ArrayList<String>();

		try {

			JSONObject constructObject = root.getJSONObject(construct);
			JSONArray constructSyns = constructObject.getJSONArray("syn");

			for (int i = 0; i < constructSyns.length(); i++) {
				String s = constructSyns.getString(i);
				synonyms.add(s);
			}
		} catch (JSONException e) {
			/*
			 * 
			 * the json returned by the web service does not include all
			 * construct definitions in every response. so, if a word has no
			 * verb usage, no verb key will be found. so, we will not treat this
			 * as an error, the program should just continue on
			 */
		}

		return synonyms;
	}

	private Map<String, ArrayList<String>> retrieveConstructSynonyms(String word) {

		Map<String, ArrayList<String>> constructSynonyms = new HashMap<String, ArrayList<String>>();

		try {

			URL url = new URL("http://words.bighugelabs.com/api/2/" + API_KEY + "/" + word + "/json");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream in = connection.getInputStream();

			String json = slurp(in);
			in.close();

			JSONObject root = new JSONObject(json);

			String[] languageConstructs = { "noun", "verb", "adjective", "adverb" };

			for (String construct : languageConstructs) {
				constructSynonyms.put(construct, findSynonymsByConstruct(construct, root));
			}

		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
		}

		return constructSynonyms;
	}

	class DownloadTask extends AsyncTask<String, Void, Map<String, ArrayList<String>>> {
		private String word;

		@Override
		protected Map<String, ArrayList<String>> doInBackground(String... params) {
			this.word = params[0];
			return retrieveConstructSynonyms(word);
		}

		@Override
		protected void onPostExecute(Map<String, ArrayList<String>> result) {
			super.onPostExecute(result);

			if (result.size() > 0) {
				nouns.clear();
				nouns.addAll(result.get("noun"));
				nounAdapter.notifyDataSetChanged();
				if (nouns.size() > 0) {
					nounButton.setVisibility(View.VISIBLE);
				} else {
					nounButton.setVisibility(View.GONE);
				}
				nounListView.setVisibility(View.GONE);

				verbs.clear();
				verbs.addAll(result.get("verb"));
				verbAdapter.notifyDataSetChanged();
				if (verbs.size() > 0) {
					verbButton.setVisibility(View.VISIBLE);
				} else {
					verbButton.setVisibility(View.GONE);
				}
				verbListView.setVisibility(View.GONE);

				adjectives.clear();
				adjectives.addAll(result.get("adjective"));
				adjectiveAdapter.notifyDataSetChanged();
				if (adjectives.size() > 0) {
					adjectiveButton.setVisibility(View.VISIBLE);
				} else {
					adjectiveButton.setVisibility(View.GONE);
				}
				adjectiveListView.setVisibility(View.GONE);

				adverbs.clear();
				adverbs.addAll(result.get("adverb"));
				adverbAdapter.notifyDataSetChanged();
				if (adverbs.size() > 0) {
					adverbButton.setVisibility(View.VISIBLE);
				} else {
					adverbButton.setVisibility(View.GONE);
				}
				adverbListView.setVisibility(View.GONE);
				
				statusTextView.setText(getString(R.string.synonyms_found_for) + " '" + word + "'");
				statusTextView.setVisibility(View.VISIBLE);
			} else {
				statusTextView.setText(getString(R.string.no_synonyms_found_for) + " '" + word + "'");
				statusTextView.setVisibility(View.VISIBLE);
			}
		}

	}

	public static String slurp(InputStream in) throws IOException {
		StringBuilder builder = new StringBuilder();
		byte[] buffer = new byte[1024];
		int bytesRead = 0;

		while ((bytesRead = in.read(buffer)) >= 0) {
			builder.append(new String(buffer, 0, bytesRead));
		}

		return builder.toString();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString("status", statusTextView.getText().toString());
		
		outState.putStringArrayList("nouns", nouns);
		outState.putStringArrayList("verbs", verbs);
		outState.putStringArrayList("adjectives", adjectives);
		outState.putStringArrayList("adverbs", adverbs);
		
		outState.putInt("nounListVisibility", nounListView.getVisibility());
		outState.putInt("verbListVisibility", verbListView.getVisibility());
		outState.putInt("adjectiveListVisibility", adjectiveListView.getVisibility());
		outState.putInt("adverbListVisibility", adverbListView.getVisibility());
	}

}
