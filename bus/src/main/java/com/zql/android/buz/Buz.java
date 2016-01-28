package com.zql.android.buz;


import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by scott on 1/26/16.
 * Buz
 */
public class Buz {

    public static final String TAG = "Buz";

    private static Buz instance ;
    private LinkedBlockingDeque<Message> mMessageDeque;

    private Map<String,Handler> mHandlerMap;
    private Map<Integer,String> mWhatMap;
    private BuzCycle mCycle;
    private Buz(){
        mMessageDeque = new LinkedBlockingDeque<>(500);
        mHandlerMap = new HashMap<>();
        mWhatMap = new HashMap<>();
    }


    /**
     * 获得Buz实例
     * @return
     */
    public static synchronized Buz getInstance(){
        if(instance == null){
            instance = new Buz();
        }
        return instance;
    }

    /**
     * 启动Buz,在Buz的整个使用过程中只需要启动一次.
     */
    public synchronized void launch(){
        if(mCycle == null){
            mCycle = new BuzCycle();
        }
        if(!mCycle.isStart()){
            mCycle.start();
        }
    }

    /**
     * 将当前类"贴"到Buz上,若当前类中使用了 @Handle 或 @What注解,Buz则会将对应的Handler加入自己的消息队列,供全局使用.
     * @param object 当前对象
     * @param clazz 当前对象所对应的类
     * @throws BuzException 出错原因:1.当@Handle.name已存在   2.@Handle对应的Handler为空  3.@What标注的值已存在
     */
    public synchronized void paste(Object object ,Class clazz) throws BuzException {

        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields){
            field.setAccessible(true);
            Handle handle = field.getAnnotation(Handle.class);
            if(handle != null){
                String name = handle.name();
                if(mHandlerMap.containsKey(name)){
                    throw new BuzException("handler name :(" + name + ") is already exist");
                }
                try {
                    Handler handler = (Handler)field.get(object);
                    if(handler == null){
                        throw new BuzException("handler with name is (" + name + ") is null");
                    }else{
                        mHandlerMap.put(name,handler);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                continue;
            }

            What what = field.getAnnotation(What.class);
            if(what != null){
                try {
                    int whatValue = (int)field.get(object);

                    if(mWhatMap.containsKey(whatValue)){
                        throw  new BuzException("what value :(" + whatValue + ")is already exist");
                    }
                    String name = what.handlerName();
                    mWhatMap.put(whatValue,name);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }

    /**
     * 释放对象中的Handler等信息在Buz中的注册.这一步很重要,无比在使用Buz的对象被销毁前调用.
     * 如Activity中的OnDestroy()方法.
     * @param object
     * @param clazz
     */
    public synchronized void free(Object object,Class clazz){
        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields){
            field.setAccessible(true);
            Handle handle = field.getAnnotation(Handle.class);
            if(handle != null){
                String name = handle.name();
                if(mHandlerMap.containsKey(name)){
                    mHandlerMap.remove(name);
                }
                continue;
            }

            What what = field.getAnnotation(What.class);
            if(what != null){
                try {
                    int whatValue = (int)field.get(object);
                    if(mWhatMap.containsKey(whatValue)){
                        mWhatMap.remove(whatValue);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }

    /**
     * 向Buz生成一个Message
     * @param what message的what字段
     * @throws InterruptedException
     */
    public synchronized void create(int what) throws InterruptedException {
        Message msg = Message.obtain();
        msg.what = what;
        mMessageDeque.put(msg);
    }

    /**
     * 向Buz生成一个Message
     * @param what message的what字段
     * @param arg1 message的arg1字段
     * @throws InterruptedException
     */
    public synchronized void create(int what,int arg1){
        try {
            Message msg =  Message.obtain();
            msg.what = what;
            msg.arg1 = arg1;
            mMessageDeque.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * 向Buz生成一个Message
     * @param what message的what字段
     * @param arg1 message的arg1字段
     * @param arg2 message的arg2字段
     * @throws InterruptedException
     */
    public synchronized void create(int what,int arg1,int arg2) throws InterruptedException {
        Message msg =  Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        mMessageDeque.put(msg);

    }

    /**
     * @param msg Message对象
     * @throws InterruptedException
     */
    public synchronized void create(@NonNull  Message msg) throws InterruptedException {

        if(msg != null){
            mMessageDeque.put(msg);
        }
    }

    private class BuzCycle extends Thread{

        private boolean isStart = false;

        public BuzCycle(){
            setName(TAG);
            setPriority(Thread.MAX_PRIORITY);
        }

        @Override
        public void run() {
            while(true){
                try {
                    Message msg = mMessageDeque.take();
                    int what = msg.what;
                    Zog.tagd(" the message.what =  " + what);

                    if(mWhatMap.containsKey(what)){
                        String name = mWhatMap.get(what);
                        Handler handler = mHandlerMap.get(name);
                        if(handler != null){
                            Message pendMsg = handler.obtainMessage(what);
                            pendMsg.copyFrom(msg);
                            pendMsg.sendToTarget();
                            msg.recycle();
                        }else{
                            throw new BuzException("Handler with name is (" + name + ") is null");
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BuzException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public synchronized void start() {
            isStart = true;
            super.start();
        }

        public boolean isStart(){
            return isStart;
        }
    }

    /**
     * Buz的异常类
     */
    public static class BuzException extends Exception{
        public BuzException(String desc){
            super("" + desc);
        }
    }
}
