package org.example.uberbookingservice.config;

import com.google.gson.*;
import com.netflix.discovery.EurekaClient;
import okhttp3.OkHttpClient;
import org.example.uberbookingservice.Apis.LocationServiceApi;
import org.example.uberbookingservice.Apis.ReviewServiceApi;
import org.example.uberbookingservice.Apis.UberSocketApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class RetrofitConfig {

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${services.location.url:http://localhost:7777}")
    private String locationServiceUrl;

    @Value("${services.socket.url:http://localhost:8222}")
    private String socketServiceUrl;

    @Value("${services.review.url:http://localhost:8080}")
    private String reviewServiceUrl;

    private String getServiceURL(String serviceName, String fallbackUrl){
        try {
            return normalizeBaseUrl(eurekaClient.getNextServerFromEureka(serviceName,false).getHomePageUrl());
        } catch (Exception exception) {
            return normalizeBaseUrl(fallbackUrl);
        }
    }

    private String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Service URL is missing");
        }
        return url.endsWith("/") ? url : url + "/";
    }

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
            .create();

    @Bean
    public LocationServiceApi locationServiceApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceURL("UBERPROJECT-LOCATIONSERVICE", locationServiceUrl))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(new OkHttpClient().newBuilder().build())
                .build()
                .create(LocationServiceApi.class);
    }

    @Bean
    public UberSocketApi uberSocketApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceURL("UBERSOCKETSERVER", socketServiceUrl))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(new OkHttpClient().newBuilder().build())
                .build()
                .create(UberSocketApi.class);
    }

    @Bean
    public ReviewServiceApi reviewServiceApi(){
        return new Retrofit.Builder()
                .baseUrl(getServiceURL("UBERREVIEWSERVICE", reviewServiceUrl))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(new OkHttpClient().newBuilder().build())
                .build()
                .create(ReviewServiceApi.class);
    }
}
