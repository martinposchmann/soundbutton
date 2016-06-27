package de.mposchmann.soundbutton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class SoundButtonWidgetProvider extends AppWidgetProvider {

	private static final String TAG = SoundButtonWidgetProvider.class.getSimpleName();

	private static final String ACTION_CLICK = "ACTION_CLICK";

	private static final Map<Integer, ButtonContext> BUTTON_CONTEXTS = new HashMap<Integer, ButtonContext>();
	//private static List<MediaPlayer> MEDIA_PLAYERS = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, SoundButtonWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			// Create some random data
			int number = (new Random().nextInt(100));

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			Log.w("WidgetExample", String.valueOf(number));
			// Set the text
			// remoteViews.setTextViewText(R.id.update, String.valueOf(number));

			// set onclick handler
			Intent clickIntent = new Intent(context, SoundButtonWidgetProvider.class);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setAction(ACTION_CLICK);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, widgetId, clickIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive" + (intent.getAction() != null ? intent.getAction() : "null"));

		// check if is broadcast action of the button
		if (intent.getAction() != null && ACTION_CLICK.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);

				Log.i(TAG, "onReceive: widget-id = " + appWidgetId);

				// init media-players

				ButtonContext buttonContext = BUTTON_CONTEXTS.get(appWidgetId);
				if (buttonContext == null) {
					buttonContext = new ButtonContext();
					buttonContext.setContext(context);
					BUTTON_CONTEXTS.put(appWidgetId, buttonContext);
					
					//init media players
					List<MediaPlayer> mediaPlayers = new ArrayList<MediaPlayer>();
					
					mediaPlayers.add(createMediaPlayer(context, appWidgetId, R.raw.didi2));
					//mediaPlayers.add(createMediaPlayer(context, appWidgetId, R.raw.didi3));

					buttonContext.setMediaPlayers(mediaPlayers);
				}

				MediaPlayer currentPlayer = buttonContext.getCurrentPlayer();
				if (currentPlayer != null && currentPlayer.isPlaying()) {

					// stop
					currentPlayer.pause();
					currentPlayer.seekTo(0);

					// restore old volume
					// audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					// 0, AudioManager.FLAG_PLAY_SOUND);

				} else {
					// choose next player
					int nextPlayer;
					if (buttonContext.getMediaPlayers().size() > 1) {
						nextPlayer = buttonContext.getCurrentPlayerIndex();
						while (nextPlayer == buttonContext.getCurrentPlayerIndex()) {
							nextPlayer = (int) (Math.random() * (double) buttonContext.getMediaPlayers().size());
						}
					} else {
						nextPlayer = 0;
					}
					currentPlayer = buttonContext.getMediaPlayers().get(nextPlayer);
					buttonContext.setCurrentPlayer(currentPlayer);
					buttonContext.setCurrentPlayerIndex(nextPlayer);
					
					currentPlayer.start();
				}

				// toogle background shape
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
				//remoteViews.setInt(R.id.update, "setBackgroundResource",
				//		(buttonContext.isPressed() ? R.drawable.button_green_shape : R.drawable.button_red_shape));
				//remoteViews.setTextViewText(R.id.update, (buttonContext.isPressed() ? "..." : "HaHa!"));
				//toggle button image
				remoteViews.setImageViewResource(R.id.update, (buttonContext.isPressed() ? R.mipmap.button_green : R.mipmap.button_red));
				buttonContext.setPressed(!buttonContext.isPressed());

				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		} else {
			super.onReceive(context, intent);
		}

	}
	
	private MediaPlayer createMediaPlayer(Context context, final int appWidgetId, int resId) {
		
		MediaPlayer mPlayer = MediaPlayer.create(context, resId);
		
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		    public void onCompletion(MediaPlayer mp) {
		    	Log.i(TAG, "media-player if widget-id = " + appWidgetId + " finished");
		    	
		    	ButtonContext buttonContext = BUTTON_CONTEXTS.get(appWidgetId);
		    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(buttonContext.getContext());
		    	// toogle background shape
				RemoteViews remoteViews = new RemoteViews(buttonContext.getContext().getPackageName(), R.layout.widget_layout);
				//toggle button image
				remoteViews.setImageViewResource(R.id.update, (buttonContext.isPressed() ? R.mipmap.button_green : R.mipmap.button_red));
				
				buttonContext.setPressed(!buttonContext.isPressed());

				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		    }
		});
		
		return mPlayer;
	}

	private static class ButtonContext {
		private boolean pressed = false;
		private List<MediaPlayer> mediaPlayers = null;
		private MediaPlayer currentPlayer;
		private int currentPlayerIndex = -1;
		private Context context;

		public boolean isPressed() {
			return pressed;
		}

		public void setPressed(boolean pressed) {
			this.pressed = pressed;
		}

		public MediaPlayer getCurrentPlayer() {
			return currentPlayer;
		}

		public void setCurrentPlayer(MediaPlayer currentPlayer) {
			this.currentPlayer = currentPlayer;
		}

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public int getCurrentPlayerIndex() {
			return currentPlayerIndex;
		}

		public void setCurrentPlayerIndex(int currentPlayerIndex) {
			this.currentPlayerIndex = currentPlayerIndex;
		}

		public List<MediaPlayer> getMediaPlayers() {
			return mediaPlayers;
		}

		public void setMediaPlayers(List<MediaPlayer> mediaPlayers) {
			this.mediaPlayers = mediaPlayers;
		}
		
		

	}
}