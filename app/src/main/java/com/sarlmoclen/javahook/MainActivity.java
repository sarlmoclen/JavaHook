package com.sarlmoclen.javahook;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btReflection = findViewById(R.id.bt_reflection);
        btReflection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("sarlmoclen","click button reflection");
            }
        });
        Button btInvocationHandlerAndProxy = findViewById(R.id.bt_invocationHandlerAndProxy);
        btInvocationHandlerAndProxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("sarlmoclen","click button invocationHandlerAndProxy");
            }
        });
        reflectionHook(btReflection);
        invocationHandlerAndProxyHook(btInvocationHandlerAndProxy);
    }

    private void reflectionHook(View view){
        try {
            //得到view的ListenerInfo对象
            Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
            getListenerInfo.setAccessible(true);
            Object listenerInfo = getListenerInfo.invoke(view);
            //得到view的OnClickListener对象
            Class<?> listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
            Field mOnClickListener = listenerInfoClass.getDeclaredField("mOnClickListener");
            mOnClickListener.setAccessible(true);
            View.OnClickListener viewOnClickListener = (View.OnClickListener) mOnClickListener.get(listenerInfo);
            //用自定义的OnClickListener替换view的OnClickListener
            View.OnClickListener hookedOnClickListener = new HookOnClickListener(viewOnClickListener);
            mOnClickListener.set(listenerInfo, hookedOnClickListener);
        } catch (Exception e) {
            Log.i("sarlmoclen","hook clickListener failed!" + e.toString());
        }
    }

    class HookOnClickListener implements View.OnClickListener {

        private View.OnClickListener onClickListener;

        HookOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @Override
        public void onClick(View v) {
            Log.i("sarlmoclen","before click");
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
            Log.i("sarlmoclen","after click");
        }

    }

    private void invocationHandlerAndProxyHook(View view){
        try {
            //得到view的ListenerInfo对象
            Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
            getListenerInfo.setAccessible(true);
            Object listenerInfo = getListenerInfo.invoke(view);
            //得到view的OnClickListener对象
            Class<?> listenerInfoClass = Class.forName("android.view.View$ListenerInfo");
            Field mOnClickListener = listenerInfoClass.getDeclaredField("mOnClickListener");
            mOnClickListener.setAccessible(true);
            View.OnClickListener viewOnClickListener = (View.OnClickListener) mOnClickListener.get(listenerInfo);
            //使用Proxy生成自定义的OnClickListener替换view的OnClickListener
            ClickInvocationHandler clickInvocationHandler = new ClickInvocationHandler(viewOnClickListener);
            View.OnClickListener hookedOnClickListener = (View.OnClickListener) Proxy.newProxyInstance(View.OnClickListener.class.getClassLoader(),
                    new Class[]{View.OnClickListener.class}, clickInvocationHandler);
            mOnClickListener.set(listenerInfo, hookedOnClickListener);
        } catch (Exception e) {
            Log.i("sarlmoclen","hook clickListener failed!" + e.toString());
        }
    }

    class ClickInvocationHandler implements InvocationHandler{

        private View.OnClickListener onClickListener;

        ClickInvocationHandler(View.OnClickListener onClickListener){
            this.onClickListener = onClickListener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(method.getName().equals("onClick")){
                Log.i("sarlmoclen","before click");
                method.invoke(onClickListener,args);
                Log.i("sarlmoclen","after click");
            }
            return null;
        }

    }
    
}
