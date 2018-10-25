package com.wulee.administrator.zuji.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定义所有访问sharedpreference的方法
 */
public class Config {
	private final static String TAG = Config.class.getSimpleName();

	// 存放sp信息的文件名
	public static final String CONFIG_FILE = "zuji_sp";
	private SharedPreferences mSp = null;

	public Config(Context context) {
		mSp = context.getSharedPreferences(CONFIG_FILE, Context.MODE_MULTI_PROCESS);
	}

	/**
	 * 专门用于不同进程之间的操作
	 * 
	 * @param c
	 * @return
	 */
	public static Config get(Context c) {
		return new Config(c);
	}

	public void clear() {
		mSp.edit().clear().commit();
	}

	public void remove(String key) {
		mSp.edit().remove(key).commit();
	}

	public void removeAll() {
		Set<String> keys = mSp.getAll().keySet();
		for (String key : keys) {
			mSp.edit().remove(key).commit();
		}
	}

	public boolean put(String key, boolean value) {
		return mSp.edit().putBoolean(key, value).commit();
	}

	public boolean put(String key, int value) {
		return mSp.edit().putInt(key, value).commit();
	}

	public boolean put(String key, long value) {
		return mSp.edit().putLong(key, value).commit();
	}

	public boolean put(String key, float value) {
		return mSp.edit().putFloat(key, value).commit();
	}

	public boolean put(String key, String value) {
		return mSp.edit().putString(key, value).commit();
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return mSp.getBoolean(key, defaultValue);
	}

	public int getInt(String key, int defaultValue) {
		return mSp.getInt(key, defaultValue);
	}

	public long getLong(String key, long defaultValue) {
		return mSp.getLong(key, defaultValue);
	}

	public float getFloat(String key, float defaultValue) {
		return mSp.getFloat(key, defaultValue);
	}

	public String getString(String key, String defaultValue) {
		return mSp.getString(key, defaultValue);
	}

	/**
	 * 保存List
	 * @param tag
	 * @param datalist
	 */
	public <T> boolean setDataList(String tag, List<T> datalist) {
		if (null == datalist || datalist.size() <= 0)
			return false;

		Gson gson = new Gson();
		//转换成json数据，再保存
		String strJson = gson.toJson(datalist);
		return mSp.edit().putString(tag, strJson).commit();
	}

	/**
	 * 获取List
	 * @param tag
	 * @return
	 */
	public <T> List<T> getDataList(String tag,Class<T> cls) {
		List<T> datalist=new ArrayList<T>();
		String strJson = mSp.getString(tag, null);
		if (null == strJson) {
			return datalist;
		}
		Gson gson = new Gson();
		JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
		for (JsonElement jsonElement : arry) {
			datalist.add(gson.fromJson(jsonElement, cls));
		}
		return datalist;
	}

	/**
	 * 存储Map集合
	 * @param key 键
	 * @param map 存储的集合
	 * @param <K> 指定Map的键
	 * @param <T> 指定Map的值
	 */

	public <K,T> boolean setMap(String key , Map<K,T> map){
		if (map == null || map.isEmpty() || map.size() < 1){
			return false;
		}
		Gson gson = new Gson();
		String strJson  = gson.toJson(map);
		return  mSp.edit().putString(key ,strJson).commit();
	}

	/**
	 * 获取Map集合
	 * */
	public <K,T> Map<K,T> getMap(String key){
		Map<K,T> map = new HashMap<>();
		String strJson = mSp.getString(key,null);
		if (strJson == null){
			return map;
		}
		Gson gson = new Gson();
		map = gson.fromJson(strJson,new TypeToken<Map<K,T> >(){}.getType());
		return map;
	}

	/**
	 * 保存对象
	 * @param key     键
	 * @param obj     要保存的对象（Serializable的子类）
	 * @param <T>     泛型定义
	 */
	public  <T extends Serializable> void putObject(String key, T obj) {
		try {
			put(key, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取对象
	 * @param key     键
	 * @param <T>     指定泛型
	 * @return 泛型对象
	 */
	public  <T extends Serializable> T getObject(String key) {
		try {
			return (T) get(key,"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**存储对象*/
	private  boolean put(String key, Object obj) throws IOException {
		if (obj == null) {//判断对象是否为空
			return false;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos  = null;
		oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		// 将对象放到OutputStream中
		// 将对象转换成byte数组，并将其进行base64编码
		String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
		baos.close();
		oos.close();

		return put(key, objectStr);
	}

	/**获取对象*/
	private  Object get(String key,String defaultValue) throws IOException, ClassNotFoundException {
		String wordBase64 = getString(key,defaultValue);
		// 将base64格式字符串还原成byte数组
		if (TextUtils.isEmpty(wordBase64)) { //不可少，否则在下面会报java.io.StreamCorruptedException
			return null;
		}
		byte[]               objBytes = Base64.decode(wordBase64.getBytes(), Base64.DEFAULT);
		ByteArrayInputStream bais     = new ByteArrayInputStream(objBytes);
		ObjectInputStream ois      = new ObjectInputStream(bais);
		// 将byte数组转换成product对象
		Object obj = ois.readObject();
		bais.close();
		ois.close();
		return obj;
	}


	/**
	 * 返回程序的配置文件的绝对路径 /data/data/packagename/shared_prefs/zuji_sp
	 * @return
	 */
	private String getSPFilePathNoSuffix(Context context) {
		ApplicationInfo ai = context.getApplicationInfo();
		StringBuilder sb = new StringBuilder().append(ai.dataDir);
		sb.append("/shared_prefs/").append(CONFIG_FILE);
		return sb.toString();
	}

}
