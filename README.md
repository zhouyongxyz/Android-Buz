# Android-Buz
## 简介
Buz是一个用来管理Android中Handler的类.当项目中只有一个两个Handler的时候可能感觉不到使用上的困难,但一旦Activity一多,各种异步线程也就跟着多了起来这时候再去使用Handler就会感觉晕晕的了.
Buz就是针对这个问题提供了一个较为良好的使用体验,所有的Handler由它统一管理,任何时候需要Handler处理事情的时候只要通知Buz就行了.
Buz这个名字有Bus(总线)演变过来,把Buz当作是Handler消息处理的总线,你只需要往Buz里塞Message就行了,具体哪个Handler去消耗这个Message由Buz分配.

## 使用方法

### 注解
```java
@What(handlerName = "name")
```
用于标注自定义的Message中的what字段.`handlerName`指定what的目标Handler的名字.

```java
@Handle(name = "name")
```
用于标注Handler变量,`name`指定这个Handler在整个应用中的唯一名称,用于`@What`指定`handlerName`.

### Demo
```java
    @What(handlerName = "MainActivityHandler")
    public static final int MESSAGE_FRESH_LIST = 100000;

    @Handle(name = "MainActivityHandler")
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_FRESH_LIST:
                    Zog.tagd("  handle message with msg.what = " + msg.what + " arg1 = " + msg.arg1 + "  arg2 = " + msg.arg2 + "   obj = " + msg.obj);
                    break;
            }
        }
    };
```
上面这部分和平时写的唯一不同点就是给相应变量加`@What`和`@Handle`标注.

```java
Buz.getInstance().paste(this,this.getClass());
```
将当前类"粘贴"进Buz,用于对注解的解析.
```java
Buz.getInstance().launch();
```
启动Buz,整个Buz的使用过程中仅需要启动一次.
```java
  Message msg = Message.obtain();
  msg.what = MESSAGE_FRESH_LIST;
  msg.arg1 = 2;
  msg.arg2 = 3;
  msg.obj = "3";
  Buz.getInstance().create(msg);
```
将需要传递的Message交给Buz.这是会由Buz找到对应的Handler进行事件投递.

最后很重要的一步,释放在Buz中注册
```java
Buz.getInstance().free(this,this.getClass());
```
这一步一般在当前对象要被销毁前调用,如在Activity的onDestroy中调用.

这就是Buz的所有教程,尽情的在任何地方毫无顾及的使用它吧.