package com.iobits.tech.app.ai_identifier.di

import android.content.Context
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.database.AppDataBase
import com.iobits.tech.app.ai_identifier.database.dao.CollectionDao
import com.iobits.tech.app.ai_identifier.network.PixabayApiService
import com.iobits.tech.app.ai_identifier.utils.NetworkConnectionInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

//    @Provides
//    @Singleton
//    fun getDatabase(@ApplicationContext context: Context): MainDB {
//        return Room.databaseBuilder(context, MainDB::class.java, "my_db")
//            .fallbackToDestructiveMigration()
//            .build()
//    }


//    @Provides
//    @Singleton
//    fun hRDao(mydatabase: MainDB): HeartRateDao {
//        return mydatabase.getHrDAo()
//    }

    @Provides
    @Singleton

    fun provideUnsafeOkHttpClient(
        okHttpLoggingInterceptor: HttpLoggingInterceptor,
        networkConnectionInterceptor: NetworkConnectionInterceptor
    ): OkHttpClient {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            //val sslContext = SSLContext.getInstance("SSL")
            // sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager

            val trustManager = trustAllCerts.get(0) as X509TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)

//                .addInterceptor(networkConnectionInterceptor)
//         .addInterceptor(okHttpLoggingInterceptor)


            val sslSocketFactory = sslContext.socketFactory
            OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(HostnameVerifier { hostname, session -> true })
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {

        return HttpLoggingInterceptor()
    }

    @Provides
    @Singleton
    fun provideNetWorkCheckInterceptor(): NetworkConnectionInterceptor {

        return NetworkConnectionInterceptor(MyApplication.getInstance())
    }

    @Singleton
    @Provides
    @Named("pixabay")
    fun provideRetrofitImages(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://pixabay.com/") // Correct base URL
        .client(okHttpClient) // Use the OkHttp client for the Retrofit instance
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(KotlinJsonAdapterFactory()).build()))
        .build()

    @Singleton
    @Provides
    @Named("pixabay")
    fun providePixabayApiService(@Named("pixabay") retrofit: Retrofit): PixabayApiService = retrofit.create(PixabayApiService::class.java)

    /*
          Data Base
      */
    @Singleton
    @Provides
    fun getDatabaseManager(): AppDataBase = AppDataBase.getDb()

    @Singleton
    @Provides
    fun getScanHistoryDao(db: AppDataBase): CollectionDao = db.getCollectionDao()

}