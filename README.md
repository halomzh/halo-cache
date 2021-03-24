# halo-cache
## 介绍

halo-cache是基于redis的分布式缓存服务，支持条件缓存、定时缓存更新等

## 原理

![分布式缓存原理.jpg](https://raw.githubusercontent.com/halomzh/pic/master/20210324232912.jpeg)

## 开始

### 缓存基本配置

```yaml
halo:
  cache:
    redis:
      enable: true #是否开启缓存
      expire-after-access: 60 #访问缓存后多少秒过期
      expire-after-write: 60 #写入缓存后多少秒过期
      refresh-after-write: 30 #更新缓存后多少秒刷新
```

### 缓存获取

未命中或命中无效状态缓存：执行业务逻辑，将执行结果加入缓存，当多个请求同时未命中缓存时，利用双检锁机制有且仅有一个线程执行业务逻辑并更新缓存，其余请求将命中首个线程存储的缓存，用以避免业务逻辑重复执行。

命中有效且无需更新状态缓存：返还缓存中结果

命中有效且需要更新状态缓存：返还缓存中结果，并异步更新缓存（有且仅有执行一次缓存更新，并发状态下余下的请求将命中更新后的缓存）

```java
@GetMapping("/get")
@CacheGet(nameSpace = "example", name = "'id:' + #id", condition = "#id != '5'")
public String get(@RequestParam(name = "id") String id, @RequestParam(name = "name") String name) {
   log.info("进入get程序");
   return name + "#12312312312312312#" + id;
}
```

### 缓存主动更新

更新指定缓存

```java
@GetMapping("/put")
@CachePut(nameSpace = "example", name = "'id:' + #id", condition = "#id != '5'")
public String put(@RequestParam(name = "id") String id, @RequestParam(name = "name") String name) {
   log.info("进入put程序");
   return name + "#12312312312312312#" + id;
}
```

### 缓存驱逐

驱逐指定缓存

```java
@GetMapping("/evict")
@CacheEvict(nameSpace = "example", names = "'id:' + #id")
public void evict(@RequestParam(name = "id") String id) {
   log.info("删除缓存: id[{}]", id);
}
```

### 缓存全量驱逐

驱逐指定命名空间下所有缓存

```java
@GetMapping("/evictAll")
@CacheEvict(nameSpace = "example", allEntries = true, condition = "#del")
public void evictAll(@RequestParam(name = "del") boolean del) {
   log.info("删除所有缓存");
}
```