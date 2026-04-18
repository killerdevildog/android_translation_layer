package android.animation;

import android.util.Log;
import android.util.Property;
import java.lang.reflect.Method;

public class PropertyValuesHolder {

	private float values_float[];
	private int values_int[];
	private Object values_object[];
	private Object value;
	private Keyframe values_keyframe[];

	private String property_name;
	private Method setter;
	Property property;
	private TypeEvaluator<Object> evaluator;

	public static PropertyValuesHolder ofFloat(String propertyName, float... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_float = values;
		propertyValuesHolder.property_name = propertyName;
		propertyValuesHolder.value = values[0];
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofObject(String propertyName, TypeEvaluator evaluator, Object... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_object = values;
		propertyValuesHolder.property_name = propertyName;
		propertyValuesHolder.value = values[0];
		propertyValuesHolder.evaluator = evaluator;
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofInt(String propertyName, int... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_int = values;
		propertyValuesHolder.property_name = propertyName;
		propertyValuesHolder.value = values[0];
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofKeyframe(String propertyName, Keyframe... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_keyframe = values;
		propertyValuesHolder.property_name = propertyName;
		propertyValuesHolder.value = values[0];
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofFloat(Property property, float... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_float = values;
		propertyValuesHolder.property_name = property.getName();
		propertyValuesHolder.property = property;
		propertyValuesHolder.value = values[0];
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofObject(Property property, TypeEvaluator evaluator, Object... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_object = values;
		propertyValuesHolder.property_name = property.getName();
		propertyValuesHolder.property = property;
		propertyValuesHolder.value = values[0];
		propertyValuesHolder.evaluator = evaluator;
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofInt(Property property, int... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_int = values;
		propertyValuesHolder.property_name = property.getName();
		propertyValuesHolder.property = property;
		propertyValuesHolder.value = values[0];
		return propertyValuesHolder;
	}

	public static PropertyValuesHolder ofKeyframe(Property property, Keyframe... values) {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.values_keyframe = values;
		propertyValuesHolder.property_name = property.getName();
		propertyValuesHolder.property = property;
		propertyValuesHolder.value = values[0];
		return propertyValuesHolder;
	}

	public void setIntValues(int... values) {
		values_int = values;
	}

	public void setFloatValues(float... values) {
		values_float = values;
	}

	public void setObjectValues(Object... values) {
		values_object = values;
	}

	public void setKeyframes(Keyframe... values) {
		values_keyframe = values;
	}

	public String getProperty_name() {
		return property_name;
	}

	public void setProperty_name(String propertyName) {
		this.property_name = propertyName;
		property = null;
	}

	public void setProperty(Property property) {
		property_name = property.getName();
		this.property = property;
	}

	public void init() {}

	public Object getAnimatedValue() {
		return value;
	}

	public void setEvaluator(TypeEvaluator<Object> evaluator) {
		this.evaluator = evaluator;
	}

	public void calculateValue(float fraction) {
		if (fraction < 0f)
			fraction = 0f;
		if (fraction > 1f)
			fraction = 1f;
		if (values_object != null && evaluator != null) {
			int i = (int)(fraction * (values_object.length - 1));
			float f = fraction * (values_object.length - 1) - i;
			value = evaluator.evaluate(f, values_object[i], values_object[i >= values_object.length - 1 ? i : i + 1]);
		} else if (values_object != null) {
			value = values_object[(int)(fraction * (values_object.length - 1) + 0.5f)];
		} else if (values_float != null) {
			int i = (int)(fraction * (values_float.length - 1));
			float f = fraction * (values_float.length - 1) - i;
			value = values_float[i] * (1 - f) + ((f != 0.f) ? values_float[i + 1] * f : 0.f);
		} else if (values_int != null) {
			int i = (int)(fraction * (values_int.length - 1));
			float f = fraction * (values_int.length - 1) - i;
			value = (int)(values_int[i] * (1 - f) + ((f != 0.f) ? values_int[i + 1] * f : 0.f) + 0.5f);
		} else {
			Log.e("PropertyValuesHolder", "No values set");
		}
	}

	public PropertyValuesHolder clone() {
		PropertyValuesHolder propertyValuesHolder = new PropertyValuesHolder();
		propertyValuesHolder.property = property;
		propertyValuesHolder.property_name = property_name;
		propertyValuesHolder.setter = setter;
		propertyValuesHolder.value = value;
		propertyValuesHolder.values_float = values_float;
		propertyValuesHolder.values_int = values_int;
		propertyValuesHolder.values_object = values_object;
		propertyValuesHolder.values_keyframe = values_keyframe;
		return propertyValuesHolder;
	}

	public void setupSetterAndGetter(Object target) {
		if (property != null) {
			setter = null;
			if (values_int != null && values_int.length == 1) {
				values_int = new int[] {(Integer)property.get(target), values_int[0]};
			} else if (values_float != null && values_float.length == 1) {
				values_float = new float[] {(Float)property.get(target), values_float[0]};
			} else if (values_object != null && values_object.length == 1) {
				values_object = new Object[] {property.get(target), values_object[0]};
			}
			return;
		}
		try {
			Class<?> clazz;
			if (values_float != null) {
				clazz = float.class;
			} else if (values_int != null) {
				clazz = int.class;
			} else {
				clazz = values_object[0].getClass();
			}
			setter = target.getClass().getMethod("set" + property_name.substring(0, 1).toUpperCase() + property_name.substring(1), clazz);
			Method getter = target.getClass().getMethod("get" + property_name.substring(0, 1).toUpperCase() + property_name.substring(1));
			if (values_int != null && values_int.length == 1) {
				values_int = new int[] {(Integer)getter.invoke(target), values_int[0]};
			} else if (values_float != null && values_float.length == 1) {
				values_float = new float[] {(Float)getter.invoke(target), values_float[0]};
			} else if (values_object != null && values_object.length == 1) {
				values_object = new Object[] {getter.invoke(target), values_object[0]};
			}
		} catch (ReflectiveOperationException e) {
			Log.e("PropertyValuesHolder", "failed to setup setter and getter", e);
		}
	}

	public void setupStartValue(Object target) {
	}

	public void setupEndValue(Object target) {
	}

	public void setAnimatedValue(Object target) {
		if (property != null) {
			property.set(target, value);
			return;
		} else if (setter != null && value != null) {
			try {
				setter.invoke(target, value);
			} catch (ReflectiveOperationException e) {
				Log.e("PropertyValuesHolder", "failed to invoke setter", e);
			}
		} else {
			Log.e("PropertyValuesHolder", "no setter or value set");
		}
	}
}
