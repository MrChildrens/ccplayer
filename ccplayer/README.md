通用播放器，内部封装了基本播放逻辑，控制条基本逻辑，内置android的mediaplayer，ijkplayer，google的exoplayer。

## 引用ccplayer教程:

建议主程序minSdkVersion为16



1. gitlab地址：http://dev.cnv8.tv:8900/Ciel/ccplayer.git

   

2. ccplayer作为项目的Module引用项目。

   在Project的settings.gradle文件添加module声明：

   ```
   include ':ccplayer'
   ```

   虽然成功引用Module，但还需要更多的配置。

   

3. Project的gradle.properties文件添加播放器类型常量player的声明。

   目前支持的播放器类型有android，ijk，exo。

   

4. android播放器：

   Project的gradle.properties文件添加

   ```
   player="android"
   ```

   

5. exoplayer播放器

   Project的gradle.properties文件添加

   ```
   player="exo"
   ```

   在Project的build.gradle文件添加dirs project(':ccplayer').file('libs/exo')，才能加载libs里相关的文件

   ```
   allprojects {
       repositories {
           ...
           flatDir {
               dirs project(':ccplayer').file('libs/exo')
           }
       }
   }
   ```

   

6. ijkplayer播放器

   Project的gradle.properties文件添加

   ```
   player="ijk"
   ```

   

7. 主程序Module的build.gradle的android节点添加

   ​		sourceCompatibility JavaVersion.VERSION_1_8
   ​        targetCompatibility JavaVersion.VERSION_1_8：

   ```
   android {
   	compileOptions {
           sourceCompatibility JavaVersion.VERSION_1_8
           targetCompatibility JavaVersion.VERSION_1_8
       }
   }
   ```

​       

主程序Module的build.gradle的android节点添加 multiDexEnabled true：

```
android {
    ...
    defaultConfig {
        ...
        multiDexEnabled true
    }
}
```