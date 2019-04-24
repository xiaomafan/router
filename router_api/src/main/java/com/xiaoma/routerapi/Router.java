package com.xiaoma.routerapi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.xiaoma.annotation.router.FullUrl;
import com.xiaoma.annotation.router.IntentExtrasParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {

    private Context context;
    private static final String TAG = "Router";

    public Router(Context context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> service){
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                StringBuilder builder = new StringBuilder();
                FullUrl fullUrl = method.getAnnotation(FullUrl.class);
                if(fullUrl instanceof FullUrl){
                    builder.append(fullUrl.value()).append("?");
                }else {
                    throw new IllegalArgumentException("");
                }

                Annotation[][] parameterAnnotations = method.getParameterAnnotations();

                HashMap<String, Object> serializedParams = new HashMap<>();
                for (int i=0;i<parameterAnnotations.length;i++){
                    Annotation[] parameterAnnotation = parameterAnnotations[i];
                    if (parameterAnnotation == null || parameterAnnotation.length == 0)
                        break;

                    Annotation annotation = parameterAnnotation[0];
                    if(annotation instanceof IntentExtrasParam){
                       IntentExtrasParam intentExtrasParam= (IntentExtrasParam) annotation;
                       serializedParams.put(intentExtrasParam.value(),args[i]);
                    }
                }
                performJump(builder.toString(),serializedParams);
                return null;
            }
        });
    }

    private void performJump(String routerUri, HashMap<String, Object> serializedParams) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(routerUri));

        Log.e(TAG, "performJump: "+ Uri.parse(routerUri));
        Bundle bundle = new Bundle();
        for (Map.Entry<String,Object> entry:serializedParams.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof String){
                bundle.putString(key,(String) value);
                Log.e(TAG, "string: "+(String) value);
            }else if(value instanceof Parcelable){
                bundle.putParcelable(key, (Parcelable) value);
                Log.e(TAG, "Parcelable: "+(Parcelable) value);
            }else {
                throw new IllegalArgumentException("不支持的数据类型");
            }
        }
        intent.putExtras(bundle);
        Log.e(TAG, "performJump: "+intent);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        if (!activities.isEmpty()) {
            context.startActivity(intent);
        }

    }
}
