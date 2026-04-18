package android.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

public class MediaMetadataRetriever {

	private MediaPlayer mediaPlayer;

	public void release() {
		if (mediaPlayer != null)
			mediaPlayer.release();
	}

	public void setDataSource(Context context, Uri uri) {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(uri.getPath());
	}

	public void setDataSource(String path) {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDataSource(path);
	}

	public byte[] getEmbeddedPicture() {
		return null;
	}

	public String extractMetadata(int key) {
		switch (key) {
			case 9 /*METADATA_KEY_DURATION*/:
				return String.valueOf(mediaPlayer.getDuration());
			default:
				return null;
		}
	}

	public Bitmap getFrameAtTime(long time) {
		return null;
	}

	public Bitmap getFrameAtTime() {
		return null;
	}
}
