# 简介
ar4j是一个Java版的ActiveRecord实现。可以灵活地对数据库进行操作。特色如下：
* 零配置
* 零依赖
* 自定义SQL
* 支持多数据库
* 支持分表查询
* 支持批量写入

# 用法
## 准备工作
先创建一个表：
```sql
CREATE TABLE ar_user (
  id          INTEGER   AUTO_INCREMENT UNIQUE   NOT NULL PRIMARY KEY,
  username    VARCHAR(32) UNIQUE                NOT NULL,
  password    VARCHAR(32)                       NOT NULL,
  insert_time DATETIME                          NOT NULL,
  update_time TIMESTAMP DEFAULT current_timestamp
);
```
## 引入依赖
在pom中添加如下依赖：
```xml
     <dependency>
          <groupId>com.zhyea.ar4j.core</groupId>
          <artifactId>ar4j-core</artifactId>
          <version>0.1-SNAPSHOT</version>
     </dependency>
```
用户还需要自己实现Dialect和DataSourcePlugin两个接口。也可以继续引入ar4j-ext依赖，使用已有的Dialect和DataSourcePlugin实现：
```xml
      <dependency>
          <groupId>com.zhyea.ar4j.ext</groupId>
          <artifactId>ar4j-ext</artifactId>
          <version>0.1-SNAPSHOT</version>
      </dependency>
```
## 配置数据源
ar4j使用ArConfig对象保存数据库配置信息。
```text
    final String url = "jdbc:mysql://127.0.0.1:3306/ar_test";
    final String username = "root";
    final String password = "root";
        
    DataSourcePlugin dsp = new DbcpPlugin(url, username, password);
    ArConfig config = new ArConfig("localhost3306", dsp, new MySqlDialect());
```
配置数据源时需要指明数据源及相关的Dialect。
## 创建Model并注册
一个Model类表示一个表。一个Model实现类的实例表示表中的一行记录。先来创建一个Model类：
```java
import com.zhyea.ar4j.core.Model;

public class ArUser extends Model<ArUser> {
}
```
将Model注册到ArConfig实例中：
```text
config.regTable("ar_user", ArUser.class, "id");
```
注册表时需要指明表名、Model类、以及主键。如果Model类的名称采用了规范的驼峰式命名，如表名为ar_user，类名为ArUser.class就可以在注册时省略掉表名：
```text
config.regTable(ArUser.class, "id");
```
此外如果表的主键名与Dialect中设置的默认主键名一致的话,主键名也是可以省略掉的：
```text
config.regTable(ArUser.class);
```
有一块需要注意，在做分表时，也就是说如果Model类是继承的SeqModel.class，那么在注册相关的表时只需要填写表名前缀，且表名不可省略。
## 增删改查
### 执行insert
写入数据需要创建一个新的Model实例并执行save()方法：
```text
new ArUser().set("username", "robin")
            .set("password", "zhyea.com")
            .set("insert_time", new Date()).save();
```
执行batchSave方法可以将多个Model实例批量写入数据库：
```text
ArUser arUserService = new ArUser();
List<ArUser> records = new ArrayList<>();
for (int i = 0; i < 50; i++) {
      ArUser t = new ArUser();
      t.set("username", "robin" + i).set("password", "zhyea.com").set("insert_time", new Date());
      records.add(t);
}
arUserService.batchSave(records);
```
### 执行query
在Model中封装了一些简单的查询方法，可以直接拿过来使用：
```text
ArUser arUserService = new ArUser();
List<ArUser> list = arUserService.find("select * from " + arUserService.getTableName());
```
还有根据主键获取记录的findByPrimaryKey()以及获取第一条记录的findFirst()方法。
ar4j对于in查询的也有些不足，当前只是在Model.class类提供了buildInClause()方法来构建in语句。
### 执行update
执行Model实例的set方法设置新的属性，设置完成后执行update方法可以完成更新：
```text
user.set("update_time", new Timestamp(System.currentTimeMillis())).update();
```
### 执行delete
执行Model实例的delete方法可以删除实例对应的记录：
```text
user.delete();
```
## 分表
ar4j目前只支持相同表名前缀样式的分表，如ar_user_201701、ar_user_201702等。  
要做分表时需要继承SeqModel类，并实现latestSuffix()和suffixRegex()两个方法。latestSuffix()返回的值是分表的最新的表名后缀。suffixRegex()返回的是表名后缀正则表达式。  
在ArConfig实例中注册时需要注册表名前缀，且不可省略。如果省略了，在执行查询时就只会查询最新的表，起不到分表查询的效果。    
SeqModel类中提供了两个分表查询的方法：findInSeq()和findFirstInSeq()，只有调用这两个方法才能起到分表查询的效果。调用其他的查询方法如果find()和findFirst()会默认只查询最新的表。  
## 缓存
ar4j为所有的查询都提供了缓存查询方案，只需在执行时传入一个Cache对象以及一个key值。不过目前ar4j并没有提供具体的Cache实现方案，需要用户自己继承Cache接口实现。
## 其他
目前ar4j只是为了适应我自己的开发工作而完成的。所以目前并不支持组合主键。其他的一些方面比如缓存、join操作、in查询也有些欠缺，需要在以后的时间里慢慢补全。

