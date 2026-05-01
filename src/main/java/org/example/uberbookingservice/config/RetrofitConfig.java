package org.example.uberbookingservice.config;

import com.netflix.discovery.EurekaClient;
import okhttp3.OkHttpClient;
import org.example.uberbookingservice.Apis.LocationServiceApi;
import org.example.uberbookingservice.Apis.ReviewServiceApi;
import org.example.uberbookingservice.Apis.UberSocketApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitConfig {

    @Autowired
    private EurekaClient eurekaClient;

    private String getServiceURL(String serviceName){
       return eurekaClient.getNextServerFromEureka(serviceName,false).getHomePageUrl();
    }

    @Bean
    public LocationServiceApi locationServiceApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceURL("UBERPROJECT-LOCATIONSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().build())
                .build()
                .create(LocationServiceApi.class);
    }

    @Bean
    public UberSocketApi uberSocketApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceURL("UBERSOCKETSERVER"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().build())
                .build()
                .create(UberSocketApi.class);
    }

    @Bean
    public ReviewServiceApi reviewServiceApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceURL("UBERREVIEWSERVICE"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().build())
                .build()
                .create(ReviewServiceApi.class);
    }
}
