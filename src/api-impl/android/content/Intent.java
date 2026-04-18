package android.content;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;

public class Intent implements Parcelable {
	public static final int FLAG_ACTIVITY_CLEAR_TOP = 1 << 26;
	public static final int FLAG_ACTIVITY_NEW_TASK = 1 << 28;

	public static final String ACTION_ACTIVITY_RECOGNIZER = "android.intent.action.ACTIVITY_RECOGNIZER";
	public static final String ACTION_ADVANCED_SETTINGS_CHANGED = "android.intent.action.ADVANCED_SETTINGS";
	public static final String ACTION_AIRPLANE_MODE_CHANGED = "android.intent.action.AIRPLANE_MODE";
	public static final String ACTION_ALARM_CHANGED = "android.intent.action.ALARM_CHANGED";
	public static final String ACTION_ALL_APPS = "android.intent.action.ALL_APPS";
	public static final String ACTION_ANSWER = "android.intent.action.ANSWER";
	public static final String ACTION_APP_ERROR = "android.intent.action.APP_ERROR";
	public static final String ACTION_APPLICATION_LOCALE_CHANGED = "android.intent.action.APPLICATION_LOCALE_CHANGED";
	public static final String ACTION_APPLICATION_PREFERENCES = "android.intent.action.APPLICATION_PREFERENCES";
	public static final String ACTION_APPLICATION_RESTRICTIONS_CHANGED = "android.intent.action.APPLICATION_RESTRICTIONS_CHANGED";
	public static final String ACTION_ASSIST = "android.intent.action.ASSIST";
	public static final String ACTION_ATTACH_DATA = "android.intent.action.ATTACH_DATA";
	public static final String ACTION_AUTO_REVOKE_PERMISSIONS = "android.intent.action.AUTO_REVOKE_PERMISSIONS";
	public static final String ACTION_BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
	public static final String ACTION_BATTERY_LEVEL_CHANGED = "android.intent.action.BATTERY_LEVEL_CHANGED";
	public static final String ACTION_BATTERY_LOW = "android.intent.action.BATTERY_LOW";
	public static final String ACTION_BATTERY_OKAY = "android.intent.action.BATTERY_OKAY";
	public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	public static final String ACTION_BUG_REPORT = "android.intent.action.BUG_REPORT";
	public static final String ACTION_CALL = "android.intent.action.CALL";
	public static final String ACTION_CALL_BUTTON = "android.intent.action.CALL_BUTTON";
	public static final String ACTION_CALL_EMERGENCY = "android.intent.action.CALL_EMERGENCY";
	public static final String ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
	public static final String ACTION_CAMERA_BUTTON = "android.intent.action.CAMERA_BUTTON";
	public static final String ACTION_CANCEL_ENABLE_ROLLBACK = "android.intent.action.CANCEL_ENABLE_ROLLBACK";
	public static final String ACTION_CARRIER_SETUP = "android.intent.action.CARRIER_SETUP";
	public static final String ACTION_CHOOSER = "android.intent.action.CHOOSER";
	public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
	public static final String ACTION_CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
	public static final String ACTION_CREATE_DOCUMENT = "android.intent.action.CREATE_DOCUMENT";
	public static final String ACTION_CREATE_NOTE = "android.intent.action.CREATE_NOTE";
	public static final String ACTION_CREATE_REMINDER = "android.intent.action.CREATE_REMINDER";
	public static final String ACTION_CREATE_SHORTCUT = "android.intent.action.CREATE_SHORTCUT";
	public static final String ACTION_DATE_CHANGED = "android.intent.action.DATE_CHANGED";
	public static final String ACTION_DEFINE = "android.intent.action.DEFINE";
	public static final String ACTION_DELETE = "android.intent.action.DELETE";
	public static final String ACTION_DEVICE_CUSTOMIZATION_READY = "android.intent.action.DEVICE_CUSTOMIZATION_READY";
	public static final String ACTION_DEVICE_INITIALIZATION_WIZARD = "android.intent.action.DEVICE_INITIALIZATION_WIZARD";
	public static final String ACTION_DEVICE_LOCKED_CHANGED = "android.intent.action.DEVICE_LOCKED_CHANGED";
	public static final String ACTION_DEVICE_STORAGE_FULL = "android.intent.action.DEVICE_STORAGE_FULL";
	public static final String ACTION_DEVICE_STORAGE_LOW = "android.intent.action.DEVICE_STORAGE_LOW";
	public static final String ACTION_DEVICE_STORAGE_NOT_FULL = "android.intent.action.DEVICE_STORAGE_NOT_FULL";
	public static final String ACTION_DEVICE_STORAGE_OK = "android.intent.action.DEVICE_STORAGE_OK";
	public static final String ACTION_DIAL = "android.intent.action.DIAL";
	public static final String ACTION_DIAL_EMERGENCY = "android.intent.action.DIAL_EMERGENCY";
	public static final String ACTION_DISMISS_KEYBOARD_SHORTCUTS = "com.android.intent.action.DISMISS_KEYBOARD_SHORTCUTS";
	public static final String ACTION_DISTRACTING_PACKAGES_CHANGED = "android.intent.action.DISTRACTING_PACKAGES_CHANGED";
	public static final String ACTION_DOCK_ACTIVE = "android.intent.action.DOCK_ACTIVE";
	public static final String ACTION_DOCK_EVENT = "android.intent.action.DOCK_EVENT";
	public static final String ACTION_DOCK_IDLE = "android.intent.action.DOCK_IDLE";
	public static final String ACTION_DOMAINS_NEED_VERIFICATION = "android.intent.action.DOMAINS_NEED_VERIFICATION";
	public static final String ACTION_DREAMING_STARTED = "android.intent.action.DREAMING_STARTED";
	public static final String ACTION_DREAMING_STOPPED = "android.intent.action.DREAMING_STOPPED";
	public static final String ACTION_DYNAMIC_SENSOR_CHANGED = "android.intent.action.DYNAMIC_SENSOR_CHANGED";
	public static final String ACTION_EDIT = "android.intent.action.EDIT";
	public static final String ACTION_EXTERNAL_APPLICATIONS_AVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
	public static final String ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
	public static final String ACTION_FACTORY_RESET = "android.intent.action.FACTORY_RESET";
	public static final String ACTION_FACTORY_TEST = "android.intent.action.FACTORY_TEST";
	public static final String ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT";
	public static final String ACTION_GET_RESTRICTION_ENTRIES = "android.intent.action.GET_RESTRICTION_ENTRIES";
	public static final String ACTION_GLOBAL_BUTTON = "android.intent.action.GLOBAL_BUTTON";
	public static final String ACTION_GTALK_SERVICE_CONNECTED = "android.intent.action.GTALK_CONNECTED";
	public static final String ACTION_GTALK_SERVICE_DISCONNECTED = "android.intent.action.GTALK_DISCONNECTED";
	public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
	public static final String ACTION_IDLE_MAINTENANCE_END = "android.intent.action.ACTION_IDLE_MAINTENANCE_END";
	public static final String ACTION_IDLE_MAINTENANCE_START = "android.intent.action.ACTION_IDLE_MAINTENANCE_START";
	public static final String ACTION_INCIDENT_REPORT_READY = "android.intent.action.INCIDENT_REPORT_READY";
	public static final String ACTION_INPUT_METHOD_CHANGED = "android.intent.action.INPUT_METHOD_CHANGED";
	public static final String ACTION_INSERT = "android.intent.action.INSERT";
	public static final String ACTION_INSERT_OR_EDIT = "android.intent.action.INSERT_OR_EDIT";
	public static final String ACTION_INSTALL_FAILURE = "android.intent.action.INSTALL_FAILURE";
	public static final String ACTION_INSTALL_INSTANT_APP_PACKAGE = "android.intent.action.INSTALL_INSTANT_APP_PACKAGE";
	public static final String ACTION_INSTALL_PACKAGE = "android.intent.action.INSTALL_PACKAGE";
	public static final String ACTION_INSTANT_APP_RESOLVER_SETTINGS = "android.intent.action.INSTANT_APP_RESOLVER_SETTINGS";
	public static final String ACTION_INTENT_FILTER_NEEDS_VERIFICATION = "android.intent.action.INTENT_FILTER_NEEDS_VERIFICATION";
	public static final String ACTION_LAUNCH_CAPTURE_CONTENT_ACTIVITY_FOR_NOTE = "android.intent.action.LAUNCH_CAPTURE_CONTENT_ACTIVITY_FOR_NOTE";
	public static final String ACTION_LOAD_DATA = "android.intent.action.LOAD_DATA";
	public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
	public static final String ACTION_LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
	public static final String ACTION_MAIN = "android.intent.action.MAIN";
	public static final String ACTION_MAIN_USER_LOCKSCREEN_KNOWLEDGE_FACTOR_CHANGED = "android.intent.action.MAIN_USER_LOCKSCREEN_KNOWLEDGE_FACTOR_CHANGED";
	public static final String ACTION_MANAGE_APP_PERMISSION = "android.intent.action.MANAGE_APP_PERMISSION";
	public static final String ACTION_MANAGE_APP_PERMISSIONS = "android.intent.action.MANAGE_APP_PERMISSIONS";
	public static final String ACTION_MANAGE_DEFAULT_APP = "android.intent.action.MANAGE_DEFAULT_APP";
	public static final String ACTION_MANAGED_PROFILE_ADDED = "android.intent.action.MANAGED_PROFILE_ADDED";
	public static final String ACTION_MANAGED_PROFILE_AVAILABLE = "android.intent.action.MANAGED_PROFILE_AVAILABLE";
	public static final String ACTION_MANAGED_PROFILE_REMOVED = "android.intent.action.MANAGED_PROFILE_REMOVED";
	public static final String ACTION_MANAGED_PROFILE_UNAVAILABLE = "android.intent.action.MANAGED_PROFILE_UNAVAILABLE";
	public static final String ACTION_MANAGED_PROFILE_UNLOCKED = "android.intent.action.MANAGED_PROFILE_UNLOCKED";
	public static final String ACTION_MANAGE_NETWORK_USAGE = "android.intent.action.MANAGE_NETWORK_USAGE";
	public static final String ACTION_MANAGE_PACKAGE_STORAGE = "android.intent.action.MANAGE_PACKAGE_STORAGE";
	public static final String ACTION_MANAGE_PERMISSION_APPS = "android.intent.action.MANAGE_PERMISSION_APPS";
	public static final String ACTION_MANAGE_PERMISSIONS = "android.intent.action.MANAGE_PERMISSIONS";
	public static final String ACTION_MANAGE_PERMISSION_USAGE = "android.intent.action.MANAGE_PERMISSION_USAGE";
	public static final String ACTION_MANAGE_SPECIAL_APP_ACCESSES = "android.intent.action.MANAGE_SPECIAL_APP_ACCESSES";
	public static final String ACTION_MANAGE_UNUSED_APPS = "android.intent.action.MANAGE_UNUSED_APPS";
	public static final String ACTION_MASTER_CLEAR = "android.intent.action.MASTER_CLEAR";
	public static final String ACTION_MASTER_CLEAR_NOTIFICATION = "android.intent.action.MASTER_CLEAR_NOTIFICATION";
	public static final String ACTION_MEDIA_BAD_REMOVAL = "android.intent.action.MEDIA_BAD_REMOVAL";
	public static final String ACTION_MEDIA_BUTTON = "android.intent.action.MEDIA_BUTTON";
	public static final String ACTION_MEDIA_CHECKING = "android.intent.action.MEDIA_CHECKING";
	public static final String ACTION_MEDIA_EJECT = "android.intent.action.MEDIA_EJECT";
	public static final String ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
	public static final String ACTION_MEDIA_NOFS = "android.intent.action.MEDIA_NOFS";
	public static final String ACTION_MEDIA_REMOVED = "android.intent.action.MEDIA_REMOVED";
	public static final String ACTION_MEDIA_RESOURCE_GRANTED = "android.intent.action.MEDIA_RESOURCE_GRANTED";
	public static final String ACTION_MEDIA_SCANNER_FINISHED = "android.intent.action.MEDIA_SCANNER_FINISHED";
	public static final String ACTION_MEDIA_SCANNER_SCAN_FILE = "android.intent.action.MEDIA_SCANNER_SCAN_FILE";
	public static final String ACTION_MEDIA_SCANNER_STARTED = "android.intent.action.MEDIA_SCANNER_STARTED";
	public static final String ACTION_MEDIA_SHARED = "android.intent.action.MEDIA_SHARED";
	public static final String ACTION_MEDIA_UNMOUNTABLE = "android.intent.action.MEDIA_UNMOUNTABLE";
	public static final String ACTION_MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED";
	public static final String ACTION_MEDIA_UNSHARED = "android.intent.action.MEDIA_UNSHARED";
	public static final String ACTION_MY_PACKAGE_REPLACED = "android.intent.action.MY_PACKAGE_REPLACED";
	public static final String ACTION_MY_PACKAGE_SUSPENDED = "android.intent.action.MY_PACKAGE_SUSPENDED";
	public static final String ACTION_MY_PACKAGE_UNSUSPENDED = "android.intent.action.MY_PACKAGE_UNSUSPENDED";
	public static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
	public static final String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
	public static final String ACTION_OPEN_DOCUMENT_TREE = "android.intent.action.OPEN_DOCUMENT_TREE";
	public static final String ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED";
	public static final String ACTION_PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
	public static final String ACTION_PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED";
	public static final String ACTION_PACKAGE_DATA_CLEARED = "android.intent.action.PACKAGE_DATA_CLEARED";
	public static final String ACTION_PACKAGE_ENABLE_ROLLBACK = "android.intent.action.PACKAGE_ENABLE_ROLLBACK";
	public static final String ACTION_PACKAGE_FIRST_LAUNCH = "android.intent.action.PACKAGE_FIRST_LAUNCH";
	public static final String ACTION_PACKAGE_FULLY_REMOVED = "android.intent.action.PACKAGE_FULLY_REMOVED";
	public static final String ACTION_PACKAGE_INSTALL = "android.intent.action.PACKAGE_INSTALL";
	public static final String ACTION_PACKAGE_NEEDS_INTEGRITY_VERIFICATION = "android.intent.action.PACKAGE_NEEDS_INTEGRITY_VERIFICATION";
	public static final String ACTION_PACKAGE_NEEDS_VERIFICATION = "android.intent.action.PACKAGE_NEEDS_VERIFICATION";
	public static final String ACTION_PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
	public static final String ACTION_PACKAGE_REMOVED_INTERNAL = "android.intent.action.PACKAGE_REMOVED_INTERNAL";
	public static final String ACTION_PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
	public static final String ACTION_PACKAGE_RESTARTED = "android.intent.action.PACKAGE_RESTARTED";
	public static final String ACTION_PACKAGES_SUSPENDED = "android.intent.action.PACKAGES_SUSPENDED";
	public static final String ACTION_PACKAGES_SUSPENSION_CHANGED = "android.intent.action.PACKAGES_SUSPENSION_CHANGED";
	public static final String ACTION_PACKAGES_UNSUSPENDED = "android.intent.action.PACKAGES_UNSUSPENDED";
	public static final String ACTION_PACKAGE_UNSTOPPED = "android.intent.action.PACKAGE_UNSTOPPED";
	public static final String ACTION_PACKAGE_UNSUSPENDED_MANUALLY = "android.intent.action.PACKAGE_UNSUSPENDED_MANUALLY";
	public static final String ACTION_PACKAGE_VERIFIED = "android.intent.action.PACKAGE_VERIFIED";
	public static final String ACTION_PASTE = "android.intent.action.PASTE";
	public static final String ACTION_PENDING_INCIDENT_REPORTS_CHANGED = "android.intent.action.PENDING_INCIDENT_REPORTS_CHANGED";
	public static final String ACTION_PICK_ACTIVITY = "android.intent.action.PICK_ACTIVITY";
	public static final String ACTION_PICK = "android.intent.action.PICK";
	public static final String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
	public static final String ACTION_POWER_DISCONNECTED = "android.intent.action.ACTION_POWER_DISCONNECTED";
	public static final String ACTION_POWER_USAGE_SUMMARY = "android.intent.action.POWER_USAGE_SUMMARY";
	public static final String ACTION_PRE_BOOT_COMPLETED = "android.intent.action.PRE_BOOT_COMPLETED";
	public static final String ACTION_PREFERRED_ACTIVITY_CHANGED = "android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED";
	public static final String ACTION_PROCESS_TEXT = "android.intent.action.PROCESS_TEXT";
	public static final String ACTION_PROFILE_ACCESSIBLE = "android.intent.action.PROFILE_ACCESSIBLE";
	public static final String ACTION_PROFILE_ADDED = "android.intent.action.PROFILE_ADDED";
	public static final String ACTION_PROFILE_AVAILABLE = "android.intent.action.PROFILE_AVAILABLE";
	public static final String ACTION_PROFILE_INACCESSIBLE = "android.intent.action.PROFILE_INACCESSIBLE";
	public static final String ACTION_PROFILE_REMOVED = "android.intent.action.PROFILE_REMOVED";
	public static final String ACTION_PROFILE_UNAVAILABLE = "android.intent.action.PROFILE_UNAVAILABLE";
	public static final String ACTION_PROVIDER_CHANGED = "android.intent.action.PROVIDER_CHANGED";
	public static final String ACTION_QUERY_PACKAGE_RESTART = "android.intent.action.QUERY_PACKAGE_RESTART";
	public static final String ACTION_QUICK_CLOCK = "android.intent.action.QUICK_CLOCK";
	public static final String ACTION_QUICK_VIEW = "android.intent.action.QUICK_VIEW";
	public static final String ACTION_REBOOT = "android.intent.action.REBOOT";
	public static final String ACTION_REMOTE_INTENT = "com.google.android.c2dm.intent.RECEIVE";
	public static final String ACTION_REQUEST_SHUTDOWN = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
	public static final String ACTION_RESOLVE_INSTANT_APP_PACKAGE = "android.intent.action.RESOLVE_INSTANT_APP_PACKAGE";
	public static final String ACTION_REVIEW_ACCESSIBILITY_SERVICES = "android.intent.action.REVIEW_ACCESSIBILITY_SERVICES";
	public static final String ACTION_REVIEW_APP_DATA_SHARING_UPDATES = "android.intent.action.REVIEW_APP_DATA_SHARING_UPDATES";
	public static final String ACTION_REVIEW_ONGOING_PERMISSION_USAGE = "android.intent.action.REVIEW_ONGOING_PERMISSION_USAGE";
	public static final String ACTION_REVIEW_PERMISSION_HISTORY = "android.intent.action.REVIEW_PERMISSION_HISTORY";
	public static final String ACTION_REVIEW_PERMISSIONS = "android.intent.action.REVIEW_PERMISSIONS";
	public static final String ACTION_REVIEW_PERMISSION_USAGE = "android.intent.action.REVIEW_PERMISSION_USAGE";
	public static final String ACTION_ROLLBACK_COMMITTED = "android.intent.action.ROLLBACK_COMMITTED";
	public static final String ACTION_RUN = "android.intent.action.RUN";
	public static final String ACTION_SAFETY_CENTER = "android.intent.action.SAFETY_CENTER";
	public static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
	public static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
	public static final String ACTION_SEARCH = "android.intent.action.SEARCH";
	public static final String ACTION_SEARCH_LONG_PRESS = "android.intent.action.SEARCH_LONG_PRESS";
	public static final String ACTION_SEND = "android.intent.action.SEND";
	public static final String ACTION_SEND_MULTIPLE = "android.intent.action.SEND_MULTIPLE";
	public static final String ACTION_SENDTO = "android.intent.action.SENDTO";
	public static final String ACTION_SERVICE_STATE = "android.intent.action.SERVICE_STATE";
	public static final String ACTION_SETTING_RESTORED = "android.os.action.SETTING_RESTORED";
	public static final String ACTION_SET_WALLPAPER = "android.intent.action.SET_WALLPAPER";
	public static final String ACTION_SHOW_APP_INFO = "android.intent.action.SHOW_APP_INFO";
	public static final String ACTION_SHOW_BRIGHTNESS_DIALOG = "com.android.intent.action.SHOW_BRIGHTNESS_DIALOG";
	public static final String ACTION_SHOW_FOREGROUND_SERVICE_MANAGER = "android.intent.action.SHOW_FOREGROUND_SERVICE_MANAGER";
	public static final String ACTION_SHOW_KEYBOARD_SHORTCUTS = "com.android.intent.action.SHOW_KEYBOARD_SHORTCUTS";
	public static final String ACTION_SHOW_SUSPENDED_APP_DETAILS = "android.intent.action.SHOW_SUSPENDED_APP_DETAILS";
	public static final String ACTION_SHOW_WORK_APPS = "android.intent.action.SHOW_WORK_APPS";
	public static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
	public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	public static final String ACTION_SPLIT_CONFIGURATION_CHANGED = "android.intent.action.SPLIT_CONFIGURATION_CHANGED";
	public static final String ACTION_SYNC = "android.intent.action.SYNC";
	public static final String ACTION_SYSTEM_TUTORIAL = "android.intent.action.SYSTEM_TUTORIAL";
	public static final String ACTION_THERMAL_EVENT = "android.intent.action.THERMAL_EVENT";
	public static final String ACTION_TIME_CHANGED = "android.intent.action.TIME_SET";
	public static final String ACTION_TIME_TICK = "android.intent.action.TIME_TICK";
	public static final String ACTION_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED";
	public static final String ACTION_TRANSLATE = "android.intent.action.TRANSLATE";
	public static final String ACTION_UID_REMOVED = "android.intent.action.UID_REMOVED";
	public static final String ACTION_UMS_CONNECTED = "android.intent.action.UMS_CONNECTED";
	public static final String ACTION_UMS_DISCONNECTED = "android.intent.action.UMS_DISCONNECTED";
	public static final String ACTION_UNARCHIVE_PACKAGE = "android.intent.action.UNARCHIVE_PACKAGE";
	public static final String ACTION_UNINSTALL_PACKAGE = "android.intent.action.UNINSTALL_PACKAGE";
	public static final String ACTION_UPGRADE_SETUP = "android.intent.action.UPGRADE_SETUP";
	public static final String ACTION_USER_ADDED = "android.intent.action.USER_ADDED";
	public static final String ACTION_USER_BACKGROUND = "android.intent.action.USER_BACKGROUND";
	public static final String ACTION_USER_FOREGROUND = "android.intent.action.USER_FOREGROUND";
	public static final String ACTION_USER_INFO_CHANGED = "android.intent.action.USER_INFO_CHANGED";
	public static final String ACTION_USER_INITIALIZE = "android.intent.action.USER_INITIALIZE";
	public static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
	public static final String ACTION_USER_REMOVED = "android.intent.action.USER_REMOVED";
	public static final String ACTION_USER_STARTED = "android.intent.action.USER_STARTED";
	public static final String ACTION_USER_STARTING = "android.intent.action.USER_STARTING";
	public static final String ACTION_USER_STOPPED = "android.intent.action.USER_STOPPED";
	public static final String ACTION_USER_STOPPING = "android.intent.action.USER_STOPPING";
	public static final String ACTION_USER_SWITCHED = "android.intent.action.USER_SWITCHED";
	public static final String ACTION_USER_UNLOCKED = "android.intent.action.USER_UNLOCKED";
	public static final String ACTION_VIEW = "android.intent.action.VIEW";
	public static final String ACTION_VIEW_APP_FEATURES = "android.intent.action.VIEW_APP_FEATURES";
	public static final String ACTION_VIEW_LOCUS = "android.intent.action.VIEW_LOCUS";
	public static final String ACTION_VIEW_PERMISSION_USAGE = "android.intent.action.VIEW_PERMISSION_USAGE";
	public static final String ACTION_VIEW_PERMISSION_USAGE_FOR_PERIOD = "android.intent.action.VIEW_PERMISSION_USAGE_FOR_PERIOD";
	public static final String ACTION_VIEW_SAFETY_CENTER_QS = "android.intent.action.VIEW_SAFETY_CENTER_QS";
	public static final String ACTION_VOICE_ASSIST = "android.intent.action.VOICE_ASSIST";
	public static final String ACTION_VOICE_COMMAND = "android.intent.action.VOICE_COMMAND";
	public static final String ACTION_WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED";
	public static final String ACTION_WEB_SEARCH = "android.intent.action.WEB_SEARCH";

	public static final String ACTION_DEFAULT = ACTION_VIEW;

	public static final String CATEGORY_ACCESSIBILITY_SHORTCUT_TARGET = "android.intent.category.ACCESSIBILITY_SHORTCUT_TARGET";
	public static final String CATEGORY_ALTERNATIVE = "android.intent.category.ALTERNATIVE";
	public static final String CATEGORY_APP_BROWSER = "android.intent.category.APP_BROWSER";
	public static final String CATEGORY_APP_CALCULATOR = "android.intent.category.APP_CALCULATOR";
	public static final String CATEGORY_APP_CALENDAR = "android.intent.category.APP_CALENDAR";
	public static final String CATEGORY_APP_CONTACTS = "android.intent.category.APP_CONTACTS";
	public static final String CATEGORY_APP_EMAIL = "android.intent.category.APP_EMAIL";
	public static final String CATEGORY_APP_FILES = "android.intent.category.APP_FILES";
	public static final String CATEGORY_APP_FITNESS = "android.intent.category.APP_FITNESS";
	public static final String CATEGORY_APP_GALLERY = "android.intent.category.APP_GALLERY";
	public static final String CATEGORY_APP_MAPS = "android.intent.category.APP_MAPS";
	public static final String CATEGORY_APP_MARKET = "android.intent.category.APP_MARKET";
	public static final String CATEGORY_APP_MESSAGING = "android.intent.category.APP_MESSAGING";
	public static final String CATEGORY_APP_MUSIC = "android.intent.category.APP_MUSIC";
	public static final String CATEGORY_APP_WEATHER = "android.intent.category.APP_WEATHER";
	public static final String CATEGORY_BROWSABLE = "android.intent.category.BROWSABLE";
	public static final String CATEGORY_CAR_DOCK = "android.intent.category.CAR_DOCK";
	public static final String CATEGORY_CAR_LAUNCHER = "android.intent.category.CAR_LAUNCHER";
	public static final String CATEGORY_CAR_MODE = "android.intent.category.CAR_MODE";
	public static final String CATEGORY_COMMUNAL_MODE = "android.intent.category.COMMUNAL_MODE";
	public static final String CATEGORY_DEFAULT = "android.intent.category.DEFAULT";
	public static final String CATEGORY_DESK_DOCK = "android.intent.category.DESK_DOCK";
	public static final String CATEGORY_DEVELOPMENT_PREFERENCE = "android.intent.category.DEVELOPMENT_PREFERENCE";
	public static final String CATEGORY_EMBED = "android.intent.category.EMBED";
	public static final String CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST = "android.intent.category.FRAMEWORK_INSTRUMENTATION_TEST";
	public static final String CATEGORY_HE_DESK_DOCK = "android.intent.category.HE_DESK_DOCK";
	public static final String CATEGORY_HOME = "android.intent.category.HOME";
	public static final String CATEGORY_HOME_MAIN = "android.intent.category.HOME_MAIN";
	public static final String CATEGORY_INFO = "android.intent.category.INFO";
	public static final String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";
	public static final String CATEGORY_LAUNCHER_APP = "android.intent.category.LAUNCHER_APP";
	public static final String CATEGORY_LEANBACK_LAUNCHER = "android.intent.category.LEANBACK_LAUNCHER";
	public static final String CATEGORY_LEANBACK_SETTINGS = "android.intent.category.LEANBACK_SETTINGS";
	public static final String CATEGORY_LE_DESK_DOCK = "android.intent.category.LE_DESK_DOCK";
	public static final String CATEGORY_MONKEY = "android.intent.category.MONKEY";
	public static final String CATEGORY_OPENABLE = "android.intent.category.OPENABLE";
	public static final String CATEGORY_PREFERENCE = "android.intent.category.PREFERENCE";
	public static final String CATEGORY_SAMPLE_CODE = "android.intent.category.SAMPLE_CODE";
	public static final String CATEGORY_SECONDARY_HOME = "android.intent.category.SECONDARY_HOME";
	public static final String CATEGORY_SELECTED_ALTERNATIVE = "android.intent.category.SELECTED_ALTERNATIVE";
	public static final String CATEGORY_SETUP_WIZARD = "android.intent.category.SETUP_WIZARD";
	public static final String CATEGORY_TAB = "android.intent.category.TAB";
	public static final String CATEGORY_TEST = "android.intent.category.TEST";
	public static final String CATEGORY_TYPED_OPENABLE = "android.intent.category.TYPED_OPENABLE";
	public static final String CATEGORY_UNIT_TEST = "android.intent.category.UNIT_TEST";
	public static final String CATEGORY_VOICE = "android.intent.category.VOICE";
	public static final String CATEGORY_VR_HOME = "android.intent.category.VR_HOME";

	public static final String EXTRA_ALARM_COUNT = "android.intent.extra.ALARM_COUNT";
	public static final String EXTRA_ALLOW_MULTIPLE = "android.intent.extra.ALLOW_MULTIPLE";
	public static final String EXTRA_ALLOW_REPLACE = "android.intent.extra.ALLOW_REPLACE";
	public static final String EXTRA_ALTERNATE_INTENTS = "android.intent.extra.ALTERNATE_INTENTS";
	public static final String EXTRA_ARCHIVAL = "android.intent.extra.ARCHIVAL";
	public static final String EXTRA_ASSIST_CONTEXT = "android.intent.extra.ASSIST_CONTEXT";
	public static final String EXTRA_ASSIST_INPUT_DEVICE_ID = "android.intent.extra.ASSIST_INPUT_DEVICE_ID";
	public static final String EXTRA_ASSIST_INPUT_HINT_KEYBOARD = "android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD";
	public static final String EXTRA_ASSIST_PACKAGE = "android.intent.extra.ASSIST_PACKAGE";
	public static final String EXTRA_ASSIST_UID = "android.intent.extra.ASSIST_UID";
	public static final String EXTRA_ATTRIBUTION_TAGS = "android.intent.extra.ATTRIBUTION_TAGS";
	public static final String EXTRA_AUTO_LAUNCH_SINGLE_CHOICE = "android.intent.extra.AUTO_LAUNCH_SINGLE_CHOICE";
	public static final String EXTRA_BCC = "android.intent.extra.BCC";
	public static final String EXTRA_BRIGHTNESS_DIALOG_IS_FULL_WIDTH = "android.intent.extra.BRIGHTNESS_DIALOG_IS_FULL_WIDTH";
	public static final String EXTRA_BUG_REPORT = "android.intent.extra.BUG_REPORT";
	public static final String EXTRA_CALLING_PACKAGE = "android.intent.extra.CALLING_PACKAGE";
	public static final String EXTRA_CAPTURE_CONTENT_FOR_NOTE_STATUS_CODE = "android.intent.extra.CAPTURE_CONTENT_FOR_NOTE_STATUS_CODE";
	public static final String EXTRA_CC = "android.intent.extra.CC";
	public static final String EXTRA_CDMA_DEFAULT_ROAMING_INDICATOR = "cdmaDefaultRoamingIndicator";
	public static final String EXTRA_CDMA_ROAMING_INDICATOR = "cdmaRoamingIndicator";
	public static final String EXTRA_CHANGED_COMPONENT_NAME = "android.intent.extra.changed_component_name";
	public static final String EXTRA_CHANGED_COMPONENT_NAME_LIST = "android.intent.extra.changed_component_name_list";
	public static final String EXTRA_CHANGED_PACKAGE_LIST = "android.intent.extra.changed_package_list";
	public static final String EXTRA_CHANGED_UID_LIST = "android.intent.extra.changed_uid_list";
	public static final String EXTRA_CHOOSER_ADDITIONAL_CONTENT_URI = "android.intent.extra.CHOOSER_ADDITIONAL_CONTENT_URI";
	public static final String EXTRA_CHOOSER_CONTENT_TYPE_HINT = "android.intent.extra.CHOOSER_CONTENT_TYPE_HINT";
	public static final String EXTRA_CHOOSER_CUSTOM_ACTIONS = "android.intent.extra.CHOOSER_CUSTOM_ACTIONS";
	public static final String EXTRA_CHOOSER_FOCUSED_ITEM_POSITION = "android.intent.extra.CHOOSER_FOCUSED_ITEM_POSITION";
	public static final String EXTRA_CHOOSER_MODIFY_SHARE_ACTION = "android.intent.extra.CHOOSER_MODIFY_SHARE_ACTION";
	public static final String EXTRA_CHOOSER_REFINEMENT_INTENT_SENDER = "android.intent.extra.CHOOSER_REFINEMENT_INTENT_SENDER";
	public static final String EXTRA_CHOOSER_RESULT = "android.intent.extra.CHOOSER_RESULT";
	public static final String EXTRA_CHOOSER_RESULT_INTENT_SENDER = "android.intent.extra.CHOOSER_RESULT_INTENT_SENDER";
	public static final String EXTRA_CHOOSER_TARGETS = "android.intent.extra.CHOOSER_TARGETS";
	public static final String EXTRA_CHOSEN_COMPONENT = "android.intent.extra.CHOSEN_COMPONENT";
	public static final String EXTRA_CHOSEN_COMPONENT_INTENT_SENDER = "android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER";
	public static final String EXTRA_CLIENT_INTENT = "android.intent.extra.client_intent";
	public static final String EXTRA_CLIENT_LABEL = "android.intent.extra.client_label";
	public static final String EXTRA_COMPONENT_NAME = "android.intent.extra.COMPONENT_NAME";
	public static final String EXTRA_CONTENT_ANNOTATIONS = "android.intent.extra.CONTENT_ANNOTATIONS";
	public static final String EXTRA_CONTENT_QUERY = "android.intent.extra.CONTENT_QUERY";
	public static final String EXTRA_CSS_INDICATOR = "cssIndicator";
	public static final String EXTRA_DATA_OPERATOR_ALPHA_LONG = "data-operator-alpha-long";
	public static final String EXTRA_DATA_OPERATOR_ALPHA_SHORT = "data-operator-alpha-short";
	public static final String EXTRA_DATA_OPERATOR_NUMERIC = "data-operator-numeric";
	public static final String EXTRA_DATA_RADIO_TECH = "dataRadioTechnology";
	public static final String EXTRA_DATA_REG_STATE = "dataRegState";
	public static final String EXTRA_DATA_REMOVED = "android.intent.extra.DATA_REMOVED";
	public static final String EXTRA_DATA_ROAMING_TYPE = "dataRoamingType";
	public static final String EXTRA_DISTRACTION_RESTRICTIONS = "android.intent.extra.distraction_restrictions";
	public static final String EXTRA_DOCK_STATE = "android.intent.extra.DOCK_STATE";
	public static final String EXTRA_DONT_KILL_APP = "android.intent.extra.DONT_KILL_APP";
	public static final String EXTRA_DURATION_MILLIS = "android.intent.extra.DURATION_MILLIS";
	public static final String EXTRA_EMAIL = "android.intent.extra.EMAIL";
	public static final String EXTRA_EMERGENCY_ONLY = "emergencyOnly";
	public static final String EXTRA_END_TIME = "android.intent.extra.END_TIME";
	public static final String EXTRA_EXCLUDE_COMPONENTS = "android.intent.extra.EXCLUDE_COMPONENTS";
	public static final String EXTRA_FORCE_FACTORY_RESET = "android.intent.extra.FORCE_FACTORY_RESET";
	public static final String EXTRA_FORCE_MASTER_CLEAR = "android.intent.extra.FORCE_MASTER_CLEAR";
	public static final String EXTRA_FROM_STORAGE = "android.intent.extra.FROM_STORAGE";
	public static final String EXTRA_HTML_TEXT = "android.intent.extra.HTML_TEXT";
	public static final String EXTRA_INDEX = "android.intent.extra.INDEX";
	public static final String EXTRA_INITIAL_INTENTS = "android.intent.extra.INITIAL_INTENTS";
	public static final String EXTRA_INSTALLER_PACKAGE_NAME = "android.intent.extra.INSTALLER_PACKAGE_NAME";
	public static final String EXTRA_INSTALL_RESULT = "android.intent.extra.INSTALL_RESULT";
	public static final String EXTRA_INSTANT_APP_ACTION = "android.intent.extra.INSTANT_APP_ACTION";
	public static final String EXTRA_INSTANT_APP_BUNDLES = "android.intent.extra.INSTANT_APP_BUNDLES";
	public static final String EXTRA_INSTANT_APP_EXTRAS = "android.intent.extra.INSTANT_APP_EXTRAS";
	public static final String EXTRA_INSTANT_APP_FAILURE = "android.intent.extra.INSTANT_APP_FAILURE";
	public static final String EXTRA_INSTANT_APP_HOSTNAME = "android.intent.extra.INSTANT_APP_HOSTNAME";
	public static final String EXTRA_INSTANT_APP_SUCCESS = "android.intent.extra.INSTANT_APP_SUCCESS";
	public static final String EXTRA_INSTANT_APP_TOKEN = "android.intent.extra.INSTANT_APP_TOKEN";
	public static final String EXTRA_INTENT = "android.intent.extra.INTENT";
	public static final String EXTRA_IS_DATA_ROAMING_FROM_REGISTRATION = "isDataRoamingFromRegistration";
	public static final String EXTRA_IS_RESTORE = "android.intent.extra.IS_RESTORE";
	public static final String EXTRA_IS_USING_CARRIER_AGGREGATION = "isUsingCarrierAggregation";
	public static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
	public static final String EXTRA_KEY_EVENT = "android.intent.extra.KEY_EVENT";
	public static final String EXTRA_LOCALE_LIST = "android.intent.extra.LOCALE_LIST";
	public static final String EXTRA_LOCAL_ONLY = "android.intent.extra.LOCAL_ONLY";
	public static final String EXTRA_LOCUS_ID = "android.intent.extra.LOCUS_ID";
	public static final String EXTRA_LONG_VERSION_CODE = "android.intent.extra.LONG_VERSION_CODE";
	public static final String EXTRA_LTE_EARFCN_RSRP_BOOST = "LteEarfcnRsrpBoost";
	public static final String EXTRA_MANUAL = "manual";
	public static final String EXTRA_MEDIA_RESOURCE_TYPE = "android.intent.extra.MEDIA_RESOURCE_TYPE";
	public static final String EXTRA_METADATA_TEXT = "android.intent.extra.METADATA_TEXT";
	public static final String EXTRA_MIME_TYPES = "android.intent.extra.MIME_TYPES";
	public static final String EXTRA_NETWORK_ID = "networkId";
	public static final String EXTRA_NOT_UNKNOWN_SOURCE = "android.intent.extra.NOT_UNKNOWN_SOURCE";
	public static final String EXTRA_OPERATOR_ALPHA_LONG = "operator-alpha-long";
	public static final String EXTRA_OPERATOR_ALPHA_SHORT = "operator-alpha-short";
	public static final String EXTRA_OPERATOR_NUMERIC = "operator-numeric";
	public static final String EXTRA_ORIGINATING_UID = "android.intent.extra.ORIGINATING_UID";
	public static final String EXTRA_ORIGINATING_URI = "android.intent.extra.ORIGINATING_URI";
	public static final String EXTRA_PACKAGE_NAME = "android.intent.extra.PACKAGE_NAME";
	public static final String EXTRA_PACKAGES = "android.intent.extra.PACKAGES";
	public static final String EXTRA_PERMISSION_GROUP_NAME = "android.intent.extra.PERMISSION_GROUP_NAME";
	public static final String EXTRA_PERMISSION_NAME = "android.intent.extra.PERMISSION_NAME";
	public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";
	public static final String EXTRA_PROCESS_TEXT = "android.intent.extra.PROCESS_TEXT";
	public static final String EXTRA_PROCESS_TEXT_READONLY = "android.intent.extra.PROCESS_TEXT_READONLY";
	public static final String EXTRA_QUARANTINED = "android.intent.extra.quarantined";
	public static final String EXTRA_QUICK_VIEW_ADVANCED = "android.intent.extra.QUICK_VIEW_ADVANCED";
	public static final String EXTRA_QUICK_VIEW_FEATURES = "android.intent.extra.QUICK_VIEW_FEATURES";
	public static final String EXTRA_QUIET_MODE = "android.intent.extra.QUIET_MODE";
	public static final String EXTRA_REASON = "android.intent.extra.REASON";
	public static final String EXTRA_REBROADCAST_ON_UNLOCK = "rebroadcastOnUnlock";
	public static final String EXTRA_REFERRER = "android.intent.extra.REFERRER";
	public static final String EXTRA_REFERRER_NAME = "android.intent.extra.REFERRER_NAME";
	public static final String EXTRA_REMOTE_CALLBACK = "android.intent.extra.REMOTE_CALLBACK";
	public static final String EXTRA_REMOTE_INTENT_TOKEN = "android.intent.extra.remote_intent_token";
	public static final String EXTRA_REMOVED_FOR_ALL_USERS = "android.intent.extra.REMOVED_FOR_ALL_USERS";
	public static final String EXTRA_REPLACEMENT_EXTRAS = "android.intent.extra.REPLACEMENT_EXTRAS";
	public static final String EXTRA_REPLACING = "android.intent.extra.REPLACING";
	public static final String EXTRA_RESTRICTIONS_BUNDLE = "android.intent.extra.restrictions_bundle";
	public static final String EXTRA_RESTRICTIONS_INTENT = "android.intent.extra.restrictions_intent";
	public static final String EXTRA_RESTRICTIONS_LIST = "android.intent.extra.restrictions_list";
	public static final String EXTRA_RESULT_NEEDED = "android.intent.extra.RESULT_NEEDED";
	public static final String EXTRA_RESULT_RECEIVER = "android.intent.extra.RESULT_RECEIVER";
	public static final String EXTRA_RETURN_RESULT = "android.intent.extra.RETURN_RESULT";
	public static final String EXTRA_ROLE_NAME = "android.intent.extra.ROLE_NAME";
	public static final String EXTRA_SETTING_NAME = "setting_name";
	public static final String EXTRA_SETTING_NEW_VALUE = "new_value";
	public static final String EXTRA_SETTING_PREVIOUS_VALUE = "previous_value";
	public static final String EXTRA_SETTING_RESTORED_FROM_SDK_INT = "restored_from_sdk_int";
	public static final String EXTRA_SHORTCUT_ICON = "android.intent.extra.shortcut.ICON";
	public static final String EXTRA_SHORTCUT_ICON_RESOURCE = "android.intent.extra.shortcut.ICON_RESOURCE";
	public static final String EXTRA_SHORTCUT_ID = "android.intent.extra.shortcut.ID";
	public static final String EXTRA_SHORTCUT_INTENT = "android.intent.extra.shortcut.INTENT";
	public static final String EXTRA_SHORTCUT_NAME = "android.intent.extra.shortcut.NAME";
	public static final String EXTRA_SHOWING_ATTRIBUTION = "android.intent.extra.SHOWING_ATTRIBUTION";
	public static final String EXTRA_SHOW_WIPE_PROGRESS = "android.intent.extra.SHOW_WIPE_PROGRESS";
	public static final String EXTRA_SHUTDOWN_USERSPACE_ONLY = "android.intent.extra.SHUTDOWN_USERSPACE_ONLY";
	public static final String EXTRA_SIM_ACTIVATION_RESPONSE = "android.intent.extra.SIM_ACTIVATION_RESPONSE";
	public static final String EXTRA_SIM_LOCKED_REASON = "reason";
	public static final String EXTRA_SIM_STATE = "ss";
	public static final String EXTRA_SPLIT_NAME = "android.intent.extra.SPLIT_NAME";
	public static final String EXTRA_START_TIME = "android.intent.extra.START_TIME";
	public static final String EXTRA_STREAM = "android.intent.extra.STREAM";
	public static final String EXTRA_SUBJECT = "android.intent.extra.SUBJECT";
	public static final String EXTRA_SUSPENDED_PACKAGE_EXTRAS = "android.intent.extra.SUSPENDED_PACKAGE_EXTRAS";
	public static final String EXTRA_SYSTEM_ID = "systemId";
	public static final String EXTRA_SYSTEM_UPDATE_UNINSTALL = "android.intent.extra.SYSTEM_UPDATE_UNINSTALL";
	public static final String EXTRA_TASK_ID = "android.intent.extra.TASK_ID";
	public static final String EXTRA_TEMPLATE = "android.intent.extra.TEMPLATE";
	public static final String EXTRA_TEXT = "android.intent.extra.TEXT";
	public static final String EXTRA_THERMAL_STATE = "android.intent.extra.THERMAL_STATE";
	public static final String EXTRA_TIME = "android.intent.extra.TIME";
	public static final String EXTRA_TIME_PREF_24_HOUR_FORMAT = "android.intent.extra.TIME_PREF_24_HOUR_FORMAT";
	public static final String EXTRA_TIMEZONE = "time-zone";
	public static final String EXTRA_TITLE = "android.intent.extra.TITLE";
	public static final String EXTRA_UID = "android.intent.extra.UID";
	public static final String EXTRA_UNINSTALL_ALL_USERS = "android.intent.extra.UNINSTALL_ALL_USERS";
	public static final String EXTRA_UNKNOWN_INSTANT_APP = "android.intent.extra.UNKNOWN_INSTANT_APP";
	public static final String EXTRA_USER = "android.intent.extra.USER";
	public static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
	public static final String EXTRA_USER_ID = "android.intent.extra.USER_ID";
	public static final String EXTRA_USER_INITIATED = "android.intent.extra.USER_INITIATED";
	public static final String EXTRA_USER_REQUESTED_SHUTDOWN = "android.intent.extra.USER_REQUESTED_SHUTDOWN";
	public static final String EXTRA_USE_STYLUS_MODE = "android.intent.extra.USE_STYLUS_MODE";
	public static final String EXTRA_VERIFICATION_BUNDLE = "android.intent.extra.VERIFICATION_BUNDLE";
	public static final String EXTRA_VERSION_CODE = "android.intent.extra.VERSION_CODE";
	public static final String EXTRA_VISIBILITY_ALLOW_LIST = "android.intent.extra.VISIBILITY_ALLOW_LIST";
	public static final String EXTRA_VOICE_RADIO_TECH = "radioTechnology";
	public static final String EXTRA_VOICE_REG_STATE = "voiceRegState";
	public static final String EXTRA_VOICE_ROAMING_TYPE = "voiceRoamingType";
	public static final String EXTRA_WIPE_ESIMS = "com.android.internal.intent.extra.WIPE_ESIMS";
	public static final String EXTRA_WIPE_EXTERNAL_STORAGE = "android.intent.extra.WIPE_EXTERNAL_STORAGE";

	public static final String METADATA_DOCK_HOME = "android.dock_home";
	public static final String METADATA_SETUP_VERSION = "android.SETUP_VERSION";

	public static final String SIM_ABSENT_ON_PERM_DISABLED = "PERM_DISABLED";
	public static final String SIM_LOCKED_NETWORK = "NETWORK";
	public static final String SIM_LOCKED_ON_PIN = "PIN";
	public static final String SIM_LOCKED_ON_PUK = "PUK";
	public static final String SIM_STATE_ABSENT = "ABSENT";
	public static final String SIM_STATE_IMSI = "IMSI";
	public static final String SIM_STATE_LOADED = "LOADED";
	public static final String SIM_STATE_LOCKED = "LOCKED";
	public static final String SIM_STATE_NOT_READY = "NOT_READY";
	public static final String SIM_STATE_PRESENT = "PRESENT";
	public static final String SIM_STATE_READY = "READY";
	public static final String SIM_STATE_UNKNOWN = "UNKNOWN";

	private ComponentName component;
	private Bundle extras = new Bundle();
	private String action;
	private Uri data;
	private int flags;
	private String type;
	private String packageName;
	private Intent selector;

	public Intent() {}
	public Intent(Intent o) {
		this.action = o.action;
		this.data = o.data;
		this.extras = o.extras;
		this.component = o.component;
	}
	public Intent(String action) {
		this.action = action;
	}
	public Intent(String action, Uri uri) {
		this.action = action;
		this.data = uri;
	}
	public Intent(Context packageContext, Class<?> cls) {
		setClass(packageContext, cls);
	}
	public Intent(String action, Uri uri, Context packageContext, Class<?> cls) {
		this(action, uri);
		setClass(packageContext, cls);
	}

	public Intent addFlags(int flags) {
		this.flags |= flags;
		return this;
	}

	public Intent setFlags(int flags) {
		this.flags = flags;
		return this;
	}

	public int getFlags() {
		return flags;
	}

	public Intent setPackage(String packageName) {
		this.packageName = packageName;
		return this;
	}

	public Intent setType(String type) {
		this.type = type;
		return this;
	}

	public Intent putExtra(String name, Parcelable value) {
		extras.putParcelable(name, value);
		return this;
	}

	public Intent putExtra(String name, long[] value) {
		extras.putLongArray(name, value);
		return this;
	}

	public Intent putExtra(String name, byte value) {
		extras.putByte(name, value);
		return this;
	}

	public Intent putExtra(String name, double[] value) {
		extras.putDoubleArray(name, value);
		return this;
	}

	public Intent putExtra(String name, CharSequence value) {
		extras.putCharSequence(name, value);
		return this;
	}

	public Intent putExtra(String name, boolean[] value) {
		extras.putBooleanArray(name, value);
		return this;
	}

	public Intent putExtra(String name, int value) {
		extras.putInt(name, value);
		return this;
	}

	public Intent putExtra(String name, char[] value) {
		extras.putCharArray(name, value);
		return this;
	}

	public Intent putExtra(String name, byte[] value) {
		extras.putByteArray(name, value);
		return this;
	}

	public Intent putExtra(String name, Parcelable[] value) {
		extras.putParcelableArray(name, value);
		return this;
	}

	public Intent putExtra(String name, Bundle value) {
		extras.putBundle(name, value);
		return this;
	}

	public Intent putExtra(String name, CharSequence[] value) {
		extras.putCharSequenceArray(name, value);
		return this;
	}

	public Intent putExtra(String name, float[] value) {
		extras.putFloatArray(name, value);
		return this;
	}

	public Intent putExtra(String name, double value) {
		extras.putDouble(name, value);
		return this;
	}

	public Intent putExtra(String name, int[] value) {
		extras.putIntArray(name, value);
		return this;
	}

	public Intent putExtra(String name, String[] value) {
		extras.putStringArray(name, value);
		return this;
	}

	public Intent putExtra(String name, short[] value) {
		extras.putShortArray(name, value);
		return this;
	}

	public Intent putExtra(String name, boolean value) {
		extras.putBoolean(name, value);
		return this;
	}

	public Intent putExtra(String name, String value) {
		extras.putString(name, value);
		return this;
	}

	public Intent putExtra(String name, long value) {
		extras.putLong(name, value);
		return this;
	}

	public Intent putExtra(String name, char value) {
		extras.putChar(name, value);
		return this;
	}

	public Intent putExtra(String name, Serializable value) {
		extras.putSerializable(name, value);
		return this;
	}

	public Intent putExtra(String name, float value) {
		extras.putFloat(name, value);
		return this;
	}

	public Intent putExtra(String name, short value) {
		extras.putShort(name, value);
		return this;
	}

	public Intent putExtras(Intent src) {
		// FIXME HACK
		this.extras = src.getExtras();
		return this;
	}

	public Intent putExtras(Bundle extras) {
		// FIXME HACK
		this.extras = extras;
		return this;
	}

	public Intent replaceExtras(Bundle extras) {
		this.extras = extras;
		return this;
	}

	public Intent setClass(Context packageContext, Class<?> cls) {
		setComponent(new ComponentName(packageContext, cls));
		return this;
	}

	public Intent setClassName(String packageName, String className) {
		setComponent(new ComponentName(packageName, className));
		return this;
	}

	public String getStringExtra(String name) {
		return (String)extras.get(name);
	}

	public Uri getData() {
		return data;
	}

	public String getDataString() {
		if (data == null)
			return "";

		return data.toString();
	}

	public boolean getBooleanExtra(String name, boolean defaultValue) {
		return extras.getBoolean(name, defaultValue);
	}

	public Intent setAction(String action) {
		this.action = action;
		return this;
	}

	public String getAction() {
		return action;
	}

	public Bundle getBundleExtra(String name) {
		return (Bundle)extras.get(name);
	}

	public Intent setComponent(ComponentName component) {
		this.component = component;
		return this;
	}

	public ComponentName getComponent() {
		return component;
	}

	public boolean hasExtra(String name) {
		return extras.containsKey(name);
	}

	public Serializable getSerializableExtra(String name) {
		return (Serializable)extras.get(name);
	}

	public Parcelable getParcelableExtra(String name) {
		return extras.getParcelable(name);
	}

	public String[] getStringArrayExtra(String name) {
		return extras.getStringArray(name);
	}

	public int getIntExtra(String name, int def) {
		return extras.getInt(name, def);
	}

	public Bundle getExtras() {
		return extras;
	}

	public Intent addCategory(String action) {
		return this;
	}

	@Override
	public String toString() {
		return "Intent [component=" + component + ", extras=" + extras + ", action=" + action + ", type=" + type + ", uri=" + data + "]";
	}

	public static Intent createChooser(Intent target, CharSequence title) {
		return target;
	}

	public Intent setDataAndType(Uri uri, String type) {
		this.data = uri;
		this.type = type;
		return this;
	}

	public long getLongExtra(String name, long def) {
		return extras.getLong(name, def);
	}

	public char getCharExtra(String name, char def) {
		return extras.getChar(name, def);
	}

	public Parcelable[] getParcelableArrayExtra(String name) {
		return extras.getParcelableArray(name);
	}

	public String getType() {
		return type;
	}

	public Intent setData(Uri uri) {
		this.data = uri;
		return this;
	}

	public ArrayList<Parcelable> getParcelableArrayListExtra(String name) {
		return extras.getParcelableArrayList(name);
	}

	public String getPackage() {
		return packageName;
	}

	public String getScheme() {
		return data == null ? null : data.getScheme();
	}

	public Intent putStringArrayListExtra(String name, ArrayList<String> value) {
		extras.putStringArrayList(name, value);
		return this;
	}

	public ArrayList<String> getStringArrayListExtra(String name) {
		return extras.getStringArrayList(name);
	}

	public ClipData getClipData() {
		return null;
	}

	public static class ShortcutIconResource {

		public static ShortcutIconResource fromContext(Context context, int id) {
			return new ShortcutIconResource();
		}
	}

	public void setExtrasClassLoader(ClassLoader loader) {}
	public Intent setClassName(Context packageContext, String className) {
		setComponent(new ComponentName(packageContext, className));
		return this;
	}

	public String resolveTypeIfNeeded(ContentResolver resolver) {
		return type;
	}

	public Set<String> getCategories() {
		return Collections.emptySet();
	}

	public byte[] getByteArrayExtra(String name) {
		return extras.getByteArray(name);
	}

	public void removeExtra(String name) {
		extras.remove(name);
	}

	public Intent putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value) {
		extras.putParcelableArrayList(name, value);
		return this;
	}

	public int filterHashCode() {
		return 0;
	}

	public static Intent parseIntent(Resources res, XmlPullParser parser, AttributeSet attrs) {
		return new Intent();
	}

	public ComponentName resolveActivity(PackageManager pm) {
		return component;
	}

	public void setSourceBounds(Rect sourceBounds) {}

	public Rect getSourceBounds() {
		return null;
	}

	public void setSelector(Intent selector) {
		this.selector = selector;
	}

	public void setClipData(ClipData clip) {}

	public String resolveType(Context context) {
		return type;
	}

	public Intent getSelector() {
		return selector;
	}

	public boolean filterEquals(Intent other) {
		return Objects.equals(this.action, other.action)
		    && Objects.equals(this.component, other.component)
		    && Objects.equals(this.data, other.data)
		    && Objects.equals(this.type, other.type);
	}

	public long[] getLongArrayExtra(String name) {
		return extras.getLongArray(name);
	}

	public ArrayList<Integer> getIntegerArrayListExtra(String name) {
		return extras.getIntegerArrayList(name);
	}

	public int[] getIntArrayExtra(String name) {
		return extras.getIntArray(name);
	}
}
