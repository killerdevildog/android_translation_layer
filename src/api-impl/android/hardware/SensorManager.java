package android.hardware;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import java.util.Arrays;
import java.util.List;

public class SensorManager {

	public static float GRAVITY_EARTH = 9.81f;

	public Sensor getDefaultSensor(int type) {
		return new Sensor(type);
	}

	public boolean registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, Handler handler) {
		return true; // we could try saying that the sensor doesn't exist and hope the app just doesn't use it then, but as long as we never call the handler the app should leave this alone
	}

	public boolean registerListener(final SensorEventListener listener, final Sensor sensor, int samplingPeriodUs) {
		switch (sensor.getType()) {
			case Sensor.TYPE_ORIENTATION:
				new LocationManager().requestLocationUpdates(null, 0, 0, new LocationListener() {
					@Override
					public void onLocationChanged(Location location) {
						listener.onSensorChanged(new SensorEvent(new float[] {location.getBearing()}, sensor));
					}
				});
				return true;
			case Sensor.TYPE_ACCELEROMETER:
				register_accelerometer_listener_native(listener, sensor, samplingPeriodUs);
				return true;
			default:
				return false;
		}
	}

	public void unregisterListener(final SensorEventListener listener) {
		unregisterListener(listener, null);
	}

	public void unregisterListener(final SensorEventListener listener, Sensor sensor) {
		System.out.println("STUB: andoroid.hw.SensorManager.unregisterListener");
	}

	native void register_accelerometer_listener_native(SensorEventListener listener, Sensor sensor, int sampling_period);

	public List<Sensor> getSensorList(int type) {
		return Arrays.asList(getDefaultSensor(type));
	}
}
