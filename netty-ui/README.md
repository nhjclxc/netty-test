# netty-ui

https://developer.mozilla.org/zh-CN/docs/Web/API/WebSocket


### vue3按需引入
https://element-plus.org/zh-CN/guide/installation.html
 ```
npm install element-plus --save
npm install -D unplugin-vue-components unplugin-auto-import
 ```
[vue.config.js](vue.config.js)文件里面加入以下代码实现vue3按需引入，之后直接再vue文件里面使用
https://element-plus.org/zh-CN/guide/quickstart.html
```
 webpack.config.js
const AutoImport = require('unplugin-auto-import/webpack')
const Components = require('unplugin-vue-components/webpack')
const { ElementPlusResolver } = require('unplugin-vue-components/resolvers')

module.exports = {
  // ...
  plugins: [
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
}
```


## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```
