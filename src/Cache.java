import jdk.swing.interop.SwingInterOpUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 利用HashMap实现一个缓存
 * 缓存以键值对形式存储
 * 缓存有一个失效时间，存入缓存时可以以秒为单位设置失效时间，若不指定失效时间，则默认失效时间为10s
 * @param <V>
 */
public class Cache<V> implements Serializable {

    private volatile static Cache cache;
    private Map<String,Node> map;

    public static Cache getInstance(){
        try{
            if(cache == null){
                synchronized (Cache.class){
                    try{
                        cache = new Cache();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return cache;
    }


    /**
     * 存入缓存
     * @param key
     * @param value
     */
    public void put(String key, V value){
        if(map == null){
            map = new HashMap<>();
        }
        synchronized (this){
            try{
                Node<V> node = new Node<>(value);
                map.put(key,node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取缓存
     * @param key
     * @return
     */
    public V get(String key){
        if(map == null){
            throw new RuntimeException("no cache");
        }
        long now = System.currentTimeMillis();
        Node node = map.get(key);
        if(node.startTime + node.keepAliveTime < now){
            System.out.println("cache is out of date");
            map.remove(key);
            return null;
        }
        return (V) node.value;
    }

    /**
     * 存入缓存并指定失效时间
     * @param key
     * @param value
     * @param keepAliveTime unit is second
     */
    public void put(String key, V value, long keepAliveTime){
        if(map == null){
            map = new HashMap<>();
        }
        synchronized (this){
            try{
                Node<V> node = new Node<>(value, keepAliveTime * 1000);
                map.put(key,node);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static class Node<V>{
        private V value;
        private long startTime;
        private long keepAliveTime;

        private long DEFAULT_KEEP_TIME_ALIVE = 10 * 1000;

        /**
         *
         * @param value
         * @param keepAliveTime unit is mill-second
         */
        Node(V value, long keepAliveTime){
            this.value = value;
            this.startTime = System.currentTimeMillis();
            this.keepAliveTime = keepAliveTime;
        }
        Node(V value){
            this.value = value;
            this.startTime = System.currentTimeMillis();
            this.keepAliveTime = DEFAULT_KEEP_TIME_ALIVE;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Cache<String> cache = Cache.getInstance();
        cache.put("person_1","zhangsan",5);
        cache.put("person_2","lisi");
        System.out.println(cache.get("person_1"));
        System.out.println(cache.get("person_2"));
        Thread.sleep(6 * 1000 );
        if(cache.get("person_1") == null){
            System.out.println("缓存person_1已失效");
        }else{
            System.out.println(cache.get("person_1"));
        }
        if(cache.get("person_2") == null){
            System.out.println("缓存person_2已失效");
        }else{
            System.out.println(cache.get("person_2"));
        }
        Thread.sleep(5 * 1000);
        if(cache.get("person_2") == null){
            System.out.println("缓存person_2已失效");
        }else{
            System.out.println(cache.get("person_2"));
        }

    }
}
