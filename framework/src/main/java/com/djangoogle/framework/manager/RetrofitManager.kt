package com.djangoogle.framework.manager

import android.content.Context
import com.djangoogle.framework.BuildConfig
import com.djangoogle.framework.constants.DjangoConst
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by Djangoogle on 2019/04/28 11:14 with Android Studio.
 */
class RetrofitManager private constructor() {

	companion object {
		val INSTANCE: RetrofitManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
			RetrofitManager()
		}

		//网络框架超时时间
		private const val OK_HTTP_TIME_OUT = 10L
	}

	var mRetrofit: Retrofit? = null
		private set

	private var mOkHttpClient: OkHttpClient? = null

	/**
	 * 初始化
	 *
	 * @param context   上下文
	 */
	fun initialize(context: Context) {
		//初始化OkHttpClient
		initOkHttpClient(context)
	}

	/**
	 * 初始化OkHttpClient
	 *
	 * @param context 上下文
	 */
	private fun initOkHttpClient(context: Context) {
		val okHttpClientBuilder = OkHttpClient.Builder()
		//配置log
		okHttpClientBuilder.addInterceptor(LoggingInterceptor.Builder().loggable(BuildConfig.DEBUG)
				.setLevel(Level.BASIC)
				.log(Platform.INFO)
				.request("Request")
				.response("Response")
				.addHeader("version", BuildConfig.VERSION_NAME)
				.enableAndroidStudio_v3_LogsHack(true)
				.executor(Executors.newSingleThreadExecutor()).build())
		//全局的读取超时时间
		okHttpClientBuilder.readTimeout(OK_HTTP_TIME_OUT, TimeUnit.SECONDS)
		//全局的写入超时时间
		okHttpClientBuilder.writeTimeout(OK_HTTP_TIME_OUT, TimeUnit.SECONDS)
		//全局的连接超时时间
		okHttpClientBuilder.connectTimeout(OK_HTTP_TIME_OUT, TimeUnit.SECONDS)
		//使用数据库保持cookie，如果cookie不过期，则一直有效
		okHttpClientBuilder.cookieJar(PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context)))
		mOkHttpClient = okHttpClientBuilder.build()
	}

	/**
	 * 初始化Retrofit
	 *
	 * @param serverUrl 服务器地址
	 */
	fun initRetrofit(serverUrl: String) {
		MMKV.defaultMMKV().encode(DjangoConst.SERVER_URL, serverUrl)
		mRetrofit = Retrofit.Builder().client(mOkHttpClient!!)
				.baseUrl(serverUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.build()
	}
}
