# 品智云
---

## 前端资源管理

前端资源默认已统一打包到 `webapp/dist` 目录下，若需要进行前端开发，请按以下步骤进行：


### 安装工具

* 安装NodeJS，测试npm命令是否正常： npm -v
* 安装Gulp：npm install -g gulp --registry=https://registry.npm.taobao.org
* 进入项目根目录执行：npm install --registry=https://registry.npm.taobao.org

> 推荐使用cnpm安装相关模块：npm install -g cnpm --registry=https://registry.npm.taobao.org

### 快速构建

1. 使用IDEA引入Web项目
2. 右键`gulpfile.js`选择【Show Gulp Tasks】
3. 双击`release`任务对web项目资源进行构建并输出到`dist`目录
4. 所有项目资源请相对`/dist`目录引用

### 如何使用

示例定义登录页面的login.scss、login.js以及图片使用：

`login.js`:

1. 创建文件：`scripts/pages/login/login.js`。PS: 所有js文件推荐使用闭包方式创建
2. 执行`build-scripts`任务，编译输出文件
3. 相对`dist`目录引用js文件：`/dist/scripts/pages/login/login.js`

`login.scss`：

1. 创建文件：`scss/pages/login/login.scss`，在顶部定义：`@import "../../imports";`，使之可引用平台相关通用样式。
2. 执行`build-scss`任务，编译输出文件
3. 相对`dist`目录引用css文件：`/dist/css/pages/login/login.css`

`图片资源`：

各页面中使用的图片可拷贝到`images/**`目录下，然后通过`build-images`任务对图片进行压缩并输出到`dist/images`目录下。

> 图片压缩不影响原始图片大小以及尺寸    
> PS: **所有页面资源文件必须有相对`dist`引用**