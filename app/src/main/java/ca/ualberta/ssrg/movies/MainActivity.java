package ca.ualberta.ssrg.movies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import ca.ualberta.ssrg.androidelasticsearch.R;

public class MainActivity extends Activity {

	private ListView movieList;
	private Movies movies;
	private ArrayAdapter<Movie> moviesViewAdapter;
	private ESMovieManager movieManager;
	private MoviesController moviesController;

	private Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		movieList = (ListView) findViewById(R.id.movieList);
	}

	@Override
	protected void onStart() {
		super.onStart();

		movies = new Movies();
		moviesViewAdapter = new ArrayAdapter<Movie>(this, R.layout.list_item,movies);
		movieList.setAdapter(moviesViewAdapter);
		movieManager = new ESMovieManager("");

		// Show details when click on a movie
		movieList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,	long id) {
				int movieId = movies.get(pos).getId();
				startDetailsActivity(movieId);
			}

		});

		// Delete movie on long click
		movieList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Movie movie = movies.get(position);
				Toast.makeText(mContext, "Deleting " + movie.getTitle(), Toast.LENGTH_LONG).show();

				Thread thread = new DeleteThread(movie.getId());
				thread.start();

				return true;
			}
		});
	}

	//sets up search thread attribute
	@Override
	protected void onResume() {
		super.onResume();
		
		
		SearchThread thread = new SearchThread("*");

		thread.start();


	}
	
	/** 
	 * Called when the model changes
	 */
	public void notifyUpdated() {
		// Thread to update adapter after an operation
		//Switch t UI thread
		Runnable doUpdateGUIList = new Runnable() {
			public void run() {
				moviesViewAdapter.notifyDataSetChanged();
			}
		};
		//run on UI thread
		runOnUiThread(doUpdateGUIList);
	}

	/** 
	 * Search for movies with a given word(s) in the text view
	 * @param view
	 */
	public void search(View view) {
		movies.clear();

		// TODO: Extract search query from text view
		EditText search = (EditText) findViewById(R.id.editText1);
		//edittext1
		
		// TODO: Run the search thread
		SearchThread thread = new SearchThread(search.getText().toString());
		thread.start();
	}
	
	/**
	 * Starts activity with details for a movie
	 * @param movieId Movie id
	 */
	public void startDetailsActivity(int movieId) {
		Intent intent = new Intent(mContext, DetailsActivity.class);
		intent.putExtra(DetailsActivity.MOVIE_ID, movieId);

		startActivity(intent);
	}
	
	/**
	 * Starts activity to add a new movie
	 * @param view
	 */
	public void add(View view) {
		Intent intent = new Intent(mContext, AddActivity.class);
		startActivity(intent);
	}

//calling .start creates the new thread and calls the run object
	class SearchThread extends Thread {
		private String search;

		public SearchThread(String search) {
			this.search = search;
		}

		@Override
		public void run() {
			movies.clear();
			movies.addAll(movieManager.searchMovies(search, null));
			//call notifyupdated to switch back to UI thread
			notifyUpdated();
		}
		
	}

	
	class DeleteThread extends Thread {
		private int movieId;

		public DeleteThread(int movieId) {
			this.movieId = movieId;
		}

		@Override
		public void run() {
			moviesController.deleteMovie(movieId);

			// Remove movie from local list
			for (int i = 0; i < movies.size(); i++) {
				Movie m = movies.get(i);

				if (m.getId() == movieId) {
					movies.remove(m);
					break;
				}
			}
		}
	}
}