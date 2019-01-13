package org.hygieiasoft.cordova.uid;

import android.Manifest;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.util.Log;

import java.lang.reflect.Method;

public class UID extends CordovaPlugin {

	public static final String TAG = "UID";
	public CallbackContext callbackContext;
	public static final int REQUEST_READ_PHONE_STATE = 0;

	public static String uuid; // Device UUID
	public static String imei; // Device IMEI
	public static String imsi; // Device IMSI
	public static String iccid; // Sim IMSI
	public static String mac; // MAC address

	protected final static String permission = Manifest.permission.READ_PHONE_STATE;

	/**
	 * Constructor.
	 */
	public UID() {
	}

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 *
	 * @param cordova The context of the main Activity.
	 * @param webView The CordovaWebView Cordova is running in.
	 */
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		// Context context = cordova.getActivity().getApplicationContext();
		// UID.uuid = getUuid(context);
		// UID.imei = getImei(context);
		// UID.imsi = getImsi(context);
		// UID.iccid = getIccid(context);
		// UID.mac = getMac(context);
	}

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action            The action to execute.
	 * @param args              JSONArry of arguments for the plugin.
	 * @param callbackContext   The callback id used when calling back into JavaScript.
	 * @return                  True if the action was valid, false if not.
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		try {
			if (action.equals("getUID")) {
				// permission work
				if(this.hasPermission(permission)) {
					dispatchValues();
				} else {
					this.requestPermission(this, REQUEST_READ_PHONE_STATE, permission);
				}
			} else {
				this.callbackContext.error("Invalid action");
				return false;
			}
		} catch(Exception e ) {
            this.callbackContext.error("Exception occurred: ".concat(e.getMessage()));
            return false;
        }
		return true;
	}

	/**
	 * Get the device's Universally Unique Identifier (UUID).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getUuid(Context context) {
		String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return uuid;
	}

	/**
	 * Get the device's International Mobile Station Equipment Identity (IMEI).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getImei(Context context) {
		final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephony.getDeviceId();
		return imei;
	}

	/**
	 * Get the device's International mobile Subscriber Identity (IMSI).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getImsi(Context context) {
		final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephony.getSubscriberId();
		return imsi;
	}

	/**
	 * Get the sim's Integrated Circuit Card Identifier (ICCID).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getIccid(Context context) {
		final TelephonyManager mTelephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String iccid = mTelephony.getSimSerialNumber();
		return iccid;
	}

	/**
	 * Get the Media Access Control address (MAC).
	 *
	 * @param context The context of the main Activity.
	 * @return
	 */
	public String getMac(Context context) {
		String mac = "";
		if (android.os.Build.VERSION.SDK_INT >= 23) {
     		// only for marshmallow and newer versions
			try {
				List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface nif : all) {
					if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
	
					byte[] macBytes = nif.getHardwareAddress();
					if (macBytes == null) {
						return "";
					}
	
					StringBuilder res1 = new StringBuilder();
					for (byte b : macBytes) {
						res1.append(Integer.toHexString(b & 0xFF) + ":");
					}
	
					if (res1.length() > 0) {
						res1.deleteCharAt(res1.length() - 1);
					}
					mac = res1.toString();
				}
			} catch (Exception ex) {
        	}
		} else {
			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo wInfo = wifiManager.getConnectionInfo();
			mac = wInfo.getMacAddress();
		}
		
		return mac;
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        if(requestCode == REQUEST_READ_PHONE_STATE){
            dispatchValues();
        }
    }

	protected void dispatchValues() {
		try {
			Context context = cordova.getActivity().getApplicationContext();
			UID.uuid = getUuid(context);
			UID.imei = getImei(context);
			UID.imsi = getImsi(context);
			UID.iccid = getIccid(context);
			UID.mac = getMac(context);

		
			JSONObject r = new JSONObject();
			r.put("UUID", UID.uuid);
			r.put("IMEI", UID.imei);
			r.put("IMSI", UID.imsi);
			r.put("ICCID", UID.iccid);
			r.put("MAC", UID.mac);
			this.callbackContext.success(r);
		} catch(Exception e) {
			this.callbackContext.error("Exception occurred: ".concat(e.getMessage()));
		}
	}

	private boolean hasPermission(String permission) throws Exception{
        boolean hasPermission = true;
        Method method = null;
        try {
            method = cordova.getClass().getMethod("hasPermission", permission.getClass());
            Boolean bool = (Boolean) method.invoke(cordova, permission);
            hasPermission = bool.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Cordova v" + CordovaWebView.CORDOVA_VERSION + " does not support API 23 runtime permissions so defaulting to GRANTED for " + permission);
        }
        return hasPermission;
    }

    private void requestPermission(CordovaPlugin plugin, int requestCode, String permission) throws Exception{
        try {
            java.lang.reflect.Method method = cordova.getClass().getMethod("requestPermission", org.apache.cordova.CordovaPlugin.class ,int.class, java.lang.String.class);
            method.invoke(cordova, plugin, requestCode, permission);
        } catch (NoSuchMethodException e) {
            throw new Exception("requestPermission() method not found in CordovaInterface implementation of Cordova v" + CordovaWebView.CORDOVA_VERSION);
        }
    }

}